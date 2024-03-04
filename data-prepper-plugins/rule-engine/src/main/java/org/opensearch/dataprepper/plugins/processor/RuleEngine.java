package org.opensearch.dataprepper.plugins.processor;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.dataprepper.plugins.processor.generator.FindingGenerator;
import org.opensearch.dataprepper.plugins.processor.model.event.EventWrapper;
import org.opensearch.dataprepper.plugins.processor.model.findings.Finding;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
import org.opensearch.dataprepper.plugins.processor.rules.RuleConverter;
import org.opensearch.dataprepper.plugins.processor.rules.RuleFetcher;
import org.opensearch.dataprepper.plugins.processor.rules.RuleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RuleEngine {
    private static final String FINDINGS_INDEX = ".opensearch-sap-cloudtrail-findings";

    private static final Logger LOG = LoggerFactory.getLogger(RuleEngine.class);

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    private final RuleStore ruleStore;
    private final FindingGenerator findingGenerator;
    private final OpenSearchClient openSearchClient;

    public RuleEngine(final RuleEngineConfig config, final OpenSearchClient openSearchClient) {
        ruleStore = new RuleStore();
        findingGenerator = new FindingGenerator();
        this.openSearchClient = openSearchClient;
        setupRuleFetching(config, ruleStore);
    }

    public void doExecute(final Collection<EventWrapper> eventWrappers) {
        if (eventWrappers.isEmpty()) {
            return;
        }

        final Map<String, List<Rule>> indexToSigmaRules = ruleStore.getRules();
        LOG.info("Got index to rules: {}", indexToSigmaRules);

        final Map<String, List<Finding>> allFindings = new HashMap<>();
        eventWrappers.forEach(eventWrapper -> {
            final String indexName = eventWrapper.getIndexName();
            final List<Rule> sigmaRules = indexToSigmaRules.get(indexName);
            if (sigmaRules == null || sigmaRules.isEmpty()) {
                return;
            }

            // TODO - should just model after OpenSearch SAP, have list of detectors, iterate over them
            final List<Rule> ruleMatches = sigmaRules.stream()
                    .filter(sigmaRule -> sigmaRule.getCondition().test(eventWrapper.getEvent()))
                    .collect(Collectors.toList());

            if (!ruleMatches.isEmpty()) {
                final Map<String, List<Finding>> findingsIndexToFindings = findingGenerator.generateFindings(eventWrapper, ruleMatches);
                findingsIndexToFindings.forEach((findingsIndex, findings) -> {
                    allFindings.putIfAbsent(findingsIndex, new ArrayList<>());
                    allFindings.get(findingsIndex).addAll(findings);
                });
            }
        });

        indexFindings(allFindings);
    }

    private void indexFindings(final Map<String, List<Finding>> allFindings) {
        if (allFindings.isEmpty()) {
            LOG.debug("No findings to index");
            return;
        }

        final BulkRequest bulkRequest = createBulkRequest(allFindings);
        try {
            final BulkResponse bulkResponse = openSearchClient.bulk(bulkRequest);
            if (bulkResponse.errors()) {
                LOG.error("BulkResponse has errors");
                bulkResponse.items().stream()
                        .filter(bulkResponseItem -> bulkResponseItem.error() != null)
                        .forEach(bulkResponseItem -> LOG.error("BulkItemError for ID {}: {}", bulkResponseItem.id(), bulkResponseItem.error().reason()));
            }
        } catch (IOException e) {
            LOG.error("Caught exception indexing findings", e);
        }
    }

    private BulkRequest createBulkRequest(final Map<String, List<Finding>> allFindings) {
        final List<BulkOperation> allBulkOperations = new ArrayList<>();
        allFindings.forEach((findingsIndex, findings) -> {
            final List<BulkOperation> bulkOperations = findings.stream()
                    .peek(finding -> LOG.info("Indexing finding with ID: {}", finding.getId()))
                    .map(finding -> new IndexOperation.Builder<>()
                            .id(finding.getId())
                            .index(findingsIndex)
                            .document(finding)
                            .build())
                    .map(idxOp -> new BulkOperation.Builder().index(idxOp).build())
                    .collect(Collectors.toList());
            allBulkOperations.addAll(bulkOperations);
        });

        return new BulkRequest.Builder()
                .operations(allBulkOperations)
                .build();
    }

    private void setupRuleFetching(final RuleEngineConfig config, final RuleStore ruleStore) {
        final RuleConverter ruleConverter = new RuleConverter(config);
        final RuleFetcher ruleFetcher = new RuleFetcher(openSearchClient, ruleStore, ruleConverter);
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
                ruleFetcher,
                0L,
                config.getRuleRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }
}
