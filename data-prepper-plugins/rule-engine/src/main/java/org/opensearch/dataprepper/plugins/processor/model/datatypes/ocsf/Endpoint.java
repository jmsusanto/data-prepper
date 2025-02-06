package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import lombok.Data;

@Data
public class Endpoint {
    private String uid;
    private String ip;
    private String domain;
}
