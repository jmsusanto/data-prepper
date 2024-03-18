package org.opensearch.dataprepper.plugins.processor.model.datatypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import org.opensearch.dataprepper.expression.ExpressionEvaluator;
import org.opensearch.dataprepper.model.event.DefaultEventHandle;
import org.opensearch.dataprepper.model.event.DefaultEventMetadata;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.event.EventHandle;
import org.opensearch.dataprepper.model.event.EventMetadata;
import org.opensearch.dataprepper.model.event.EventType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataType implements Event {
    @JsonIgnore
    private final EventMetadata eventMetadata;
    @JsonIgnore
    private final HashMap<String, String> metadata;
    @JsonIgnore
    private final transient EventHandle eventHandle;

    public DataType() {
        eventMetadata = DefaultEventMetadata.builder().withEventType(EventType.LOG.toString()).build();
        metadata = new HashMap<>();
        eventHandle = new DefaultEventHandle(eventMetadata.getTimeReceived());
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


    @Override
    public void put(String key, Object value) {

    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> List<T> getList(String key, Class<T> clazz) {
        return null;
    }

    @Override
    public void delete(String key) {

    }

    @Override
    public String toJsonString() {
        return null;
    }

    @Override
    public JsonNode getJsonNode() {
        return null;
    }

    @Override
    public String getAsJsonString(String key) {
        return null;
    }

    @Override
    public EventMetadata getMetadata() {
        return eventMetadata;
    }

    @Override
    public boolean containsKey(String key) {
        return false;
    }

    @Override
    public boolean isValueAList(String key) {
        return false;
    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public String formatString(String format) {
        return null;
    }

    @Override
    public String formatString(String format, ExpressionEvaluator expressionEvaluator) {
        return format;
    }

    @Override
    public EventHandle getEventHandle() {
        return eventHandle;
    }

    @Override
    public JsonStringBuilder jsonBuilder() {
        return null;
    }
}
