package org.opensearch.dataprepper.plugins.processor.rules;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.opensearch.dataprepper.plugins.processor.model.matches.Match;

import java.time.Duration;
import java.util.List;

@SuperBuilder
@Getter
public class StatefulRule extends Rule<Match, List<Match>> {
    private List<String> filterFields;
    private Duration timeframe;
}
