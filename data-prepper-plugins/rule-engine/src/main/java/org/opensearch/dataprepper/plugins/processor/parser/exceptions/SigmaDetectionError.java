/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaDetectionError extends SigmaError {

    public SigmaDetectionError(String message) {
        super(message);
    }
}