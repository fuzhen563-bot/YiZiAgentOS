package com.agentos.knowledge.config;

import com.agentos.knowledge.fabric.ContextFabric;
import com.agentos.knowledge.graph.GraphService;
import com.agentos.knowledge.graph.RelationExtractor;
import com.agentos.knowledge.graph.SimpleEntityExtractor;
import com.agentos.knowledge.ingestion.connector.FileSystemConnector;
import com.agentos.knowledge.ingestion.etl.EtlPipeline;
import com.agentos.knowledge.ingestion.parser.MarkdownParser;
import com.agentos.knowledge.ingestion.parser.PdfParser;
import com.agentos.knowledge.memory.InMemoryMemoryStore;
import com.agentos.knowledge.memory.MemoryService;
import com.agentos.knowledge.rag.KeywordRetriever;
import com.agentos.knowledge.rag.RagService;
import com.agentos.knowledge.rag.SemanticRetriever;
import com.agentos.knowledge.vectorization.OpenAiEmbeddingService;
import com.agentos.knowledge.vectorization.VectorizationPipeline;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KnowledgeEngineConfig {

    @Value("${OPENAI_API_KEY:}") private String apiKey;

    @Bean
    public OpenAiEmbeddingService embeddingService() {
        return new OpenAiEmbeddingService(apiKey.isBlank() ? "sk-dummy" : apiKey, "text-embedding-3-small");
    }

    @Bean
    public VectorizationPipeline vectorizationPipeline(OpenAiEmbeddingService embeddingService) {
        return new VectorizationPipeline(embeddingService, 1000);
    }

    @Bean
    public SemanticRetriever semanticRetriever(OpenAiEmbeddingService embeddingService) {
        return new SemanticRetriever(embeddingService);
    }

    @Bean
    public KeywordRetriever keywordRetriever() {
        return new KeywordRetriever();
    }

    @Bean
    public RagService ragService(VectorizationPipeline vp, SemanticRetriever sr, KeywordRetriever kr) {
        return new RagService(vp, sr, kr);
    }

    @Bean
    public SimpleEntityExtractor entityExtractor() {
        return new SimpleEntityExtractor();
    }

    @Bean
    public RelationExtractor relationExtractor() {
        return new RelationExtractor();
    }

    @Bean
    public GraphService graphService(SimpleEntityExtractor ee, RelationExtractor re) {
        return new GraphService(ee, re);
    }

    @Bean
    @Qualifier("semanticStore")
    public InMemoryMemoryStore semanticStore(OpenAiEmbeddingService es) { return new InMemoryMemoryStore(es); }

    @Bean
    @Qualifier("episodicStore")
    public InMemoryMemoryStore episodicStore(OpenAiEmbeddingService es) { return new InMemoryMemoryStore(es); }

    @Bean
    @Qualifier("proceduralStore")
    public InMemoryMemoryStore proceduralStore(OpenAiEmbeddingService es) { return new InMemoryMemoryStore(es); }

    @Bean
    public MemoryService memoryService(
            @Qualifier("semanticStore") InMemoryMemoryStore s,
            @Qualifier("episodicStore") InMemoryMemoryStore e,
            @Qualifier("proceduralStore") InMemoryMemoryStore p) {
        return new MemoryService(s, e, p);
    }

    @Bean
    public ContextFabric contextFabric(RagService rs, GraphService gs, MemoryService ms) {
        return new ContextFabric(rs, gs, ms);
    }

    @Bean
    public EtlPipeline etlPipeline() {
        EtlPipeline pipeline = new EtlPipeline();
        pipeline.registerConnector(new FileSystemConnector());
        pipeline.registerParser(new MarkdownParser());
        pipeline.registerParser(new PdfParser());
        return pipeline;
    }
}