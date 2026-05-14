package com.agentos.knowledge.rag;

import com.agentos.knowledge.vectorization.Chunk;
import java.util.List;

public interface RagRetriever {
    List<SearchResult> retrieve(String query, int topK);
    String getName();
}