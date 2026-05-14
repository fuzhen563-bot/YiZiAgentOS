package com.agentos.knowledge.rag;

import com.agentos.knowledge.vectorization.Chunk;
import com.agentos.knowledge.vectorization.EmbeddingService;
import java.util.*;
import java.util.stream.Collectors;

public class SemanticRetriever implements RagRetriever {
    private final EmbeddingService embeddingService;
    private final List<Chunk> index = new ArrayList<>();

    public SemanticRetriever(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public void index(List<Chunk> chunks) {
        index.addAll(chunks);
    }

    @Override
    public String getName() { return "semantic"; }

    @Override
    public List<SearchResult> retrieve(String query, int topK) {
        float[] queryEmbedding = embeddingService.embed(query);
        return index.parallelStream()
            .filter(chunk -> chunk.getEmbedding() != null)
            .map(chunk -> new SearchResult(chunk, cosineSimilarity(queryEmbedding, chunk.getEmbedding())))
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .limit(topK)
            .collect(Collectors.toList());
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }
}