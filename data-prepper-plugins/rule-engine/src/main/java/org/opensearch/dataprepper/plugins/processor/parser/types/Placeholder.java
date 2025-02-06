/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.types;

public class Placeholder {
    private String name;

    public Placeholder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}