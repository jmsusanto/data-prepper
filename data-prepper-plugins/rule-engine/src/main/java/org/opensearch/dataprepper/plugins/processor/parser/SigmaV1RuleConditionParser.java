package org.opensearch.dataprepper.plugins.processor.parser;

import org.opensearch.dataprepper.plugins.processor.formats.accessors.FieldAccessor;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionAND;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionFieldEqualsValueExpression;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionItem;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionNOT;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionOR;
import org.opensearch.dataprepper.plugins.processor.parser.condition.ConditionValueExpression;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaCondition;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaBool;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaFloat;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaInteger;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaNull;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaString;
import org.opensearch.dataprepper.plugins.processor.parser.types.SigmaType;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SigmaV1RuleConditionParser {
    private static final Logger LOG = LoggerFactory.getLogger(SigmaV1RuleConditionParser.class);

    private final FieldAccessor fieldAccessor;

    public SigmaV1RuleConditionParser(final Map<String, String> mapping) {
        this.fieldAccessor = new FieldAccessor(mapping);
    }

    public Predicate<DataType> parseRuleCondition(final SigmaRule sigmaRule) {
        return sigmaRule.getDetection().getParsedConditions().stream()
                .map(SigmaCondition::parsed)
                .map(this::parsePredicateFromConditionItem)
                // TODO - Not sure on this, need to figure out how there could be multiple conditions for same rule
                .reduce(Predicate::and)
                // Default to no match if there were no predicates
                .orElse(x -> false);
    }

    private Predicate<DataType> parsePredicateFromConditionItem(final ConditionItem conditionItem) {
        if (conditionItem instanceof ConditionAND) {
            return convertAndCondition(conditionItem);
        } else if (conditionItem instanceof ConditionOR) {
            return convertOrCondition(conditionItem);
        } else if (conditionItem instanceof ConditionNOT) {
            return convertNotCondition(conditionItem);
        } else if (conditionItem instanceof ConditionFieldEqualsValueExpression) {
            return convertFieldEquals((ConditionFieldEqualsValueExpression) conditionItem);
        } else if (conditionItem instanceof ConditionValueExpression) {
            // TODO - not sure what this means yet
            throw new UnsupportedOperationException("Can't proceed from ConditionValueExpression yet");
        } else {
            throw new IllegalArgumentException("Unexpected condition type class in condition parse tree: " + conditionItem.getClass().getName());
        }
    }

    private Predicate<DataType> convertAndCondition(final ConditionItem condition) {
        return getPredicatesFromConditions(condition)
                .reduce(Predicate::and)
                // TODO - not sure on this, need to figure out why right would be a string to land here with an empty optional
                .orElse(x -> true);
    }

    private Predicate<DataType> convertOrCondition(final ConditionItem condition) {
        return getPredicatesFromConditions(condition)
                .reduce(Predicate::or)
                // TODO - not sure on this, need to figure out why right would be a string to land here with an empty optional
                .orElse(x -> true);
    }

    private Predicate<DataType> convertNotCondition(final ConditionItem condition) {
        return getPredicatesFromConditions(condition)
                .map(Predicate::negate)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Expected exactly on condition for NOT operator"));
    }

    private Stream<Predicate<DataType>> getPredicatesFromConditions(final ConditionItem condition) {
        return condition.getArgs().stream()
                // Filter on is another condition
                .filter(Either::isLeft)
                .map(Either::getLeft)
                .map(this::parsePredicateFromConditionItem);
    }

    private Predicate<DataType> convertFieldEquals(final ConditionFieldEqualsValueExpression condition) {
        final SigmaType conditionValue = condition.getValue();

        if (conditionValue instanceof SigmaString) {
            return convertStringEquals(condition);
        } else if (conditionValue instanceof SigmaBool) {
            return convertBooleanEquals(condition);
        } else if (conditionValue instanceof SigmaInteger) {
            return convertIntegerEquals(condition);
        } else if (conditionValue instanceof SigmaFloat) {
            return convertFloatEquals(condition);
        } else if (conditionValue instanceof SigmaNull) {
            return convertNullEquals(condition);
        } else {
            throw new IllegalArgumentException("Unexpected value type class in condition parse tree: " + conditionValue.getClass().getName());
        }
    }

    private Predicate<DataType> convertStringEquals(final ConditionFieldEqualsValueExpression condition) {
        final SigmaString sigmaString = (SigmaString) condition.getValue();
        return event -> {
            final String value = fieldAccessor.getStringValue(event, condition.getField());

            return sigmaString.getOriginal().equals(value);
        };
    }

    private Predicate<DataType> convertBooleanEquals(final ConditionFieldEqualsValueExpression condition) {
        final SigmaBool sigmaBool = (SigmaBool) condition.getValue();
        return event -> sigmaBool.getBoolean().equals(fieldAccessor.getBooleanValue(event, condition.getField()));
    }

    private Predicate<DataType> convertIntegerEquals(final ConditionFieldEqualsValueExpression condition) {
        final SigmaInteger sigmaInteger = (SigmaInteger) condition.getValue();
        return event -> sigmaInteger.getInteger().equals(fieldAccessor.getIntegerValue(event, condition.getField()));
    }

    private Predicate<DataType> convertFloatEquals(final ConditionFieldEqualsValueExpression condition) {
        final SigmaFloat sigmaFloat = (SigmaFloat) condition.getValue();
        return event -> sigmaFloat.getFloat().equals(fieldAccessor.getFloatValue(event, condition.getField()));
    }

    private Predicate<DataType> convertNullEquals(final ConditionFieldEqualsValueExpression condition) {
        return event -> fieldAccessor.getObjectValue(event, condition.getField()) == null;
    }
}
