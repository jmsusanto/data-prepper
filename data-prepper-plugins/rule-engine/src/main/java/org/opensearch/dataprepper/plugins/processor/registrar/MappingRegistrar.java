package org.opensearch.dataprepper.plugins.processor.registrar;

import org.opensearch.dataprepper.plugins.processor.mappings.FileMappingProvider;
import org.opensearch.dataprepper.plugins.processor.model.log.LogType;
import org.opensearch.dataprepper.plugins.processor.model.mappings.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class MappingRegistrar {
    private static final Logger LOG = LoggerFactory.getLogger(MappingRegistrar.class);

    private final Map<String, Supplier<Mapping>> mappingSuppliers;

    public MappingRegistrar() {
        mappingSuppliers = new HashMap<>();

        final FileMappingProvider mappingProvider = new FileMappingProvider();
        Arrays.stream(LogType.values()).forEach(logType ->
                registerMapping(logType.name().toLowerCase(), () -> mappingProvider.getMapping(logType.getMappingsFile())));
    }

    public void registerMapping(final String logType, final Supplier<Mapping> mappingSupplier) {
        mappingSuppliers.put(logType, mappingSupplier);
    }

    public Map<String, String> getMapping(final String logType, final String logFormat) {
        final Optional<Map<String, String>> optionalMapping = Optional.of(mappingSuppliers)
                .map(mappingSuppliers -> mappingSuppliers.get(logType))
                .map(Supplier::get)
                .map(mapping -> mapping.getFieldMappingForLogFormat(logFormat));

        if (optionalMapping.isEmpty()) {
            LOG.warn("No mappings found for log type {} and log format {}. Using empty mappings", logType, logFormat);
            return Collections.emptyMap();
        }

        return optionalMapping.get();
    }
}
