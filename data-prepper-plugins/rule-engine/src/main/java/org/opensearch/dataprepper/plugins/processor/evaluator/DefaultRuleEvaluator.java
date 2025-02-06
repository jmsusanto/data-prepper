package org.opensearch.dataprepper.plugins.processor.evaluator;

import lombok.extern.log4j.Log4j2;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;
import org.opensearch.dataprepper.plugins.processor.rules.RuleStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
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

        final List<StatelessRule> statelessRules = ruleStore.getRules();
        final List<Match> matches = new ArrayList<>();

        data.forEach(item -> {
            final List<Rule> statelessRuleMatches = statelessRules.stream()
                    // Skip rules that don't apply
                    .filter(rule -> rule.testEvaluationCondition(item))
                    .filter(rule -> rule.testRuleCondition(item))
                    .collect(Collectors.toList());

            if (statelessRuleMatches.size() > 0) {
                matches.add(Match.builder()
                        .dataType(item)
                        .rules(statelessRuleMatches)
                        .build());
            }
        });
        log.info("Found {} matches from {} docs", matches.size(), data.size());

        return matches;
    }
}
