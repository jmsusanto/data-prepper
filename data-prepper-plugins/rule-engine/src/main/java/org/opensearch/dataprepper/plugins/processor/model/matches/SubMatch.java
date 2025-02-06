package org.opensearch.dataprepper.plugins.processor.model.matches;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class SubMatch {
    private String id;
    private Map<String, String> groupByFields;
    private long time;
}
