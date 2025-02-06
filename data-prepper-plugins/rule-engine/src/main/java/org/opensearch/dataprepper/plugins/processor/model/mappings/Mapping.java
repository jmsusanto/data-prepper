package org.opensearch.dataprepper.plugins.processor.model.mappings;

import org.opensearch.dataprepper.plugins.processor.model.log.LogFormat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Mapping {
    private List<Map<String, String>> mappings;

    public void setMappings(final List<Map<String, String>> mappings) {
        this.mappings = mappings;
    }

    public Map<String, String> getFieldMappingForLogFormat(final LogFormat logFormat) {
        return getFieldMappingForLogFormat(logFormat.getKeyName());
    }

    public Map<String, String> getFieldMappingForLogFormat(final String logFormat) {
        // TODO - restructure mappings file to be map of raw name to map of other format mappings
        return mappings.stream()
                .map(fieldMapping -> Map.entry(fieldMapping.get(LogFormat.NONE.getKeyName()), fieldMapping.get(logFormat)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
