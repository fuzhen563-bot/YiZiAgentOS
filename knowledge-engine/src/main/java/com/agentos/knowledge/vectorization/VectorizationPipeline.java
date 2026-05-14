package com.agentos.knowledge.vectorization;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.etl.TextCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class VectorizationPipeline {
    private static final Logger log = LoggerFactory.getLogger(VectorizationPipeline.class);
    private final EmbeddingService embeddingService;
    private final TextCleaner textCleaner;
    private final int maxChunkSize;

    public VectorizationPipeline(EmbeddingService embeddingService, int maxChunkSize) {
        this.embeddingService = embeddingService;
        this.textCleaner = new TextCleaner();
        this.maxChunkSize = maxChunkSize;
    }

    public List<Chunk> process(Document document) {
        textCleaner.process(document);
        List<String> rawChunks = textCleaner.chunk(document, maxChunkSize);
        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < rawChunks.size(); i++) {
            Chunk chunk = new Chunk(document.getId(), rawChunks.get(i), i);
            float[] embedding = embeddingService.embed(rawChunks.get(i));
            chunk.setEmbedding(embedding);
            chunk.setTokenCount(rawChunks.get(i).length() / 4);
            chunks.add(chunk);
        }
        log.info("Vectorized document '{}' into {} chunks", document.getTitle(), chunks.size());
        return chunks;
    }

    public List<Chunk> processBatch(List<Document> documents) {
        List<Chunk> allChunks = new ArrayList<>();
        for (Document doc : documents) {
            allChunks.addAll(process(doc));
        }
        return allChunks;
    }
}