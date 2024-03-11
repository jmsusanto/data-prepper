package org.opensearch.dataprepper.plugins.processor.converters;

import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.OCSF;

public class OCSFConverter {
    public OCSF convert(final Record<Event> record) {
        final Event event = record.getData();
        final String apiOperation = event.get("/api/operation", String.class);
        final String apiServiceName = event.get("/api/service/name", String.class);

        return new OCSF(apiOperation, apiServiceName);
    }
}
