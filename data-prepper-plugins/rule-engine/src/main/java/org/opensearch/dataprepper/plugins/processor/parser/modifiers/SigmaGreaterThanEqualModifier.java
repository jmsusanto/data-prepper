/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.modifiers;

import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaCompareExpression;

import java.util.List;

public class SigmaGreaterThanEqualModifier extends SigmaCompareModifier {

    public SigmaGreaterThanEqualModifier(SigmaDetectionItem detectionItem, List<Class<? extends SigmaModifier>> appliedModifiers) {
        super(detectionItem, appliedModifiers);
        this.setOp(SigmaCompareExpression.CompareOperators.GTE);
    }
}