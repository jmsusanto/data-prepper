package org.opensearch.dataprepper.plugins.processor.rules;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class OpenSearchSigmaV1StatelessRule extends StatelessRule {
    private OpenSearchRuleMetadata openSearchRuleMetadata;
    private SigmaV1RuleMetadata sigmaV1RuleMetadata;
}
