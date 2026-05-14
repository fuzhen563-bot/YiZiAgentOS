package com.agentos.knowledge.ingestion.connector;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;
import java.util.List;

public class CrmConnector implements Connector {
    @Override
    public String getName() { return "crm"; }

    @Override
    public DocumentType supportedType() { return DocumentType.CRM; }

    @Override
    public List<Document> fetch(String apiEndpoint) {
        return List.of();
    }

    @Override
    public boolean supportsIncremental() { return true; }

    @Override
    public List<Document> fetchIncremental(String apiEndpoint, String lastSyncToken) {
        return fetch(apiEndpoint);
    }
}