package org.opensearch.dataprepper.plugins.processor.evaluator;

import lombok.extern.log4j.Log4j2;
import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.rules.Rule;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
public class AhoCorasickPrefilter implements RulePrefilter {
    private AhoCorasickTrie trie;
    private List<StatelessRule> compiledRules;

    @Override
    public void compilePrefilter(List<StatelessRule> rules) {
        if (!rules.equals(compiledRules)) {
            trie = new AhoCorasickTrie(rules);
            compiledRules = new ArrayList<>(rules);
            log.debug("Built trie for {} rules", rules.size());
        }
    }

    @Override
    public List<StatelessRule> filterRules(DataType log) {
        if (trie == null) {
            throw new IllegalStateException("Prefilter not compiled");
        }

        return trie.match(log);
    }
}
