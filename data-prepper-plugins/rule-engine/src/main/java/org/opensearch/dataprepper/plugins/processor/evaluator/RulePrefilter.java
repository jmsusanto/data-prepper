package org.opensearch.dataprepper.plugins.processor.evaluator;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;

import java.util.List;

public interface RulePrefilter {
    void compilePrefilter(List<StatelessRule> rules);
    List<StatelessRule> filterRules(DataType log);
}
