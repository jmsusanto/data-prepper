package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;

import java.util.List;
import java.util.function.Predicate;

public class SigmaV1Rule extends Rule {
    private final String title;
    private final String id;
    private final List<String> tags;

    public SigmaV1Rule(final String title, final String id, final List<String> tags, final Predicate<DataType> ruleCondition,
                       final Predicate<DataType> evaluationCondition) {
        super(ruleCondition, evaluationCondition);
        this.title = title;
        this.id = id;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }
}
