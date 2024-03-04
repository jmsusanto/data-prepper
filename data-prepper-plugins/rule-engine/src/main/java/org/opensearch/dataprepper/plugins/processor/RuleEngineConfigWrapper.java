package org.opensearch.dataprepper.plugins.processor;

import org.opensearch.dataprepper.model.configuration.PluginSetting;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class RuleEngineConfigWrapper {
    public static final String RULE_ENGINE = "rule_engine";

    private final RuleEngineConfig ruleEngineConfig;

    public RuleEngineConfig getRuleEngineConfig() {
        return ruleEngineConfig;
    }

    public static class Builder {
        private RuleEngineConfig ruleEngineConfig;

        public Builder withRuleEngineConfig(final RuleEngineConfig ruleEngineConfig) {
            checkNotNull(ruleEngineConfig, "ruleEngineConfig cannot be null.");
            this.ruleEngineConfig = ruleEngineConfig;
            return this;
        }

        public RuleEngineConfigWrapper build() {
            return new RuleEngineConfigWrapper(this);
        }
    }

    private RuleEngineConfigWrapper(final Builder builder) {
        this.ruleEngineConfig = builder.ruleEngineConfig;
    }

    public static RuleEngineConfigWrapper readRuleEngineConfigWrapper(final PluginSetting pluginSetting) {
        RuleEngineConfigWrapper.Builder builder = new RuleEngineConfigWrapper.Builder();

        final Map<String, Object> ruleEngineSettingsMap = (Map<String, Object>) pluginSetting.getAttributeFromSettings(RULE_ENGINE);
        if (ruleEngineSettingsMap != null) {
            final PluginSetting ruleEngineSettings = new PluginSetting(RULE_ENGINE, ruleEngineSettingsMap);
            final RuleEngineConfig ruleEngineConfig = RuleEngineConfig.readRuleEngineConfig(ruleEngineSettings);
            builder = builder.withRuleEngineConfig(ruleEngineConfig);
        }

        return builder.build();
    }
}
