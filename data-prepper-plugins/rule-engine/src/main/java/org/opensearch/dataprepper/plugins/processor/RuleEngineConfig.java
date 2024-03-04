package org.opensearch.dataprepper.plugins.processor;

import org.opensearch.dataprepper.model.configuration.PluginSetting;
import org.opensearch.dataprepper.plugins.processor.model.log.LogFormat;
import org.opensearch.dataprepper.plugins.processor.model.log.LogType;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkNotNull;

public class RuleEngineConfig {
    static final long DEFAULT_RULE_REFRESH_INTERVAL_MILLIS = Duration.ofMinutes(1).toMillis();
    static final LogFormat DEFAULT_LOG_FORMAT = LogFormat.NONE;

    public static final String RULE_REFRESH_INTERVAL_MILLIS = "rule_refresh_interval_millis";
    public static final String LOG_TYPE = "log_type";
    public static final String LOG_FORMAT = "log_format";

    private final Duration ruleRefreshInterval;
    private final LogType logType;
    private final LogFormat logFormat;

    public Duration getRuleRefreshInterval() {
        return ruleRefreshInterval;
    }

    public LogType getLogType() {
        return logType;
    }

    public LogFormat getLogFormat() {
        return logFormat;
    }

    public static class Builder {
        private Duration ruleRefreshInterval = Duration.ofMillis(DEFAULT_RULE_REFRESH_INTERVAL_MILLIS);
        private LogType logType;
        private LogFormat logFormat = DEFAULT_LOG_FORMAT;

        public Builder withRuleRefreshInterval(final Duration ruleRefreshInterval) {
            checkNotNull(ruleRefreshInterval, "ruleRefreshInterval cannot be null.");
            this.ruleRefreshInterval = ruleRefreshInterval;
            return this;
        }

        public Builder withLogType(final LogType logType) {
            checkNotNull(logType, "logType cannot be null.");
            this.logType = logType;
            return this;
        }

        public Builder withLogFormat(final LogFormat logFormat) {
            checkNotNull(logFormat, "logFormat cannot be null");
            this.logFormat = logFormat;
            return this;
        }

        public RuleEngineConfig build() {
            return new RuleEngineConfig(this);
        }
    }

    private RuleEngineConfig(final Builder builder) {
        this.ruleRefreshInterval = builder.ruleRefreshInterval;
        this.logType = checkNotNull(builder.logType, "logType cannot be null.");
        this.logFormat = builder.logFormat;
    }

    public static RuleEngineConfig readRuleEngineConfig(final PluginSetting pluginSetting) {
        RuleEngineConfig.Builder builder = new RuleEngineConfig.Builder();

        final Duration ruleRefreshInterval = Duration.ofMillis(
                pluginSetting.getLongOrDefault(RULE_REFRESH_INTERVAL_MILLIS, DEFAULT_RULE_REFRESH_INTERVAL_MILLIS)
        );
        builder = builder.withRuleRefreshInterval(ruleRefreshInterval);

        final LogType logType = LogType.valueOf(pluginSetting.getStringOrDefault(LOG_TYPE, null).toUpperCase());
        builder = builder.withLogType(logType);

        final LogFormat logFormat = LogFormat.valueOf(pluginSetting.getStringOrDefault(LOG_FORMAT, DEFAULT_LOG_FORMAT.name()).toUpperCase());
        builder = builder.withLogFormat(logFormat);

        return builder.build();
    }
}
