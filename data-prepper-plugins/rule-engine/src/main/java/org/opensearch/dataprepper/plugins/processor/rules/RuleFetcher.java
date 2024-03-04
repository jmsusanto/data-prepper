package org.opensearch.dataprepper.plugins.processor.rules;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.MgetRequest;
import org.opensearch.client.opensearch.core.MgetResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.mget.MultiGetResponseItem;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.dataprepper.plugins.processor.exceptions.RuleRefreshException;
import org.opensearch.dataprepper.plugins.processor.model.detector.Detector;
import org.opensearch.dataprepper.plugins.processor.model.detector.DetectorInput;
import org.opensearch.dataprepper.plugins.processor.model.detector.DetectorRule;
import org.opensearch.dataprepper.plugins.processor.model.detector.DetectorWrapper;
import org.opensearch.dataprepper.plugins.processor.model.detector.Input;
import org.opensearch.dataprepper.plugins.processor.model.rule.RuleWrapper;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RuleFetcher implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(RuleFetcher.class);

    private static final String DETECTORS_INDEX = ".opensearch-sap-detectors-config";
    private static final String PREPACKAGED_RULES_INDEX = ".opensearch-sap-pre-packaged-rules-config";
    private static final String CUSTOM_RULES_INDEX = ".opensearch-sap-custom-rules-config";

    private final OpenSearchClient openSearchClient;
    private final RuleStore sigmaRuleStore;
    private final RuleConverter ruleConverter;

    public RuleFetcher(final OpenSearchClient openSearchClient,
                       final RuleStore sigmaRuleStore,
                       final RuleConverter ruleConverter) {
        this.openSearchClient = openSearchClient;
        this.sigmaRuleStore = sigmaRuleStore;
        this.ruleConverter = ruleConverter;
    }

    @Override
    public void run() {
        try {
            LOG.debug("Starting rule fetch");
            final List<Detector> detectors = getDetectors();
            LOG.debug("Found detectors: {}", detectors);

            final Map<String, List<Rule>> indexToPrepackagedRules = getIndexToPrepackagedRules(detectors);
            final Map<String, List<Rule>> indexToCustomRules = getIndexToCustomRules(detectors);

            final Map<String, List<Rule>> mergedIndexToRules = mergeIndexToRules(List.of(indexToPrepackagedRules, indexToCustomRules));
            sigmaRuleStore.updateRuleStore(mergedIndexToRules);
        } catch (final Exception e) {
            LOG.error("Caught exception refreshing rules", e);
        }
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

    // TODO - build API in OpenSearch
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

    private Map<String, List<Rule>> getIndexToPrepackagedRules(final List<Detector> detectors) {
        final Map<String, List<DetectorDTO>> indexToPrepackagedRuleIds = getIndexToDetectorMetadata(detectors, DetectorInput::getPrePackagedRules);
        final Set<String> prepackagedRuleIds = indexToPrepackagedRuleIds.values().stream()
                .flatMap(Collection::stream)
                .map(DetectorDTO::getRuleIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        final Optional<MgetRequest> getPrepackagedRulesRequest = getMgetRequest(PREPACKAGED_RULES_INDEX, prepackagedRuleIds);

        if (getPrepackagedRulesRequest.isEmpty()) {
            // No rules to get
            return Collections.emptyMap();
        }

        try {
            final MgetResponse<RuleWrapper> getRulesResponse = openSearchClient.mget(getPrepackagedRulesRequest.get(), RuleWrapper.class);
            return getIndexToRules(indexToPrepackagedRuleIds, getRulesResponse);
        } catch (final Exception e) {
            throw new RuleRefreshException("Exception getting prepackaged rules", e);
        }
    }

    private Map<String, List<Rule>> getIndexToCustomRules(final List<Detector> detectors) {
        final Map<String, List<DetectorDTO>> indexToCustomRuleIds = getIndexToDetectorMetadata(detectors, DetectorInput::getCustomRules);
        final Set<String> customRuleIds = indexToCustomRuleIds.values().stream()
                .flatMap(Collection::stream)
                .map(DetectorDTO::getRuleIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        final Optional<MgetRequest> getCustomRulesRequest = getMgetRequest(CUSTOM_RULES_INDEX, customRuleIds);

        if (getCustomRulesRequest.isEmpty()) {
            // No rules to get
            return Collections.emptyMap();
        }

        try {
            final MgetResponse<RuleWrapper> getRulesResponse = openSearchClient.mget(getCustomRulesRequest.get(), RuleWrapper.class);
            return getIndexToRules(indexToCustomRuleIds, getRulesResponse);
        } catch (final Exception e) {
            throw new RuleRefreshException("Exception getting custom rules", e);
        }
    }

    private Map<String, List<DetectorDTO>> getIndexToDetectorMetadata(final List<Detector> detectors, final Function<DetectorInput, List<DetectorRule>> ruleGetter) {
        final Map<String, List<DetectorDTO>> indexToRuleIds = new HashMap<>();
        detectors.stream()
                .forEach(detector -> {
                    final List<Input> inputs = detector.getInputs();
                    final List<DetectorInput> detectorInputs = inputs.stream()
                            .map(Input::getDetectorInput)
                            .collect(Collectors.toList());

                    detectorInputs.forEach(detectorInput -> {
                        final Set<String> ruleIds = getRuleIds(detectorInput, ruleGetter);
                        final DetectorDTO detectorDTO = new DetectorDTO(ruleIds, detector.getMonitorId().get(0), detector.getName(), detector.getFindingsIndex());

                        detectorInput.getIndices().forEach(index -> {
                            indexToRuleIds.putIfAbsent(index, new ArrayList<>());
                            indexToRuleIds.get(index).add(detectorDTO);
                        });
                    });
                });

        return indexToRuleIds;
    }

    private Set<String> getRuleIds(final DetectorInput detectorInput, final Function<DetectorInput, List<DetectorRule>> ruleGetter) {
        return ruleGetter.apply(detectorInput).stream()
                .map(DetectorRule::getId)
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

    private Map<String, List<Rule>> getIndexToRules(final Map<String, List<DetectorDTO>> indexToDetectorMetadata, final MgetResponse<RuleWrapper> getRulesResponse) {
        final Map<String, Rule> ruleIdToSigmaRule = getRuleIdToSigmaRule(getRulesResponse);

        final Map<String, List<Rule>> indexToRules = new HashMap<>();
        indexToDetectorMetadata.forEach((index, detectorDTOs) -> {
            final List<Rule> rules = detectorDTOs.stream()
                    .map(detectorDTO -> {
                        final Set<String> ruleIds = detectorDTO.getRuleIds();
                        return ruleIds.stream()
                                .map(ruleId -> {
                                    final Rule rule = ruleIdToSigmaRule.get(ruleId);
                                    rule.setMonitorId(detectorDTO.getMonitorID());
                                    rule.setFindingsIndex(detectorDTO.getFindingsIndex());
                                    rule.setMonitorName(detectorDTO.getMonitorName());

                                    return rule;
                                })
                                .collect(Collectors.toList());
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            if (!rules.isEmpty()) {
                indexToRules.put(index, rules);
            }
        });

        return indexToRules;
    }

    private Map<String, Rule> getRuleIdToSigmaRule(final MgetResponse<RuleWrapper> getRulesResponse) {
        return getRulesResponse.docs().stream()
                .map(MultiGetResponseItem::result)
                .filter(ruleWrapperGetResult -> Objects.nonNull(ruleWrapperGetResult.source()))
                .map(ruleWrapperGetResult -> {
                    final String ruleID = ruleWrapperGetResult.id();
                    final Rule sigmaRule = parseSigmaRule(ruleWrapperGetResult.source());

                    return Map.entry(ruleID, sigmaRule);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Rule parseSigmaRule(final RuleWrapper ruleWrapper) {
        final String ruleString = ruleWrapper.getRule().getRule();
        final SigmaRule sigmaRule = SigmaRule.fromYaml(ruleString, true);

        return ruleConverter.apply(sigmaRule);
    }

    private Map<String, List<Rule>> mergeIndexToRules(final List<Map<String, List<Rule>>> indexToRulesList) {
        final Map<String, List<Rule>> indexToRules = new HashMap<>();

        indexToRulesList.forEach(indexToRulesEntry -> {
            indexToRulesEntry.forEach((index, sigmaRules) -> {
                indexToRules.putIfAbsent(index, new ArrayList<>());
                indexToRules.get(index).addAll(sigmaRules);
            });
        });

        return indexToRules;
    }
}
