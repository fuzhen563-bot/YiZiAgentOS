# AgentOS Phase 1: Infrastructure Layer

> 搭建容器化基础设施，提供 GPU 调度、数据库、缓存、消息队列等核心中间件支持。

---

## 目录结构

```
infrastructure/
├── docker/
│   ├── Dockerfile                 # 多阶段构建（Node + Maven + Runtime）
│   ├── docker-compose.yml         # 本地开发环境
│   └── .env.example               # 环境变量模板
├── db/
│   └── migration/
│       ├── V0__enable_extensions.sql   # pgvector 扩展启用
│       └── V1__initial_schema.sql       # 完整数据库 Schema
├── k8s/
│   ├── base/
│   │   ├── namespace.yaml              # 命名空间定义
│   │   ├── config.yaml                 # ConfigMap + Secret + PVC
│   │   ├── network-policy.yaml         # 网络隔离策略
│   │   ├── gpu.yaml                    # GPU 调度配置
│   │   ├── postgres.yaml               # PostgreSQL StatefulSet
│   │   ├── redis.yaml                  # Redis StatefulSet
│   │   ├── minio.yaml                  # 对象存储
│   │   ├── qdrant.yaml                 # 向量数据库
│   │   ├── loki.yaml                   # 日志系统
│   │   ├── prometheus.yaml             # 指标系统
│   │   ├── grafana.yaml                # 可视化
│   │   ├── server.yaml                 # AgentOS Server Deployment
│   │   ├── kustomization.yaml          # 应用 Kustomization
│   │   └── infra-kustomization.yaml    # 基础设施 Kustomization
│   └── helm/agentos/                   # Helm Chart
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
│           ├── _helpers.tpl
│           ├── configmap.yaml
│           ├── deployment.yaml
│           └── ingress.yaml
├── monitoring/
│   ├── loki-config.yml                 # Loki 配置（本地开发）
│   ├── prometheus.yml                  # Prometheus 配置（本地开发）
│   └── grafana-datasources.yml         # Grafana 数据源（本地开发）
├── scripts/
│   ├── deploy-k8s.sh                   # K8s 部署脚本
│   └── backup.sh                       # 备份脚本
└── cicd/
    └── github-actions.yml               # CI/CD 流水线
```

---

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **容器化与编排** | | |
| Docker 多阶段构建 | ✅ | Node 前端 + Maven 后端 + Playwright Runtime |
| Kubernetes Deployment/StatefulSet | ✅ | 高可用 + 健康检查 |
| Helm Chart | ✅ | 可复用部署包 |
| Kustomization | ✅ | 环境差异化配置 |
| Docker Compose 本地开发 | ✅ | 一键启动 |
| **数据层** | | |
| PostgreSQL 16 Schema | ✅ | 多租户 + 向量检索 |
| pgvector 扩展 | ✅ | 向量嵌入存储与相似度检索 |
| Flyway 迁移 | ✅ | V0 + V1 脚本 |
| Redis 配置 | ✅ | 持久化 + AOF |
| Vector DB (Qdrant) | ✅ | K8s 部署 |
| **存储层** | | |
| 对象存储 (MinIO) | ✅ | S3 兼容 |
| 备份脚本 | ✅ | PostgreSQL + Redis → MinIO |
| **基础设施安全** | | |
| DevSecOps | ✅ | GitHub Actions CI/CD |
| Secret 管理 | ✅ | K8s Secret + Vault 对接 |
| 网络策略 | ✅ | 命名空间隔离 |
| GPU 调度 | ✅ | NVIDIA Device Plugin |
| **高可用** | | |
| 多副本部署 | ✅ | replicas: 2 |
| 健康检查探针 | ✅ | liveness + readiness |
| 备份策略 | ✅ | 自动备份脚本 |

---

## 数据库 Schema

### 核心表

