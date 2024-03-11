package org.opensearch.dataprepper.plugins.processor.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.CloudTrail;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.OCSF;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SigmaV1SigmaV1RuleConditionParserTest {
    private static final String RULES_PATH_FORMAT = "src/test/resources/rules/%s";
    private static final String DELETE_IDENTITY_RULE_FILE = "aws_delete_identity.yml";
    private static final String GUARDDUTY_DISRUPTION_RULE_FILE = "aws_guardduty_disruption.yml";

    private SigmaV1RuleConditionParser sigmaSigmaV1RuleConditionParser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sigmaSigmaV1RuleConditionParser = new SigmaV1RuleConditionParser(Collections.emptyMap());
    }

    @Test
    void parse_SingleFieldEqualsCondition() {
        final SigmaRule sigmaRule = getSigmaRule(DELETE_IDENTITY_RULE_FILE);
        final Predicate<DataType> result = sigmaSigmaV1RuleConditionParser.parseRuleCondition(sigmaRule);

        assertTrue(result.test(getCloudTrail("", "ses.amazonaws.com")));
        assertFalse(result.test(getCloudTrail("", UUID.randomUUID().toString())));
    }

    @Test
    void parse_SingleFieldEqualsCondition_OCSF() {
        sigmaSigmaV1RuleConditionParser = new SigmaV1RuleConditionParser(Map.of(
                "eventName", "api.operation",
                "eventSource", "api.service.name"
        ));

        final SigmaRule sigmaRule = getSigmaRule(GUARDDUTY_DISRUPTION_RULE_FILE);
        final Predicate<DataType> result = sigmaSigmaV1RuleConditionParser.parseRuleCondition(sigmaRule);

        assertTrue(result.test(getOCSF("CreateIPSet", "guardduty.amazonaws.com")));
        assertFalse(result.test(getOCSF("DeleteIPSet", "guardduty.amazonaws.com")));
    }

    private SigmaRule getSigmaRule(final String ruleFile) {
        try {
            final Path rulePath = Path.of(String.format(RULES_PATH_FORMAT, ruleFile));
            final String ruleString = Files.readString(rulePath, StandardCharsets.UTF_8);

            return SigmaRule.fromYaml(ruleString, true);
        } catch (final Exception e) {
            throw new RuntimeException("Exception parsing rule: " + ruleFile, e);
        }
    }

    private DataType getCloudTrail(final String eventName, final String eventSource) {
        return new CloudTrail(eventName, eventSource);
    }

    private DataType getOCSF(final String eventName, final String eventSource) {
        return new OCSF(eventName, eventSource);
    }
}
