/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.types;

public class SigmaNull implements SigmaType {
    private Object nullVal = null;

    @Override
    public boolean equals(Object o) {
        return getClass() == o.getClass();
    }
}