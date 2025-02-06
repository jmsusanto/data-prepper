package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import lombok.Data;

@Data
public class Api {
    private Response response;
    private String operation;
    private String version;
    private Service service;
    private Request request;
}
