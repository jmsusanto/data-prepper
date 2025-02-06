/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaValueError extends SigmaError {

    public SigmaValueError(String message) {
        super(message);
    }
}