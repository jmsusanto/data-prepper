package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.dataprepper.plugins.processor.parser.RuleParser;
import org.opensearch.dataprepper.plugins.processor.provider.rules.RuleProvider;
import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RuleRefresher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RuleRefresher.class);

    private final RuleProvider ruleProvider;
    private final RuleParser ruleParser;
    private final RuleStore sigmaRuleStore;

    public RuleRefresher(final RuleProvider ruleProvider,
                         final RuleParser ruleParser,
                         final RuleStore sigmaRuleStore) {
        this.ruleProvider = ruleProvider;
        this.ruleParser = ruleParser;
        this.sigmaRuleStore = sigmaRuleStore;
    }

    @Override
    public void run() {
        try {
            final List<RuleData> ruleData = ruleProvider.getRules();
            final List<StatelessRule> statelessRules = new ArrayList<>();
            final List<StatefulRule> statefulRules = new ArrayList<>();

            ruleData.forEach(ruleDatum -> ruleParser.parseRule(ruleDatum, statelessRules::add, statefulRules::add));
            sigmaRuleStore.updateRuleStore(statelessRules, statefulRules);
        } catch (final Exception e) {
            LOG.error("Caught exception refreshing rules", e);
        }
    }
}