| 表名 | 说明 |
|------|------|
| **多租户** | |
| workspaces | 工作区/租户 |
| users | 用户 |
| **Agent 核心** | |
| agents | Agent 配置 |
| conversations | 会话 |
| messages | 消息记录 |
| **长期目标** | |
| goals | 目标 |
| tasks | 任务 |
| task_checkpoints | 任务检查点 |
| **知识层** | |
| knowledge_bases | 知识库 |
| raw_materials | 原始材料 |
| wiki_pages | Wiki 页面（含向量） |
| memory_records | 记忆记录（语义/情景/程序） |
| **工具技能** | |
| tools | 工具注册 |
| tool_guard_rules | 工具安全规则 |
| mcp_servers | MCP 服务器 |
| skills | 技能包 |
| **审计合规** | |
| audit_logs | 审计日志 |
| approvals | 审批流程 |
| **模型网关** | |
| model_providers | 模型提供商 |
| model_instances | 模型实例 |
| token_usage | Token 使用记录 |
| **调度** | |
| cron_jobs | 定时任务 |
| shedlock | 分布式锁 |

### 向量检索支持

```sql
-- Wiki 页面向量索引
CREATE INDEX idx_wiki_pages_embedding ON wiki_pages USING ivfflat(embedding vector_cosine_ops);

-- 记忆记录向量索引
CREATE INDEX idx_memory_embedding ON memory_records USING ivfflat(embedding vector_cosine_ops);
```

---

## 本地开发

### 启动所有服务
```bash
cd infrastructure/docker
cp .env.example .env  # 编辑配置
docker compose up -d

# 验证
docker compose ps
```

### 访问地址
| 服务 | 地址 |
|------|------|
| AgentOS Server | http://localhost:18080 |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |
| Qdrant Dashboard | http://localhost:6333 |
| MinIO Console | http://localhost:9001 |
| Grafana | http://localhost:3000 |
| Prometheus | http://localhost:9090 |

### 环境变量
```bash
DB_NAME=agentos
DB_USERNAME=agentos
DB_PASSWORD=CHANGE_ME_STRONG_PASSWORD
S3_ACCESS_KEY=agentos
S3_SECRET_KEY=CHANGE_ME_STRONG_SECRET_KEY
JWT_SECRET=CHANGE_ME_AT_LEAST_32_CHARS
GRAFANA_PASSWORD=admin123
```

## K8s 部署

### 部署基础设施
```bash
cd infrastructure/k8s/base

# 部署基础设施（postgres, redis, minio, qdrant, loki, prometheus, grafana）
kubectl apply -k infra-kustomization.yaml

# 等待就绪
kubectl wait --for=condition=ready pod -l app=postgres -n agentos-infra --timeout=120s
kubectl wait --for=condition=ready pod -l app=redis -n agentos-infra --timeout=60s
```

### 部署 AgentOS 应用
```bash
kubectl apply -k kustomization.yaml
kubectl get pods -n agentos
```

### Helm 部署
```bash
cd infrastructure/k8s/helm/agentos
helm install agentos . -n agentos --create-namespace
```

## CI/CD

### GitHub Actions 工作流
```yaml
# 触发条件
on:
  push:
    branches: [main, develop]
    tags: ['v*']
  pull_request:
    branches: [main]

# 主要步骤
1. Build mateclaw-plugin-api
2. Build mateclaw-server with frontend
3. Build and push Docker image
4. Deploy to Kubernetes (on main branch)
```

### 镜像仓库
```
ghcr.io/agentos/agentos/agentos-server:latest
ghcr.io/agentos/agentos/agentos-server:{version}
```

---

## 验收标准

Phase 1 验收标准：`kubectl get pods` 所有 pod Running；数据库可连接

| 组件 | 验证方式 |
|------|----------|
| PostgreSQL | `pg_isready -h localhost -p 5432` |
| Redis | `redis-cli ping` → PONG |
| Qdrant | `curl http://localhost:6333/health` |
| MinIO | `mc ready local` |
| Prometheus | `curl http://localhost:9090/-/healthy` |
| Grafana | `curl http://localhost:3000/api/health` |

---

## 后续阶段

- **Phase 2**: 可观测性层（日志、指标、追踪）✅
- **Phase 3**: 模型网关层（第 9 层）- 多模型统一接入