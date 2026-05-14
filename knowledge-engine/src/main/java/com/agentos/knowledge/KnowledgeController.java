package com.agentos.knowledge;

import com.agentos.knowledge.fabric.ContextFabric;
import com.agentos.knowledge.graph.GraphService;
import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;
import com.agentos.knowledge.ingestion.etl.EtlPipeline;
import com.agentos.knowledge.memory.MemoryService;
import com.agentos.knowledge.rag.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    @Autowired private EtlPipeline etlPipeline;
    @Autowired private RagService ragService;
    @Autowired private GraphService graphService;
    @Autowired private MemoryService memoryService;
    @Autowired private ContextFabric contextFabric;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingest(@RequestBody IngestRequest request) {
        Document doc = new Document(request.title, request.content, request.type, request.source);
        ragService.ingest(doc);
        graphService.processDocument(doc);
        return ResponseEntity.ok(Map.of("status", "ingested", "title", request.title));
    }

    @PostMapping("/ingest/sync")
    public ResponseEntity<?> sync() {
        var docs = etlPipeline.runFullSync();
        ragService.ingestBatch(docs);
        docs.forEach(graphService::processDocument);
        return ResponseEntity.ok(Map.of("status", "synced", "count", docs.size()));
    }

    @GetMapping("/query")
    public ResponseEntity<?> query(@RequestParam String q, @RequestParam(defaultValue = "5") int topK) {
        return ResponseEntity.ok(ragService.retrieveWithMetadata(q, topK));
    }

    @GetMapping("/graph/entity")
    public ResponseEntity<?> graphEntity(@RequestParam String name) {
        return ResponseEntity.ok(graphService.queryEntity(name));
    }

    @GetMapping("/graph/summary")
    public ResponseEntity<?> graphSummary() {
        return ResponseEntity.ok(graphService.getGraphSummary());
    }

    @PostMapping("/memory")
    public ResponseEntity<?> storeMemory(@RequestBody MemoryRequest request) {
        switch (request.type) {
            case "semantic" -> memoryService.storeSemantic(request.agentId, request.content, request.importance);
            case "episodic" -> memoryService.storeEpisodic(request.agentId, request.content);
            case "procedural" -> memoryService.storeProcedural(request.agentId, request.content);
        }
        return ResponseEntity.ok(Map.of("status", "stored"));
    }

    @GetMapping("/memory/recall")
    public ResponseEntity<?> recall(@RequestParam String agentId, @RequestParam String q) {
        return ResponseEntity.ok(memoryService.recall(agentId, q));
    }

    @PostMapping("/context")
    public ResponseEntity<?> context(@RequestBody ContextRequest request) {
        return ResponseEntity.ok(contextFabric.buildContext(request.query, request.agentId, request.topK));
    }

    @PostMapping("/context/prompt")
    public ResponseEntity<?> contextPrompt(@RequestBody ContextRequest request) {
        return ResponseEntity.ok(Map.of("prompt", contextFabric.buildPrompt(request.query, request.agentId)));
    }

    public static class IngestRequest {
        public String title; public String content; public DocumentType type; public String source;
    }

    public static class MemoryRequest {
        public String agentId; public String content; public String type; public double importance;
    }

    public static class ContextRequest {
        public String query; public String agentId; public int topK = 5;
    }
}