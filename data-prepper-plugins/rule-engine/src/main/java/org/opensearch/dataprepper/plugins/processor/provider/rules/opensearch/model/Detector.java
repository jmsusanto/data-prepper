package org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Detector {
    private String name;
    private List<Input> inputs;
    @JsonProperty("findings_index")
    private String findingsIndex;
    @JsonProperty("monitor_id")
    private List<String> monitorId;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Input> getInputs() {
        return inputs;
    }

    public void setInputs(final List<Input> inputs) {
        this.inputs = inputs;
    }

    public String getFindingsIndex() {
        return findingsIndex;
    }

    public void setFindingsIndex(final String findingsIndex) {
        this.findingsIndex = findingsIndex;
    }

    public List<String> getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(final List<String> monitorId) {
        this.monitorId = monitorId;
    }
}
