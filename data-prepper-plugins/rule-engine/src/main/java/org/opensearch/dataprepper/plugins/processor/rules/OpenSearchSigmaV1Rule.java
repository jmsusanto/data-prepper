package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;

import java.util.List;
import java.util.function.Predicate;

public class OpenSearchSigmaV1Rule extends SigmaV1Rule {
    private final String monitorId;
    private final String detectorName;
    private final String findingsIndex;

    public OpenSearchSigmaV1Rule(final String monitorId, final String detectorName, final String findingsIndex,
                                 final String title, final String id, final List<String> tags,
                                 final Predicate<DataType> ruleCondition, final Predicate<DataType> evaluationCondition) {
        super(title, id, tags, ruleCondition, evaluationCondition);
        this.monitorId = monitorId;
        this.detectorName = detectorName;
        this.findingsIndex = findingsIndex;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public String getFindingsIndex() {
        return findingsIndex;
    }
}
