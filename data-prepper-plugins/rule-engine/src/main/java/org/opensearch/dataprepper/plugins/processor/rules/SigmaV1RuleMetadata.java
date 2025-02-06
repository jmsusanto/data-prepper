package org.opensearch.dataprepper.plugins.processor.rules;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SigmaV1RuleMetadata {
    private final String title;
    private final List<String> tags;
}
