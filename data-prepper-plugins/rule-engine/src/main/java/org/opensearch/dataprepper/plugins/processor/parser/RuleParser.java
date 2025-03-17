package org.opensearch.dataprepper.plugins.processor.parser;

import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;

import java.util.function.Consumer;

public interface RuleParser {
    void parseRule(RuleData ruleData, Consumer<StatelessRule> ruleConsumer);
}
