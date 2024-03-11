package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.dataprepper.plugins.processor.parser.RuleParser;
import org.opensearch.dataprepper.plugins.processor.provider.rules.RuleProvider;
import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

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
            final List<Rule> rules = ruleData.stream()
                    .map(ruleParser::parseRule)
                    .collect(Collectors.toList());
            sigmaRuleStore.updateRuleStore(rules);
        } catch (final Exception e) {
            LOG.error("Caught exception refreshing rules", e);
        }
    }
}
