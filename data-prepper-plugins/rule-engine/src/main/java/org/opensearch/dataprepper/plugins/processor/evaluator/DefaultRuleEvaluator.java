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
    private final RulePrefilter rulePrefilter;

    public DefaultRuleEvaluator(final RuleStore ruleStore, final RulePrefilter rulePrefilter) {
        this.rulePrefilter = rulePrefilter;
        this.ruleStore = ruleStore;
    }

    @Override
    public Collection<Match> evaluate(final Collection<DataType> data) {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }

        final List<StatelessRule> statelessRules = ruleStore.getRules();
        final List<Match> matches = new ArrayList<>();

        log.info("Compiling prefilter for {} rules", statelessRules.size());
        rulePrefilter.compilePrefilter(statelessRules);

        data.forEach(item -> {
            final List<StatelessRule> prefilteredRules = rulePrefilter.filterRules(item);

            log.info("Prefilter reduced rules from {} to {} for item",
                    statelessRules.size(), prefilteredRules.size());

            if (!prefilteredRules.isEmpty()) {  // Add check to skip processing if no rules passed prefilter
                final List<Rule> statelessRuleMatches = prefilteredRules.stream()
                        .filter(rule -> rule.testEvaluationCondition(item))
                        .filter(rule -> rule.testRuleCondition(item))
                        .collect(Collectors.toList());

                if (!statelessRuleMatches.isEmpty()) {  // Use isEmpty() for better readability
                    matches.add(Match.builder()
                            .dataType(item)
                            .rules(statelessRuleMatches)
                            .build());
                }
            }
        });

        log.info("Found {} matches from {} docs ({}% match rate)",
                matches.size(),
                data.size(),
                String.format("%.2f", (matches.size() * 100.0) / data.size()));

        return matches;
    }
}
