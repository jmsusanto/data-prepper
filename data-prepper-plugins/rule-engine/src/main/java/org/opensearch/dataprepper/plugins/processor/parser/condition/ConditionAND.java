/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.condition;

import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.List;

public class ConditionAND extends ConditionItem {

    private int argCount;
    private boolean operator;

    public ConditionAND(boolean tokenList, List<Either<ConditionItem, String>> args) {
        super(2, tokenList, args);
        this.argCount = 2;
        this.operator = true;
    }
}