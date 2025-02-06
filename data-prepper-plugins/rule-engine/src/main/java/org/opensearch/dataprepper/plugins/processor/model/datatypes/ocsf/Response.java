package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import lombok.Data;

@Data
public class Response {
    private String error;
    private String message;
}
