package org.opensearch.dataprepper.plugins.processor.generator;

import org.opensearch.dataprepper.plugins.processor.model.event.EventWrapper;
import org.opensearch.dataprepper.plugins.processor.model.findings.DocLevelQuery;
import org.opensearch.dataprepper.plugins.processor.model.findings.Finding;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FindingGenerator {
    public Map<String, List<Finding>> generateFindings(final EventWrapper eventWrapper, final List<Rule> ruleMatches) {
        final Map<String, List<Rule>> monitorIdToRules = new HashMap<>();
        ruleMatches.forEach(rule -> {
            final String monitorId = rule.getMonitorId();
            monitorIdToRules.putIfAbsent(monitorId, new ArrayList<>());
            monitorIdToRules.get(monitorId).add(rule);
        });

        final Map<String, List<Finding>> findingsIndexToFindings = new HashMap<>();
        monitorIdToRules.forEach((monitorId, rules) -> {
                    final String findingsIndex = rules.get(0).getFindingsIndex();

                    final List<DocLevelQuery> queries = rules.stream()
                            .map(this::createDocLevelQuery)
                            .collect(Collectors.toList());

                    final Finding finding = new Finding(
                            UUID.randomUUID().toString(),
                            List.of(eventWrapper.getDocId()),
                            List.of(eventWrapper.getDocId()),
                            monitorId,
                            rules.get(0).getMonitorName(),
                            eventWrapper.getIndexName(),
                            queries,
                            Instant.now().toEpochMilli()
                    );

                    findingsIndexToFindings.putIfAbsent(findingsIndex, new ArrayList<>());
                    findingsIndexToFindings.get(findingsIndex).add(finding);
                });

        return findingsIndexToFindings;
    }

    private DocLevelQuery createDocLevelQuery(final Rule rule) {
        return new DocLevelQuery(rule.getId(), rule.getId(), rule.getTags(), rule.getQuery());
    }
}
