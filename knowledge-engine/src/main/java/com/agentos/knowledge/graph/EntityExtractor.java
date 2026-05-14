package com.agentos.knowledge.graph;

import com.agentos.knowledge.ingestion.Document;

public interface EntityExtractor {
    void extract(Document document, KnowledgeGraph graph);
}