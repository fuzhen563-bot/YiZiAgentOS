package com.agentos.knowledge.rag;

import java.util.*;
import java.util.stream.Collectors;

public class KeywordRetriever implements RagRetriever {
    private final Map<String, List<String>> invertedIndex = new HashMap<>();

    public void index(String chunkId, String content) {
        String[] tokens = content.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (token.length() > 2) {
                invertedIndex.computeIfAbsent(token, k -> new ArrayList<>()).add(chunkId);
            }
        }
    }

    @Override
    public String getName() { return "keyword"; }

    @Override
    public List<SearchResult> retrieve(String query, int topK) {
        String[] tokens = query.toLowerCase().split("\\W+");
        Map<String, Integer> scores = new HashMap<>();
        for (String token : tokens) {
            List<String> chunkIds = invertedIndex.getOrDefault(token, List.of());
            for (String id : chunkIds) {
                scores.merge(id, 1, Integer::sum);
            }
        }
        return scores.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(topK)
            .map(e -> new SearchResult(null, e.getValue()))
            .collect(Collectors.toList());
    }
}