package org.opensearch.dataprepper.plugins.processor.evaluator;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
import org.opensearch.dataprepper.plugins.processor.rules.RuleStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultRuleEvaluator implements RuleEvaluator {
    private final RuleStore ruleStore;

    public DefaultRuleEvaluator(final RuleStore ruleStore) {
        this.ruleStore = ruleStore;
    }

    @Override
    public Collection<Match> evaluate(final Collection<DataType> data) {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Rule> rules = ruleStore.getRules();
        final List<Match> matches = new ArrayList<>();

        data.stream().forEach(item -> {
            final List<Rule> ruleMatches = rules.stream()
                    // Skip rules that don't apply
                    .filter(rule -> rule.getEvaluationCondition().test(item))
                    .filter(rule -> rule.getRuleCondition().test(item))
                    .collect(Collectors.toList());

            if (ruleMatches.size() > 0) {
                matches.add(new Match(item, ruleMatches));
            }
        });

        return matches;
    }
}
