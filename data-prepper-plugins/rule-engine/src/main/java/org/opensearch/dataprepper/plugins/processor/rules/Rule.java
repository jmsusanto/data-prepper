package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;

import java.util.function.Predicate;

public abstract class Rule {
    private final Predicate<DataType> ruleCondition;
        private final Predicate<DataType> evaluationCondition;

    public Rule(final Predicate<DataType> ruleCondition, final Predicate<DataType> evaluationCondition) {
        this.ruleCondition = ruleCondition;
        this.evaluationCondition = evaluationCondition;
    }

    // Helper for always evaluate rules
    public Rule(final Predicate<DataType> ruleCondition) {
        this.ruleCondition = ruleCondition;
        this.evaluationCondition = i -> true;
    }

    public Predicate<DataType> getRuleCondition() {
        return ruleCondition;
    }

    public Predicate<DataType> getEvaluationCondition() {
        return evaluationCondition;
    }
}
