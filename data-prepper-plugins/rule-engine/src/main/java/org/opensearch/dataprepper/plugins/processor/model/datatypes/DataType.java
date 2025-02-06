package org.opensearch.dataprepper.plugins.processor.model.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf.OCSF;

import java.util.HashMap;
import java.util.Map;

@JsonDeserialize(as = OCSF.class)
public abstract class DataType {
    @JsonIgnore
    private final HashMap<String, String> dataTypeMetadata;

    public DataType() {
        dataTypeMetadata = new HashMap<>();
    }

    public abstract Object getValue(final String fieldName);
    public abstract String getTimeFieldName();

    public void putDataTypeMetadataValue(final String metadataFieldName, final String metadataFieldValue) {
        dataTypeMetadata.put(metadataFieldName, metadataFieldValue);
    }

    public void putAllDataTypeMetadata(final Map<String, String> metadataEntries) {
        dataTypeMetadata.putAll(metadataEntries);
    }

    public String getDataTypeMetadataValue(final String metadataFieldName) {
        return dataTypeMetadata.get(metadataFieldName);
    }
}
