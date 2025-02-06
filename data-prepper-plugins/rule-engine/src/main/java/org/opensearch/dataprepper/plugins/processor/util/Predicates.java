package org.opensearch.dataprepper.plugins.processor.util;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;

import java.util.function.Predicate;

public enum Predicates {
    ALWAYS_TRUE(x -> true);

    private final Predicate<DataType> value;

    Predicates(final Predicate<DataType> value) {
        this.value = value;
    }

    public Predicate<DataType> getValue() {
        return value;
    }
}
