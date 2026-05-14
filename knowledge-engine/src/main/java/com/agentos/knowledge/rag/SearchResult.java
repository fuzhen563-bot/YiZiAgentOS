package com.agentos.knowledge.rag;

import com.agentos.knowledge.vectorization.Chunk;

import java.util.List;

public class SearchResult {
    private Chunk chunk;
    private double score;
    private String documentTitle;
    private String source;

    public SearchResult(Chunk chunk, double score) {
        this.chunk = chunk;
        this.score = score;
    }

    public Chunk getChunk() { return chunk; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}