package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Actor {
    private User user;
    private Session session;
    @JsonProperty("invoked_by")
    private String invokedBy;
    private Idp idp;
}
