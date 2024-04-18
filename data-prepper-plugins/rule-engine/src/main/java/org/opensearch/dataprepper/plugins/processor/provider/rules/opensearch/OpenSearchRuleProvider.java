package org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch;

import org.apache.commons.lang3.tuple.Pair;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.MgetRequest;
import org.opensearch.client.opensearch.core.MgetResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.mget.MultiGetResponseItem;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.dataprepper.plugins.processor.exceptions.RuleRefreshException;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.provider.rules.model.RuleData;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model.Detector;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model.DetectorInput;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model.DetectorRule;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model.DetectorWrapper;
import org.opensearch.dataprepper.plugins.processor.provider.rules.RuleProvider;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model.Input;
import org.opensearch.dataprepper.plugins.processor.provider.rules.opensearch.model.RuleWrapper;
import org.opensearch.dataprepper.plugins.processor.util.OpenSearchDocMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenSearchRuleProvider implements RuleProvider {
    private static final Logger LOG = LoggerFactory.getLogger(OpenSearchRuleProvider.class);

    private static final String DETECTORS_INDEX = ".opensearch-sap-detectors-config";
    private static final String PREPACKAGED_RULES_INDEX = ".opensearch-sap-pre-packaged-rules-config";
    private static final String CUSTOM_RULES_INDEX = ".opensearch-sap-custom-rules-config";

    private final OpenSearchClient openSearchClient;

    public OpenSearchRuleProvider(final OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    @Override
    public List<RuleData> getRules() {
        final List<Detector> detectors = getDetectors();
        final Map<Detector, Pair<List<String>, List<String>>> detectorToRuleIds = getDetectorToRuleIds(detectors);
        final Map<String, String> ruleIdToRuleAsString = getRuleIdToRuleAsString(detectorToRuleIds);

        return buildRules(detectorToRuleIds, ruleIdToRuleAsString);
    }

    private List<Detector> getDetectors() {
        final SearchRequest listDetectorsRequest = getListDetectorsRequest();

        try {
            final SearchResponse<DetectorWrapper> listDetectorsResponse = openSearchClient.search(listDetectorsRequest, DetectorWrapper.class);
            return parseDetectors(listDetectorsResponse);
        } catch (final Exception e) {
            throw new RuleRefreshException("Exception listing detectors", e);
        }
    }

    // TODO - build API in OpenSearch?
    private SearchRequest getListDetectorsRequest() {
        return new SearchRequest.Builder()
                .index(DETECTORS_INDEX)
                .size(10000) // TODO - pagination
                .build();
    }

    private List<Detector> parseDetectors(final SearchResponse<DetectorWrapper> listDetectorsResponse) {
        return listDetectorsResponse.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .map(DetectorWrapper::getDetector)
                .collect(Collectors.toList());
    }

    private Map<Detector, Pair<List<String>, List<String>>> getDetectorToRuleIds(final List<Detector> detectors) {
        return detectors.stream()
                .map(detector -> {
                    final List<String> prepackagedRuleIds = getRuleIds(detector, DetectorInput::getPrePackagedRules);
                    final List<String> customRuleIds = getRuleIds(detector, DetectorInput::getCustomRules);

                    return Map.entry(detector, Pair.of(prepackagedRuleIds, customRuleIds));
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<String> getRuleIds(final Detector detector, final Function<DetectorInput, List<DetectorRule>> ruleGetter) {
        return detector.getInputs().stream()
                .map(Input::getDetectorInput)
                .map(ruleGetter)
                .flatMap(Collection::stream)
                .map(DetectorRule::getId)
                .collect(Collectors.toList());
    }

    private Map<String, String> getRuleIdToRuleAsString(final Map<Detector, Pair<List<String>, List<String>>> detectorToRuleIds) {
        final Set<String> prepackagedRuleIds = getRuleIdsSet(detectorToRuleIds, Pair::getLeft);
        final Set<String> customRuleIds = getRuleIdsSet(detectorToRuleIds, Pair::getRight);

        final Optional<MgetRequest> getPrepackagedRulesRequest = getMgetRequest(PREPACKAGED_RULES_INDEX, prepackagedRuleIds);
        final Optional<MgetRequest> getCustomRulesRequest = getMgetRequest(CUSTOM_RULES_INDEX, customRuleIds);

        final HashMap<String, String> ruleIdToRuleAsString = new HashMap<>();
        ruleIdToRuleAsString.putAll(fetchRules(getPrepackagedRulesRequest));
        ruleIdToRuleAsString.putAll(fetchRules(getCustomRulesRequest));

        return ruleIdToRuleAsString;
    }

    private Set<String> getRuleIdsSet(final Map<Detector, Pair<List<String>, List<String>>> detectorToRuleIds,
                                       final Function<Pair<List<String>, List<String>>, List<String>> rulesGetter) {
        return detectorToRuleIds.values().stream()
                .map(rulesGetter)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Optional<MgetRequest> getMgetRequest(final String index, final Set<String> docIds) {
        if (docIds.isEmpty()) {
            return Optional.empty();
        }

        final List<String> docIdsList = new ArrayList<>(docIds);

        return Optional.of(new MgetRequest.Builder()
                .index(index)
                .ids(docIdsList)
                .build());
    }

    private Map<String, String> fetchRules(final Optional<MgetRequest> optionalGetRulesRequest) {
        if (optionalGetRulesRequest.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            final MgetResponse<RuleWrapper> getRulesResponse = openSearchClient.mget(optionalGetRulesRequest.get(), RuleWrapper.class);
            return parseGetRulesResponse(getRulesResponse);
        } catch (final Exception e) {
            throw new RuleRefreshException("Exception getting prepackaged rules", e);
        }
    }

    private Map<String, String> parseGetRulesResponse(final MgetResponse<RuleWrapper> getRulesResponse) {
        return getRulesResponse.docs().stream()
                .map(MultiGetResponseItem::result)
                .filter(ruleWrapperGetResult -> Objects.nonNull(ruleWrapperGetResult.source()))
                .map(ruleWrapperGetResult -> {
                    final String ruleId = ruleWrapperGetResult.id();
                    final String ruleAsString = ruleWrapperGetResult.source().getRule().getRule();

                    return Map.entry(ruleId, ruleAsString);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<RuleData> buildRules(final Map<Detector, Pair<List<String>, List<String>>> detectorToRuleIds,
                                      final Map<String, String> ruleIdToRuleAsString) {
        return detectorToRuleIds.entrySet().stream()
                .map(mapEntry -> buildDetectorRules(mapEntry.getKey(), mapEntry.getValue(), ruleIdToRuleAsString))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<RuleData> buildDetectorRules(final Detector detector, final Pair<List<String>, List<String>> ruleIdsPair,
                                              final Map<String, String> ruleIdToRuleAsString) {
        final Predicate<DataType> evaluationCondition = getDetectorEvaluationCondition(detector);
        final Map<String, String> metadata = Map.of(
                "monitorId", detector.getMonitorId().get(0),
                "detectorName", detector.getName(),
                "findingsIndex", detector.getFindingsIndex()
        );

        return Stream.concat(ruleIdsPair.getLeft().stream(), ruleIdsPair.getRight().stream())
                .map(ruleId -> new RuleData(ruleIdToRuleAsString.get(ruleId), evaluationCondition, metadata))
                .collect(Collectors.toList());
    }

    private Predicate<DataType> getDetectorEvaluationCondition(final Detector detector) {
        // TODO - aliases, index patterns, etc
        final Set<String> detectorIndices = detector.getInputs().stream()
                .map(Input::getDetectorInput)
                .map(DetectorInput::getIndices)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        return dataType -> {
            final String index = dataType.getDataTypeMetadataValue(OpenSearchDocMetadata.INDEX.getFieldName());
            if (index == null) {
                return false;
            }

            return detectorIndices.contains(index);
        };
    }
}
