package org.opensearch.dataprepper.plugins.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.dataprepper.metrics.PluginMetrics;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.log.JacksonLog;
import org.opensearch.dataprepper.model.plugin.PluginFactory;
import org.opensearch.dataprepper.model.processor.AbstractProcessor;
import org.opensearch.dataprepper.model.processor.Processor;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.plugins.processor.converters.OCSFConverter;
import org.opensearch.dataprepper.plugins.processor.evaluator.RuleEvaluator;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.provider.rules.RuleProvider;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.OpenSearchRuleProvider;
import org.opensearch.dataprepper.plugins.sink.opensearch.ConnectionConfiguration;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@DataPrepperPlugin(name = "rule_engine", pluginType = Processor.class, pluginConfigurationType = RuleEngineProcessorConfig.class)
public class RuleEngineProcessor extends AbstractProcessor<Record<Event>, Record<Event>> {
    private final RuleEvaluator ruleEvaluator;
    private final RuleEngineProcessorConfig config;
    private final OCSFConverter ocsfConverter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @DataPrepperPluginConstructor
    public RuleEngineProcessor(final PluginMetrics pluginMetrics,
                               final RuleEngineProcessorConfig config,
                               final PluginFactory pluginFactory) {
        super(pluginMetrics);

        final RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.registerRuleProvider("opensearch", this::getRuleProvider);
        this.config = config;

        final RuleEngineConfig ruleEngineConfig = new RuleEngineConfig(config.getRuleRefreshInterval(), config.getLogFormat(), config.getLogType(),
                config.getRuleSchema(), config.getRuleLocation());
        ruleEvaluator = ruleEngine.start(ruleEngineConfig);
        ocsfConverter = new OCSFConverter();
    }

    @Override
    public Collection<Record<Event>> doExecute(final Collection<Record<Event>> records) {
        final Collection<Match> dataWithMatches = ruleEvaluator.evaluate(convertToOCSF(records));
        final Collection<Record<Event>> matches = convertMatchesToEvents(dataWithMatches);

        if (config.isDropData()) {
            return matches;
        }

        records.addAll(matches);
        return records;
    }

    private Collection<DataType> convertToOCSF(final Collection<Record<Event>> records) {
        return records.stream()
                .map(ocsfConverter::convert)
                .peek(ocsf -> ocsf.putMetadataValue("index", "test-index-4"))
                .collect(Collectors.toList());
    }

    private List<Record<Event>> convertMatchesToEvents(final Collection<Match> dataWithMatches) {
        return dataWithMatches.stream()
                .map(match -> (Map<String, Object>) objectMapper.convertValue(match, new TypeReference<>() {}))
                .map(matchesMap -> JacksonLog.builder().withData(matchesMap).build())
                .map(event -> new Record<Event>(event))
                .collect(Collectors.toList());
    }

    @Override
    public void prepareForShutdown() {
    }

    @Override
    public boolean isReadyForShutdown() {
        return true;
    }

    @Override
    public void shutdown() {
    }

    private RuleProvider getRuleProvider() {
        final ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration.Builder(List.of("localhost:9200"))
                .withInsecure(true)
                .build();
        final OpenSearchClient openSearchClient = connectionConfiguration.createOpenSearchClient(connectionConfiguration.createClient(null), null);

        return new OpenSearchRuleProvider(openSearchClient);
    }
}
