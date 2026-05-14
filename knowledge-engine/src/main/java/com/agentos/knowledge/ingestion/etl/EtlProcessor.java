package com.agentos.knowledge.ingestion.etl;

import com.agentos.knowledge.ingestion.Document;

public interface EtlProcessor {
    void process(Document document);
}