package com.agentos.knowledge.graph;

import com.agentos.knowledge.ingestion.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GraphService {
    private static final Logger log = LoggerFactory.getLogger(GraphService.class);
    private final KnowledgeGraph graph = new KnowledgeGraph();
    private final EntityExtractor entityExtractor;
    private final RelationExtractor relationExtractor;

    public GraphService(EntityExtractor entityExtractor, RelationExtractor relationExtractor) {
        this.entityExtractor = entityExtractor;
        this.relationExtractor = relationExtractor;
    }

    public void processDocument(Document document) {
        entityExtractor.extract(document, graph);
        relationExtractor.extract(document, graph);
        log.info("Graph updated with document '{}'", document.getTitle());
    }

    public KnowledgeGraph getGraph() { return graph; }

    public Map<String, Object> queryEntity(String entityName) {
        KnowledgeGraph.Entity entity = graph.getEntities().values().stream()
            .filter(e -> e.getName().equalsIgnoreCase(entityName))
            .findFirst().orElse(null);
        if (entity == null) return Map.of("error", "Entity not found");
        return Map.of(
            "entity", Map.of("id", entity.getId(), "name", entity.getName(), "type", entity.getType()),
            "relations", graph.getRelations(entity.getId()).stream().map(r -> {
                KnowledgeGraph.Entity src = graph.getEntity(r.getSourceId());
                KnowledgeGraph.Entity tgt = graph.getEntity(r.getTargetId());
                return Map.of(
                    "source", src != null ? src.getName() : "unknown",
                    "target", tgt != null ? tgt.getName() : "unknown",
                    "type", r.getType()
                );
            }).toList(),
            "neighbors", graph.getNeighbors(entity.getId(), 2)
        );
    }

    public Map<String, Object> getGraphSummary() {
        long relationCount = 0;
        for (KnowledgeGraph.Entity e : graph.getEntities().values()) {
            relationCount += graph.getRelations(e.getId()).size();
        }
        return Map.of(
            "total_entities", graph.getEntities().size(),
            "total_relations", relationCount,
            "entity_types", graph.getEntityTypeDistribution()
        );
    }
}