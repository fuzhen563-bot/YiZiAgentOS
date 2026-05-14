# AgentOS SIEM Integration

> 提供标准化接口对接企业 SIEM 系统（Splunk, IBM QRadar, ArcSight, Elastic SIEM 等）

---

## SIEM 对接概述

AgentOS 审计日志和可观测性数据可通过以下方式对接企业 SIEM：

1. **Syslog 推送** - 直接推送日志到 SIEM collector
2. **Webhook 回调** - 实时事件通知
3. **API 拉取** - SIEM 系统主动拉取
4. **文件导出** - 生成 CEF/LEEF 格式日志文件

---

## Syslog 对接

### 配置

```yaml
# application.yml
agentos:
  observability:
    syslog:
      enabled: true
      host: siem.company.com
      port: 514
      protocol: tcp          # tcp 或 udp
      format: cef           # CEF (Common Event Format)
      facility: local0
      severity-mapping:
        auth.login: low
        auth.logout: low
        auth.login.failed: medium
        user.deleted: high
        admin.export.data: critical
```

### CEF 格式输出

```
CEF:0|AgentOS|Audit|1.0|100|User Login|7|src=192.168.1.100 dst=10.0.0.50 user=admin@example.com outcome=success
```

---

## Webhook 对接

### 配置

```yaml
agentos:
  observability:
    webhook:
      enabled: true
      endpoints:
        - name: splunk-hec
          url: https://splunk.company.com:8088/services/collector
          token: ${SPLUNK_HEC_TOKEN}
          events:
            - auth.*
            - admin.*
            - tool.denied
        - name: generic-siem
          url: https://siem.company.com/api/v1/events
          headers:
            Authorization: Bearer ${SIEM_API_TOKEN}
          events:
            - "*"  # 接收所有事件
          retry:
            max-attempts: 3
            backoff: exponential
```

### Webhook 事件格式

```json
{
  "event_id": "evt_abc123",
  "timestamp": "2026-05-07T12:00:00Z",
  "event_type": "auth.login",
  "severity": "low",
  "workspace_id": "ws_xxx",
  "user_id": "usr_yyy",
  "actor": {
    "ip_address": "192.168.1.100",
    "user_agent": "Mozilla/5.0..."
  },
  "resource": {
    "type": "session",
    "id": "sess_zzz"
  },
  "outcome": "success",
  "trace_id": "trace_abc123",
  "details": {}
}
```

---

## API 拉取接口

### 审计日志查询 API

```
GET /api/v1/audit/logs

Query Parameters:
  - workspace_id: UUID (required)
  - start_time: ISO8601
  - end_time: ISO8601
  - event_type: string (supports wildcards)
  - user_id: UUID
  - severity: low|medium|high|critical
  - limit: int (default 100, max 1000)
  - offset: int

Response:
{
  "total": 1234,
  "events": [
    {
      "id": "evt_xxx",
      "timestamp": "2026-05-07T12:00:00Z",
      "event_type": "auth.login",
      "severity": "low",
      "user_id": "usr_yyy",
      "user_email": "admin@example.com",
      "action": "auth.login",
      "resource_type": "session",
      "resource_id": "sess_zzz",
      "ip_address": "192.168.1.100",
      "outcome": "success",
      "trace_id": "trace_abc123",
      "details": {}
    }
  ]
}
```

### 安全事件 API

```
GET /api/v1/audit/security/events

Response:
{
  "events": [
    {
      "id": "sec_xxx",
      "timestamp": "2026-05-07T12:00:00Z",
      "rule_name": "Failed login attempts",
      "condition": {"threshold": 5, "window": "1h"},
      "triggered_count": 7,
      "severity": "high",
      "status": "open",
      "first_seen": "2026-05-07T11:30:00Z",
      "last_seen": "2026-05-07T12:00:00Z"
    }
  ]
}
```

### 合规报告 API

```
GET /api/v1/audit/compliance/report

Query Parameters:
  - workspace_id: UUID (required)
  - period: daily|weekly|monthly
  - format: json|pdf|csv

Response:
{
  "report_id": "rpt_xxx",
  "period": {
    "start": "2026-04-01",
    "end": "2026-04-30"
  },
  "summary": {
    "total_events": 50000,
    "by_category": {
      "authentication": 10000,
      "user_management": 500,
      "agent_activity": 35000,
      "tool_usage": 4500
    },
    "by_severity": {
      "critical": 10,
      "high": 50,
      "medium": 200,
      "low": 49740
    }
  },
  "security_findings": [...],
  "generated_at": "2026-05-01T00:00:00Z"
}
```

