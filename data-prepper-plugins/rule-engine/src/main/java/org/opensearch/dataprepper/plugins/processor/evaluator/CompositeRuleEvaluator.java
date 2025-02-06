package org.opensearch.dataprepper.plugins.processor.evaluator;

import lombok.Builder;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;

import java.util.Collection;

@Builder
public class CompositeRuleEvaluator implements RuleEvaluator {
    private final RuleEvaluator ruleEvaluator;
    private final CorrelationEvaluator correlationEvaluator;

    @Override
    public Collection<Match> evaluate(final Collection<DataType> data) {
        final Collection<Match> matches = ruleEvaluator.evaluate(data);
        return correlationEvaluator.evaluate(matches);
    }
}
