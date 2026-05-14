package com.agentos.knowledge.rag;

import java.util.*;
import java.util.stream.Collectors;

public class HybridRetriever implements RagRetriever {
    private final SemanticRetriever semanticRetriever;
    private final KeywordRetriever keywordRetriever;
    private final double semanticWeight;

    public HybridRetriever(SemanticRetriever semanticRetriever, KeywordRetriever keywordRetriever, double semanticWeight) {
        this.semanticRetriever = semanticRetriever;
        this.keywordRetriever = keywordRetriever;
        this.semanticWeight = semanticWeight;
    }

    @Override
    public String getName() { return "hybrid"; }

    @Override
    public List<SearchResult> retrieve(String query, int topK) {
        List<SearchResult> semanticResults = semanticRetriever.retrieve(query, topK);
        List<SearchResult> keywordResults = keywordRetriever.retrieve(query, topK);
        Map<String, ScoredResult> merged = new LinkedHashMap<>();
        for (SearchResult r : semanticResults) {
            if (r.getChunk() != null) {
                merged.put(r.getChunk().getId(), new ScoredResult(r, r.getScore() * semanticWeight));
            }
        }
        for (SearchResult r : keywordResults) {
            if (r.getChunk() != null) {
                merged.merge(r.getChunk().getId(), new ScoredResult(r, r.getScore() * (1 - semanticWeight)),
                    (a, b) -> { a.score += b.score; return a; });
            }
        }
        return merged.values().stream()
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .limit(topK)
            .map(sr -> {
                sr.result.setScore(sr.score);
                return sr.result;
            })
            .collect(Collectors.toList());
    }

    private static class ScoredResult {
        final SearchResult result;
        double score;
        ScoredResult(SearchResult result, double score) { this.result = result; this.score = score; }
    }
}