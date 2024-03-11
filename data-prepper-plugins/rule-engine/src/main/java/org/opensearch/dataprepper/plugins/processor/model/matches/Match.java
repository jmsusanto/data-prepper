package org.opensearch.dataprepper.plugins.processor.model.matches;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;

import java.util.List;

public class Match {
    private final DataType dataType;
    private final List<Rule> ruleMatches;

    public Match(final DataType dataType, final List<Rule> ruleMatches) {
        this.dataType = dataType;
        this.ruleMatches = ruleMatches;
    }

    public DataType getDataType() {
        return dataType;
    }

    public List<Rule> getRuleMatches() {
        return ruleMatches;
    }
}
