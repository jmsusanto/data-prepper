package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.dataprepper.model.event.Event;

import java.util.List;
import java.util.function.Predicate;

public class Rule {
    private final String title;
    private final String id;
    private final Predicate<Event> condition;
    private final List<String> tags;
    private final String query;
    private String monitorId;
    private String monitorName;
    private String findingsIndex;

    public Rule(final String title, final String id, final Predicate<Event> condition, final List<String> tags) {
        this.title = title;
        this.id = id;
        this.condition = condition;
        this.tags = tags;
        this.query = "PLACEHOLDER_QUERY";
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public Predicate<Event> getCondition() {
        return condition;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getQuery() {
        return query;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public String getFindingsIndex() {
        return findingsIndex;
    }

    public void setMonitorId(final String monitorId) {
        this.monitorId = monitorId;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public void setFindingsIndex(final String findingsIndex) {
        this.findingsIndex = findingsIndex;
    }
}
