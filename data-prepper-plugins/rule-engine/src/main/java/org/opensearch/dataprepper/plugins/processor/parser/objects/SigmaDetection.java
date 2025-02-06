/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.objects;

import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionAND;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionFieldEqualsValueExpression;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionItem;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionOR;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionValueExpression;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaConditionError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaDetectionError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaModifierError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaRegularExpressionError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaValueError;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SigmaDetection {

    private List<Either<SigmaDetectionItem, SigmaDetection>> detectionItems;
    private Either<Class<ConditionAND>, Class<ConditionOR>> itemLinking;

    private Either<ConditionItem, SigmaDetection> parent;

    public SigmaDetection(List<Either<SigmaDetectionItem, SigmaDetection>> detectionItems,
                          Either<Class<ConditionAND>, Class<ConditionOR>> itemLinking) throws SigmaDetectionError {
        this.detectionItems = detectionItems;
        this.itemLinking = itemLinking == null? Either.left(ConditionAND.class): itemLinking;

        if (this.detectionItems.size() == 0) {
            throw new SigmaDetectionError("Detection is empty");
        }

        List<Class<?>> typeSet = new ArrayList<>();
        for (Either<SigmaDetectionItem, SigmaDetection> detectionItem: detectionItems) {
            if (detectionItem.isLeft()) {
                typeSet.add(SigmaDetectionItem.class);
            }
            if (detectionItem.isRight()) {
                typeSet.add(SigmaDetection.class);
            }
        }

        if (typeSet.contains(SigmaDetectionItem.class)) {
            this.itemLinking = Either.left(ConditionAND.class);
        } else {
            this.itemLinking = Either.right(ConditionOR.class);
        }
    }

    @SuppressWarnings("unchecked")
    protected static SigmaDetection fromDefinition(Object definition) throws SigmaModifierError, SigmaDetectionError, SigmaValueError, SigmaRegularExpressionError {
        List<Either<SigmaDetectionItem, SigmaDetection>> detectionItems = new ArrayList<>();
        if (definition instanceof Map) {
            for (Map.Entry<String, Object> defEntry: ((Map<String, Object>) definition).entrySet()) {
                Object val = defEntry.getValue();

                if (val == null) {
                    detectionItems.add(Either.left(SigmaDetectionItem.fromMapping(defEntry.getKey(),
                            Either.left(null))));
                } else if (val instanceof Integer) {
                    detectionItems.add(Either.left(SigmaDetectionItem.fromMapping(defEntry.getKey(),
                            Either.left((Integer) val))));
                } else if (val instanceof Float) {
                    detectionItems.add(Either.left(SigmaDetectionItem.fromMapping(defEntry.getKey(),
                            Either.left((Float) val))));
                } else if (val instanceof String) {
                    detectionItems.add(Either.left(SigmaDetectionItem.fromMapping(defEntry.getKey(),
                            Either.left(val.toString()))));
                } else if (val instanceof Boolean) {
                    detectionItems.add(Either.left(SigmaDetectionItem.fromMapping(defEntry.getKey(),
                            Either.left((Boolean) val))));
                } else if (val instanceof List) {
                    SigmaDetectionItem item =
                    SigmaDetectionItem.fromMapping(defEntry.getKey(), Either.right(((List<Object>) val)));
                    detectionItems.add(Either.left(item));
                }
            }
            return new SigmaDetection(detectionItems, null);
        } else if (definition instanceof String || definition instanceof Integer) {
            detectionItems.add(Either.left(SigmaDetectionItem.fromValue(Either.left(definition))));
            return new SigmaDetection(detectionItems, null);
        } else if (definition instanceof ArrayList) {
            List<Object> definitionList = (List<Object>) definition;

            boolean isItem = true;
            for (Object definitionElem: definitionList) {
                if (!(definitionElem instanceof String) && !(definitionElem instanceof Integer)) {
                    detectionItems.add(Either.right(SigmaDetection.fromDefinition(definitionElem)));
                    isItem = false;
                }
            }

            if (isItem) {
                detectionItems.add(Either.left(SigmaDetectionItem.fromValue(Either.right(definitionList))));
                return new SigmaDetection(detectionItems, null);
            }
            return new SigmaDetection(detectionItems, null);
        }
        throw new SigmaValueError("Unexpected Values");
    }

    public ConditionItem postProcess(SigmaDetections detections, Object parent) throws SigmaConditionError {
        this.parent = parent instanceof ConditionItem? Either.left((ConditionItem) parent): Either.right((SigmaDetection)parent);

        List<Either<ConditionItem, String>> valueExpressions = new ArrayList<>();
        for (Either<SigmaDetectionItem, SigmaDetection> detectionItem: this.detectionItems) {
            if (detectionItem.isLeft()) {
                Either<Either<ConditionAND, ConditionOR>, Either<ConditionFieldEqualsValueExpression, ConditionValueExpression>> item =
                    detectionItem.getLeft().postProcess(detections, this);

                if (item.isLeft() && item.getLeft().isLeft()) {
                    valueExpressions.add(Either.left(item.getLeft().getLeft()));
                } else if (item.isLeft() && item.getLeft().isRight()) {
                    valueExpressions.add(Either.left(item.getLeft().get()));
                } else if (item.isRight() && item.get().isLeft()) {
                    valueExpressions.add(Either.left(item.get().getLeft()));
                } else if (item.isRight() && item.get().isRight()) {
                    valueExpressions.add(Either.left(item.get().get()));
                }
            } else if (detectionItem.isRight()) {
                ConditionItem item = detectionItem.get().postProcess(detections, this);
                valueExpressions.add(Either.left(item));
            }
        }

        if (valueExpressions.size() == 1) {
            return valueExpressions.get(0).getLeft();
        } else {
            if (itemLinking.isLeft()) {
                return new ConditionAND(false, valueExpressions);
            } else if (itemLinking.isRight()) {
                return new ConditionOR(false, valueExpressions);
            }
        }
        return null;
    }

    public List<Either<SigmaDetectionItem, SigmaDetection>> getDetectionItems() {
        return detectionItems;
    }

    public Either<Class<ConditionAND>, Class<ConditionOR>> getItemLinking() {
        return itemLinking;
    }
}