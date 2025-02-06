/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.objects;

import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaConditionError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaDetectionError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaModifierError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaRegularExpressionError;
import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaValueError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigmaDetections {

    private Map<String, SigmaDetection> detections;

    private List<String> conditions;

    private String timeframe;

    private List<SigmaCondition> parsedConditions;

    public SigmaDetections(Map<String, SigmaDetection> detections, List<String> conditions, String timeframe) throws SigmaDetectionError {
        this.detections = detections;
        this.conditions = conditions;
        this.timeframe = timeframe;

        if (this.detections.isEmpty()) {
            throw new SigmaDetectionError("No detections defined in Sigma rule");
        }

        this.parsedConditions = new ArrayList<>();
        for (String cond: this.conditions) {
            this.parsedConditions.add(new SigmaCondition(cond, this));
        }
    }

    @SuppressWarnings("unchecked")
    protected static SigmaDetections fromDict(Map<String, Object> detectionMap) throws SigmaConditionError, SigmaDetectionError, SigmaModifierError, SigmaValueError, SigmaRegularExpressionError {
        List<String> conditionList = new ArrayList<>();
        if (detectionMap.containsKey("condition") && detectionMap.get("condition") instanceof List) {
            conditionList.addAll((List<String>) detectionMap.get("condition"));
        } else if (detectionMap.containsKey("condition")) {
            conditionList.add(detectionMap.get("condition").toString());
        } else {
            throw new SigmaConditionError("Sigma rule must contain at least one condition");
        }

        Map<String, SigmaDetection> detections = new HashMap<>();
        for (Map.Entry<String, Object> detection: detectionMap.entrySet()) {
            if (!"condition".equals(detection.getKey())) {
                detections.put(detection.getKey(), SigmaDetection.fromDefinition(detection.getValue()));
            }
        }

        String timeframe = null;
        if (detectionMap.containsKey("timeframe")) {
            timeframe = detectionMap.get("timeframe").toString();
        }

        return new SigmaDetections(detections, conditionList, timeframe);
    }

    public Map<String, SigmaDetection> getDetections() {
        return detections;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public List<SigmaCondition> getParsedConditions() {
        return parsedConditions;
    }

    public String getTimeframe() {
        return timeframe;
    }
}