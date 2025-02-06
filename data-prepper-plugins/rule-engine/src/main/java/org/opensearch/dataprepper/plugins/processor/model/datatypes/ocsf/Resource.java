package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Resource {
    private String uid;
    @JsonProperty("account_uid")
    private String accountUid;
    private String type;
}
