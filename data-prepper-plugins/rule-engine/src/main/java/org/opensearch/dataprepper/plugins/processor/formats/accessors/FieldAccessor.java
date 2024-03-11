package org.opensearch.dataprepper.plugins.processor.formats.accessors;

import org.opensearch.dataprepper.plugins.processor.exceptions.MappingException;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FieldAccessor {
    private static final Logger LOG = LoggerFactory.getLogger(FieldAccessor.class);

    private final Map<String, String> mapping;

    public FieldAccessor(final Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public String getStringValue(final DataType event, final String fieldName) {
        return getValue(event, convertFieldName(fieldName), String.class);
    }

    public Boolean getBooleanValue(final DataType event, final String fieldName) {
        return getValue(event, convertFieldName(fieldName), Boolean.class);
    }

    public Integer getIntegerValue(final DataType event, final String fieldName) {
        return getValue(event, convertFieldName(fieldName), Integer.class);
    }

    public Float getFloatValue(final DataType event, final String fieldName) {
        return getValue(event, convertFieldName(fieldName), Float.class);
    }

    public Object getObjectValue(final DataType event, final String fieldName) {
        return getValue(event, convertFieldName(fieldName), Object.class);
    }

    private <T> T getValue(final DataType event, final String fieldName, final Class<T> clazz) {
        try {
            return clazz.cast(event.getValue(fieldName));
        } catch (final ClassCastException e) {
            throw new MappingException("Unable to cast field " + fieldName + " to class " + clazz.getName(), e);
        }
    }

    private String convertFieldName(final String fieldName) {
        final String mappedFieldName = mapping.get(fieldName);
        return mappedFieldName == null ? fieldName : mappedFieldName;
    }

    // TODO - need flag for this
    private String getJsonPointer(final String field) {
        //return field;
        return field.replace(".", "/");
    }
}
