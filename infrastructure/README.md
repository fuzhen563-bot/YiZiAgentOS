# AgentOS Infrastructure

企业级 AI Agent 操作系统的底层基础设施，参考 MateClaw/OpenClaw/Hermes 架构设计。

---

## 阶段文档

| 阶段 | 文档 | 内容 |
|------|------|------|
| **Phase 1** | [PHASE1_INFRASTRUCTURE.md](./PHASE1_INFRASTRUCTURE.md) | 基础设施层（K8s, Docker, PostgreSQL, Redis, Vector DB, 对象存储） |
| **Phase 2** | [PHASE2_OBSERVABILITY.md](./PHASE2_OBSERVABILITY.md) | 可观测性层（日志、指标、链路追踪、审计合规） |
| **Phase 3** | [PHASE3_MODEL_GATEWAY.md](./PHASE3_MODEL_GATEWAY.md) | 模型网关层（多模型路由、Failover、计费、安全） |
| **Phase 4** | [PHASE4_KNOWLEDGE_ENGINE.md](./PHASE4_KNOWLEDGE_ENGINE.md) | 企业知识层（RAG、知识图谱、记忆系统） |
| **Phase 5** | [PHASE5_MCP_REGISTRY.md](./PHASE5_MCP_REGISTRY.md) | MCP + 工具注册层（协议适配器、工具注册表、SkillOS） |
| **Phase 6** | [PHASE6_SECURE_EXECUTION.md](./PHASE6_SECURE_EXECUTION.md) | 安全执行层（浏览器自动化、沙箱、策略守卫、回滚） |
| **Phase 7** | [PHASE7_EVOLUTION_ENGINE.md](./PHASE7_EVOLUTION_ENGINE.md) | 自我进化层（Reflection、SOP、技能进化、市场） |
| **Phase 8** | [PHASE8_AGENT_RUNTIME.md](./PHASE8_AGENT_RUNTIME.md) | Agent 运行时层（ReAct、Plan-Execute、多 Agent、工作流） |
| **Phase 9+** | 开发中... | 长期目标层... |

---

## 快速导航

### Phase 1: 基础设施层
- PostgreSQL 16 + pgvector 向量检索
- Redis 7 缓存 + 分布式锁
- MinIO S3 兼容对象存储
- Qdrant 向量数据库
- Kubernetes 部署配置
- Docker Compose 本地开发环境

### Phase 2: 可观测性层
- Loki 日志收集 + Promtail
- Prometheus 指标 + Grafana 大屏
- Jaeger 分布式追踪
- OpenTelemetry Collector
- 业务指标定义（Agent tasks, LLM tokens）
- 告警规则（15+ 条）
- 审计日志 + 合规报告

---

## 当前状态

```
Phase 1: ✅ 完成
Phase 2: ✅ 完成
Phase 3: ✅ 完成
Phase 4: ✅ 完成
Phase 5: ✅ 完成
Phase 6: ✅ 完成
Phase 7: ✅ 完成
Phase 8: ✅ 完成
Phase 9: ✅ 完成
Phase 10: ✅ 完成
Phase 11: ✅ 完成
```

---

## 环境变量配置

```bash
# 复制环境变量模板
cp docker/.env.example docker/.env

# 必填项
DB_PASSWORD=your_strong_password
S3_SECRET_KEY=your_strong_secret
JWT_SECRET=at_least_32_characters
```

## 本地启动

```bash
cd docker
docker compose up -d

# 验证
docker compose ps
```

## K8s 部署

```bash
# 基础设施
kubectl apply -k k8s/base/infra-kustomization.yaml

# 应用
kubectl apply -k k8s/base/kustomization.yaml
```