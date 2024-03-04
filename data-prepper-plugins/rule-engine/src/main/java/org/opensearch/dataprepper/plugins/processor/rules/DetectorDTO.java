package org.opensearch.dataprepper.plugins.processor.rules;

import java.util.Set;

public class DetectorDTO {
    private Set<String> ruleIds;
    private String monitorID;
    private String monitorName;
    private String findingsIndex;

    public DetectorDTO(final Set<String> ruleIds, final String monitorID, final String monitorName, final String findingsIndex) {
        this.ruleIds = ruleIds;
        this.monitorID = monitorID;
        this.monitorName = monitorName;
        this.findingsIndex = findingsIndex;
    }

    public Set<String> getRuleIds() {
        return ruleIds;
    }

    public String getMonitorID() {
        return monitorID;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public String getFindingsIndex() {
        return findingsIndex;
    }
}
