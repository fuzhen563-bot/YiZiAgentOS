package com.agentos.knowledge.memory;

import com.agentos.knowledge.vectorization.EmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryMemoryStore implements MemoryStore {
    private static final Logger log = LoggerFactory.getLogger(InMemoryMemoryStore.class);
    private final Map<String, MemoryRecord> store = new ConcurrentHashMap<>();
    private final EmbeddingService embeddingService;

    public InMemoryMemoryStore(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @Override
    public void store(MemoryRecord record) {
        record.setId(UUID.randomUUID().toString());
        record.setEmbedding(embeddingService.embed(record.getContent()));
        store.put(record.getId(), record);
        log.debug("Stored {} memory: {}", record.getType(), record.getId());
    }

    @Override
    public MemoryRecord retrieve(String id) {
        MemoryRecord record = store.get(id);
        if (record != null) record.touch();
        return record;
    }

    @Override
    public List<MemoryRecord> search(String query, MemoryType type, int topK) {
        float[] queryEmbedding = embeddingService.embed(query);
        return store.values().stream()
            .filter(r -> type == null || r.getType() == type)
            .map(r -> new AbstractMap.SimpleEntry<>(r, cosineSimilarity(queryEmbedding, r.getEmbedding())))
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(topK)
            .peek(e -> e.getKey().touch())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public List<MemoryRecord> getByAgent(String agentId, MemoryType type) {
        return store.values().stream()
            .filter(r -> r.getAgentId().equals(agentId) && (type == null || r.getType() == type))
            .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) { store.remove(id); }

    @Override
    public void consolidate(String agentId) {
        List<MemoryRecord> records = getByAgent(agentId, null);
        records.sort((a, b) -> Double.compare(b.getImportance(), a.getImportance()));
        int maxMemories = 100;
        if (records.size() > maxMemories) {
            records.subList(maxMemories, records.size()).forEach(r -> store.remove(r.getId()));
            log.info("Consolidated memories for agent {}: kept {} of {}", agentId, maxMemories, records.size());
        }
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