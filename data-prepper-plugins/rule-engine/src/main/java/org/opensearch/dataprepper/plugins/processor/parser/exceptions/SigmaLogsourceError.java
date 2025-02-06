/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaLogsourceError extends SigmaError {

    public SigmaLogsourceError(String message) {
        super(message);
    }
}