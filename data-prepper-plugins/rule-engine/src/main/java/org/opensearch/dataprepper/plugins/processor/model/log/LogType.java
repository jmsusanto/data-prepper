package org.opensearch.dataprepper.plugins.processor.model.log;

public enum LogType {
    CLOUDTRAIL("cloudtrail.json");

    private final String mappingsFile;

    LogType(final String mappingsFile) {
        this.mappingsFile = mappingsFile;
    }

    public String getMappingsFile() {
        return mappingsFile;
    }
}
