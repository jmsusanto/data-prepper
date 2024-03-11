package org.opensearch.dataprepper.plugins.processor.parser;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRuleTag;
import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
import org.opensearch.dataprepper.plugins.processor.rules.SigmaV1Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SigmaV1RuleParser implements RuleParser {
    private final SigmaV1RuleConditionParser conditionParser;

    public SigmaV1RuleParser(final Map<String, String> mapping) {
        this.conditionParser = new SigmaV1RuleConditionParser(mapping);
    }

    @Override
    public Rule parseRule(final RuleData ruleData) {
        final SigmaRule sigmaRule = SigmaRule.fromYaml(ruleData.getRuleAsString(), true);
        final Predicate<DataType> ruleCondition = conditionParser.parseRuleCondition(sigmaRule);
        final List<String> tags = new ArrayList<>();
        tags.add(sigmaRule.getLevel().toString());
        tags.add(sigmaRule.getLogSource().getService());
        sigmaRule.getTags().stream()
                .map(SigmaRuleTag::toString)
                .forEach(tags::add);

        return new SigmaV1Rule(sigmaRule.getTitle(), sigmaRule.getId().toString(), tags, ruleCondition, ruleData.getEvaluationCondition());
    }
}
