package com.agentos.knowledge.memory;

import java.time.LocalDateTime;
import java.util.Map;

public class MemoryRecord {
    private String id;
    private MemoryType type;
    private String agentId;
    private String content;
    private float[] embedding;
    private Map<String, Object> metadata;
    private double importance;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private int accessCount;

    public MemoryRecord() {}

    public MemoryRecord(MemoryType type, String agentId, String content) {
        this.type = type;
        this.agentId = agentId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
        this.accessCount = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public MemoryType getType() { return type; }
    public void setType(MemoryType type) { this.type = type; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public double getImportance() { return importance; }
    public void setImportance(double importance) { this.importance = importance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void touch() { this.lastAccessedAt = LocalDateTime.now(); this.accessCount++; }
    public int getAccessCount() { return accessCount; }
}