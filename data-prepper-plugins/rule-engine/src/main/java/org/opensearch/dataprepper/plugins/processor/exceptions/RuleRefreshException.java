package org.opensearch.dataprepper.plugins.processor.exceptions;

public class RuleRefreshException extends RuntimeException {
    public RuleRefreshException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
