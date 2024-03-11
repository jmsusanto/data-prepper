package org.opensearch.dataprepper.plugins.processor.model.datatypes;

public class OCSF extends DataType {
    private final String apiOperation;
    private final String apiServiceName;

    public OCSF(final String apiOperation, final String apiServiceName) {
        super();
        this.apiOperation = apiOperation;
        this.apiServiceName = apiServiceName;
    }

    @Override
    public Object getValue(final String fieldName) {
        switch (fieldName) {
            case "api.operation": return apiOperation;
            case "api.service.name": return apiServiceName;
            default: throw new IllegalArgumentException("Field " + fieldName + " does not exist in class " + getClass().getName());
        }
    }
}
