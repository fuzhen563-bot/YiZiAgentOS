package com.agentos.knowledge.fabric;

import com.agentos.knowledge.graph.GraphService;
import com.agentos.knowledge.memory.MemoryService;
import com.agentos.knowledge.rag.RagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ContextFabric {
    private static final Logger log = LoggerFactory.getLogger(ContextFabric.class);
    private final RagService ragService;
    private final GraphService graphService;
    private final MemoryService memoryService;

    public ContextFabric(RagService ragService, GraphService graphService, MemoryService memoryService) {
        this.ragService = ragService;
        this.graphService = graphService;
        this.memoryService = memoryService;
    }

    public Map<String, Object> buildContext(String query, String agentId, int topK) {
        long start = System.currentTimeMillis();
        Map<String, Object> ragResults = ragService.retrieveWithMetadata(query, topK);
        Map<String, Object> memoryResults = memoryService.recall(agentId, query);
        Map<String, Object> graphResults = graphService.queryEntity(query);
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("query", query);
        context.put("agent_id", agentId);
        context.put("knowledge", ragResults);
        context.put("memory", memoryResults);
        context.put("graph", graphResults);
        context.put("latency_ms", System.currentTimeMillis() - start);
        log.info("Context built for query '{}' in {}ms", query, context.get("latency_ms"));
        return context;
    }

    public String buildPrompt(String query, String agentId) {
        Map<String, Object> context = buildContext(query, agentId, 5);
        StringBuilder sb = new StringBuilder();
        sb.append("## Context\n\n");
        Map<String, Object> knowledge = (Map<String, Object>) context.get("knowledge");
        if (knowledge.containsKey("results")) {
            sb.append("### Relevant Knowledge\n");
            List<Map<String, Object>> results = (List<Map<String, Object>>) knowledge.get("results");
            for (Map<String, Object> r : results) {
                sb.append("- [").append(r.get("score")).append("] ").append(r.get("content")).append("\n");
            }
        }
        Map<String, Object> memory = (Map<String, Object>) context.get("memory");
        sb.append("\n### Memory\n");
        for (String key : List.of("semantic", "episodic", "procedural")) {
            List<String> items = (List<String>) memory.get(key);
            if (items != null && !items.isEmpty()) {
                sb.append("**").append(key).append("**: ").append(String.join("; ", items)).append("\n");
            }
        }
        Map<String, Object> graph = (Map<String, Object>) context.get("graph");
        if (graph.containsKey("entity")) {
            sb.append("\n### Knowledge Graph\n");
            sb.append("Entity: ").append(((Map<String, Object>) graph.get("entity")).get("name")).append("\n");
        }
        sb.append("\n## Question\n").append(query).append("\n");
        return sb.toString();
    }
}