package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class User {
    private String type;
    private String name;
    private String uid;
    private String uuid;
    @JsonProperty("account_uid")
    private String accountUid;
    @JsonProperty("credential_uid")
    private String credentialUid;
}
