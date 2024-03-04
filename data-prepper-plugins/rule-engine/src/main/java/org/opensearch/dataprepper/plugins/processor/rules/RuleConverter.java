package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.plugins.processor.RuleEngineConfig;
import org.opensearch.dataprepper.plugins.processor.parser.SigmaRulePredicateParser;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRuleTag;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RuleConverter implements Function<SigmaRule, Rule> {
    private final SigmaRulePredicateParser sigmaRulePredicateParser;

    public RuleConverter(final RuleEngineConfig config) {
        this.sigmaRulePredicateParser = new SigmaRulePredicateParser(config);
    }

    @Override
    public Rule apply(final SigmaRule sigmaRule) {
        final Predicate<Event> predicate = sigmaRulePredicateParser.parseRule(sigmaRule);

        final List<String> tags = new ArrayList<>();
        tags.add(sigmaRule.getLevel().toString());
        tags.add(sigmaRule.getLogSource().getService());
        sigmaRule.getTags().stream()
                .map(SigmaRuleTag::toString)
                .forEach(tags::add);

        return new Rule(sigmaRule.getTitle(), sigmaRule.getId().toString(), predicate, tags);
    }
}
