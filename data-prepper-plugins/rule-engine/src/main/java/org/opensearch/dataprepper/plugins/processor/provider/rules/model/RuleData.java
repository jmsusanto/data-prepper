package org.opensearch.dataprepper.plugins.processor.provider.rules.model;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.util.Predicates;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class RuleData {
    private final String ruleAsString;
    private final Predicate<DataType> evaluationCondition;
    private final Map<String, String> metadata;

    public RuleData(final String ruleAsString, final Predicate<DataType> evaluationCondition, final Map<String, String> metadata) {
        this.ruleAsString = ruleAsString;
        this.evaluationCondition = evaluationCondition;
        this.metadata = metadata;
    }

    public RuleData(final String ruleAsString, final Predicate<DataType> evaluationCondition) {
        this(ruleAsString, evaluationCondition, new HashMap<>());
    }

    // Helper method for always evaluate rules
    public RuleData(final String ruleAsString) {
        this(ruleAsString, Predicates.ALWAYS_TRUE.getValue(), new HashMap<>());
    }

    public String getRuleAsString() {
        return ruleAsString;
    }

    public Predicate<DataType> getEvaluationCondition() {
        return evaluationCondition;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }
}
