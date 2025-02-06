package org.opensearch.dataprepper.plugins.processor.rules;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OpenSearchRuleMetadata {
    private final String monitorId;
    private final String detectorName;
    private final String findingsIndex;
}
