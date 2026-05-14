package com.agentos.knowledge.ingestion.parser;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;

public class MarkdownParser implements DocumentParser {
    @Override
    public DocumentType supportedType() { return DocumentType.MARKDOWN; }

    @Override
    public Document parse(String rawContent, String fileName) {
        String title = fileName.replaceAll("\\.md$", "");
        String[] lines = rawContent.split("\n");
        for (String line : lines) {
            if (line.startsWith("# ")) {
                title = line.substring(2).trim();
                break;
            }
        }
        return new Document(title, rawContent, DocumentType.MARKDOWN, "filesystem");
    }

    @Override
    public String extractText(Document document) {
        String content = document.getContent();
        content = content.replaceAll("!\\[.*?\\]\\(.*?\\)", "");
        content = content.replaceAll("\\[([^]]+)\\]\\([^)]+\\)", "$1");
        content = content.replaceAll("#+\\s*", "");
        content = content.replaceAll("```[\\s\\S]*?```", "");
        content = content.replaceAll("\\|.*?\\|", "");
        return content.strip();
    }
}