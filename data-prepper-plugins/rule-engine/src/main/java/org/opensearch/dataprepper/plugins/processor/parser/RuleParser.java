package org.opensearch.dataprepper.plugins.processor.parser;

import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;
import org.opensearch.dataprepper.plugins.processor.rules.StatefulRule;

import java.util.function.Consumer;

public interface RuleParser {
    void parseRule(RuleData ruleData, Consumer<StatelessRule> ruleConsumer, Consumer<StatefulRule> statefulRuleConsumer);
}
