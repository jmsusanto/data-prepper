package org.opensearch.dataprepper.plugins.processor.rules;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;

@SuperBuilder
@Getter
public class StatelessRule extends Rule<DataType, DataType> {
    private final boolean isStatefulCondition;
}
