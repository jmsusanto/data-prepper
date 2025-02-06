package org.opensearch.dataprepper.plugins.processor.provider.rules;

import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;

import java.util.List;

public interface RuleProvider {
    /**
     * Gets the data of the rules to be evaluated
     *
     * @return - a list of RuleData that is used by the RuleParser to generate Rules
     */
    List<RuleData> getRules();
}
