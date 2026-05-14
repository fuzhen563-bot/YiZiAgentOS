# AgentOS Phase 8: Agent Runtime Layer

> 构建核心 Agent 运行时内核，提供 ReAct / Plan-Execute 推理、多 Agent 协作和任务规划。

---

## 架构

```
┌──────────┐   ┌──────────────────────────────────────────────────────────────┐
│  Client  │──▶│                 Agent Runtime                               │
└──────────┘   │                                                              │
               │  ┌────────────────────┐  ┌────────────────────┐              │
               │  │   Agent Registry   │  │     Router        │              │
               │  │   (Sales/HR/       │──│  (intent-based)   │              │
               │  │    Finance/Legal/  │  │                   │              │
               │  │    Support)        │  └────────────────────┘              │
               │  └────────┬───────────┘                                     │
               │           │                                                  │
               │  ┌────────▼─────────────────────────────────────────┐       │
               │  │              Reasoning Engines                    │       │
               │  │  ┌──────────────┐  ┌──────────────────┐          │       │
               │  │  │ ReAct Agent  │  │ Plan-Execute     │          │       │
               │  │  │ (Think →     │  │ Agent (Plan →    │          │       │
               │  │  │  Act → Obs)  │  │  Execute → Summ) │          │       │
               │  │  └──────────────┘  └──────────────────┘          │       │
               │  └──────────────────────────────────────────────────┘       │
               │                                                              │
               │  ┌────────────────────┐  ┌────────────────────┐              │
               │  │ Multi-Agent        │  │ Workflow Engine    │              │
               │  │ Orchestrator       │  │ (step/parallel/    │              │
               │  │ (delegate/broadcast│  │  condition)        │              │
               │  │  /collaborate)     │  │                    │              │
               │  └────────────────────┘  └────────────────────┘              │
               └──────────────────────────────────────────────────────────────┘
```

## 目录结构

```
agent-runtime/
├── pom.xml
└── src/main/java/com/agentos/runtime/
    ├── AgentRuntimeApplication.java
    ├── AgentRuntimeController.java
    ├── core/
    │   ├── Agent.java               # Agent 接口
    │   ├── BaseAgent.java           # 抽象基类（状态/记忆/生命周期）
    │   ├── AgentState.java          # 状态枚举
    │   ├── AgentRequest.java        # 请求模型
    │   ├── AgentResponse.java       # 响应模型
    │   └── AgentRegistry.java       # 注册 + 路由
    ├── prompt/
    │   └── PromptTemplate.java       # 模板引擎
    ├── reasoning/
    │   ├── ReActAgent.java           # ReAct 推理循环
    │   └── PlanExecuteAgent.java     # Plan-Execute 推理
    ├── multiagent/
    │   └── MultiAgentOrchestrator.java # 多 Agent 协作
    ├── workflow/
    │   └── WorkflowEngine.java       # 工作流引擎
    └── config/
        └── AgentRuntimeConfig.java
```

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **Agent 抽象** | ✅ | Agent 接口 + BaseAgent（状态/记忆/生命周期） |
| **ReAct Agent** | ✅ | Thought → Action → Observation 循环 |
| **Plan-Execute** | ✅ | Plan → Execute → Summary |
| **Agent Router** | ✅ | 基于意图的路由（sale/hr/finance/legal/support） |
| **多 Agent 协作** | ✅ | 委托/广播/协作模式 |
| **Workflow** | ✅ | 步骤定义 + 依赖管理 + 条件 + 并行执行 |
| **5 个部门 Agent** | ✅ | Sales / HR / Finance / Legal / Support |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/runtime/agents | 列出所有 Agent |
| POST | /api/v1/runtime/agents/{id}/execute | 执行任务 |
| POST | /api/v1/runtime/agents/{id}/interrupt | 中断 |
| POST | /api/v1/runtime/agents/{id}/resume | 恢复 |
| POST | /api/v1/runtime/agents/{id}/reset | 重置 |
| POST | /api/v1/runtime/route | 基于意图路由到合适 Agent |
| POST | /api/v1/runtime/collaborate | 多 Agent 协作 |
| POST | /api/v1/runtime/workflows/create | 创建工作流 |
| POST | /api/v1/runtime/workflows/{id}/execute | 执行工作流 |

## 部门 Agent 配置

| Agent | 类型 | 路由关键词 |
|-------|------|----------|
| SalesBot | ReAct | sale, 销售, 客户 |
| HRBot | Plan-Execute | hr, 人事, 招聘 |
| FinanceBot | ReAct | finance, 财务, 预算 |
| LegalBot | Plan-Execute | legal, 法律, 合规 |
| SupportBot | ReAct | support, 支持, 帮助 |

## 工作流定义

```json
{
  "name": "Customer Onboarding",
  "steps": [
    {"id": "step-1", "name": "Collect Info", "type": "sequential", "agentId": "SalesBot"},
    {"id": "step-2", "name": "Create Account", "type": "sequential", "agentId": "SupportBot", "dependsOn": ["step-1"]},
    {"id": "step-3", "name": "Setup Billing", "type": "sequential", "agentId": "FinanceBot", "dependsOn": ["step-1"]},
    {"id": "step-4", "name": "Welcome Email", "type": "parallel", "config": {"steps": ["email", "docs"]}, "dependsOn": ["step-2", "step-3"]}
  ]
}
```

## 本地启动

```bash
cd agent-runtime
mvn spring-boot:run
```

## K8s 部署

```bash
kubectl apply -f infrastructure/k8s/base/agent-runtime.yaml -n agentos
```

## 验收标准

Phase 8 验收标准：ReAct/Plan-Execute Agent 可完成完整任务

| 验证项 | 验证方式 |
|--------|----------|
| Agent 列表 | `curl /api/v1/runtime/agents` 返回 5 个 Agent |
| ReAct 执行 | `curl -X POST /api/v1/runtime/agents/agent-2/execute -d '{"message":"hello"}'` |
| 路由 | `curl -X POST /api/v1/runtime/route -d '{"message":"sales question"}'` 路由到 SalesBot |
| 协作 | `curl -X POST /api/v1/runtime/collaborate -d '{"primaryAgentId":"agent-2","task":"analyze","collaboratorIds":["agent-3","agent-4"]}'` |
| 工作流 | `curl -X POST /api/v1/runtime/workflows/create -d '{"name":"test"}'` → execute |