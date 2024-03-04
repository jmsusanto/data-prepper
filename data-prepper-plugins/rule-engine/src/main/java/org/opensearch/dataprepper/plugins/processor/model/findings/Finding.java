package org.opensearch.dataprepper.plugins.processor.model.findings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Finding {
    private String id;
    @JsonProperty("related_doc_ids")
    private List<String> relatedDocIds;
    @JsonProperty("correlated_doc_ids")
    private List<String> correlatedDocIds;
    @JsonProperty("monitor_id")
    private String monitorId;
    @JsonProperty("monitor_name")
    private String monitorName;
    private String index;
    private List<DocLevelQuery> queries;
    private long timestamp;

    public Finding(final String id, final List<String> relatedDocIds, final List<String> correlatedDocIds, final String monitorId,
                   final String monitorName, final String index, final List<DocLevelQuery> queries, final long timestamp) {
        this.id = id;
        this.relatedDocIds = relatedDocIds;
        this.correlatedDocIds = correlatedDocIds;
        this.monitorId = monitorId;
        this.monitorName = monitorName;
        this.index = index;
        this.queries = queries;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public List<String> getRelatedDocIds() {
        return relatedDocIds;
    }

    public List<String> getCorrelatedDocIds() {
        return correlatedDocIds;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public String getIndex() {
        return index;
    }

    public List<DocLevelQuery> getQueries() {
        return queries;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
