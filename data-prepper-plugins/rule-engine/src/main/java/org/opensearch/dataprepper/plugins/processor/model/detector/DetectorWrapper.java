package org.opensearch.dataprepper.plugins.processor.model.detector;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DetectorWrapper {
    private Detector detector;

    public Detector getDetector() {
        return detector;
    }

    public void setDetector(final Detector detector) {
        this.detector = detector;
    }
}
