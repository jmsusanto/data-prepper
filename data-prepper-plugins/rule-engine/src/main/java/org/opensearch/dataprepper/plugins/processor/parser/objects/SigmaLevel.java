/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.objects;

import java.util.Locale;

public enum SigmaLevel {
    INFORMATIONAL,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}