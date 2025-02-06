/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.opensearch.dataprepper.plugins.processor.parser.condition;

import org.opensearch.dataprepper.plugins.processor.parser.exceptions.SigmaConditionError;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetection;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaDetections;
import org.opensearch.dataprepper.plugins.processor.parser.utils.Either;

import java.util.List;

public class ConditionIdentifier extends ConditionItem {

    private int argCount;
    private boolean tokenList;
    private String identifier;

    public ConditionIdentifier(List<Either<ConditionItem, String>> args) {
        super(1, true, args);
        this.argCount = 1;
        this.tokenList = true;
        this.identifier = args.get(0).get();
    }

    public ConditionItem postProcess(SigmaDetections detections, Object parent) throws SigmaConditionError {
        this.setParent((ConditionItem) parent);

        if (detections.getDetections().containsKey(this.identifier)) {
            SigmaDetection detection = detections.getDetections().get(this.identifier);
            return detection.postProcess(detections, this);
        } else {
            throw new SigmaConditionError("Detection '" + this.identifier + "' not defined in detections");
        }
    }
}