/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.modifiers;

import org.apache.commons.lang3.tuple.Pair;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaCompareExpression;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaFloat;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaType;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.List;

public class SigmaCompareModifier extends SigmaValueModifier {
    private String op;

    public SigmaCompareModifier(SigmaDetectionItem detectionItem, List<Class<? extends SigmaModifier>> appliedModifiers) {
        super(detectionItem, appliedModifiers);
    }

    public void setOp(String op) {
        this.op = op;
    }

    @Override
    public Pair<Class<?>, Class<?>> getTypeHints() {
        return Pair.of(SigmaFloat.class, SigmaCompareExpression.class);
    }

    @Override
    public Either<SigmaType, List<SigmaType>> modify(Either<SigmaType, List<SigmaType>> val) {
        if (val.isLeft()) {
            return Either.left(new SigmaCompareExpression((SigmaFloat) val.getLeft(), this.op));
        }
        return null;
    }
}