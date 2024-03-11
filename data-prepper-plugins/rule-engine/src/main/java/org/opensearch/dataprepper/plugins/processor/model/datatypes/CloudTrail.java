package org.opensearch.dataprepper.plugins.processor.model.datatypes;

public class CloudTrail extends DataType {
    private final String eventName;
    private final String eventSource;

    public CloudTrail(final String eventName, final String eventSource) {
        super();
        this.eventName = eventName;
        this.eventSource = eventSource;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventSource() {
        return eventSource;
    }

    @Override
    public Object getValue(final String fieldName) {
        switch (fieldName) {
            case "eventName": return eventName;
            case "eventSource": return eventSource;
            default: throw new IllegalArgumentException("Field " + fieldName + " does not exist in class " + getClass().getName());
        }
    }
}
