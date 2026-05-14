package com.agentos.knowledge.rag;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.vectorization.VectorizationPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RagService {
    private static final Logger log = LoggerFactory.getLogger(RagService.class);
    private final VectorizationPipeline vectorizationPipeline;
    private final SemanticRetriever semanticRetriever;
    private final KeywordRetriever keywordRetriever;
    private final HybridRetriever hybridRetriever;

    public RagService(VectorizationPipeline vectorizationPipeline,
                      SemanticRetriever semanticRetriever,
                      KeywordRetriever keywordRetriever) {
        this.vectorizationPipeline = vectorizationPipeline;
        this.semanticRetriever = semanticRetriever;
        this.keywordRetriever = keywordRetriever;
        this.hybridRetriever = new HybridRetriever(semanticRetriever, keywordRetriever, 0.7);
    }

    public void ingest(Document document) {
        var chunks = vectorizationPipeline.process(document);
        semanticRetriever.index(chunks);
        for (var chunk : chunks) {
            keywordRetriever.index(chunk.getId(), chunk.getContent());
        }
        log.info("Ingested document '{}' into RAG index", document.getTitle());
    }

    public void ingestBatch(List<Document> documents) {
        for (Document doc : documents) ingest(doc);
    }

    public String query(String query, int topK, String strategy) {
        RagRetriever retriever = switch (strategy) {
            case "semantic" -> semanticRetriever;
            case "keyword" -> keywordRetriever;
            default -> hybridRetriever;
        };
        List<SearchResult> results = retriever.retrieve(query, topK);
        return results.stream()
            .map(r -> String.format("[Score: %.4f] %s", r.getScore(),
                r.getChunk() != null ? r.getChunk().getContent() : ""))
            .collect(Collectors.joining("\n\n"));
    }

    public Map<String, Object> retrieveWithMetadata(String query, int topK) {
        List<SearchResult> results = hybridRetriever.retrieve(query, topK);
        return Map.of(
            "query", query,
            "results", results.stream().map(r -> Map.of(
                "content", r.getChunk() != null ? r.getChunk().getContent() : "",
                "score", r.getScore(),
                "source", r.getSource() != null ? r.getSource() : "unknown"
            )).toList()
        );
    }
}