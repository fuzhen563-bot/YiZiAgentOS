# 亦梓AgentOS

> **企业级 AI Agent 操作系统** — 11 层分层架构，从基础设施到用户体验全面覆盖。

<p align="center">
  <img src="logo@2x.png" alt="亦梓AgentOS Logo" width="200">
</p>

[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-Apache--2.0-red.svg)](LICENSE)

---

## 概述

亦梓AgentOS 是一个开源的 **企业级 AI Agent 操作系统**，采用 11 层分层架构设计。它不仅是一个聊天机器人，而是一个完整的 AI Agent 运行平台，具备：

- **多 Agent 协作** — 5 个部门级 Agent（销售/人事/财务/法务/技术支持）
- **工具调用** — 22+ 内置工具（GitHub/邮件/日历/CRM/ERP）
- **RAG 知识库** — 语义+关键词混合检索
- **安全治理** — Policy Guard + 审批流
- **多模型路由** — 支持 OpenAI/Claude/Qwen/DeepSeek 等
- **全链路可观测** — 日志/指标/追踪/审计

---

## 架构全景

```
体验交互层 (Web UI / Admin Console)
企业控制平面 (多租户 / RBAC / 计费)
长期目标层 (Goal Registry / KPI / Checkpoint)
Agent 运行时 (ReAct / Plan-Execute / 多 Agent)
自我进化层 (Reflection / SOP / Skill 进化)
安全执行层 (沙箱 / Policy Guard / Rollback)
MCP + 工具注册层 (协议适配器 / 22+ 工具)
企业知识层 (RAG / 知识图谱 / 3 种记忆)
模型网关层 (多模型路由 / Failover / 计费)
可观测性层 (Loki / Prometheus / Grafana)
基础设施层 (K8s / PostgreSQL / Redis / Docker)
```

---

## 项目结构

```
agentos/
├── agent-runtime/          # Agent 运行时 (Layer 4)
│   ├── core/               # Agent 接口、状态管理、请求/响应模型
│   ├── reasoning/          # ReAct / Plan-Execute 推理引擎
│   ├── multiagent/         # 多 Agent 协作编排
│   ├── workflow/           # 工作流引擎 (步骤/依赖/并行)
│   └── prompt/             # Prompt 模板引擎
│
├── model-gateway/          # 模型网关 (Layer 9)
│   ├── provider/           # OpenAI/Claude/Qwen/DeepSeek 适配器
│   ├── router/             # 成本优先/速度优先/混合路由
│   ├── circuitbreaker/     # 熔断器 + Failover
│   ├── billing/            # Token 计费
│   └── security/           # 内容过滤/Prompt 注入检测/脱敏
│
├── knowledge-engine/       # 知识引擎 (Layer 8)
│   ├── rag/                # 语义/关键词/混合检索
│   ├── graph/              # 知识图谱 (实体/关系/邻域)
│   ├── memory/             # 语义/情景/程序性记忆
│   └── fabric/             # 上下文 Fabric
│
├── mcp-registry/           # MCP + 工具注册 (Layer 7)
│   ├── adapter/            # stdio/HTTP/SSE 适配器
│   ├── connector/          # GitHub/Email/Calendar/CRM/ERP
│   ├── registry/           # 工具注册表 + 元数据
│   └── skill/              # SkillOS 基础
│
├── secure-execution/       # 安全执行层 (Layer 6)
│   ├── browser/            # 浏览器自动化
│   ├── sandbox/            # 沙箱执行环境
│   ├── policy/             # Policy Guard + Permission Broker
│   └── rollback/           # 操作回滚 + 快照
│
├── evolution-engine/       # 自我进化层 (Layer 5)
│   ├── reflection/         # 任务分析/优化建议
│   ├── sop/                # SOP 抽取/质量评估
│   ├── skill/              # 技能生成/变异/淘汰
│   └── market/             # Skill Marketplace
│
├── goal-engine/            # 长期目标层 (Layer 3)
│   ├── core/               # Goal Registry / Objective Engine
│   ├── task/               # Task Graph / 依赖管理
│   └── persist/            # Checkpoint / 恢复
│
├── control-plane/          # 企业控制平面 (Layer 2)
│   ├── tenant/             # 多租户 + 资源配额
│   ├── iam/                # 用户/身份管理
│   ├── rbac/               # RBAC + ABAC 策略
│   └── billing/            # 订阅 + 用量计费
│
├── infrastructure/         # 基础设施 (Layer 11+10)
│   ├── docker/             # Docker Compose
│   ├── k8s/                # Kubernetes 部署
│   ├── monitoring/         # Prometheus/Grafana/Loki
│   └── db/                 # PostgreSQL Schema
│
└── web-ui/                 # 体验层 (Layer 1)
    ├── index.html          # 聊天界面
    └── admin/              # 管理后台
```

