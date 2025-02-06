package org.opensearch.dataprepper.plugins.processor.model.log;

public enum LogFormat {
    NONE("raw_field"),
    OCSF("ocsf"),
    ECS("ecs");

    private final String keyName;

    LogFormat(final String keyName) {
        this.keyName = keyName;
    }

    public String getKeyName() {
        return keyName;
    }
}
