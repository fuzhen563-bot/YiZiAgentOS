# AgentOS Phase 7: Hermes Evolution Layer

> 构建自我进化和技能生成体系，使 Agent 能够从经验中学习并生成可复用技能。

---

## 架构

```
┌──────────┐   ┌──────────────────────────────────────────────────────────────┐
│  Client  │──▶│               Evolution Engine                              │
└──────────┘   │                                                              │
               │  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐ │
               │  │  Reflection  │─▶│   SOP Engine  │─▶│  Skill Evolution  │ │
               │  │  (analyze/   │  │  (extract/    │  │  (create/mutate/  │ │
               │  │   optimize)  │  │   evaluate)   │  │   evaluate/score) │ │
               │  └──────────────┘  └──────────────┘  └────────────────────┘ │
               │                                          │                  │
               │                                          ▼                  │
               │  ┌──────────────────────────────────────────────────────┐   │
               │  │              Skill Marketplace                       │   │
               │  │  (publish / search / trending / top-rated / download)│   │
               │  └──────────────────────────────────────────────────────┘   │
               └──────────────────────────────────────────────────────────────┘
```

## 目录结构

```
evolution-engine/
├── pom.xml
└── src/main/java/com/agentos/evolution/
    ├── EvolutionEngineApplication.java
    ├── EvolutionController.java
    ├── reflection/
    │   └── ReflectionEngine.java       # 任务追踪 + 成功/失败分析 + 优化建议
    ├── sop/
    │   └── SopEngine.java               # 行为日志 + SOP 抽取 + 版本管理 + 质量评估
    ├── skill/
    │   └── SkillEvolutionEngine.java    # 技能生成/变异/验证/评分/淘汰
    ├── market/
    │   └── SkillMarketplace.java        # 发布/搜索/分类/排行/下载/评分
    └── config/
        └── EvolutionConfig.java
```

## 交付物清单

| 组件 | 状态 | 说明 |
|------|------|------|
| **Reflection** | ✅ | 任务追踪 + 成功/失败分析 + Prompt 优化 + 成本报告 |
| **SOP 抽取** | ✅ | 行为日志 + SOP 结构化抽取 + 质量评估 (excellent/good/fair/poor) |
| **技能进化** | ✅ | 技能生成/变异/沙箱验证/评分/低效淘汰 |
| **Marketplace** | ✅ | 发布/搜索/分类/排行/下载/评分/统计 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| **Reflection** | | |
| POST | /api/v1/evolution/tasks/start | 开始任务追踪 |
| POST | /api/v1/evolution/tasks/{id}/complete | 完成任务 |
| POST | /api/v1/evolution/tasks/{id}/fail | 标记失败 |
| GET | /api/v1/evolution/tasks/{id}/analyze | 分析报告 |
| GET | /api/v1/evolution/agents/{id}/summary | Agent 汇总 |
| **SOP** | | |
| POST | /api/v1/evolution/sop/extract | 从行为日志抽取 SOP |
| POST | /api/v1/evolution/sop/create | 手动创建 SOP |
| POST | /api/v1/evolution/sop/{id}/evaluate | 评估 SOP 质量 |
| **Skill** | | |
| POST | /api/v1/evolution/skills/create | 创建技能 |
| POST | /api/v1/evolution/skills/{id}/mutate | 变异生成 |
| POST | /api/v1/evolution/skills/{id}/evaluate | 评分 |
| POST | /api/v1/evolution/skills/{id}/validate | 沙箱验证 |
| GET | /api/v1/evolution/skills/candidates | 进化候选列表 |
| **Marketplace** | | |
| POST | /api/v1/evolution/market/publish | 发布技能 |
| GET | /api/v1/evolution/market/search?q= | 搜索技能 |
| GET | /api/v1/evolution/market/trending | 热门排行 |
| POST | /api/v1/evolution/market/{id}/download | 下载 |
| POST | /api/v1/evolution/market/{id}/rate | 评分 |

## 进化流水线

```
行为日志 → SOP 抽取 → 技能候选生成
                            ↓
                   沙箱验证 ← 技能变异
                            ↓
                         评分评估
                        /        \
                score >= 70    score < 40
                   ↓               ↓
               发布到市场       低效淘汰
```

## 质量评估标准

| 等级 | 分数 | 条件 |
|------|------|------|
| excellent | ≥ 80 | ≥ 3 步骤 + 预期结果完整 + 使用 ≥ 5 次 + 工具完备 |
| good | ≥ 50 | 基本步骤完整 |
| fair | ≥ 30 | 有步骤但缺少细节 |
| poor | < 30 | 不完整 |

## 本地启动

```bash
cd evolution-engine
mvn spring-boot:run
```

## K8s 部署

```bash
kubectl apply -f infrastructure/k8s/base/evolution-engine.yaml -n agentos
```

## 验收标准

Phase 7 验收标准：SOP 可从示范中抽取；Skill 可评分和发布

| 验证项 | 验证方式 |
|--------|----------|
| 任务分析 | `POST /tasks/start` → `POST /tasks/{id}/complete` → `GET /tasks/{id}/analyze` |
| SOP 抽取 | `POST /behavior/log` (3次) → `POST /sop/extract` 返回步骤 |
| 技能进化 | `POST /skills/create` → `POST /skills/{id}/mutate` → `POST /skills/{id}/evaluate` |
| 市场发布 | `POST /market/publish` → `GET /market/search?q=xxx` → `POST /market/{id}/download` |