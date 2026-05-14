# AgentOS Phase 9: Long-Horizon Goal Layer

> 实现跨时间、跨会话的长期目标追踪和任务管理。

## 交付物

| 组件 | 说明 |
|------|------|
| **Goal Registry** | 目标 CRUD、状态管理、KPI 追踪 |
| **Objective Engine** | 目标分解、进度计算、Deadline 监控 |
| **Task Graph** | 任务依赖图、状态管理、就绪队列 |
| **Checkpoint** | 快照保存/恢复、跨会话恢复 |
| **Replanning** | 失败重规划、步骤简化 |

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/v1/goals | 创建目标 |
| GET | /api/v1/goals | 列表 |
| GET | /api/v1/goals/{id} | 详情 |
| POST | /api/v1/goals/{id}/decompose | 分解为子任务 |
| POST | /api/v1/goals/{id}/status | 更新状态 |
| POST | /api/v1/goals/{id}/kpi | 设置 KPI |
| GET | /api/v1/goals/{id}/progress | 进度 |
| GET | /api/v1/goals/{id}/task-graph | 任务图 |
| POST | /api/v1/goals/{id}/checkpoint | 保存检查点 |
| GET | /api/v1/goals/{id}/recovery | 恢复方案 |
| GET | /api/v1/goals/overdue | 过期目标 |
| GET | /api/v1/goals/stats | 统计 |