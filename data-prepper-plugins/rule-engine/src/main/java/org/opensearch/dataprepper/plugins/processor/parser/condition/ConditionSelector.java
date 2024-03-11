/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.condition;

import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaConditionError;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetections;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ConditionSelector {

    private int argCount;
    private boolean tokenList;
    private Either<Class<ConditionAND>, Class<ConditionOR>> condClass;
    private String pattern;

    private Either<ConditionItem, SigmaDetectionItem> parent;
    private boolean operator;

    public ConditionSelector(String quantifier, String identifierPattern) {
        this.argCount = 2;
        this.tokenList = true;

        if ("1".equals(quantifier) || "any".equals(quantifier)) {
            this.condClass = Either.right(ConditionOR.class);
        } else {
            this.condClass = Either.left(ConditionAND.class);
        }
        this.pattern = identifierPattern;
    }

    public ConditionItem postProcess(SigmaDetections detections, Object parent) throws SigmaConditionError {
        this.parent = parent instanceof ConditionItem? Either.left((ConditionItem) parent): Either.right((SigmaDetectionItem) parent);

        Pattern r;
        if (this.pattern.equals("them")) {
            r = Pattern.compile(".*");
        } else {
            r = Pattern.compile(this.pattern.replace("*", ".*"));
        }

        List<Either<ConditionItem, String>> ids = new ArrayList<>();
        for (String identifier: detections.getDetections().keySet()) {
            if (r.matcher(identifier).matches()) {
                ConditionItem item = new ConditionIdentifier(List.of(Either.right(identifier))).postProcess(detections, parent);
                ids.add(Either.left(item));
            }
        }

        ConditionItem conditionItem;
        if (this.condClass.isLeft()) {
            conditionItem = new ConditionAND(false, ids);
        } else {
            conditionItem = new ConditionOR(false, ids);
        }
        return conditionItem;
    }
}