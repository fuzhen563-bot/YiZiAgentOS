package com.agentos.knowledge.vectorization;

public class Chunk {
    private String id;
    private String documentId;
    private String content;
    private int index;
    private float[] embedding;
    private int tokenCount;

    public Chunk() {}

    public Chunk(String documentId, String content, int index) {
        this.documentId = documentId;
        this.content = content;
        this.index = index;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public float[] getEmbedding() { return embedding; }
    public void setEmbedding(float[] embedding) { this.embedding = embedding; }
    public int getTokenCount() { return tokenCount; }
    public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }
}