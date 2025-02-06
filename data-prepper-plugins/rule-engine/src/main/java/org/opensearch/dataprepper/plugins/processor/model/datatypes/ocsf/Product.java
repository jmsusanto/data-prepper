package org.opensearch.dataprepper.plugins.processor.model.datatypes.ocsf;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Product {
    private String version;
    private String name;
    @JsonProperty("vendor_name")
    private String vendorName;
    private Feature feature;
}
