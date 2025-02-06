/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaStatusError extends SigmaError {

    public SigmaStatusError(String message) {
        super(message);
    }
}