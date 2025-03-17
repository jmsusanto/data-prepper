package org.opensearch.dataprepper.plugins.processor.parser;

import lombok.extern.log4j.Log4j2;
import org.opensearch.dataprepper.plugins.processor.formats.accessors.FieldAccessor;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.parser.aggregation.AggregationItem;
import org.opensearch.dataprepper.plugins.processor.parser.condition.*;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaCondition;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRuleTag;
import org.opensearch.dataprepper.plugins.processor.parser.types.*;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;
import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchRuleMetadata;
import org.opensearch.dataprepper.plugins.processor.rules.OpenSearchSigmaV1StatelessRule;
import org.opensearch.dataprepper.plugins.processor.rules.SigmaV1RuleMetadata;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
public class OpenSearchSigmaV1RuleParser implements RuleParser {
    private static final String TIMEFRAME_REGEX = "^(\\d+)(s|m|h|d)$";
    private static final Pattern TIMEFRAME_PATTERN = Pattern.compile(TIMEFRAME_REGEX);

    private final FieldAccessor fieldAccessor;
    private final SigmaV1RuleStatelessConverter conditionParser;

    public OpenSearchSigmaV1RuleParser(final Map<String, String> mapping) {
        this.fieldAccessor = new FieldAccessor(mapping);
        this.conditionParser = new SigmaV1RuleStatelessConverter(fieldAccessor);
    }

    @Override
    public void parseRule(final RuleData ruleData, final Consumer<StatelessRule> ruleConsumer) {
        final SigmaRule sigmaRule = SigmaRule.fromYaml(ruleData.getRuleAsString(), true);
        final List<ConditionItem> conditionItems = getConditionItems(sigmaRule);
        final List<String> keywords = extractKeywords(conditionItems);
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
                .keywords(keywords)
                .evaluationCondition(ruleData.getEvaluationCondition());


        ruleConsumer.accept(sigmaV1RuleBuilder.isStatefulCondition(false).build());
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
                .filter(item -> item.getAggFunction() != null)
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

    private Function<Long, Duration> getDurationCreatorFromUnit(final String durationUnit) {
        switch (durationUnit) {
            case "s": return Duration::ofSeconds;
            case "m": return Duration::ofMinutes;
            case "h": return Duration::ofHours;
            case "d": return Duration::ofDays;
            default: throw new UnsupportedOperationException("Unsupported timeframe unit \"" + durationUnit + "\"");
        }
    }

    private List<String> extractKeywords(List<ConditionItem> conditionItems) {
        List<String> keywords = new ArrayList<>();
        for (ConditionItem item : conditionItems) {
            extractKeywordsFromConditionItem(item, keywords);
        }
        return keywords;
    }

    private void extractKeywordsFromConditionItem(ConditionItem conditionItem, List<String> keywords) {
        if (conditionItem instanceof ConditionFieldEqualsValueExpression) {
            ConditionFieldEqualsValueExpression fieldEquals = (ConditionFieldEqualsValueExpression) conditionItem;
            SigmaType value = fieldEquals.getValue();

            if (value != null) {
                String keyword = null;
                if (value instanceof SigmaString) {
                    keyword = ((SigmaString) value).getOriginal();
                } else if (value instanceof SigmaInteger) {
                    keyword = ((SigmaInteger) value).getInteger().toString();
                } else if (value instanceof SigmaFloat) {
                    keyword = ((SigmaFloat) value).getFloat().toString();
                } else if (value instanceof SigmaBool) {
                    keyword = ((SigmaBool) value).getBoolean().toString();
                }

                if (keyword != null) {
                    keywords.add(keyword);
                    log.debug("Added keyword: {} from field: {}", keyword, fieldEquals.getField());
                }
            }
        } else if (conditionItem instanceof ConditionAND ||
                conditionItem instanceof ConditionOR ||
                conditionItem instanceof ConditionNOT) {
            for (Either<ConditionItem, String> arg : conditionItem.getArgs()) {
                if (arg.isLeft()) {
                    extractKeywordsFromConditionItem(arg.getLeft(), keywords);
                }
            }
        }
    }
}
