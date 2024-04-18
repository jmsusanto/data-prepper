package org.opensearch.dataprepper.plugins.processor.evaluator;

import lombok.extern.log4j.Log4j2;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;
import org.opensearch.dataprepper.plugins.processor.retrievers.SubMatchAccessor;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;
import org.opensearch.dataprepper.plugins.processor.rules.RuleStore;
import org.opensearch.dataprepper.plugins.processor.rules.StatefulRule;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class DefaultCorrelationEvaluator implements CorrelationEvaluator {
    private final RuleStore ruleStore;
    private final SubMatchAccessor subMatchAccessor;

    public DefaultCorrelationEvaluator(final RuleStore ruleStore, final SubMatchAccessor subMatchAccessor) {
        this.ruleStore = ruleStore;
        this.subMatchAccessor = subMatchAccessor;
    }

    @Override
    public Collection<Match> evaluate(final Collection<Match> matches) {
        if (matches.isEmpty()) {
            return matches;
        }

        log.info("Processing {} matches", matches.size());
        storeSubMatches(matches);

        final List<StatefulRule> rules = ruleStore.getStatefulRules();

        matches.forEach(match -> {
            log.info("Evaluating match");
            final List<StatefulRule> ruleMatches = rules.stream()
                    .filter(rule -> rule.testEvaluationCondition(match))
                    .filter(rule -> evaluateRule(rule, match))
                    .collect(Collectors.toList());
            log.info("Got stateful rule matches {}", ruleMatches);
            match.setStatefulRuleMatches(ruleMatches);

            // Remove the stateless condition matches to avoid them generating findings
            final List<StatelessRule> statefulConditionMatch = match.getStatelessRuleMatches().stream()
                    .filter(StatelessRule::isStatefulCondition)
                    .collect(Collectors.toList());
            match.getStatelessRuleMatches().removeAll(statefulConditionMatch);
        });

        final List<Match> filteredMatches = matches.stream()
                .filter(match -> !match.getStatefulRuleMatches().isEmpty() || !match.getStatelessRuleMatches().isEmpty())
                .collect(Collectors.toList());
        log.info("Outputting match count {}", filteredMatches.size());

        return filteredMatches;
    }

    private void storeSubMatches(final Collection<Match> matches) {
        final List<Match> subMatches = new ArrayList<>();
        matches.forEach(match -> {
            final List<StatelessRule> statefulConditionMatch = match.getStatelessRuleMatches().stream()
                    .filter(StatelessRule::isStatefulCondition)
                    .collect(Collectors.toList());

            final Match submatch = Match.builder()
                    .statelessRuleMatches(statefulConditionMatch)
                    .dataType(match.getDataType())
                    .build();
            subMatches.add(submatch);
        });


        log.info("Storing {} matches", subMatches.size());
        subMatchAccessor.storeSubMatches(subMatches);
    }

    private boolean evaluateRule(final StatefulRule statefulRule, final Match match) {
        final ArrayList<Match> matches = (ArrayList<Match>) subMatchAccessor.getSubMatches(statefulRule, match.getDataType());
        log.info("Evaluating rule with id {} against {} matches", statefulRule.getId(), matches.size());
        matches.add(match);
        matches.sort(new MatchTimeComparator());

        final int currentMatchIndex = matches.indexOf(match);

        int startIndex = 0;
        while (startIndex <= currentMatchIndex) {
            final List<Match> subList = getSubList(statefulRule.getTimeframe(), matches, startIndex, currentMatchIndex);
            log.info("Start index {}, currentMatchIndex {}, subList size {}", startIndex, currentMatchIndex, subList.size());
            if (subList != null && statefulRule.testRuleCondition(subList)) {
                log.info("Condition matched!");
                return true;
            }

            startIndex++;
        }

        return false;
    }

    private List<Match> getSubList(final Duration window, final List<Match> matches, final int startIndex, final int currentMatchIndex) {
        final String timeFieldName = matches.get(startIndex).getDataType().getTimeFieldName();
        final long windowStart = (long) matches.get(startIndex).getDataType().getValue(timeFieldName);
        final long windowEnd = Instant.ofEpochMilli(windowStart).plus(window).toEpochMilli();

        final int endIndex = findIndexOfTime(matches, startIndex, windowEnd);

        if (currentMatchIndex > endIndex) {
            return null;
        }

        return matches.subList(startIndex, endIndex);
    }

    private int findIndexOfTime(final List<Match> matches, final int startIndex, final long epochMillis) {
        if (startIndex >= matches.size()) {
            return -1;
        }

        // TODO binary search
        for (int i = startIndex; i < matches.size(); i++) {
            final Match match = matches.get(i);
            final String timeFieldName = match.getDataType().getTimeFieldName();
            if ((long) match.getDataType().getValue(timeFieldName) > epochMillis) {
                return i - 1;
            }
        }

        return matches.size() - 1;
    }

    private static class MatchTimeComparator implements Comparator<Match> {
        public int compare(final Match m1, final Match m2) {
            final String timeField1 = m1.getDataType().getTimeFieldName();
            final Long epochMillis1 = (long) m1.getDataType().getValue(timeField1);

            final String timeField2 = m2.getDataType().getTimeFieldName();
            final Long epochMillis2 = (long) m2.getDataType().getValue(timeField2);

            return epochMillis1.compareTo(epochMillis2);
        }
    }
}
