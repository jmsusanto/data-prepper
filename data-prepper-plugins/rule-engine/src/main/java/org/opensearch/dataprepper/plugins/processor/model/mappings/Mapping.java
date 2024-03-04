package org.opensearch.dataprepper.plugins.processor.model.mappings;

import java.util.List;
import java.util.Map;

public class Mapping {
    private List<Map<String, String>> mappings;

    public List<Map<String, String>> getMappings() {
        return mappings;
    }

    public void setMappings(final List<Map<String, String>> mappings) {
        this.mappings = mappings;
    }
}
