package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import lombok.Data;

import java.util.List;

@Data
public class Metadata {
    private Product product;
    private String uid;
    private List<String> profiles;
    private String version;
}
