package org.opensearch.dataprepper.plugins.processor.model.datatypes;

import java.util.HashMap;
import java.util.Map;

public abstract class DataType {
    private final HashMap<String, String> metadata;

    public DataType() {
        metadata = new HashMap<>();
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public abstract Object getValue(final String fieldName);

    public void putMetadataValue(final String metadataFieldName, final String metadataFieldValue) {
        metadata.put(metadataFieldName, metadataFieldValue);
    }

    public void putAllMetadata(final Map<String, String> metadataEntries) {
        metadata.putAll(metadataEntries);
    }

    public String getMetadataValue(final String metadataFieldName) {
        return metadata.get(metadataFieldName);
    }
}
