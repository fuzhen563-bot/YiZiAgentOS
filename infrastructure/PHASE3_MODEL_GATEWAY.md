# AgentOS Phase 3: Model Gateway Layer

> 建立统一的模型接入层，支持 OpenAI / Claude / Qwen / DeepSeek / Local LLM 的智能路由与成本优化。

---

## 架构

```
┌──────────┐    ┌──────────────────────────────────────────────┐    ┌──────────┐
│  Client  │───▶│             Model Gateway                     │───▶│ OpenAI   │
└──────────┘    │                                              │    ├──────────┤
                │  ┌─────────┐  ┌──────────┐  ┌─────────────┐ │───▶│ Claude   │
                │  │Security │─▶│  Router  │─▶│  Failover   │ │    ├──────────┤
                │  │ Filter  │  │(Cost/    │  │  Manager    │ │───▶│ Qwen     │
                │  │ + Mask  │  │ Speed/   │  │ + Circuit   │ │    ├──────────┤
                │  └─────────┘  │ Hybrid)  │  │  Breaker    │ │───▶│ DeepSeek │
                │               └──────────┘  └─────────────┘ │    ├──────────┤
                │                                              │───▶│ Local LLM│
                │  ┌──────────────────────────────────────┐    │    └──────────┘
                │  │         Billing + Token Counter       │    │
                │  └──────────────────────────────────────┘    │
                └──────────────────────────────────────────────┘
```

## 目录结构

```
model-gateway/
├── pom.xml
├── src/main/java/com/agentos/gateway/
│   ├── ModelGatewayApplication.java
│   ├── GatewayController.java
│   ├── core/
│   │   ├── ModelProvider.java          # Provider 接口
│   │   ├── ModelRequest.java            # 请求体
│   │   ├── ModelResponse.java           # 响应体
│   │   ├── ModelHealth.java             # 健康状态
│   │   ├── ModelRole.java               # 模型角色枚举
│   │   ├── ProviderConfig.java          # 提供者配置
│   │   └── ModelGatewayService.java     # 网关主服务
│   ├── provider/
│   │   ├── AbstractModelProvider.java   # 抽象基类
│   │   ├── OpenAIProvider.java
│   │   ├── ClaudeProvider.java
│   │   ├── QwenProvider.java
│   │   ├── DeepSeekProvider.java
│   │   └── LocalLLMProvider.java
│   ├── router/
│   │   ├── Router.java                  # 路由接口
│   │   ├── ModelRouter.java             # 路由管理器
│   │   ├── CostPriorityRouter.java      # 成本优先
│   │   ├── SpeedPriorityRouter.java     # 速度优先
│   │   ├── HybridRouter.java            # 混合策略
│   │   └── RoutingStrategy.java         # 策略枚举
│   ├── circuitbreaker/
│   │   ├── CircuitBreaker.java          # 熔断器
│   │   ├── CircuitBreakerState.java     # 熔断状态
│   │   └── FailoverManager.java         # 故障转移
│   ├── billing/
│   │   ├── TokenCounter.java            # Token 计数器
│   │   ├── CostCalculator.java          # 成本计算
│   │   └── BillingService.java          # 计费服务
│   ├── security/
│   │   ├── ContentFilter.java           # 内容过滤
│   │   ├── PromptInjectionDetector.java # Prompt注入检测
│   │   └── SensitiveDataMasker.java     # 敏感信息脱敏
│   └── config/
│       └── ModelGatewayConfig.java      # Spring 配置
└── src/main/resources/
    └── application.yml
```

---

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **模型抽象层** | ✅ | `ModelProvider` 接口 + `AbstractModelProvider` 基类 |
| **Provider 实现** | ✅ | OpenAI, Claude, Qwen, DeepSeek, Local LLM |
| **路由策略引擎** | ✅ | 成本优先 / 速度优先 / 混合策略 |
| **Token 计费系统** | ✅ | `TokenCounter` + `CostCalculator` + `BillingService` |
| **Failover 机制** | ✅ | `FailoverManager` + `CircuitBreaker` |
| **安全过滤** | ✅ | ContentFilter + PromptInjectionDetector + SensitiveDataMasker |
| **REST API** | ✅ | `/chat`, `/models`, `/health`, `/usage` |
| **K8s 部署** | ✅ | Deployment + Service + Ingress |

