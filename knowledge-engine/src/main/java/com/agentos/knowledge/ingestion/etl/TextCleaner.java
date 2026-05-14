package com.agentos.knowledge.ingestion.etl;

import com.agentos.knowledge.ingestion.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TextCleaner implements EtlProcessor {
    private static final Logger log = LoggerFactory.getLogger(TextCleaner.class);

    @Override
    public void process(Document document) {
        String content = document.getContent();
        if (content == null) {
            document.setContent("");
            return;
        }
        content = content.replaceAll("\\s+", " ");
        content = content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
        content = content.strip();
        document.setContent(content);
        log.debug("Cleaned document: {}", document.getTitle());
    }

    public List<String> chunk(Document document, int maxChunkSize) {
        String text = document.getContent();
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());
            if (end < text.length()) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int split = Math.max(lastPeriod, lastNewline);
                if (split > start) end = split + 1;
            }
            chunks.add(text.substring(start, end).strip());
            start = end;
        }
        return chunks;
    }
}