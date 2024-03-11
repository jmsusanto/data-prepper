package org.opensearch.dataprepper.plugins.processor;

import org.opensearch.dataprepper.plugins.processor.model.rule.RuleSchema;

import java.time.Duration;

public class RuleEngineConfig {
    private final Duration ruleRefreshInterval;
    private final String logFormat;
    private final String logType;
    private final RuleSchema ruleSchema;
    private final String ruleLocation;

    public RuleEngineConfig(final Duration ruleRefreshInterval, final String logFormat, final String logType,
                            final RuleSchema ruleSchema, final String ruleLocation) {
        this.ruleRefreshInterval = ruleRefreshInterval;
        this.logFormat = logFormat;
        this.logType = logType;
        this.ruleSchema = ruleSchema;
        this.ruleLocation = ruleLocation;
    }

    public Duration getRuleRefreshInterval() {
        return ruleRefreshInterval;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public String getLogType() {
        return logType;
    }

    public RuleSchema getRuleSchema() {
        return ruleSchema;
    }

    public String getRuleLocation() {
        return ruleLocation;
    }
}
