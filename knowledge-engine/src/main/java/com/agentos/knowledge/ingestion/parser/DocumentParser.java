package com.agentos.knowledge.ingestion.parser;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;

public interface DocumentParser {
    DocumentType supportedType();
    Document parse(String rawContent, String fileName);
    String extractText(Document document);
}