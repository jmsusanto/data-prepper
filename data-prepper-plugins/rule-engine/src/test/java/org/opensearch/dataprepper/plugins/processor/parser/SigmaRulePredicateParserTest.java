package org.opensearch.dataprepper.plugins.processor.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.opensearch.dataprepper.model.event.Event;
import org.opensearch.dataprepper.model.log.JacksonLog;
import org.opensearch.dataprepper.plugins.processor.RuleEngineConfig;
import org.opensearch.dataprepper.plugins.processor.model.log.LogFormat;
import org.opensearch.dataprepper.plugins.processor.model.log.LogType;
import org.opensearch.dataprepper.plugins.processor.parser.objects.SigmaRule;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SigmaRulePredicateParserTest {
    private static final String RULES_PATH_FORMAT = "src/test/resources/rules/%s";
    private static final String DELETE_IDENTITY_RULE_FILE = "aws_delete_identity.yml";
    private static final String GUARDDUTY_DISRUPTION_RULE_FILE = "aws_guardduty_disruption.yml";

    private SigmaRulePredicateParser sigmaRulePredicateParser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        final RuleEngineConfig ruleEngineConfig = new RuleEngineConfig.Builder()
                .withLogFormat(LogFormat.NONE)
                .withLogType(LogType.CLOUDTRAIL)
                .build();
        sigmaRulePredicateParser = new SigmaRulePredicateParser(ruleEngineConfig);
    }

    @Test
    void parse_SingleFieldEqualsCondition() {
        final SigmaRule sigmaRule = getSigmaRule(DELETE_IDENTITY_RULE_FILE);
        final Predicate<Event> result = sigmaRulePredicateParser.parseRule(sigmaRule);

        assertTrue(result.test(getEvent(Map.of("eventSource", "ses.amazonaws.com"))));
        assertFalse(result.test(getEvent(Map.of("eventSource", UUID.randomUUID().toString()))));
    }

    @Test
    void parse_SingleFieldEqualsCondition_OCSF() {
        sigmaRulePredicateParser = new SigmaRulePredicateParser(new RuleEngineConfig.Builder()
                .withLogFormat(LogFormat.OCSF)
                .withLogType(LogType.CLOUDTRAIL)
                .build());

        final SigmaRule sigmaRule = getSigmaRule(GUARDDUTY_DISRUPTION_RULE_FILE);
        final Predicate<Event> result = sigmaRulePredicateParser.parseRule(sigmaRule);

        assertTrue(result.test(getEvent(Map.of(
                "api.service.name", "guardduty.amazonaws.com",
                "api.operation", "CreateIPSet"
        ))));
        assertFalse(result.test(getEvent(Map.of(
                "api.service.name", "guardduty.amazonaws.com",
                "api.operation", "DeleteIPSet"
        ))));
    }

    @Test
    void parse_SingleFieldEqualsCondition_Nested() {
        sigmaRulePredicateParser = new SigmaRulePredicateParser(new RuleEngineConfig.Builder()
                .withLogFormat(LogFormat.OCSF)
                .withLogType(LogType.CLOUDTRAIL)
                .build());

        final SigmaRule sigmaRule = getSigmaRule(GUARDDUTY_DISRUPTION_RULE_FILE);
        final Predicate<Event> result = sigmaRulePredicateParser.parseRule(sigmaRule);

        assertTrue(result.test(getEvent(Map.of(
                "api", Map.of(
                        "service", Map.of(
                                "name", "guardduty.amazonaws.com"
                        ),
                        "operation", "CreateIPSet"
                )
        ))));
        assertFalse(result.test(getEvent(Map.of(
                "api", Map.of(
                        "service", Map.of(
                                "name", "guardduty.amazonaws.com"
                        ),
                        "operation", "DeleteIPSet"
                )
        ))));
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

    private Event getEvent(final Map<String, Object> data) {
        return JacksonLog.builder()
                .withData(data)
                .build();
    }
}
