/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.dataprepper.plugins.sink.opensearch;

import org.opensearch.dataprepper.model.configuration.PluginSetting;
import org.opensearch.dataprepper.plugins.processor.RuleEngineConfig;
import org.opensearch.dataprepper.plugins.processor.RuleEngineConfigWrapper;
import org.opensearch.dataprepper.plugins.sink.opensearch.index.IndexConfiguration;
import org.opensearch.dataprepper.expression.ExpressionEvaluator;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public class OpenSearchSinkConfiguration {

  private final ConnectionConfiguration connectionConfiguration;
  private final IndexConfiguration indexConfiguration;
  private final RetryConfiguration retryConfiguration;
  private final RuleEngineConfigWrapper ruleEngineConfigWrapper;

  public ConnectionConfiguration getConnectionConfiguration() {
    return connectionConfiguration;
  }

  public IndexConfiguration getIndexConfiguration() {
    return indexConfiguration;
  }

  public RetryConfiguration getRetryConfiguration() {
    return retryConfiguration;
  }

  public Optional<RuleEngineConfig> getRuleEngineConfig() {
    return Optional.ofNullable(ruleEngineConfigWrapper.getRuleEngineConfig());
  }

  private OpenSearchSinkConfiguration(
          final ConnectionConfiguration connectionConfiguration, final IndexConfiguration indexConfiguration,
          final RetryConfiguration retryConfiguration, final RuleEngineConfigWrapper ruleEngineConfigWrapper) {
    checkNotNull(connectionConfiguration, "connectionConfiguration cannot be null");
    checkNotNull(indexConfiguration, "indexConfiguration cannot be null");
    checkNotNull(retryConfiguration, "retryConfiguration cannot be null");
    checkNotNull(ruleEngineConfigWrapper, "ruleEngineConfigWrapper cannot be null");
    this.connectionConfiguration = connectionConfiguration;
    this.indexConfiguration = indexConfiguration;
    this.retryConfiguration = retryConfiguration;
    this.ruleEngineConfigWrapper = ruleEngineConfigWrapper;
  }

  public static OpenSearchSinkConfiguration readESConfig(final PluginSetting pluginSetting) {
    return readESConfig(pluginSetting, null);
  }

  public static OpenSearchSinkConfiguration readESConfig(final PluginSetting pluginSetting, final ExpressionEvaluator expressionEvaluator) {
    final ConnectionConfiguration connectionConfiguration =
            ConnectionConfiguration.readConnectionConfiguration(pluginSetting);
    final IndexConfiguration indexConfiguration = IndexConfiguration.readIndexConfig(pluginSetting, expressionEvaluator);
    final RetryConfiguration retryConfiguration = RetryConfiguration.readRetryConfig(pluginSetting);
    final RuleEngineConfigWrapper ruleEngineConfigWrapper = RuleEngineConfigWrapper.readRuleEngineConfigWrapper(pluginSetting);

    return new OpenSearchSinkConfiguration(connectionConfiguration, indexConfiguration, retryConfiguration, ruleEngineConfigWrapper);
  }
}
