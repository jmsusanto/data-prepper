/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.condition;

import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetections;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaType;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.Collections;

public class ConditionFieldEqualsValueExpression extends ConditionItem {

    private String field;
    private SigmaType value;

    private Either<ConditionItem, SigmaDetectionItem> parent;

    public ConditionFieldEqualsValueExpression(String field, SigmaType value) {
        super(2, false, Collections.emptyList());
        this.field = field;
        this.value = value;
    }

    public ConditionFieldEqualsValueExpression postProcess(SigmaDetections detections, Object parent) {
        this.parent = parent instanceof ConditionItem? Either.left((ConditionItem) parent): Either.right((SigmaDetectionItem) parent);
        return this;
    }

    public String getField() {
        return field;
    }

    public SigmaType getValue() {
        return value;
    }
}