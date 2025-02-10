package org.opensearch.dataprepper.plugins.processor.model.matches;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
import org.opensearch.dataprepper.plugins.processor.rules.StatefulRule;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match implements Serializable {
    private DataType dataType;
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Rule> rules;

    private List<StatefulRule> statefulRuleMatches;
    private List<StatelessRule> statelessRuleMatches;
}
