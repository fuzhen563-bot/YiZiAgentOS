package com.agentos.knowledge.memory;

import java.util.List;

public interface MemoryStore {
    void store(MemoryRecord record);
    MemoryRecord retrieve(String id);
    List<MemoryRecord> search(String query, MemoryType type, int topK);
    List<MemoryRecord> getByAgent(String agentId, MemoryType type);
    void delete(String id);
    void consolidate(String agentId);
}