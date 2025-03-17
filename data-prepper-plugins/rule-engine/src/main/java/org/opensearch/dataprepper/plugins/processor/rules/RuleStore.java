package org.opensearch.dataprepper.plugins.processor.rules;

import java.util.ArrayList;
import java.util.List;

// TODO - does this need locking?
public class RuleStore {
    private List<StatelessRule> statelessRules;

    public RuleStore() {
        this.statelessRules = new ArrayList<>();
    }

    public void updateRuleStore(final List<StatelessRule> updatedStatelessRules) {
        statelessRules = updatedStatelessRules;
    }

    public List<StatelessRule> getRules() {
        return statelessRules;
    }
}
