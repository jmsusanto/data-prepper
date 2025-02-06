/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.modifiers;

import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaRegularExpressionError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaTypeError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaValueError;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaType;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.List;

public abstract class SigmaValueModifier extends SigmaModifier {

    public SigmaValueModifier(SigmaDetectionItem detectionItem, List<Class<? extends SigmaModifier>> appliedModifiers) {
        super(detectionItem, appliedModifiers);
    }

    public abstract Either<SigmaType, List<SigmaType>> modify(Either<SigmaType, List<SigmaType>> val) throws SigmaValueError, SigmaRegularExpressionError, SigmaTypeError;
}