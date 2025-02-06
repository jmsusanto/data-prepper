/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.modifiers;

import org.apache.commons.lang3.tuple.Pair;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaTypeError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaValueError;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaCIDRExpression;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaString;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaType;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.List;

public class SigmaCIDRModifier extends SigmaValueModifier {

    public SigmaCIDRModifier(SigmaDetectionItem detectionItem, List<Class<? extends SigmaModifier>> appliedModifiers) {
        super(detectionItem, appliedModifiers);
    }

    @Override
    public Pair<Class<?>, Class<?>> getTypeHints() {
        return Pair.of(SigmaString.class, null);
    }

    @Override
    public Either<SigmaType, List<SigmaType>> modify(Either<SigmaType, List<SigmaType>> val) throws SigmaValueError, SigmaTypeError {
        if (val.isLeft() && val.getLeft() instanceof SigmaString) {
            if (this.getAppliedModifiers().size() > 0) {
                throw new SigmaValueError("CIDR expression modifier only applicable to unmodified values");
            }
            return Either.left(new SigmaCIDRExpression(val.getLeft().toString()));
        }
        return null;
    }
}