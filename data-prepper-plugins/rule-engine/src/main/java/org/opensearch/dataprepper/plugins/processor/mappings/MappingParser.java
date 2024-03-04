package org.opensearch.dataprepper.plugins.processor.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.dataprepper.plugins.processor.exceptions.MappingException;
import org.opensearch.dataprepper.plugins.processor.model.log.LogFormat;
import org.opensearch.dataprepper.plugins.processor.model.log.LogType;
import org.opensearch.dataprepper.plugins.processor.model.mappings.Mapping;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

public class MappingParser {
    private static final String MAPPING_PATH_FORMAT = "mappings/%s";

    private final ObjectMapper objectMapper;

    public MappingParser(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, String> parseMappings(final LogType logType, final LogFormat logFormat) {
        final Mapping mapping = getMapping(logType);

        return mapping.getMappings().stream()
                .map(fieldMapping -> Map.entry(fieldMapping.get(LogFormat.NONE.getKeyName()), fieldMapping.get(logFormat.getKeyName())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Mapping getMapping(final LogType logType) {
        final String relativePath = String.format(MAPPING_PATH_FORMAT, logType.getMappingsFile());
        final URL mappingsPath = getClass().getClassLoader().getResource(relativePath);
        try {
            return objectMapper.readValue(mappingsPath, Mapping.class);
        } catch (final IOException e) {
            throw new MappingException("Exception reading mapping: " + relativePath, e);
        }
    }
}
