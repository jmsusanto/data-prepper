package org.opensearch.dataprepper.plugins.processor.rules;

import java.util.ArrayList;
import java.util.List;

// TODO - does this need locking?
public class RuleStore {
    private List<Rule> rules;

    public RuleStore() {
        this.rules = new ArrayList<>();
    }

    public void updateRuleStore(final List<Rule> updatedRules) {
        rules = updatedRules;
    }

    public List<Rule> getRules() {
        return rules;
    }
}
