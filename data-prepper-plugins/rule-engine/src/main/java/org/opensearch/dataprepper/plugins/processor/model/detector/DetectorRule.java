package org.opensearch.dataprepper.plugins.processor.model.detector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DetectorRule {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
