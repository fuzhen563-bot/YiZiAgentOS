package com.agentos.knowledge.ingestion.parser;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;

public class PdfParser implements DocumentParser {
    @Override
    public DocumentType supportedType() { return DocumentType.PDF; }

    @Override
    public Document parse(String rawContent, String fileName) {
        String title = fileName.replaceAll("\\.pdf$", "");
        return new Document(title, rawContent, DocumentType.PDF, "filesystem");
    }

    @Override
    public String extractText(Document document) {
        return document.getContent();
    }
}