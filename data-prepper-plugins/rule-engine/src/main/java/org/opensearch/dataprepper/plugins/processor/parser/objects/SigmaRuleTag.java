/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.objects;

public class SigmaRuleTag {

    private String namespace;

    private String name;

    public SigmaRuleTag(String namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    public static SigmaRuleTag fromStr(String tag) {
        String[] tagParts = tag.split("\\.", 2);
        return new SigmaRuleTag(tagParts[0], tagParts[1]);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s.%s", namespace, name);
    }
}