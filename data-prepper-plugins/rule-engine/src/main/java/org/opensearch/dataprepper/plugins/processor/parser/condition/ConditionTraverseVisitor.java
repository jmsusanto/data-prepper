/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.condition;

import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaConditionError;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaCondition;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;
import org.opensearch.dataprepper.plugins.processor.rules.antlr.ConditionBaseVisitor;
import org.opensearch.dataprepper.plugins.processor.rules.antlr.ConditionParser;

import java.util.List;

public class ConditionTraverseVisitor extends ConditionBaseVisitor<Either<ConditionItem, String>> {

    private final SigmaCondition sigmaCondition;

    public ConditionTraverseVisitor(SigmaCondition sigmaCondition) {
        this.sigmaCondition = sigmaCondition;
    }

    @Override
    public Either<ConditionItem, String> visitStart(ConditionParser.StartContext ctx) {
        return super.visit(ctx.expression());
    }

    @Override
    public Either<ConditionItem, String> visitIdentOrSelectExpression(ConditionParser.IdentOrSelectExpressionContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            return Either.right(ctx.IDENTIFIER().getText());
        }
        return Either.right(ctx.SELECTOR().getText());
    }

    @Override
    public Either<ConditionItem, String> visitNotExpression(ConditionParser.NotExpressionContext ctx) {
        try {
            Either<ConditionItem, String> exp = visit(ctx.expression());
            ConditionNOT condition = new ConditionNOT(false, List.of(exp.isLeft()? Either.left(exp.getLeft()): Either.right(exp.get())));
            condition.setArgs(sigmaCondition.convertArgs(condition.getArgs()));
            return Either.left(condition);
        } catch (SigmaConditionError ex) {
            return null;
        }
    }

    @Override
    public Either<ConditionItem, String> visitAndExpression(ConditionParser.AndExpressionContext ctx) {
        try {
            Either<ConditionItem, String> left = visit(ctx.left);
            Either<ConditionItem, String> right = visit(ctx.right);
            ConditionAND condition = new ConditionAND(false, List.of(
                    left.isLeft()? Either.left(left.getLeft()): Either.right(left.get()),
                    right.isLeft()? Either.left(right.getLeft()): Either.right(right.get())));
            condition.setArgs(sigmaCondition.convertArgs(condition.getArgs()));
            return Either.left(condition);
        } catch (SigmaConditionError ex) {
            return null;
        }
    }

    @Override
    public Either<ConditionItem, String> visitOrExpression(ConditionParser.OrExpressionContext ctx) {
        try {
            Either<ConditionItem, String> left = visit(ctx.left);
            Either<ConditionItem, String> right = visit(ctx.right);
            ConditionOR condition = new ConditionOR(false, List.of(
                    left.isLeft()? Either.left(left.getLeft()): Either.right(left.get()),
                    right.isLeft()? Either.left(right.getLeft()): Either.right(right.get())));
            condition.setArgs(sigmaCondition.convertArgs(condition.getArgs()));
            return Either.left(condition);
        } catch (SigmaConditionError ex) {
            return null;
        }
    }

    @Override
    public Either<ConditionItem, String> visitParenExpression(ConditionParser.ParenExpressionContext ctx) {
        return super.visit(ctx.expression());
    }
}