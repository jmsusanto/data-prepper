package org.opensearch.dataprepper.plugins.processor.converters;

import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchRuleMetadata;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchSigmaV1StatefulRule;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchSigmaV1StatelessRule;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
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

        match.getRules().forEach(rule -> {
            final String monitorId = rule instanceof OpenSearchSigmaV1StatelessRule ?
                    ((OpenSearchSigmaV1StatelessRule) rule).getOpenSearchRuleMetadata().getMonitorId() :
                    ((OpenSearchSigmaV1StatefulRule) rule).getOpenSearchRuleMetadata().getMonitorId();

            monitorToRules.putIfAbsent(monitorId, new ArrayList<>());
            monitorToRules.get(monitorId).add(rule);
        });

        return monitorToRules;
    }

    private Map<String, Object> generateEventForMonitor(final Match match, final List<Rule> rules) {
        final OpenSearchRuleMetadata openSearchRuleMetadata = rules.get(0) instanceof OpenSearchSigmaV1StatelessRule ?
                ((OpenSearchSigmaV1StatelessRule) rules.get(0)).getOpenSearchRuleMetadata() :
                ((OpenSearchSigmaV1StatefulRule) rules.get(0)).getOpenSearchRuleMetadata();

        final Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("id", UUID.randomUUID().toString());
        eventMap.put("monitor_id", openSearchRuleMetadata.getMonitorId());
        eventMap.put("monitor_name", openSearchRuleMetadata.getDetectorName());
        eventMap.put("index", match.getDataType().getDataTypeMetadataValue(OpenSearchDocMetadata.INDEX.getFieldName()));
        eventMap.put("queries", rules.stream().map(this::getQuery).collect(Collectors.toList()));
        eventMap.put("timestamp", Instant.now().toEpochMilli());
        eventMap.put(OpenSearchDocMetadata.RULE_ENGINE_DOC_ID_REPLACEMENT_FIELDS.getFieldName(), List.of("related_doc_ids", "correlated_doc_ids"));
        eventMap.put(OpenSearchDocMetadata.RULE_ENGINE_DOC_MATCH_ID.getFieldName(), match.getDataType().getDataTypeMetadataValue(OpenSearchDocMetadata.RULE_ENGINE_ID.getFieldName()));
        eventMap.put(OpenSearchDocMetadata.FINDINGS_INDEX_NAME.getFieldName(), openSearchRuleMetadata.getFindingsIndex());

        return eventMap;
    }

    private Map<String, Object> getQuery(final Rule rule) {
        List<String> tags = null;
        if (rule instanceof OpenSearchSigmaV1StatelessRule) {
            tags = ((OpenSearchSigmaV1StatelessRule) rule).getSigmaV1RuleMetadata().getTags();
        } else if (rule instanceof OpenSearchSigmaV1StatefulRule) {
            tags = ((OpenSearchSigmaV1StatefulRule) rule).getSigmaV1RuleMetadata().getTags();
        }

        final Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("id", rule.getId());
        queryMap.put("name", rule.getId());
        queryMap.put("query", "PLACEHOLDER");
        queryMap.put("tags", tags);

        return queryMap;
    }
}
