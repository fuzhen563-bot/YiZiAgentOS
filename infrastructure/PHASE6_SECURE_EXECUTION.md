# AgentOS Phase 6: Secure Execution Layer

> 构建安全自动化执行环境，提供浏览器自动化、脚本执行、沙箱隔离和 Policy Guard。

---

## 架构

```
┌──────────┐   ┌──────────────────────────────────────────────────────────────┐
│  Client  │──▶│               Secure Execution                              │
└──────────┘   │                                                              │
               │  ┌────────────────────┐  ┌────────────────────┐              │
               │  │ Browser Automation │  │ Sandbox Environment│              │
               │  │ (session/navigate/ │  │ (create/exec/      │              │
               │  │  click/type/screen)│  │  destroy)          │              │
               │  └────────────────────┘  └────────────────────┘              │
               │                                                              │
               │  ┌────────────────────┐  ┌────────────────────┐              │
               │  │   Permission       │  │   Rollback         │              │
               │  │   Broker + Policy  │  │   Manager          │              │
               │  │   Guard           │  │   (snapshot/restore)│              │
               │  │   (evaluate/       │  │                    │              │
               │  │    approve/reject) │  │                    │              │
               │  └────────────────────┘  └────────────────────┘              │
               └──────────────────────────────────────────────────────────────┘
```

## 目录结构

```
secure-execution/
├── pom.xml
└── src/main/java/com/agentos/secure/
    ├── SecureExecutionApplication.java
    ├── SecureExecutionController.java
    ├── browser/
    │   ├── BrowserAutomation.java   # 浏览器自动化（session/navigate/click/type）
    │   └── BrowserSession.java       # 浏览器会话管理
    ├── sandbox/
    │   └── SandboxEnvironment.java   # 沙箱执行环境（create/exec/destroy）
    ├── policy/
    │   ├── PermissionBroker.java    # 权限注册 + 审批流程
    │   └── PolicyGuard.java          # 策略规则引擎（allow/deny/require_approval）
    ├── rollback/
    │   └── RollbackManager.java      # 操作回滚 + 快照恢复
    └── config/
        └── SecureExecutionConfig.java
```

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **浏览器自动化** | ✅ | Session 管理 + navigate/click/type/screenshot/evaluate |
| **沙箱执行** | ✅ | 实例创建/命令执行/资源清理 |
| **Permission Broker** | ✅ | 14 条默认权限 + 审批请求/批准/拒绝 |
| **Policy Guard** | ✅ | 规则引擎（allow/deny/require_approval）+ 通配符匹配 |
| **Rollback** | ✅ | 操作日志 + 快照 + 单条/批量回滚 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| **Browser** | | |
| POST | /api/v1/secure/browser/session | 创建浏览器会话 |
| POST | /api/v1/secure/browser/navigate | 导航到 URL |
| POST | /api/v1/secure/browser/click | 点击元素 |
| POST | /api/v1/secure/browser/type | 输入文本 |
| POST | /api/v1/secure/browser/screenshot | 截图 |
| POST | /api/v1/secure/browser/evaluate | 执行 JS |
| **Sandbox** | | |
| POST | /api/v1/secure/sandbox/create | 创建沙箱 |
| POST | /api/v1/secure/sandbox/exec | 执行命令 |
| DELETE | /api/v1/secure/sandbox/{id} | 销毁沙箱 |
| **Policy** | | |
| GET | /api/v1/secure/policy/permissions | 列出权限 |
| POST | /api/v1/secure/policy/evaluate | 评估操作 |
| POST | /api/v1/secure/policy/approval/request | 申请审批 |
| POST | /api/v1/secure/policy/approval/{id}/approve | 批准 |
| POST | /api/v1/secure/policy/approval/{id}/reject | 拒绝 |
| **Rollback** | | |
| POST | /api/v1/secure/rollback/begin | 开始操作 |
| POST | /api/v1/secure/rollback/{sessionId}/{opId} | 回滚单条 |
| POST | /api/v1/secure/rollback/{sessionId}/all | 回滚全部 |

## 默认权限策略

| 动作 | 资源 | 风险等级 | 需要审批 |
|------|------|----------|----------|
| file.write | filesystem | high | ✅ |
| file.delete | filesystem | critical | ✅ |
| file.read | filesystem | low | ❌ |
| shell.exec | shell | critical | ✅ |
| shell.read | shell | high | ✅ |
| network.http | network | low | ❌ |
| db.write | database | critical | ✅ |
| browser.* | browser | low | ❌ |
| email.send | email | high | ✅ |
| admin.config | admin | critical | ✅ |

## 默认策略规则

| 规则 | 效果 | 说明 |
|------|------|------|
| block-dangerous-shell | deny | 阻止所有 shell 执行 |
| block-rm-rf | deny | 阻止 `rm -rf` 命令 |
| block-format | deny | 阻止 `mkfs`/`format` 命令 |
| allow-browser | allow | 允许所有浏览器操作 |
| allow-file-read | allow | 允许文件读取 |
| require-approval-email | require_approval | 发邮件需要审批 |
| require-approval-db-write | require_approval | 数据库写入需要审批 |

## 本地启动

```bash
cd secure-execution
mvn spring-boot:run
```

## K8s 部署

```bash
kubectl apply -f infrastructure/k8s/base/secure-execution.yaml -n agentos
```

## 验收标准

Phase 6 验收标准：浏览器自动化可完成表单提交；沙箱隔离生效

| 验证项 | 验证方式 |
|--------|----------|
| 浏览器会话 | `curl -X POST /api/v1/secure/browser/session` 返回 sessionId |
| 策略评估 | `curl -X POST /api/v1/secure/policy/evaluate -d '{"action":"shell.exec"}'` 返回 denied |
| 审批流程 | 申请 → 批准 → 执行 完整链路 |
| 沙箱执行 | 创建沙箱 → 执行命令 → 销毁 |
| 回滚 | 开始操作 → 完成 → 回滚 |