# AgentOS Phase 2: Observability Layer

> 建立统一日志、指标、追踪体系，确保所有上层服务可被观测和审计。

---

## 目录结构

```
infrastructure/
├── monitoring/
│   ├── logback-spring.xml           # 结构化日志配置（Logback → JSON）
│   ├── promtail.yml                  # Promtail 日志收集配置
│   ├── micrometer-metrics.yml        # 业务指标定义
│   ├── prometheus-alerts.yml         # 告警规则
│   ├── otel-collector.yml            # OpenTelemetry Collector 配置
│   ├── grafana-dashboards/
│   │   └── agentos-overview.json    # AgentOS 监控大屏
│   ├── grafana-datasources.yml       # Grafana 数据源
│   ├── loki-config.yml               # Loki 配置（本地开发）
│   └── prometheus.yml                # Prometheus 配置（本地开发）
└── k8s/base/
    ├── loki.yaml                     # Loki 部署
    ├── prometheus.yaml               # Prometheus 部署
    ├── grafana.yaml                  # Grafana 部署
    ├── jaeger.yaml                   # Jaeger 分布式追踪
    └── otel-collector.yaml            # OpenTelemetry Collector
```

---

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **日志系统** | | |
| 结构化日志（Logback → JSON） | ✅ | 支持 trace_id, span_id 透传 |
| 日志收集 Agent（Promtail） | ✅ | 多文件监控 + label 注入 |
| Loki 日志存储 | ✅ | K8s Deployment + PVC |
| 日志保留策略 | ✅ | 168h (7天) 默认 |
| **指标监控** | | |
| Micrometer → Prometheus | ✅ | Spring Boot Actuator 集成 |
| 业务指标定义 | ✅ | Agent tasks, LLM tokens, MCP connections |
| 告警规则 | ✅ | 15+ 告警规则覆盖 |
| Grafana Dashboard | ✅ | System, HTTP, AI/Model, Agent Tasks |
| **链路追踪** | | |
| OpenTelemetry Collector | ✅ | OTLP + Jaeger + Zipkin 接收 |
| Jaeger 分布式追踪 | ✅ | K8s 部署 |
| Trace ID 透传 | ✅ | MDC + Logback 配置 |
| 慢查询分析 | ✅ | Prometheus histogram |
| **审计与合规** | | |
| 审计日志 Schema | ✅ | `V2__audit_and_compliance.sql` |
| 操作溯源查询 | ✅ | trace_id 索引 |
| 合规报告视图 | ✅ | compliance_report |
| 安全规则引擎 | ✅ | audit_security_rules |

---

## 核心指标定义

### AI/Model Metrics
```yaml
ai_model_requests_total        # LLM API 请求总数
ai_model_requests_success      # 成功请求数
ai_model_requests_failure      # 失败请求数
ai_model_requests_latency      # 请求延迟分布
ai_model_tokens_prompt_total   # Prompt tokens 消耗
ai_model_tokens_completion_total # Completion tokens 消耗
```

### Agent Metrics
```yaml
agent_tasks_total              # Agent 任务总数
agent_tasks_active             # 活跃任务数
agent_tasks_success            # 成功任务数
agent_tasks_failure            # 失败任务数
agent_task_duration_seconds    # 任务执行时长
conversations_total            # 会话总数
conversations_active           # 活跃会话数
conversations_messages_total   # 消息总数
```

### Tool & Skill Metrics
```yaml
tool_invocations_total         # 工具调用总数
tool_invocations_success       # 成功调用数
tool_invocations_failure       # 失败调用数
tool_invocations_duration      # 工具调用延迟
skill_executions_total         # Skill 执行总数
skill_cache_hits               # 缓存命中数
```

### MCP Metrics
```yaml
mcp_server_connections         # MCP 服务连接数
mcp_requests_total             # MCP 请求总数
mcp_request_latency            # MCP 请求延迟
```

---

## 告警规则

| 告警名称 | 条件 | 严重程度 |
|----------|------|----------|
| AgentServerDown | `up{job="agentos-server"} == 0` | Critical |
| HighErrorRate | HTTP 5xx 错误率 > 5% | Warning |
| HighLatency | P95 延迟 > 2s | Warning |
| LLMModelFailure | 模型错误率 > 10% | Critical |
| DatabaseConnectionExhausted | 连接池 > 90% | Critical |
| RedisConnectionFailed | Redis down | Critical |
| TokenUsageHigh | Token 消耗异常 | Warning |
| ActiveTasksHigh | 活跃任务 > 100 | Warning |
| DiskSpaceLow | 磁盘空间 < 10% | Warning |
| MemoryPressure | JVM Heap > 90% | Warning |
| MCPConnectionFailed | MCP server down | Warning |
| AgentTaskStuck | 任务运行 > 10min | Critical |

---

## 审计事件类型

### 认证事件
- `auth.login`, `auth.logout`, `auth.login.failed`
- `auth.password_changed`, `auth.token_refreshed`, `auth.sso_login`

### 用户管理
- `user.created`, `user.updated`, `user.deleted`, `user.role_changed`

### Agent 事件
- `agent.created`, `agent.updated`, `agent.deleted`
- `agent.started`, `agent.stopped`, `agent.config_changed`

### 工具事件
- `tool.invoked`, `tool.denied`, `tool.approval_requested`

### 知识库事件
- `kb.created`, `kb.document.uploaded`, `kb.document.processed`

### 合规事件
- `admin.approval.granted`, `admin.export.data`, `admin.system_config_changed`

---

## 本地开发

### 启动监控栈
```bash
cd infrastructure/docker
docker compose up -d prometheus loki grafana
```

### 验证组件
```bash
# Prometheus
curl http://localhost:9090/-/healthy

# Loki
curl http://localhost:3100/ready

# Grafana
curl http://localhost:3000/api/health

# Jaeger (需单独部署)
curl http://localhost:16686/
```

### 查看日志
```bash
# 通过 Loki 查询
{app="agentos", type="application"} |= "ERROR"

# 通过 Promtail tail
tail -f /var/log/agentos/app.log
```

## K8s 部署

```bash
cd infrastructure/k8s/base

# 部署可观测性组件
kubectl apply -k infra-kustomization.yaml

# 查看组件状态
kubectl get pods -n agentos-infra | grep -E "loki|prometheus|grafana|jaeger|otel"
```

## 接入应用

### 添加 Maven 依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 配置 application.yml
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

### 代码示例：自定义指标
```java
@Autowired
private MeterRegistry meterRegistry;

public void recordAgentTask(String status) {
    Counter.builder("agent_tasks_total")
        .tag("status", status)
        .register(meterRegistry)
        .increment();
}
```

### 代码示例：日志追踪
```java
import org.slf4j.MDC;

MDC.put("trace_id", traceId);
MDC.put("user_id", userId);
log.info("Processing request");
// 业务逻辑
MDC.clear();
```

---

## 验收标准

Phase 2 验收标准：Grafana 可看到应用指标；日志可检索

| 验证项 | 验证方式 |
|--------|----------|
| 应用指标暴露 | `curl http://localhost:18088/actuator/prometheus` |
| Grafana 仪表盘 | 访问 http://localhost:3000 查看 agentos-overview |
| 日志收集 | 在 Loki 中查询 `{app="agentos"}` |
| 链路追踪 | 访问 Jaeger UI 查看 Trace |
| 告警触发 | 模拟高错误率观察 Prometheus 告警 |

---

## 后续阶段

- **Phase 3**: 模型网关层（第 9 层）- 多模型统一接入
- **Phase 4**: 企业知识层（第 8 层）- RAG + 知识图谱