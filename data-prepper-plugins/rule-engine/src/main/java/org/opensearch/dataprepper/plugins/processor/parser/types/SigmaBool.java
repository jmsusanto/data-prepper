/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.types;

public class SigmaBool implements SigmaType {

    private Boolean aBoolean;

    public SigmaBool(Boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public Boolean getBoolean() {
        return aBoolean;
    }

    @Override
    public String toString() {
        return String.valueOf(aBoolean);
    }
}