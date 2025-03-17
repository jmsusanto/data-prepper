package org.opensearch.dataprepper.plugins.processor.evaluator;

import org.opensearch.dataprepper.plugins.processor.model.datatypes.DataType;
import org.opensearch.dataprepper.plugins.processor.rules.StatelessRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AhoCorasickTrie {
    private static class TrieNode {
        Map<Character, TrieNode> children;
        TrieNode failureLink;
        Set<StatelessRule> rules;

        TrieNode() {
            children = new HashMap<>();
            failureLink = null;
            rules = new HashSet<>();
        }
    }

    private final TrieNode root;

    public AhoCorasickTrie(List<StatelessRule> rules) {
        root = new TrieNode();

        for (StatelessRule rule : rules) {
            List<String> keywords = rule.getKeywords();
            for (String keyword : keywords) {
                insertPattern(keyword, rule);
            }
        }
        buildFailureLinks();
    }

    private void insertPattern(String pattern, StatelessRule rule) {
        TrieNode current = root;

        for (char c : pattern.toLowerCase().toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }

        current.rules.add(rule);
    }

    private void buildFailureLinks() {
        Queue<TrieNode> queue = new LinkedList<>();

        for (TrieNode child : root.children.values()) {
            child.failureLink = root;
            queue.add(child);
        }

        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();

            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();
                queue.add(child);

                TrieNode failureNode = current.failureLink;
                while (failureNode != null && !failureNode.children.containsKey(c)) {
                    failureNode = failureNode.failureLink;
                }

                child.failureLink = (failureNode == null) ? root : failureNode.children.get(c);
                child.rules.addAll(child.failureLink.rules);
            }
        }
    }

    public List<StatelessRule> match(DataType log) {
        Set<StatelessRule> matchedRules = new HashSet<>();
        String text = extractText(log).toLowerCase();

        TrieNode current = root;

        for (char c : text.toCharArray()) {
            while (current != root && !current.children.containsKey(c)) {
                current = current.failureLink;
            }

            current = current.children.getOrDefault(c, root);
            matchedRules.addAll(current.rules);
        }

        return new ArrayList<>(matchedRules);
    }

    private String extractText(DataType log) {
        return log.toString();
    }
}