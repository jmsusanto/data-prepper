package org.opensearch.dataprepper.plugins.processor.rules;

import java.util.ArrayList;
import java.util.List;

// TODO - does this need locking?
public class RuleStore {
    private List<StatelessRule> statelessRules;
    private List<StatefulRule> statefulRules;

    public RuleStore() {
        this.statelessRules = new ArrayList<>();
        this.statefulRules = new ArrayList<>();
    }

    public void updateRuleStore(final List<StatelessRule> updatedStatelessRules, final List<StatefulRule> updatedStatefulRules) {
        statelessRules = updatedStatelessRules;
        statefulRules = updatedStatefulRules;
    }

    public List<StatelessRule> getRules() {
        return statelessRules;
    }

    public List<StatefulRule> getStatefulRules() {
        return statefulRules;
    }
}
