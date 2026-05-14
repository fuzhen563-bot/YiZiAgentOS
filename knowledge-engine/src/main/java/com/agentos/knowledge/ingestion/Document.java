package com.agentos.knowledge.ingestion;

import java.time.LocalDateTime;
import java.util.List;

public class Document {
    private String id;
    private String title;
    private String content;
    private DocumentType type;
    private String source;
    private String filePath;
    private long fileSize;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public Document() {}

    public Document(String title, String content, DocumentType type, String source) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.source = source;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}