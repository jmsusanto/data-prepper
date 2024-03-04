package org.opensearch.dataprepper.plugins.processor.model.detector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DetectorInput {
    private List<String> indices;
    @JsonProperty("custom_rules")
    private List<DetectorRule> customRules;
    @JsonProperty("pre_packaged_rules")
    private List<DetectorRule> prePackagedRules;

    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(final List<String> indices) {
        this.indices = indices;
    }

    public List<DetectorRule> getCustomRules() {
        return customRules;
    }

    public void setCustomRules(final List<DetectorRule> customRules) {
        this.customRules = customRules;
    }

    public List<DetectorRule> getPrePackagedRules() {
        return prePackagedRules;
    }

    public void setPrePackagedRules(final List<DetectorRule> prePackagedRules) {
        this.prePackagedRules = prePackagedRules;
    }
}
