package org.opensearch.dataprepper.plugins.processor.converters;

import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchSigmaV1Rule;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
import org.opensearch.dataprepper.plugins.processor.rules.SigmaV1Rule;
import org.opensearch.dataprepper.plugins.processor.util.OpenSearchDocMetadata;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FindingConverter {

    public List<Map<String, Object>> convert(final Match match) {
        final Map<String, List<Rule>> monitorToRules = groupMatchByMonitors(match);
        return monitorToRules.values().stream()
                .map(rules -> generateEventForMonitor(match, rules))
                .collect(Collectors.toList());
    }

    private Map<String, List<Rule>> groupMatchByMonitors(final Match match) {
        final Map<String, List<Rule>> monitorToRules = new HashMap<>();

        match.getRuleMatches().forEach(rule -> {
            final String monitorId = ((OpenSearchSigmaV1Rule) rule).getMonitorId();

            monitorToRules.putIfAbsent(monitorId, new ArrayList<>());
            monitorToRules.get(monitorId).add(rule);
        });

        return monitorToRules;
    }

    private Map<String, Object> generateEventForMonitor(final Match match, final List<Rule> rules) {
        final OpenSearchSigmaV1Rule openSearchSigmaV1Rule = (OpenSearchSigmaV1Rule) (rules.get(0));

        final Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("id", UUID.randomUUID().toString());
        eventMap.put("monitor_id", openSearchSigmaV1Rule.getMonitorId());
        eventMap.put("monitor_name", openSearchSigmaV1Rule.getDetectorName());
        eventMap.put("index", match.getDataType().getMetadataValue(OpenSearchDocMetadata.INDEX.getFieldName()));
        eventMap.put("queries", rules.stream().map(this::getQuery).collect(Collectors.toList()));
        eventMap.put("timestamp", Instant.now().toEpochMilli());
        eventMap.put(OpenSearchDocMetadata.RULE_ENGINE_DOC_ID_REPLACEMENT_FIELDS.getFieldName(), List.of("related_doc_ids", "correlated_doc_ids"));
        eventMap.put(OpenSearchDocMetadata.RULE_ENGINE_DOC_MATCH_ID.getFieldName(), match.getDataType().getMetadataValue(OpenSearchDocMetadata.RULE_ENGINE_ID.getFieldName()));
        eventMap.put(OpenSearchDocMetadata.FINDINGS_INDEX_NAME.getFieldName(), openSearchSigmaV1Rule.getFindingsIndex());

        return eventMap;
    }

    private Map<String, Object> getQuery(final Rule rule) {
        final SigmaV1Rule sigmaV1Rule = (SigmaV1Rule) rule;

        final Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("id", sigmaV1Rule.getId());
        queryMap.put("name", sigmaV1Rule.getId());
        queryMap.put("query", "PLACEHOLDER");
        queryMap.put("tags", ((SigmaV1Rule) rule).getTags());

        return queryMap;
    }
}
