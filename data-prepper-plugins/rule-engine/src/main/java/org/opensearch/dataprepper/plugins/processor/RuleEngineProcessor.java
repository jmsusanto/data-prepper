package org.opensearch.dataprepper.plugins.processor;

import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.dataprepper.aws.api.AwsCredentialsSupplier;
import org.opensearch.dataprepper.expression.ExpressionEvaluator;
import org.opensearch.dataprepper.metrics.PluginMetrics;
import org.opensearch.dataprepper.model.acknowledgements.AcknowledgementSet;
import org.opensearch.dataprepper.model.annotations.DataPrepperPlugin;
import org.opensearch.dataprepper.model.annotations.DataPrepperPluginConstructor;
import org.opensearch.dataprepper.model.configuration.PluginSetting;
import org.opensearch.dataprepper.model.event.DefaultEventHandle;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.log.JacksonLog;
import org.opensearch.dataprepper.model.plugin.PluginFactory;
import org.opensearch.dataprepper.model.processor.AbstractProcessor;
import org.opensearch.dataprepper.model.processor.Processor;
import org.opensearch.dataprepper.model.record.Record;
import org.opensearch.dataprepper.plugins.processor.converters.FindingConverter;
import org.opensearch.dataprepper.plugins.processor.evaluator.RuleEvaluator;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.OpenSearchRuleProvider;
import org.opensearch.dataprepper.plugins.processor.util.OpenSearchDocMetadata;
import org.opensearch.dataprepper.plugins.sink.opensearch.OpenSearchSinkConfiguration;
import org.opensearch.dataprepper.plugins.sink.opensearch.index.ClusterSettingsParser;
import org.opensearch.dataprepper.plugins.sink.opensearch.index.IndexManager;
import org.opensearch.dataprepper.plugins.sink.opensearch.index.IndexManagerFactory;
import org.opensearch.dataprepper.plugins.sink.opensearch.index.IndexTemplateAPIWrapper;
import org.opensearch.dataprepper.plugins.sink.opensearch.index.IndexTemplateAPIWrapperFactory;
import org.opensearch.dataprepper.plugins.sink.opensearch.index.TemplateStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@DataPrepperPlugin(name = "rule_engine", pluginType = Processor.class, pluginConfigurationType = RuleEngineProcessorConfig.class)
public class RuleEngineProcessor extends AbstractProcessor<Record<Event>, Record<Event>> {
    private static final Logger LOG = LoggerFactory.getLogger(RuleEngineProcessor.class);

    private final RuleEvaluator ruleEvaluator;
    private final RuleEngineProcessorConfig config;
    private final FindingConverter findingConverter;
    private final IndexManager indexManager;
    private final ExpressionEvaluator expressionEvaluator;
    private final OpenSearchSinkConfiguration openSearchSinkConfiguration;
    private final OpenSearchClient openSearchClient;
    private AcknowledgementSet acknowledgementSet;

    @DataPrepperPluginConstructor
    public RuleEngineProcessor(final PluginMetrics pluginMetrics,
                               final RuleEngineProcessorConfig config,
                               final PluginFactory pluginFactory,
                               final ExpressionEvaluator expressionEvaluator,
                               final AwsCredentialsSupplier awsCredentialsSupplier) throws IOException {
        super(pluginMetrics);

        this.config = config;
        this.expressionEvaluator = expressionEvaluator;

        openSearchSinkConfiguration = OpenSearchSinkConfiguration.readESConfig(
                new PluginSetting("opensearch_config", config.getOpenSearchConfiguration()), expressionEvaluator);
        final RestHighLevelClient restHighLevelClient = openSearchSinkConfiguration.getConnectionConfiguration().createClient(awsCredentialsSupplier);
        openSearchClient = openSearchSinkConfiguration.getConnectionConfiguration()
                .createOpenSearchClient(restHighLevelClient, awsCredentialsSupplier);
        final IndexTemplateAPIWrapper indexTemplateAPIWrapper = IndexTemplateAPIWrapperFactory.getWrapper(
                openSearchSinkConfiguration.getIndexConfiguration(), openSearchClient);
        final TemplateStrategy templateStrategy = openSearchSinkConfiguration.getIndexConfiguration().getTemplateType()
                .createTemplateStrategy(indexTemplateAPIWrapper);
        final IndexManagerFactory indexManagerFactory = new IndexManagerFactory(new ClusterSettingsParser());
        indexManager = indexManagerFactory.getIndexManager(openSearchSinkConfiguration.getIndexConfiguration().getIndexType(), openSearchClient, restHighLevelClient,
                openSearchSinkConfiguration, templateStrategy, openSearchSinkConfiguration.getIndexConfiguration().getIndexAlias());

        final RuleEngine ruleEngine = new RuleEngine();
        ruleEngine.registerRuleProvider("opensearch", () -> new OpenSearchRuleProvider(openSearchClient));

        final RuleEngineConfig ruleEngineConfig = new RuleEngineConfig(config.getRuleRefreshInterval(), config.getLogFormat(), config.getLogType(),
                config.getRuleSchema(), config.getRuleLocation());
        ruleEvaluator = ruleEngine.start(ruleEngineConfig);
        findingConverter = new FindingConverter();
        acknowledgementSet = null;
    }

    @Override
    public Collection<Record<Event>> doExecute(final Collection<Record<Event>> records) {
        if (records.isEmpty()) {
            return records;
        }

        if (acknowledgementSet == null) {
            acknowledgementSet = ((DefaultEventHandle) (records.iterator().next().getData().getEventHandle())).getAcknowledgementSet();
        }

        final Map<String, DataType> idToData = addTrackingData(records);
        final Collection<Match> dataWithMatches = ruleEvaluator.evaluate(idToData.values());
        final Collection<Record<Event>> matches = convertMatchesToEvents(dataWithMatches);

        if (config.isDropData()) {
            return matches;
        }

        records.addAll(matches);
        return records;
    }

    private Map<String, DataType> addTrackingData(final Collection<Record<Event>> records) {
        return records.stream()
                .map(record -> {
                    final String id = UUID.randomUUID().toString();
                    final DataType dataType = (DataType) record.getData();
                    dataType.putMetadataValue(OpenSearchDocMetadata.INDEX.getFieldName(), getIndexName(record));

                    final Map.Entry<String, DataType> mapEntry = Map.entry(id, dataType);
                    record.getData().put(OpenSearchDocMetadata.RULE_ENGINE_ID.getFieldName(), id);

                    return mapEntry;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String getIndexName(final Record<Event> record) {
        String indexName = openSearchSinkConfiguration.getIndexConfiguration().getIndexAlias();
        try {
            return indexManager.getIndexName(record.getData().formatString(indexName, expressionEvaluator));
        } catch (final Exception e) {
            LOG.error("There was an exception when constructing the index name.", e);
            throw new RuntimeException(e);
        }
    }

    private List<Record<Event>> convertMatchesToEvents(final Collection<Match> dataWithMatches) {
        return dataWithMatches.stream()
                .map(findingConverter::convert)
                .flatMap(Collection::stream)
                .map(matchesMap -> JacksonLog.builder().withData(matchesMap).build())
                .peek(event -> {
                    if (acknowledgementSet != null) {
                        acknowledgementSet.add(event);
                    }
                })
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
}