## API 接口

### POST /api/v1/gateway/chat
```json
{
  "model": "gpt-4o",
  "prompt": "Hello, world!",
  "userId": "usr_xxx",
  "workspaceId": "ws_yyy",
  "maxTokens": 4096,
  "temperature": 0.7
}
```

### GET /api/v1/gateway/models
```json
["gpt-4o", "gpt-4o-mini", "claude-sonnet-4", "qwen-max", "deepseek-chat"]
```

### GET /api/v1/gateway/health
```json
{"status": "UP", "providers": [{"name": "openai", "available": true}, ...]}
```

### GET /api/v1/gateway/usage?workspaceId=ws_yyy
```json
{"workspace_id": "ws_yyy", "total_tokens": 123456, "provider_summary": {...}}
```

## 路由策略

| 策略 | 算法 | 适用场景 |
|------|------|----------|
| COST_PRIORITY | 选择单位成本最低的可用 Provider | 批量处理、非实时任务 |
| SPEED_PRIORITY | 选择延迟最低的可用 Provider | 实时对话、交互式场景 |
| HYBRID | 成本(40%) + 速度(60%) 加权评分 | 默认平衡策略 |
| FIXED | 固定使用指定 Provider | 测试、调试场景 |

## Failover 流程

```
请求进入 → 按顺序尝试 Provider A
  ├── 成功 → 返回
  └── 失败 → CircuitBreaker 记录 → 尝试 Provider B
       ├── 成功 → 返回
       └── 失败 → 尝试 Provider C ...
```

### 熔断器参数
- failureThreshold: 3 (连续失败次数)
- halfOpenTimeoutMs: 30000 (半开恢复时间)
- 状态: CLOSED → OPEN → HALF_OPEN → CLOSED

## 成本模型

| Provider | 模型 | 输入 ($/1K tokens) | 输出 ($/1K tokens) |
|----------|------|-------------------|--------------------|
| OpenAI | gpt-4o | 0.005 | 0.015 |
| OpenAI | gpt-4o-mini | 0.00015 | 0.0006 |
| Claude | claude-sonnet-4 | 0.003 | 0.015 |
| Claude | claude-haiku-3.5 | 0.0008 | 0.004 |
| Qwen | qwen-max | 0.002 | 0.006 |
| Qwen | qwen-plus | 0.0008 | 0.002 |
| DeepSeek | deepseek-chat | 0.00027 | 0.0011 |
| DeepSeek | deepseek-reasoner | 0.00055 | 0.00219 |

## 安全机制

| 模块 | 说明 |
|------|------|
| ContentFilter | 阻止危险命令、SQL注入、脚本注入 |
| PromptInjectionDetector | 检测忽略指令、角色扮演、DAN 攻击 |
| SensitiveDataMasker | 脱敏邮箱、手机号、身份证、API Key |

## 本地启动

```bash
cd model-gateway
mvn spring-boot:run

# 验证
curl http://localhost:9090/actuator/health
curl http://localhost:9090/api/v1/gateway/models
```

## K8s 部署

```bash
kubectl apply -f infrastructure/k8s/base/model-gateway.yaml -n agentos
```

## 验收标准

Phase 3 验收标准：至少 3 个模型可切换调用；Token 用量可统计

| 验证项 | 验证方式 |
|--------|----------|
| 多 Provider 路由 | `curl /api/v1/gateway/models` 返回 >=3 个模型 |
| 模型调用 | `curl -X POST /api/v1/gateway/chat -d '{"model":"gpt-4o","prompt":"hi"}'` |
| Token 统计 | `curl /api/v1/gateway/usage?workspaceId=test` |
| Failover | 停掉主 Provider 观察自动切换到备用 |
| 安全过滤 | 发送注入 prompt 应返回 403 |