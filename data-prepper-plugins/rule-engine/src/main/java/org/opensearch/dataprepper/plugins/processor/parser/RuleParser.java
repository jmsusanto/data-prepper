package org.opensearch.dataprepper.plugins.processor.parser;

import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;

public interface RuleParser {
    Rule parseRule(RuleData ruleData);
}
