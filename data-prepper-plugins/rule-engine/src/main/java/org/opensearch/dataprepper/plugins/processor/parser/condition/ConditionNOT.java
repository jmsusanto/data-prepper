/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.condition;

import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.List;

public class ConditionNOT extends ConditionItem {

    private int argCount;
    private boolean operator;

    public ConditionNOT(boolean tokenList, List<Either<ConditionItem, String>> args) {
        super(1, tokenList, args);
        this.argCount = 1;
        this.operator = true;
    }
}