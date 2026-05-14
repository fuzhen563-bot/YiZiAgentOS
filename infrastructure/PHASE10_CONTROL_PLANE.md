# AgentOS Phase 10: Enterprise Control Plane

> 构建企业级多租户管控体系，包括身份认证、权限管理、计费等。

## 交付物

| 组件 | 说明 |
|------|------|
| **Multi-Tenant** | 租户 CRUD + 数据隔离 + 资源配额 |
| **IAM** | 用户注册/登录 + 角色管理 + 状态管控 |
| **RBAC** | 5 个预置角色 (admin/owner/editor/viewer/member) + ABAC 策略 |
| **Billing** | 订阅管理 + 用量计量 + 账单生成 |
| **Audit** | API 级别内置 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/control/tenants | 创建租户 |
| GET | /api/v1/control/tenants | 租户列表 |
| GET | /api/v1/control/tenants/{id} | 租户详情 |
| POST | /api/v1/control/tenants/{id}/plan | 变更套餐 |
| GET | /api/v1/control/tenants/stats | 租户统计 |
| POST | /api/v1/control/users/register | 注册用户 |
| POST | /api/v1/control/users/login | 用户登录 |
| GET | /api/v1/control/users | 用户列表 |
| POST | /api/v1/control/users/{id}/role | 修改角色 |
| GET | /api/v1/control/roles | 角色列表 |
| POST | /api/v1/control/roles/check | 权限校验 |
| POST | /api/v1/control/policies | 添加策略 |
| GET | /api/v1/control/billing/{tenantId} | 账单 |
| POST | /api/v1/control/usage/record | 记录用量 |

## 套餐

| 套餐 | 价格 | 用户上限 | Agent 上限 | 存储 |
|------|------|----------|------------|------|
| Free | $0 | 10 | 5 | 2GB |
| Pro | $99/mo | 50 | 20 | 20GB |
| Enterprise | $499/mo | 1000 | 100 | 500GB |