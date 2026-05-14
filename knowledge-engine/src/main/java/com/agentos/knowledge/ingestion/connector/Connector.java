package com.agentos.knowledge.ingestion.connector;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;
import java.util.List;

public interface Connector {
    String getName();
    DocumentType supportedType();
    List<Document> fetch(String config);
    boolean supportsIncremental();
    List<Document> fetchIncremental(String config, String lastSyncToken);
}