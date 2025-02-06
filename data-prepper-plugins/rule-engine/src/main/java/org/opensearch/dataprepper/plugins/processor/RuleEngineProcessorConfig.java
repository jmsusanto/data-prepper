package org.opensearch.dataprepper.plugins.processor;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.opensearch.dataprepper.plugins.processor.model.log.LogFormat;
import org.opensearch.dataprepper.plugins.processor.model.rule.RuleSchema;

import java.time.Duration;
import java.util.Map;

public class RuleEngineProcessorConfig {
    static final Duration DEFAULT_RULE_REFRESH_INTERVAL = Duration.ofMinutes(1);
    static final LogFormat DEFAULT_LOG_FORMAT = LogFormat.NONE;

    @JsonProperty("rule_refresh_interval")
    private Duration ruleRefreshInterval = DEFAULT_RULE_REFRESH_INTERVAL;

    @JsonProperty("log_type")
    @NotEmpty
    @NotNull
    private String logType;

    @JsonProperty("log_format")
    private String logFormat = DEFAULT_LOG_FORMAT.getKeyName();

    @JsonProperty("rule_schema")
    @NotNull
    private RuleSchema ruleSchema;

    @JsonProperty("rule_location")
    @NotEmpty
    @NotNull
    private String ruleLocation;

    @JsonProperty("opensearch_config")
    private Map<String, Object> openSearchConfiguration;

    @JsonProperty("drop_data")
    private boolean dropData = false;

    @JsonProperty("sub_match_accessor")
    private String subMatchAccessor;

    public Duration getRuleRefreshInterval() {
        return ruleRefreshInterval;
    }

    public String getLogType() {
        return logType;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public RuleSchema getRuleSchema() {
        return ruleSchema;
    }

    public String getRuleLocation() {
        return ruleLocation;
    }

    public Map<String, Object> getOpenSearchConfiguration() {
        return openSearchConfiguration;
    }

    public boolean isDropData() {
        return dropData;
    }

    public String getSubMatchAccessor() {
        return subMatchAccessor;
    }
}
