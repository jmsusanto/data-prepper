package org.opensearch.dataprepper.plugins.processor.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO - does this need locking?
public class RuleStore {
    private Map<String, List<Rule>> rules;

    public RuleStore() {
        this.rules = new HashMap<>();
    }

    public void updateRuleStore(final Map<String, List<Rule>> updatedRules) {
        rules = updatedRules;
    }

    public Map<String, List<Rule>> getRules() {
        return rules;
    }
}
