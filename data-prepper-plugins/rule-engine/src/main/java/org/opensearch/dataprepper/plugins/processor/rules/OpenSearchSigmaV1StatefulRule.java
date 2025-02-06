package org.opensearch.dataprepper.plugins.processor.rules;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class OpenSearchSigmaV1StatefulRule extends StatefulRule {
    private OpenSearchRuleMetadata openSearchRuleMetadata;
    private SigmaV1RuleMetadata sigmaV1RuleMetadata;
}
