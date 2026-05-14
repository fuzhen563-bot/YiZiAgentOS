package com.agentos.knowledge.graph;

import com.agentos.knowledge.ingestion.Document;
import java.util.UUID;
import java.util.regex.Pattern;

public class SimpleEntityExtractor implements EntityExtractor {
    private static final Pattern PERSON_PATTERN = Pattern.compile("@([A-Z][a-z]+ [A-Z][a-z]+)");
    private static final Pattern COMPANY_PATTERN = Pattern.compile("([A-Z][a-zA-Z]+(?:Inc|Corp|Ltd|LLC|Co\\.))");
    private static final Pattern URL_PATTERN = Pattern.compile("https?://([^/\\s]+)");

    @Override
    public void extract(Document document, KnowledgeGraph graph) {
        String content = document.getContent();
        var personMatcher = PERSON_PATTERN.matcher(content);
        while (personMatcher.find()) {
            String name = personMatcher.group(1);
            String id = UUID.nameUUIDFromBytes(name.getBytes()).toString();
            graph.addEntity(id, name, "person");
        }
        var companyMatcher = COMPANY_PATTERN.matcher(content);
        while (companyMatcher.find()) {
            String name = companyMatcher.group(1);
            String id = UUID.nameUUIDFromBytes(name.getBytes()).toString();
            graph.addEntity(id, name, "organization");
        }
        var urlMatcher = URL_PATTERN.matcher(content);
        while (urlMatcher.find()) {
            String domain = urlMatcher.group(1);
            String id = UUID.nameUUIDFromBytes(domain.getBytes()).toString();
            graph.addEntity(id, domain, "website");
        }
        String docId = document.getId();
        if (docId == null) {
            docId = UUID.randomUUID().toString();
            document.setId(docId);
        }
        String docEntityId = UUID.nameUUIDFromBytes(docId.getBytes()).toString();
        graph.addEntity(docEntityId, document.getTitle(), "document");
        graph.addRelation(docEntityId, docEntityId, "contains");
    }
}