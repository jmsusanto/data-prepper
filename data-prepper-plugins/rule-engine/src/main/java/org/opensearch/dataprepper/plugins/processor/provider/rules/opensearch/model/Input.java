package org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Input {
    @JsonProperty("detector_input")
    private DetectorInput detectorInput;

    public DetectorInput getDetectorInput() {
        return detectorInput;
    }

    public void setDetectorInput(DetectorInput detectorInput) {
        this.detectorInput = detectorInput;
    }
}
