# AgentOS Phase 4: Enterprise Knowledge Layer

> 构建企业知识大脑，支持多源数据向量化、RAG 检索和知识图谱。

---

## 架构

```
┌──────────┐   ┌──────────────────────────────────────────────────────────────┐
│  Client  │──▶│                    Knowledge Engine                          │
└──────────┘   │                                                              │
               │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐ │
               │  │Ingestion │─▶│Vectorize │─▶│   RAG    │─▶│  Context     │ │
               │  │(Connector│  │(Embed)   │  │(Semantic │  │  Fabric      │ │
               │  │ + Parser)│  │ + Chunk) │  │ + Hybrid)│  │              │ │
               │  └──────────┘  └──────────┘  └──────────┘  └──────────────┘ │
               │                                                              │
               │  ┌──────────┐  ┌──────────┐  ┌──────────┐                   │
               │  │Knowledge │  │  Memory  │  │   ETL    │                   │
               │  │  Graph   │  │(Semantic │  │ Pipeline │                   │
               │  │(Entities │  │ Episodic │  │(Sync +   │                   │
               │  │ Relations│  │Procedural│  │ Clean)   │                   │
               │  └──────────┘  └──────────┘  └──────────┘                   │
               └──────────────────────────────────────────────────────────────┘
```

## 目录结构

```
knowledge-engine/
├── pom.xml
└── src/main/java/com/agentos/knowledge/
    ├── KnowledgeEngineApplication.java
    ├── KnowledgeController.java
    ├── ingestion/
    │   ├── Document.java, DocumentType.java
    │   ├── connector/  Connector, FileSystemConnector, CrmConnector, TicketConnector
    │   ├── parser/     DocumentParser, MarkdownParser, PdfParser
    │   └── etl/        EtlPipeline, EtlProcessor, TextCleaner
    ├── vectorization/
    │   ├── Chunk.java, EmbeddingService.java, OpenAiEmbeddingService.java
    │   └── VectorizationPipeline.java
    ├── rag/
    │   ├── RagRetriever, SemanticRetriever, KeywordRetriever, HybridRetriever
    │   ├── SearchResult.java
    │   └── RagService.java
    ├── graph/
    │   ├── KnowledgeGraph.java, EntityExtractor, SimpleEntityExtractor
    │   ├── RelationExtractor.java
    │   └── GraphService.java
    ├── memory/
    │   ├── MemoryType, MemoryRecord, MemoryStore, InMemoryMemoryStore
    │   └── MemoryService.java
    ├── fabric/
    │   └── ContextFabric.java
    └── config/
        └── KnowledgeEngineConfig.java
```

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **知识导入** | ✅ | 文件系统/CRM/Ticket 连接器 + Markdown/PDF 解析器 |
| **ETL 流水线** | ✅ | 全量同步 + 增量同步 + 文本清洗 + 分块 |
| **向量化** | ✅ | OpenAI Embedding + 分块流水线 |
| **RAG 检索** | ✅ | 语义检索 + 关键词检索 + 混合检索 |
| **知识图谱** | ✅ | 实体抽取 + 关系抽取 + 邻域查询 |
| **记忆系统** | ✅ | 语义/情景/程序性 三种记忆 + 合并 |
| **上下文 Fabric** | ✅ | 统一上下文构建 + Prompt 组装 |
| **REST API** | ✅ | /ingest, /query, /graph, /memory, /context |
| **K8s 部署** | ✅ | Deployment + Service |

## API 接口

### POST /api/v1/knowledge/ingest
```json
{"title": "doc", "content": "...", "type": "MARKDOWN", "source": "filesystem"}
```

### GET /api/v1/knowledge/query?q=question&topK=5
```json
{"query": "question", "results": [{"content": "...", "score": 0.95}]}
```

### GET /api/v1/knowledge/graph/entity?name=CompanyName
```json
{"entity": {"id": "...", "name": "CompanyName", "type": "organization"}, "relations": [...]}
```

### POST /api/v1/knowledge/memory
```json
{"agentId": "agt_xxx", "content": "...", "type": "semantic", "importance": 0.9}
```

### GET /api/v1/knowledge/memory/recall?agentId=agt_xxx&q=question
```json
{"agent_id": "agt_xxx", "semantic": [...], "episodic": [...], "procedural": [...]}
```

### POST /api/v1/knowledge/context
```json
{"query": "question", "agentId": "agt_xxx", "topK": 5}
```

## 检索策略

| 策略 | 算法 | 适用场景 |
|------|------|----------|
| 语义检索 | 余弦相似度 (Embedding) | 概念匹配、语义理解 |
| 关键词检索 | 倒排索引 + TF | 精确匹配、术语查找 |
| 混合检索 | 语义(70%) + 关键词(30%) | 默认平衡策略 |

## 记忆系统

| 类型 | 说明 | 重要性 |
|------|------|--------|
| Semantic | 事实和概念知识 | 0.3-1.0 |
| Episodic | 经历和事件记录 | 0.5 |
| Procedural | 技能和流程知识 | 0.8 |

## 本地启动

```bash
cd knowledge-engine
mvn spring-boot:run
```

## K8s 部署

```bash
kubectl apply -f infrastructure/k8s/base/knowledge-engine.yaml -n agentos
```

## 验收标准

Phase 4 验收标准：知识可导入并被检索；RAG 回答相关率 > 70%

| 验证项 | 验证方式 |
|--------|----------|
| 知识导入 | `curl -X POST /api/v1/knowledge/ingest -d '{"title":"test","content":"hello world","type":"MARKDOWN"}'` |
| RAG 检索 | `curl /api/v1/knowledge/query?q=hello` 返回结果 |
| 知识图谱 | `curl /api/v1/knowledge/graph/summary` 返回实体数 |
| 记忆存储 | `curl -X POST /api/v1/knowledge/memory -d '{"agentId":"agt_1","content":"test","type":"semantic"}'` |
| 上下文构建 | `curl -X POST /api/v1/knowledge/context -d '{"query":"hello","agentId":"agt_1"}'` |