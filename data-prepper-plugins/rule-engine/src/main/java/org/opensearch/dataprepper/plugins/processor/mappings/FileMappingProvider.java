package org.opensearch.dataprepper.plugins.processor.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.dataprepper.plugins.processor.exceptions.MappingException;
import org.opensearch.dataprepper.plugins.processor.model.mappings.Mapping;

import java.io.IOException;
import java.net.URL;

public class FileMappingProvider {
    private static final String MAPPING_PATH_FORMAT = "mappings/%s";

    private final ObjectMapper objectMapper;

    public FileMappingProvider() {
        this.objectMapper = new ObjectMapper();
    }

    public Mapping getMapping(final String mappingsFile) {
        final String relativePath = String.format(MAPPING_PATH_FORMAT, mappingsFile);
        final URL mappingsPath = getClass().getClassLoader().getResource(relativePath);
        try {
            return objectMapper.readValue(mappingsPath, Mapping.class);
        } catch (final IOException e) {
            throw new MappingException("Exception reading mapping: " + relativePath, e);
        }
    }
}
