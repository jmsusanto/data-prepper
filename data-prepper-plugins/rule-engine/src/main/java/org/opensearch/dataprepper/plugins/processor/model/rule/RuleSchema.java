package org.opensearch.dataprepper.plugins.processor.model.rule;

import org.opensearch.dataprepper.plugins.processor.parser.OpenSearchSigmaV1RuleParser;
import org.opensearch.dataprepper.plugins.processor.parser.RuleParser;

import java.util.Map;
import java.util.function.Function;

public enum RuleSchema {
    OPENSEARCH_SIGMA_V1(OpenSearchSigmaV1RuleParser::new);

    private final Function<Map<String, String>, RuleParser> parserConstructor;

    RuleSchema(final Function<Map<String, String>, RuleParser> parserConstructor) {
        this.parserConstructor = parserConstructor;
    }

    public Function<Map<String, String>, RuleParser> getParserConstructor() {
        return parserConstructor;
    }
}
