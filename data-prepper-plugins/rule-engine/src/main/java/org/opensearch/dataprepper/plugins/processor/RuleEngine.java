package org.opensearch.dataprepper.plugins.processor;

import org.opensearch.dataprepper.plugins.processor.evaluator.CompositeRuleEvaluator;
import org.opensearch.dataprepper.plugins.processor.evaluator.CorrelationEvaluator;
import org.opensearch.dataprepper.plugins.processor.evaluator.DefaultCorrelationEvaluator;
import org.opensearch.dataprepper.plugins.processor.evaluator.DefaultRuleEvaluator;
import org.opensearch.dataprepper.plugins.processor.evaluator.RuleEvaluator;
import org.opensearch.dataprepper.plugins.processor.model.mappings.Mapping;
import org.opensearch.dataprepper.plugins.processor.parser.RuleParser;
import org.opensearch.dataprepper.plugins.processor.provider.rules.RuleProvider;
import org.opensearch.dataprepper.plugins.processor.registrar.MappingRegistrar;
import org.opensearch.dataprepper.plugins.processor.registrar.RuleProviderRegistrar;
import org.opensearch.dataprepper.plugins.processor.registrar.SubMatchAccessorRegistrar;
import org.opensearch.dataprepper.plugins.processor.retrievers.SubMatchAccessor;
import org.opensearch.dataprepper.plugins.processor.rules.RuleRefresher;
import org.opensearch.dataprepper.plugins.processor.rules.RuleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RuleEngine {
    private static final Logger LOG = LoggerFactory.getLogger(RuleEngine.class);

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    private final MappingRegistrar mappingRegistrar;
    private final RuleProviderRegistrar ruleProviderRegistrar;
    private final SubMatchAccessorRegistrar subMatchAccessorRegistrar;
    private RuleEvaluator ruleEvaluator;
    private CorrelationEvaluator correlationEvaluator;

    public RuleEngine() {
        mappingRegistrar = new MappingRegistrar();
        ruleProviderRegistrar = new RuleProviderRegistrar();
        subMatchAccessorRegistrar = new SubMatchAccessorRegistrar();
    }

    public void registerMapping(final String logType, final Supplier<Mapping> mappingSupplier) {
        mappingRegistrar.registerMapping(logType, mappingSupplier);
    }

    public void registerRuleProvider(final String ruleLocation, final Supplier<RuleProvider> ruleProviderSupplier) {
        ruleProviderRegistrar.registerRuleProvider(ruleLocation, ruleProviderSupplier);
    }

    public void registerSubMatchAccessor(final String accessorName, final Supplier<SubMatchAccessor> subMatchAccessorSupplier) {
        subMatchAccessorRegistrar.registerSubMatchAccessor(accessorName, subMatchAccessorSupplier);
    }

    public RuleEvaluator start(final RuleEngineConfig config) {
        final RuleStore ruleStore = new RuleStore();
        setupRuleFetching(config, ruleStore);

        if (ruleEvaluator == null) {
            ruleEvaluator = new DefaultRuleEvaluator(ruleStore);
        }
        if (correlationEvaluator == null && config.getSubMatchAccessor() != null) {
            correlationEvaluator = new DefaultCorrelationEvaluator(ruleStore, subMatchAccessorRegistrar.getSubMatchAccessor(config.getSubMatchAccessor()));
            return CompositeRuleEvaluator.builder()
                    .ruleEvaluator(ruleEvaluator)
                    .correlationEvaluator(correlationEvaluator)
                    .build();
        }

        return ruleEvaluator;
    }

    private void setupRuleFetching(final RuleEngineConfig config, final RuleStore ruleStore) {
        final RuleProvider ruleProvider = ruleProviderRegistrar.getRuleProvider(config.getRuleLocation());
        LOG.info("Using RuleProvider of type {}", ruleProvider.getClass());

        final Map<String, String> mapping = mappingRegistrar.getMapping(config.getLogType(), config.getLogFormat());
        final RuleParser ruleParser = config.getRuleSchema().getParserConstructor().apply(mapping);

        final RuleRefresher ruleRefresher = new RuleRefresher(ruleProvider, ruleParser, ruleStore);
        SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(
                ruleRefresher,
                0L,
                config.getRuleRefreshInterval().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }
}
