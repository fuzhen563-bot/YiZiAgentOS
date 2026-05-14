package com.agentos.knowledge.ingestion.connector;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileSystemConnector implements Connector {
    @Override
    public String getName() { return "filesystem"; }

    @Override
    public DocumentType supportedType() { return DocumentType.MARKDOWN; }

    @Override
    public List<Document> fetch(String basePath) {
        List<Document> docs = new ArrayList<>();
        try {
            Files.walk(Paths.get(basePath))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".md") || p.toString().endsWith(".pdf"))
                .forEach(path -> {
                    try {
                        Document doc = new Document(
                            path.getFileName().toString(),
                            Files.readString(path),
                            path.toString().endsWith(".md") ? DocumentType.MARKDOWN : DocumentType.PDF,
                            "filesystem"
                        );
                        doc.setFilePath(path.toString());
                        doc.setFileSize(Files.size(path));
                        docs.add(doc);
                    } catch (Exception ignored) {}
                });
        } catch (Exception ignored) {}
        return docs;
    }

    @Override
    public boolean supportsIncremental() { return true; }

    @Override
    public List<Document> fetchIncremental(String basePath, String lastSyncToken) {
        return fetch(basePath);
    }
}