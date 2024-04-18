package org.opensearch.dataprepper.plugins.processor.evaluator;

import org.opensearch.dataprepper.plugins.processor.model.matches.Match;

import java.util.Collection;

public interface CorrelationEvaluator {
    Collection<Match> evaluate(Collection<Match> matches);
}
