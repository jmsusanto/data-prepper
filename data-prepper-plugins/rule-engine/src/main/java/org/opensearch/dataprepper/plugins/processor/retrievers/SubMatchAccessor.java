package org.opensearch.dataprepper.plugins.processor.retrievers;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.rules.StatefulRule;

import java.util.List;

public interface SubMatchAccessor {
    List<Match> getSubMatches(StatefulRule statefulRule, DataType dataType);
    void storeSubMatches(List<Match> subMatches);
}
