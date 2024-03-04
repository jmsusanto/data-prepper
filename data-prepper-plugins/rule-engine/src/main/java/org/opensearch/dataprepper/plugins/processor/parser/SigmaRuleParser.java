package org.opensearch.dataprepper.plugins.processor.parser;

import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;

public interface SigmaRuleParser<T> {
    T parseRule(SigmaRule sigmaRule);
}
