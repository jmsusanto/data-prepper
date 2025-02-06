/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaDateError extends SigmaError {

    public SigmaDateError(String message) {
        super(message);
    }
}