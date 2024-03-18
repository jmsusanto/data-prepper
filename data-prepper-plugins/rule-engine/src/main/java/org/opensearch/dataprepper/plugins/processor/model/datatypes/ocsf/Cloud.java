package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import lombok.Data;

@Data
public class Cloud {
    private String region;
    private String provider;
}
