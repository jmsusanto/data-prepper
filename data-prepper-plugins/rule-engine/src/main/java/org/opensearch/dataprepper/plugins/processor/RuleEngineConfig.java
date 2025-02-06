package org.opensearch.dataprepper.plugins.processor;

import lombok.Builder;
import lombok.Getter;
import org.opensearch.dataprepper.plugins.processor.model.rule.RuleSchema;

import java.time.Duration;

@Builder
@Getter
public class RuleEngineConfig {
    private final Duration ruleRefreshInterval;
    private final String logFormat;
    private final String logType;
    private final RuleSchema ruleSchema;
    private final String ruleLocation;
    private final String subMatchAccessor;
}
