package com.agentos.knowledge.graph;

import com.agentos.knowledge.ingestion.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class RelationExtractor {
    private static final Logger log = LoggerFactory.getLogger(RelationExtractor.class);

    public List<KnowledgeGraph.Relation> extract(Document document, KnowledgeGraph graph) {
        List<KnowledgeGraph.Relation> found = new ArrayList<>();
        List<KnowledgeGraph.Entity> entities = new ArrayList<>(graph.getEntities().values());
        String content = document.getContent().toLowerCase();
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                var e1 = entities.get(i);
                var e2 = entities.get(j);
                if (e1.getName() == null || e2.getName() == null) continue;
                if (content.contains(e1.getName().toLowerCase()) &&
                    content.contains(e2.getName().toLowerCase())) {
                    String type = inferRelation(e1.getType(), e2.getType());
                    graph.addRelation(e1.getId(), e2.getId(), type);
                    found.add(new KnowledgeGraph.Relation(e1.getId(), e2.getId(), type));
                }
            }
        }
        log.info("Extracted {} relations from document '{}'", found.size(), document.getTitle());
        return found;
    }

    private String inferRelation(String type1, String type2) {
        if (type1.equals("person") && type2.equals("organization")) return "works_at";
        if (type1.equals("organization") && type2.equals("person")) return "employs";
        if (type1.equals("document") && type2.equals("person")) return "mentions";
        if (type1.equals("document") && type2.equals("organization")) return "references";
        return "related_to";
    }
}