---

## 快速开始

### 前置要求

- **Java 21+** — [下载 Temurin JDK](https://adoptium.net/)
- **Maven 3.9+** — `scoop install maven` 或官网下载
- **PowerShell 5.1+** (Windows) 或 **curl** (Linux)

### 编译

```bash
# 编译各模块
cd model-gateway && mvn clean package -DskipTests && cd ..
cd knowledge-engine && mvn clean package -DskipTests && cd ..
cd mcp-registry && mvn clean package -DskipTests && cd ..
cd secure-execution && mvn clean package -DskipTests && cd ..
cd evolution-engine && mvn clean package -DskipTests && cd ..
cd agent-runtime && mvn clean package -DskipTests && cd ..
cd goal-engine && mvn clean package -DskipTests && cd ..
cd control-plane && mvn clean package -DskipTests && cd ..
```

### 启动核心服务

```bash
# 启动 Agent Runtime (必需)
java -jar agent-runtime/target/*.jar --server.port=9095 &

# 启动 MCP Registry (工具调用必需)
java -jar mcp-registry/target/*.jar --server.port=9092 &

# 启动其他服务 (可选)
java -jar goal-engine/target/*.jar --server.port=9096 &
java -jar control-plane/target/*.jar --server.port=9097 &
```

### 打开 Web UI

直接打开 `web-ui/index.html`，或用 HTTP 服务方式访问：

```bash
cd web-ui
python3 -m http.server 8080
# 打开 http://localhost:8080/
```

---

## API 概览

| 服务 | 端口 | 说明 |
|------|------|------|
| Agent Runtime | 9095 | Agent 执行引擎 |
| MCP Registry | 9092 | 工具注册与调用 |
| Goal Engine | 9096 | 长期目标管理 |
| Control Plane | 9097 | 多租户/权限/计费 |
| Model Gateway | 9090 | LLM 路由 (可选) |
| Knowledge Engine | 9091 | RAG 知识库 (可选) |
| Secure Execution | 9093 | 沙箱/策略 (可选) |
| Evolution Engine | 9094 | 自我进化 (可选) |

---

## 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Java 21+, JavaScript |
| **框架** | Spring Boot 3.5 |
| **数据库** | PostgreSQL 16 + pgvector |
| **向量检索** | Qdrant |
| **可观测** | Prometheus + Grafana + Loki |
| **部署** | Docker Compose / Kubernetes |
| **LLM** | OpenAI / Claude / Qwen / DeepSeek |

---

## 开发人员

[浅蓝](https://github.com/xiaoqianlan)

(https://avatars.githubusercontent.com/u/264582459?v=4&size=40)[星河](https://github.com/fuzhen563-bot)

---

##  License

Apache License 2.0

---

## 致谢

亦梓AgentOS 参考了以下优秀开源项目的架构思想：

- [MateClaw](https://github.com/matevip/mateclaw) — Agent 运行时参考
- [OpenClaw](https://github.com/openclaw/openclaw) — 安全执行层参考
- [Hermes Agent](https://github.com/NousResearch/hermes-agent) — 自我进化层参考
