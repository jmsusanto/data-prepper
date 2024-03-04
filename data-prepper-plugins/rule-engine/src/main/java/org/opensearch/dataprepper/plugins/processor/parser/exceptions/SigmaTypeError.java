/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.exceptions;

public class SigmaTypeError extends SigmaModifierError {

    public SigmaTypeError(String message) {
        super(message);
    }
}