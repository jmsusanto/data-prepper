package org.opensearch.dataprepper.plugins.processor.parser;

import org.opensearch.dataprepper.plugins.processor.formats.accessors.FieldAccessor;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.parser.aggregation.AggregationItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.DoubleStream;

public class SigmaV1RuleStatefulConverter {
    private static final Logger LOG = LoggerFactory.getLogger(SigmaV1RuleStatefulConverter.class);

    private final FieldAccessor fieldAccessor;

    public SigmaV1RuleStatefulConverter(final FieldAccessor fieldAccessor) {
        this.fieldAccessor = fieldAccessor;
    }

    public Predicate<List<Match>> parseRuleAggregation(final AggregationItem aggregationItem) {
        switch (aggregationItem.getAggFunction()) {
            case "count": return matches -> applyOperator((double) matches.size(), aggregationItem);
            case "min": return matches -> applyOperator(getOptionalValue(matches, aggregationItem.getAggField(), DoubleStream::min), aggregationItem);
            case "max": return matches -> applyOperator(getOptionalValue(matches, aggregationItem.getAggField(), DoubleStream::max), aggregationItem);
            case "avg": return matches -> applyOperator(getOptionalValue(matches, aggregationItem.getAggField(), DoubleStream::average), aggregationItem);
            case "sum": return matches -> applyOperator(getValue(matches, aggregationItem.getAggField(), DoubleStream::sum), aggregationItem);
            default: throw new UnsupportedOperationException("Unexpected aggregation function \"" + aggregationItem.getAggFunction() + "\"");
        }

    }

    private boolean applyOperator(final Double value, final AggregationItem aggregationItem) {
        switch (aggregationItem.getCompOperator()) {
            case ">": return value > aggregationItem.getThreshold();
            case "<": return value < aggregationItem.getThreshold();
            case ">=": return value >= aggregationItem.getThreshold();
            case "<=": return value <= aggregationItem.getThreshold();
            case "==": return aggregationItem.getThreshold().equals(value);
            default: throw new UnsupportedOperationException("Unexpected operator \"" + aggregationItem.getCompOperator() + "\"");
        }
    }

    private double getOptionalValue(final List<Match> matches, final String fieldName, final Function<DoubleStream, OptionalDouble> comparator) {
        final DoubleStream doubleStream = getDoubleStream(matches, fieldName);
        final OptionalDouble optionalDouble = comparator.apply(doubleStream);
        if (optionalDouble.isEmpty()) {
            throw new IllegalArgumentException("No double value found for field name \"" + fieldName + "\"");
        }

        return optionalDouble.getAsDouble();
    }

    private double getValue(final List<Match> matches, final String fieldName, final Function<DoubleStream, Double> comparator) {
        final DoubleStream doubleStream = getDoubleStream(matches, fieldName);
        return comparator.apply(doubleStream);
    }

    private DoubleStream getDoubleStream(final List<Match> matches, final String fieldName) {
         return matches.stream()
                .map(match -> fieldAccessor.getDoubleValue(match.getDataType(), fieldName))
                .mapToDouble(Double::doubleValue);
    }
}
