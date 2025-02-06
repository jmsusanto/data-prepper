package org.opensearch.dataprepper.plugins.processor.rules;

import lombok.experimental.SuperBuilder;

import java.util.function.Predicate;

@SuperBuilder
public abstract class Rule<T, U> {
    private final String id;
    private final Predicate<T> evaluationCondition;
    private final Predicate<U> ruleCondition;

    public boolean testEvaluationCondition(final T input) {
        return evaluationCondition.test(input);
    }

    public boolean testRuleCondition(final U input) {
        return ruleCondition.test(input);
    }

    public String getId() {
        return id;
    }
}
