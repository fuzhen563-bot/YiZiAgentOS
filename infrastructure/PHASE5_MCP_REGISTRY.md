# AgentOS Phase 5: MCP + Tool Registry Layer

> 建立 MCP 协议接入和统一工具注册中心，实现工具的发现、认证、成本和治理管理。

---

## 架构

```
┌──────────┐   ┌──────────────────────────────────────────────────────────────┐
│  Client  │──▶│                   MCP Registry                              │
└──────────┘   │                                                              │
               │  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐ │
               │  │ MCP Adapters │─▶│ Tool Registry │─▶│   Skill Engine    │ │
               │  │ stdio HTTP   │  │ + Metadata   │  │ upload/version/    │ │
               │  │ SSE          │  │ + Search     │  │ search/discover   │ │
               │  └──────────────┘  └──────────────┘  └────────────────────┘ │
               │                          │                                  │
               │  ┌───────────────────────┼──────────────────────┐           │
               │  │   Built-in Connectors │                      │           │
               │  │  GitHub │ Email │ Calendar │ CRM │ ERP      │           │
               │  └──────────────────────────────────────────────┘           │
               └──────────────────────────────────────────────────────────────┘
```

## 目录结构

```
mcp-registry/
├── pom.xml
└── src/main/java/com/agentos/mcp/
    ├── McpRegistryApplication.java
    ├── McpController.java
    ├── core/
    │   ├── McpAdapter.java       # MCP 适配器接口
    │   ├── McpMessage.java        # JSON-RPC 消息模型
    │   ├── McpSchema.java         # 参数 Schema 解析/验证
    │   └── McpTransport.java      # 传输协议枚举
    ├── adapter/
    │   ├── StdioAdapter.java      # stdio 模式适配器
    │   ├── HttpAdapter.java       # HTTP/Streamable HTTP 适配器
    │   └── SseAdapter.java        # SSE 模式适配器
    ├── registry/
    │   ├── ToolRegistry.java      # 工具注册表 CRUD + 搜索
    │   ├── ToolDefinition.java    # 工具定义模型
    │   └── ToolMetadata.java      # 工具元数据（Cost/Risk）
    ├── skill/
    │   ├── SkillEngine.java       # Skill 管理引擎
    │   ├── SkillDefinition.java   # Skill 定义模型
    │   └── SkillVersion.java      # 版本管理
    ├── connector/
    │   ├── GitHubConnector.java   # GitHub API 连接器
    │   ├── EmailConnector.java    # Email 连接器
    │   ├── CalendarConnector.java # 日历连接器
    │   ├── CrmConnector.java      # CRM 连接器
    │   └── ErpConnector.java      # ERP 连接器
    └── config/
        └── McpRegistryConfig.java # Spring 配置
```

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **MCP Adapter 框架** | ✅ | Adapter 接口 + stdio/HTTP/SSE 三种实现 |
| **MCP 消息协议** | ✅ | JSON-RPC 2.0 消息模型 |
| **Schema 解析/验证** | ✅ | 参数类型推断与校验 |
| **工具注册表** | ✅ | CRUD + 搜索 + 按 Provider 过滤 |
| **工具元数据** | ✅ | Cost, RiskLevel, Category, 参数 Schema |
| **SkillOS 基础** | ✅ | 上传/解析/版本管理/搜索 |
| **内置连接器** | ✅ | GitHub(4), Email(4), Calendar(4), CRM(5), ERP(5) |
| **REST API** | ✅ | 12 个端点覆盖全部功能 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/v1/mcp/tools | 列出工具（可按 provider 过滤） |
| GET | /api/v1/mcp/tools/search?q= | 搜索工具 |
| POST | /api/v1/mcp/tools/register | 注册工具 |
| POST | /api/v1/mcp/tools/call | 调用工具 |
| GET | /api/v1/mcp/tools/stats | 工具统计 |
| DELETE | /api/v1/mcp/tools/{name} | 删除工具 |
| GET | /api/v1/mcp/skills | 列出 Skill |
| POST | /api/v1/mcp/skills/upload | 上传 Skill |
| POST | /api/v1/mcp/connectors/init | 初始化内置连接器 |

## 内置工具清单

| 连接器 | 工具 | 风险等级 | Cost |
|--------|------|----------|------|
| **GitHub** | list_repos, get_file, create_issue, search_code | medium | 1 |
| **Email** | send, read, search, drafts | high | 2 |
| **Calendar** | list_events, create_event, update_event, delete_event | medium | 1 |
| **CRM** | list_contacts, get_contact, create_contact, list_deals, get_deal | high | 2 |
| **ERP** | list_inventory, get_order, list_orders, get_invoice, create_po | critical | 3 |

## MCP 传输协议

| 协议 | 实现 | 适用场景 |
|------|------|----------|
| **stdio** | StdioAdapter - 子进程 stdin/stdout | 本地 MCP 服务器 |
| **Streamable HTTP** | HttpAdapter - JSON-RPC over HTTP | 远程 MCP 服务器 |
| **SSE** | SseAdapter - Server-Sent Events | 服务端推送场景 |

## 本地启动

```bash
cd mcp-registry
mvn spring-boot:run
```

## K8s 部署

```bash
kubectl apply -f infrastructure/k8s/base/mcp-registry.yaml -n agentos
```

## 验收标准

Phase 5 验收标准：MCP Server 可连接；工具可注册并被发现

| 验证项 | 验证方式 |
|--------|----------|
| 列出工具 | `curl /api/v1/mcp/tools` 返回 >= 10 个工具 |
| 搜索工具 | `curl /api/v1/mcp/tools/search?q=github` |
| 注册工具 | `curl -X POST /api/v1/mcp/tools/register -d '{"name":"test"}'` |
| 调用工具 | `curl -X POST /api/v1/mcp/tools/call -d '{"name":"github_list_repos"}'` |
| Skill 上传 | `curl -X POST /api/v1/mcp/skills/upload -d '{"name":"my-skill"}'` |
| 连接器初始化 | `curl -X POST /api/v1/mcp/connectors/init` |