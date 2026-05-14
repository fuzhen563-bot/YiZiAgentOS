package com.agentos.knowledge.ingestion.etl;

import com.agentos.knowledge.ingestion.Document;
import com.agentos.knowledge.ingestion.DocumentType;
import com.agentos.knowledge.ingestion.connector.Connector;
import com.agentos.knowledge.ingestion.parser.DocumentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EtlPipeline {
    private static final Logger log = LoggerFactory.getLogger(EtlPipeline.class);
    private final List<Connector> connectors = new ArrayList<>();
    private final Map<DocumentType, DocumentParser> parsers = new ConcurrentHashMap<>();
    private final List<EtlProcessor> processors = new ArrayList<>();
    private final Map<String, String> connectorConfigs = new HashMap<>();

    public void registerConnector(Connector connector) {
        connectors.add(connector);
        log.info("Registered connector: {}", connector.getName());
    }

    public void registerConnector(Connector connector, String config) {
        connectors.add(connector);
        connectorConfigs.put(connector.getName(), config);
        log.info("Registered connector: {} with config", connector.getName());
    }

    public void registerParser(DocumentParser parser) {
        parsers.put(parser.supportedType(), parser);
        log.info("Registered parser for: {}", parser.supportedType());
    }

    public void registerProcessor(EtlProcessor processor) {
        processors.add(processor);
    }

    public List<Document> runFullSync() {
        List<Document> allDocs = new ArrayList<>();
        for (Connector connector : connectors) {
            try {
                String config = connectorConfigs.getOrDefault(connector.getName(), "");
                List<Document> docs = connector.fetch(config);
                for (Document doc : docs) {
                    processDocument(doc);
                }
                allDocs.addAll(docs);
                log.info("Connector {} fetched {} documents", connector.getName(), docs.size());
            } catch (Exception e) {
                log.error("Connector {} failed: {}", connector.getName(), e.getMessage());
            }
        }
        return allDocs;
    }

    public List<Document> runIncrementalSync(Map<String, String> syncTokens) {
        List<Document> allDocs = new ArrayList<>();
        for (Connector connector : connectors) {
            if (connector.supportsIncremental()) {
                try {
                    String config = connectorConfigs.getOrDefault(connector.getName(), "");
                    String token = syncTokens.getOrDefault(connector.getName(), "");
                    List<Document> docs = connector.fetchIncremental(config, token);
                    for (Document doc : docs) {
                        processDocument(doc);
                    }
                    allDocs.addAll(docs);
                } catch (Exception e) {
                    log.error("Incremental sync failed for {}: {}", connector.getName(), e.getMessage());
                }
            }
        }
        return allDocs;
    }

    private void processDocument(Document doc) {
        if (doc.getType() == null) {
            log.warn("Document {} has null type, skipping", doc.getTitle());
            return;
        }
        DocumentParser parser = parsers.get(doc.getType());
        if (parser != null) {
            doc.setContent(parser.extractText(doc));
        }
        for (EtlProcessor processor : processors) {
            try {
                processor.process(doc);
            } catch (Exception e) {
                log.warn("ETL processor failed: {}", e.getMessage());
            }
        }
    }
}