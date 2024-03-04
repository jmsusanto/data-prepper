package org.opensearch.dataprepper.plugins.processor.model.event;

import org.opensearch.dataprepper.model.event.Event;

public class EventWrapper {
    private final String indexName;
    private final String docId;
    private final Event event;

    public EventWrapper(final String indexName, final String docId, final Event event) {
        this.indexName = indexName;
        this.docId = docId;
        this.event = event;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getDocId() {
        return docId;
    }

    public Event getEvent() {
        return event;
    }
}
