/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.objects;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionFieldEqualsValueExpression;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionIdentifier;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionItem;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionSelector;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionTraverseVisitor;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionValueExpression;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaConditionError;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;
import org.opensearch.dataprepper.plugins.processor.rules.antlr.ConditionLexer;
import org.opensearch.dataprepper.plugins.processor.rules.antlr.ConditionParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SigmaCondition {

    private final String identifier = "[a-zA-Z0-9-_]+";

    private final List<String> quantifier = List.of("1", "any", "all");

    private final String identifierPattern = "[a-zA-Z0-9*_]+";

    private final List<Either<List<String>, String>> selector = List.of(Either.left(quantifier), Either.right("of"), Either.right(identifierPattern));

    private final List<String> operators = List.of("not ", " and ", " or ");

    private String condition;

    private String aggregation;

    private SigmaDetections detections;

    private ConditionParser parser;

    private ConditionTraverseVisitor conditionVisitor;

    public SigmaCondition(String condition, SigmaDetections detections) {
        if (condition.contains(" | ")) {
            this.condition = condition.split(" \\| ")[0];
            this.aggregation = condition.split(" \\| ")[1];
        } else {
            this.condition = condition;
            this.aggregation = "";
        }

        this.detections = detections;

        ConditionLexer lexer = new ConditionLexer(CharStreams.fromString(this.condition));
        this.parser = new ConditionParser(new CommonTokenStream(lexer));
        this.conditionVisitor = new ConditionTraverseVisitor(this);
    }

    public ConditionItem parsed() throws SigmaConditionError {
        ConditionItem parsedConditionItem;
        Either<ConditionItem, String> itemOrCondition = conditionVisitor.visit(parser.start());
        if (itemOrCondition.isLeft()) {
            parsedConditionItem = itemOrCondition.getLeft();
        } else {
            parsedConditionItem = Objects.requireNonNull(parsed(condition));
        }

        return parsedConditionItem;
    }

    public List<Either<ConditionItem, String>> convertArgs(List<Either<ConditionItem, String>> parsedArgs) throws SigmaConditionError {
        List<Either<ConditionItem, String>> newArgs = new ArrayList<>();

        for (Either<ConditionItem, String> parsedArg: parsedArgs) {
            if (parsedArg.isRight()) {
                ConditionItem newItem = parsed(parsedArg.get());
                newArgs.add(Either.left(newItem));
            } else {
                newArgs.add(parsedArg);
            }
        }
        return newArgs;
    }

    private ConditionItem parsed(String token) throws SigmaConditionError {
        List<String> subTokens = List.of(token.split(" "));
        if (subTokens.size() < 3 && token.matches(identifier)) {
            ConditionIdentifier conditionIdentifier =
                    new ConditionIdentifier(Collections.singletonList(Either.right(token)));
            ConditionItem item = conditionIdentifier.postProcess(detections, null);
            return item instanceof ConditionFieldEqualsValueExpression? (ConditionFieldEqualsValueExpression) item:
                    (item instanceof ConditionValueExpression ? (ConditionValueExpression) item: item);
        } else if (subTokens.size() == 3 && quantifier.contains(subTokens.get(0)) && selector.get(1).get().equals(subTokens.get(1)) &&
                subTokens.get(2).matches(identifierPattern)) {
            ConditionSelector conditionSelector =
                    new ConditionSelector(subTokens.get(0), subTokens.get(2));
            ConditionItem item = conditionSelector.postProcess(detections, null);
            return item instanceof ConditionFieldEqualsValueExpression? (ConditionFieldEqualsValueExpression) item:
                    (item instanceof ConditionValueExpression ? (ConditionValueExpression) item: item);
        }
        return null;
    }
}