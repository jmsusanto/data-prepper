package org.opensearch.dataprepper.plugins.processor.exceptions;

public class MappingException extends RuntimeException {
    public MappingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