---

## 文件导出格式

### CEF (Common Event Format)

```log
CEF:0|AgentOS|Audit|1.0|100|User Login|7|src=192.168.1.100 dst=10.0.0.50 user=admin@example.com outcome=success
CEF:0|AgentOS|Audit|1.0|101|User Logout|5|src=192.168.1.100 dst=10.0.0.50 user=admin@example.com
CEF:0|AgentOS|Audit|1.0|102|Agent Created|6|src=10.0.0.100 dst=10.0.0.50 user=admin@example.com agent_id=agt_xxx agent_name=SalesBot
```

### LEEF (Log Event Extended Format)

```log
LEEF:1.0|AgentOS|Audit|1.0|100|User Login|src=192.168.1.100 dst=10.0.0.50 user=admin@example.com outcome=success
```

### JSON Lines

```json
{"timestamp":"2026-05-07T12:00:00Z","event_type":"auth.login","severity":"low","user":"admin@example.com","ip":"192.168.1.100","outcome":"success"}
{"timestamp":"2026-05-07T12:01:00Z","event_type":"tool.invoked","severity":"medium","user":"admin@example.com","tool":"browser_automation","status":"success"}
```

---

## Splunk 集成示例

### HEC (HTTP Event Collector)

```bash
# 创建 HEC token
# 在 Splunk Web UI: Settings > Data Inputs > HTTP Event Collector

# 配置 AgentOS
agentos:
  observability:
    webhook:
      endpoints:
        - name: splunk
          url: https://splunk.company.com:8088/services/collector
          token: ${SPLUNK_HEC_TOKEN}
          format: json
```

### Splunk Dashboard 查询示例

```
# 认证失败事件
index=agentos_logs event_type="auth.login.failed" | stats count by user, src_ip | where count > 5

# 敏感操作审计
index=agentos_logs category="admin" | timechart span=1h count by action

# Agent 活动分析
index=agentos_logs event_type="agent.*" | stats avg(duration) as avg_duration, count by agent_name
```

---

## IBM QRadar 集成

### Syslog 协议

QRadar 通常通过 syslog 接收日志，配置 AgentOS 使用 LEEF 格式：

```yaml
agentos:
  observability:
    syslog:
      enabled: true
      host: qradar.company.com
      port: 514
      protocol: udp
      format: leef
      facility: local0
```

---

## Elastic SIEM 集成

### Beats 推送

使用 Filebeat 读取 AgentOS 日志文件并推送至 Elastic：

```yaml
# filebeat.yml
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /var/log/agentos/audit.log
    json.keys_under_root: true
    fields:
      log_type: agentos_audit
    fields_under_root: true

output.elasticsearch:
  hosts: ["elastic.company.com:9200"]
  index: "agentos-audit-%{+yyyy.MM.dd}"

setup.ilm.enabled: true
setup.ilm.rollover_alias: "agentos-audit"
setup.ilm.pattern: "{now/d}-000001"
```

---

## 合规报告模板

### SOC 2 报告字段

```json
{
  "compliance_type": "SOC2",
  "report_period": {
    "start": "2026-01-01",
    "end": "2026-03-31"
  },
  "controls_tested": [
    {
      "control_id": "CC6.1",
      "description": "Logical and physical access controls",
      "test_results": [
        {
          "requirement": "Authentication events logged",
          "status": "pass",
          "evidence": "100% of auth events captured"
        }
      ]
    }
  ],
  "exceptions": [],
  "auditor": "External Auditor Name",
  "report_date": "2026-04-15"
}
```

### GDPR 合规字段

```json
{
  "compliance_type": "GDPR",
  "data_processing": {
    "purpose": "AI Agent Operation",
    "legal_basis": "Legitimate interest",
    "data_categories": ["user_identifiers", "conversations", "agent_interactions"],
    "retention_period_days": 90
  },
  "rights_supported": {
    "access": true,
    "rectification": true,
    "erasure": true,
    "portability": true
  },
  "incidents": []
}
```

---

## 集成检查清单

- [ ] 确定 SIEM 产品（Splunk/QRadar/ArcSight/Elastic）
- [ ] 获取 SIEM collector endpoint 和认证凭证
- [ ] 配置 Syslog 或 Webhook 端点
- [ ] 验证日志格式（CEF/LEEF/JSON）
- [ ] 测试事件推送和接收
- [ ] 配置告警规则和 dashboard
- [ ] 验证合规报告生成