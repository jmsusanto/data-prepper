package org.opensearch.dataprepper.plugins.processor.formats.accessors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.plugins.processor.RuleEngineConfig;
import org.opensearch.dataprepper.plugins.processor.mappings.MappingParser;
import org.opensearch.dataprepper.plugins.processor.model.log.LogFormat;
import org.opensearch.dataprepper.plugins.processor.model.log.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class FieldAccessor {
    private static final Logger LOG = LoggerFactory.getLogger(FieldAccessor.class);

    private final Map<String, String> mappings;

    public FieldAccessor(final RuleEngineConfig config) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final MappingParser mappingParser = new MappingParser(objectMapper);

        final LogType logType = config.getLogType();
        LOG.info("Log type: {}", logType.name());
        final LogFormat logFormat = config.getLogFormat();
        LOG.info("Log format: {}", logFormat.name());
        mappings = mappingParser.parseMappings(logType, logFormat);
    }

    public String getStringValue(final Event event, final String fieldName) {
        return getValue(event, fieldName, String.class);
    }

    public Boolean getBooleanValue(final Event event, final String fieldName) {
        return getValue(event, fieldName, Boolean.class);
    }

    public Integer getIntegerValue(final Event event, final String fieldName) {
        return getValue(event, fieldName, Integer.class);
    }

    public Float getFloatValue(final Event event, final String fieldName) {
        return getValue(event, fieldName, Float.class);
    }

    public Object getObjectValue(final Event event, final String fieldName) {
        return getValue(event, fieldName, Object.class);
    }

    private <T> T getValue(final Event event, final String fieldName, final Class<T> clazz) {
        //LOG.info("Field: {}", fieldName);
        final String mappedField = convertFieldName(fieldName);
        //System.out.println("Mapped field: " + mappedField);
        final String jsonPointer = getJsonPointer(mappedField);
        //System.out.println("Pointer: "+ jsonPointer);
        final T value = event.get(jsonPointer, clazz);
        //LOG.info("Value: {}", value);
        return value;
    }

    private String convertFieldName(final String fieldName) {
        return mappings.get(fieldName);
    }

    // TODO - need flag for this
    private String getJsonPointer(final String field) {
        //return field;
        return field.replace(".", "/");
    }
}
