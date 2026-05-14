package com.agentos.knowledge.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MemoryService {
    private static final Logger log = LoggerFactory.getLogger(MemoryService.class);
    private final MemoryStore semanticStore;
    private final MemoryStore episodicStore;
    private final MemoryStore proceduralStore;

    public MemoryService(MemoryStore semanticStore, MemoryStore episodicStore, MemoryStore proceduralStore) {
        this.semanticStore = semanticStore;
        this.episodicStore = episodicStore;
        this.proceduralStore = proceduralStore;
    }

    public void storeSemantic(String agentId, String content, double importance) {
        MemoryRecord record = new MemoryRecord(MemoryType.SEMANTIC, agentId, content);
        record.setImportance(importance);
        semanticStore.store(record);
    }

    public void storeEpisodic(String agentId, String content) {
        MemoryRecord record = new MemoryRecord(MemoryType.EPISODIC, agentId, content);
        record.setImportance(0.5);
        episodicStore.store(record);
    }

    public void storeProcedural(String agentId, String content) {
        MemoryRecord record = new MemoryRecord(MemoryType.PROCEDURAL, agentId, content);
        record.setImportance(0.8);
        proceduralStore.store(record);
    }

    public List<MemoryRecord> search(String query, MemoryType type, int topK) {
        MemoryStore store = switch (type) {
            case SEMANTIC -> semanticStore;
            case EPISODIC -> episodicStore;
            case PROCEDURAL -> proceduralStore;
        };
        return store.search(query, type, topK);
    }

    public Map<String, Object> recall(String agentId, String query) {
        List<MemoryRecord> semantic = semanticStore.search(query, MemoryType.SEMANTIC, 5);
        List<MemoryRecord> episodic = episodicStore.search(query, MemoryType.EPISODIC, 3);
        List<MemoryRecord> procedural = proceduralStore.search(query, MemoryType.PROCEDURAL, 3);
        return Map.of(
            "agent_id", agentId,
            "semantic", semantic.stream().map(MemoryRecord::getContent).toList(),
            "episodic", episodic.stream().map(MemoryRecord::getContent).toList(),
            "procedural", procedural.stream().map(MemoryRecord::getContent).toList()
        );
    }

    public void consolidate(String agentId) {
        semanticStore.consolidate(agentId);
        episodicStore.consolidate(agentId);
        proceduralStore.consolidate(agentId);
        log.info("Consolidated all memory types for agent {}", agentId);
    }
}