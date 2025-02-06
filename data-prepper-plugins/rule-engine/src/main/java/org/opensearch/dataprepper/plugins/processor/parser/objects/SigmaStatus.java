/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.objects;

import java.util.Locale;

public enum SigmaStatus {
    STABLE,
    EXPERIMENTAL,
    TEST,
    DEPRECATED,
    UNSUPPORTED;

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}