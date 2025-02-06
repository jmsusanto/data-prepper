package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Session {
    @JsonProperty("created_time")
    private Long createdTime;
    private Boolean mfa;
    private String issuer;
}
