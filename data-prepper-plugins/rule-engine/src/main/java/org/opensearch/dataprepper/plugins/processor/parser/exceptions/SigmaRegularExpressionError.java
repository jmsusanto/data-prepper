/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaRegularExpressionError extends SigmaError {

    public SigmaRegularExpressionError(String message) {
        super(message);
    }
}