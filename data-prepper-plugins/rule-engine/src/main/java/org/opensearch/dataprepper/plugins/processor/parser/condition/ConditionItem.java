/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.condition;

import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaConditionError;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetectionItem;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetections;
import org.opensearch.dataprepper.plugins.processor.parser.utils.AnyOneOf;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.ArrayList;
import java.util.List;

public class ConditionItem {

    private int argCount;
    private boolean tokenList;
    private List<Either<ConditionItem, String>> args;

    private Either<ConditionItem, SigmaDetectionItem> parent;
    private boolean operator;

    public ConditionItem(int argCount, boolean tokenList,
                         List<Either<ConditionItem, String>> args) {
        this.argCount = argCount;
        this.tokenList = tokenList;
        this.args = args;
    }

    public ConditionItem postProcess(SigmaDetections detections, Object parent) throws SigmaConditionError {
        this.parent = parent instanceof ConditionItem? Either.left((ConditionItem) parent): Either.right((SigmaDetectionItem) parent);

        List<Either<ConditionItem, String>> newArgs = new ArrayList<>();
        for (Either<ConditionItem, String> arg: this.args) {
            newArgs.add(Either.left(arg.getLeft().postProcess(detections, parent)));
        }
        this.args = newArgs;
        return this;
    }

    public void setParent(ConditionItem parent) {
        this.parent = Either.left(parent);
    }

    public List<Either<ConditionItem, String>> getArgs() {
        return args;
    }

    public void setArgs(List<Either<ConditionItem, String>> args) {
        this.args = args;
    }
}