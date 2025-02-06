package org.opensearch.dataprepper.plugins.processor.parser;

import lombok.extern.log4j.Log4j2;
import org.opensearch.dataprepper.plugins.processor.formats.accessors.FieldAccessor;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.parser.aggregation.AggregationItem;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionItem;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaCondition;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRuleTag;
import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchRuleMetadata;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchSigmaV1StatefulRule;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchSigmaV1StatelessRule;
import org.opensearch.dataprepper.plugins.processor.rules.SigmaV1RuleMetadata;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;
import org.opensearch.dataprepper.plugins.processor.rules.StatefulRule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
public class OpenSearchSigmaV1RuleParser implements RuleParser {
    private static final String TIMEFRAME_REGEX = "^(\\d+)(s|m|h|d)$";
    private static final Pattern TIMEFRAME_PATTERN = Pattern.compile(TIMEFRAME_REGEX);

    private final FieldAccessor fieldAccessor;
    private final SigmaV1RuleStatelessConverter conditionParser;
    private final SigmaV1RuleStatefulConverter aggregationParser;

    public OpenSearchSigmaV1RuleParser(final Map<String, String> mapping) {
        this.fieldAccessor = new FieldAccessor(mapping);
        this.conditionParser = new SigmaV1RuleStatelessConverter(fieldAccessor);
        this.aggregationParser = new SigmaV1RuleStatefulConverter(fieldAccessor);
    }

    @Override
    public void parseRule(final RuleData ruleData, final Consumer<StatelessRule> ruleConsumer, final Consumer<StatefulRule> statefulRuleConsumer) {
        final SigmaRule sigmaRule = SigmaRule.fromYaml(ruleData.getRuleAsString(), true);
        final List<ConditionItem> conditionItems = getConditionItems(sigmaRule);
        final AggregationItem aggregationItem = getAggregationItem(sigmaRule);

        final Predicate<DataType> ruleCondition = conditionParser.parseRuleCondition(conditionItems);
        final List<String> tags = getTags(sigmaRule);

        final OpenSearchRuleMetadata openSearchRuleMetadata = OpenSearchRuleMetadata.builder()
                .findingsIndex(ruleData.getMetadata().get("findingsIndex"))
                .detectorName(ruleData.getMetadata().get("detectorName"))
                .monitorId(ruleData.getMetadata().get("monitorId"))
                .build();
        final SigmaV1RuleMetadata sigmaV1RuleMetadata = SigmaV1RuleMetadata.builder()
                .title(sigmaRule.getTitle())
                .tags(tags)
                .build();

        final OpenSearchSigmaV1StatelessRule.OpenSearchSigmaV1StatelessRuleBuilder sigmaV1RuleBuilder = OpenSearchSigmaV1StatelessRule.builder()
                .openSearchRuleMetadata(openSearchRuleMetadata)
                .sigmaV1RuleMetadata(sigmaV1RuleMetadata)
                .id(sigmaRule.getId().toString())
                .ruleCondition(ruleCondition)
                .evaluationCondition(ruleData.getEvaluationCondition());

        if (aggregationItem == null) {
            ruleConsumer.accept(sigmaV1RuleBuilder.isStatefulCondition(false).build());
        } else {
            ruleConsumer.accept(sigmaV1RuleBuilder.isStatefulCondition(true).build());

            final List<String> filterFields = getFilterFields(aggregationItem);
            final Duration timeframe = parseTimeframe(sigmaRule.getDetection().getTimeframe());
            final Predicate<Match> evaluationCondition = getAggregationEvaluationCondition(sigmaRule.getId().toString());

            final OpenSearchSigmaV1StatefulRule statefulRule = OpenSearchSigmaV1StatefulRule.builder()
                    .openSearchRuleMetadata(openSearchRuleMetadata)
                    .sigmaV1RuleMetadata(sigmaV1RuleMetadata)
                    .filterFields(filterFields)
                    .timeframe(timeframe)
                    .evaluationCondition(evaluationCondition)
                    .ruleCondition(aggregationParser.parseRuleAggregation(aggregationItem))
                    .id(sigmaRule.getId().toString())
                    .build();

            statefulRuleConsumer.accept(statefulRule);
        }
    }

    private List<ConditionItem> getConditionItems(final SigmaRule sigmaRule) {
        return sigmaRule.getDetection().getParsedConditions().stream()
                .map(SigmaCondition::parseConditionItem)
                .collect(Collectors.toList());
    }

    private AggregationItem getAggregationItem(final SigmaRule sigmaRule) {
        final List<AggregationItem> aggregationItems = sigmaRule.getDetection().getParsedConditions().stream()
                .map(SigmaCondition::parseAggregationItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (aggregationItems.size() > 1) {
            throw new UnsupportedOperationException("Expected 0 or 1 aggregations. Found " + aggregationItems.size() + " for rule with id " + sigmaRule.getId());
        }

        return aggregationItems.isEmpty() ? null : aggregationItems.get(0);
    }

    private List<String> getTags(final SigmaRule sigmaRule) {
        final List<String> tags = new ArrayList<>();
        tags.add(sigmaRule.getLevel().toString());
        tags.add(sigmaRule.getLogSource().getService());
        sigmaRule.getTags().stream()
                .map(SigmaRuleTag::toString)
                .forEach(tags::add);

        return tags;
    }

    private List<String> getFilterFields(final AggregationItem aggregationItem) {
        final List<String> filterFields = new ArrayList<>();
        filterFields.add(fieldAccessor.convertFieldName(aggregationItem.getGroupByField()));
        if ("count".equals(aggregationItem.getAggFunction()) && aggregationItem.getAggField() != null) {
            filterFields.add(fieldAccessor.convertFieldName(aggregationItem.getAggField()));
        }

        return filterFields;
    }

    private Predicate<Match> getAggregationEvaluationCondition(final String id) {
        return match -> {
            final List<String> ruleIds = match.getStatelessRuleMatches().stream()
                    .filter(StatelessRule::isStatefulCondition)
                    .map(StatelessRule::getId)
                    .collect(Collectors.toList());

            return ruleIds.contains(id);
        };
    }

    private Duration parseTimeframe(final String timeframeString) {
        final String durationStringNoSpaces = timeframeString.replaceAll("\\s", "");
        final Matcher matcher = TIMEFRAME_PATTERN.matcher(durationStringNoSpaces);
        if (!matcher.find()) {
            return null;
        }

        final long durationNumber = Long.parseLong(matcher.group(1));
        final String durationUnit = matcher.group(2);

        return getDurationCreatorFromUnit(durationUnit).apply(durationNumber);
    }

    private Function<Long, Duration> getDurationCreatorFromUnit(final String durationUnit) {
        switch (durationUnit) {
            case "s": return Duration::ofSeconds;
            case "m": return Duration::ofMinutes;
            case "h": return Duration::ofHours;
            case "d": return Duration::ofDays;
            default: throw new UnsupportedOperationException("Unsupported timeframe unit \"" + durationUnit + "\"");
        }
    }
}
