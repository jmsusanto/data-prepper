package org.opensearch.dataprepper.plugins.processor.evaluator;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;

import java.util.Collection;

public interface RuleEvaluator {
    Collection<Match> evaluate(Collection<DataType> data);
}
