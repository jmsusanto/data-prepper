package org.opensearch.dataprepper.plugins.processor.provider.rules.model;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.util.Predicates;

import java.util.function.Predicate;

public class RuleData {
    private final String ruleAsString;
    private final Predicate<DataType> evaluationCondition;

    public RuleData(final String ruleAsString, final Predicate<DataType> evaluationCondition) {
        this.ruleAsString = ruleAsString;
        this.evaluationCondition = evaluationCondition;
    }

    // Helper method for always evaluate rules
    public RuleData(final String ruleAsString) {
        this.ruleAsString = ruleAsString;
        evaluationCondition = Predicates.ALWAYS_TRUE.getValue();
    }

    public String getRuleAsString() {
        return ruleAsString;
    }

    public Predicate<DataType> getEvaluationCondition() {
        return evaluationCondition;
    }
}
