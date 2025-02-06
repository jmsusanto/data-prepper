/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaIdentifierError extends SigmaError {

    public SigmaIdentifierError(String message) {
        super(message);
    }
}