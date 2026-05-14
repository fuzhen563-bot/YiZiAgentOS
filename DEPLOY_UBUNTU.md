# AgentOS Ubuntu 部署教程

> 在 Ubuntu 22.04/24.04 LTS 上一键部署全部 8 层服务

---

## 环境要求

| 项目 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4 核 | 8 核 |
| 内存 | 8 GB | 16 GB |
| 磁盘 | 50 GB | 100 GB |
| OS | Ubuntu 22.04+ | Ubuntu 24.04 LTS |
| Docker | 24+ | 27+ |
| Java | 21 (Temurin) | 21 (Temurin) |
| Node.js | 22+ | 22+ |

---

## 一、基础环境安装

```bash
# 1. 系统更新
sudo apt update && sudo apt upgrade -y

# 2. 安装基础工具
sudo apt install -y curl wget git unzip build-essential

# 3. 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
# 重新登录使 docker 组生效
newgrp docker

# 4. 安装 Docker Compose
sudo apt install -y docker-compose-plugin

# 5. 安装 JDK 21 (Temurin)
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/deb $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-21-jdk

# 6. 验证安装
java -version
docker --version
docker compose version
```

---

## 二、下载项目

```bash
# 克隆项目
git clone <your-repo-url> agentos
cd agentos

# 查看项目结构
ls -la
# 应包含: model-gateway/ knowledge-engine/ mcp-registry/ secure-execution/ evolution-engine/ agent-runtime/ infrastructure/
```

---

## 三、配置环境变量

```bash
# 复制环境变量模板
cp infrastructure/docker/.env.example infrastructure/docker/.env

# 编辑 .env 文件
nano infrastructure/docker/.env
```

**.env 内容示例：**

```bash
# 数据库
DB_PASSWORD=your-strong-password-here

# 对象存储
S3_SECRET_KEY=your-minio-secret-key

# JWT 密钥
JWT_SECRET=your-jwt-secret-at-least-32-chars

# API Keys（模型网关层需要）
OPENAI_API_KEY=sk-your-openai-key
ANTHROPIC_API_KEY=sk-ant-your-claude-key
DASHSCOPE_API_KEY=sk-your-dashscope-key
DEEPSEEK_API_KEY=sk-your-deepseek-key

# Grafana
GRAFANA_PASSWORD=admin123
```

---

## 四、启动基础设施（PostgreSQL + Redis + MinIO + 监控）

```bash
cd infrastructure/docker

# 启动所有基础设施服务
docker compose up -d

# 查看状态
docker compose ps

# 验证各服务
echo "PostgreSQL: $(docker exec agentos-postgres pg_isready -U agentos -d agentos)"
echo "Redis: $(docker exec agentos-redis redis-cli ping)"
echo "Qdrant: $(curl -s http://localhost:6333/health | head -c 50)"
echo "MinIO: $(curl -s http://localhost:9001 | head -c 50)"
echo "Grafana: $(curl -s http://localhost:3000/api/health | head -c 50)"
```

| 服务 | 端口 | 访问地址 |
|------|------|----------|
| PostgreSQL | 5432 | localhost:5432 |
| Redis | 6379 | localhost:6379 |
| Qdrant | 6333 | http://localhost:6333 |
| MinIO | 9000/9001 | http://localhost:9001 |
| Grafana | 3000 | http://localhost:3000 |
| Prometheus | 9090 | http://localhost:9090 |

---

## 五、编译构建各服务

```bash
# 在项目根目录执行

# 1. 模型网关 (Model Gateway) - Layer 9
cd model-gateway
mvn clean package -DskipTests -q
cd ..

# 2. 知识引擎 (Knowledge Engine) - Layer 8
cd knowledge-engine
mvn clean package -DskipTests -q
cd ..

# 3. MCP 注册中心 (MCP Registry) - Layer 7
cd mcp-registry
mvn clean package -DskipTests -q
cd ..

# 4. 安全执行层 (Secure Execution) - Layer 6
cd secure-execution
mvn clean package -DskipTests -q
cd ..

# 5. 进化引擎 (Evolution Engine) - Layer 5
cd evolution-engine
mvn clean package -DskipTests -q
cd ..

# 6. Agent 运行时 (Agent Runtime) - Layer 4
cd agent-runtime
mvn clean package -DskipTests -q
cd ..

echo "所有服务编译完成！"
```

---

## 六、启动全部服务

```bash
# 创建日志目录
sudo mkdir -p /var/log/agentos
sudo chown $USER:$USER /var/log/agentos

# 启动脚本：start-all.sh
cat > start-all.sh << 'EOF'
#!/bin/bash
set -e

PROJECT_DIR=$(pwd)
LOG_DIR=/var/log/agentos
mkdir -p $LOG_DIR

echo "启动 Model Gateway (端口 9090)..."
java -jar $PROJECT_DIR/model-gateway/target/*.jar \
  --server.port=9090 \
  --OPENAI_API_KEY=${OPENAI_API_KEY:-} \
  --ANTHROPIC_API_KEY=${ANTHROPIC_API_KEY:-} \
  --DASHSCOPE_API_KEY=${DASHSCOPE_API_KEY:-} \
  --DEEPSEEK_API_KEY=${DEEPSEEK_API_KEY:-} \
  > $LOG_DIR/model-gateway.log 2>&1 &
echo $! > /tmp/agentos-pids/model-gateway.pid
sleep 3

echo "启动 Knowledge Engine (端口 9091)..."
java -jar $PROJECT_DIR/knowledge-engine/target/*.jar \
  --server.port=9091 \
  --OPENAI_API_KEY=${OPENAI_API_KEY:-} \
  > $LOG_DIR/knowledge-engine.log 2>&1 &
echo $! > /tmp/agentos-pids/knowledge-engine.pid
sleep 3

echo "启动 MCP Registry (端口 9092)..."
java -jar $PROJECT_DIR/mcp-registry/target/*.jar \
  --server.port=9092 \
  > $LOG_DIR/mcp-registry.log 2>&1 &
echo $! > /tmp/agentos-pids/mcp-registry.pid
sleep 2

echo "启动 Secure Execution (端口 9093)..."
java -jar $PROJECT_DIR/secure-execution/target/*.jar \
  --server.port=9093 \
  > $LOG_DIR/secure-execution.log 2>&1 &
echo $! > /tmp/agentos-pids/secure-execution.pid
sleep 2

echo "启动 Evolution Engine (端口 9094)..."
java -jar $PROJECT_DIR/evolution-engine/target/*.jar \
  --server.port=9094 \
  > $LOG_DIR/evolution-engine.log 2>&1 &
echo $! > /tmp/agentos-pids/evolution-engine.pid
sleep 2

echo "启动 Agent Runtime (端口 9095)..."
java -jar $PROJECT_DIR/agent-runtime/target/*.jar \
  --server.port=9095 \
  > $LOG_DIR/agent-runtime.log 2>&1 &
echo $! > /tmp/agentos-pids/agent-runtime.pid
sleep 2

echo ""
echo "所有服务已启动！"
echo "查看日志: tail -f /var/log/agentos/*.log"
echo "健康检查: ./health-check.sh"
EOF

chmod +x start-all.sh

# 创建 PID 目录
mkdir -p /tmp/agentos-pids

# 启动
./start-all.sh
```

---

## 七、验证部署

```bash
# 健康检查脚本：health-check.sh
cat > health-check.sh << 'EOF'
#!/bin/bash
echo "=== AgentOS 服务健康检查 ==="
echo ""

SERVICES=(
  "Model Gateway:9090/actuator/health"
  "Knowledge Engine:9091/actuator/health"
  "MCP Registry:9092/actuator/health"
  "Secure Execution:9093/actuator/health"
  "Evolution Engine:9094/actuator/health"
  "Agent Runtime:9095/actuator/health"
)

for entry in "${SERVICES[@]}"; do
  name="${entry%%:*}"
  port="${entry##*:}"
  url="http://localhost:${port#*/}"
  path="${entry#*:}"
  path="/${path#*/}"
  
  if curl -sf "http://localhost:${port%%/*}$path" > /dev/null 2>&1; then
    echo "  ✅ $name (port ${port%%/*})"
  else
    echo "  ❌ $name (port ${port%%/*})"
  fi
done

echo ""
echo "基础设施状态:"
docker ps --format "  {{.Names}}: {{.Status}}" 2>/dev/null | grep agentos || echo "  (docker compose 未运行)"
EOF

chmod +x health-check.sh

# 运行健康检查
./health-check.sh

# 预期输出:
#   ✅ Model Gateway (port 9090)
#   ✅ Knowledge Engine (port 9091)
#   ✅ MCP Registry (port 9092)
#   ✅ Secure Execution (port 9093)
#   ✅ Evolution Engine (port 9094)
#   ✅ Agent Runtime (port 9095)
#   agentos-postgres: Up 2 minutes
#   agentos-redis: Up 2 minutes
#   agentos-grafana: Up 2 minutes
```

---

## 八、功能测试

```bash
# 1. 测试模型网关 - LLM 调用
curl -s -X POST http://localhost:9090/api/v1/gateway/chat \
  -H "Content-Type: application/json" \
  -d '{"model":"gpt-4o-mini","prompt":"Hello, what is 2+2?"}' | python3 -m json.tool

# 2. 测试 MCP 工具注册 - 列出内置工具
curl -s http://localhost:9092/api/v1/mcp/tools | python3 -m json.tool

# 3. 测试知识引擎 - 导入知识
curl -s -X POST http://localhost:9091/api/v1/knowledge/ingest \
  -H "Content-Type: application/json" \
  -d '{"title":"AgentOS Guide","content":"AgentOS is an enterprise AI Agent operating system.","type":"MARKDOWN","source":"docs"}' | python3 -m json.tool

# 4. 测试知识检索
curl -s "http://localhost:9091/api/v1/knowledge/query?q=AgentOS&topK=3" | python3 -m json.tool

# 5. 测试策略守卫 - 验证 shell 被阻止
curl -s -X POST http://localhost:9093/api/v1/secure/policy/evaluate \
  -H "Content-Type: application/json" \
  -d '{"action":"shell.exec","resource":"shell"}' | python3 -m json.tool
# 预期: blocked = true

# 6. 测试 Agent 运行时 - 列出所有 Agent
curl -s http://localhost:9095/api/v1/runtime/agents | python3 -m json.tool

# 7. 测试 Agent 路由 - 销售问题自动路由到 SalesBot
curl -s -X POST http://localhost:9095/api/v1/runtime/route \
  -H "Content-Type: application/json" \
  -d '{"message":"I need help with a sales question","userId":"test"}' | python3 -m json.tool

# 8. 测试工作流
WF_ID=$(curl -s -X POST http://localhost:9095/api/v1/runtime/workflows/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Workflow","description":"test"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['workflowId'])")
echo "Workflow ID: $WF_ID"
curl -s -X POST "http://localhost:9095/api/v1/runtime/workflows/$WF_ID/execute" | python3 -m json.tool
```

---

## 九、日志查看

```bash
# 实时查看所有日志
tail -f /var/log/agentos/*.log

# 查看具体服务日志
tail -f /var/log/agentos/model-gateway.log
tail -f /var/log/agentos/agent-runtime.log

# 通过 Loki 查询日志（需 Grafana 已配置）
# 访问 http://localhost:3000 → Explore → Loki
# 查询: {app="agentos"}
```

---

## 十、停止服务

```bash
# 停止脚本：stop-all.sh
cat > stop-all.sh << 'EOF'
#!/bin/bash
echo "停止所有 AgentOS 服务..."
for pidfile in /tmp/agentos-pids/*.pid; do
  if [ -f "$pidfile" ]; then
    pid=$(cat "$pidfile")
    service=$(basename "$pidfile" .pid)
    if kill -0 $pid 2>/dev/null; then
      kill $pid
      echo "  ✅ 已停止 $service (PID: $pid)"
    else
      echo "  ⚠️  $service 未运行"
    fi
    rm -f "$pidfile"
  fi
done

echo ""
echo "停止基础设施..."
cd infrastructure/docker
docker compose down
echo "  ✅ 已停止所有容器"
EOF

chmod +x stop-all.sh
./stop-all.sh
```

---

## 十一、Systemd 服务（开机自启）

```bash
# 为每个服务创建 systemd 单元文件
# 以 Model Gateway 为例:

sudo tee /etc/systemd/system/agentos-model-gateway.service << 'SERVICE'
[Unit]
Description=AgentOS Model Gateway
After=network.target docker.service
Requires=docker.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/agentos
Environment="OPENAI_API_KEY=sk-your-key"
ExecStart=/usr/bin/java -jar /home/ubuntu/agentos/model-gateway/target/*.jar --server.port=9090
Restart=on-failure
RestartSec=10
StandardOutput=append:/var/log/agentos/model-gateway.log
StandardError=append:/var/log/agentos/model-gateway.log

[Install]
WantedBy=multi-user.target
SERVICE

# 对其他服务重复类似配置...

# 启用服务
sudo systemctl daemon-reload
sudo systemctl enable agentos-model-gateway
sudo systemctl start agentos-model-gateway
sudo systemctl status agentos-model-gateway
```

---

## 十二、服务端口汇总

| 服务 | 端口 | 层 |
|------|------|----|
| PostgreSQL | 5432 | Layer 11 |
| Redis | 6379 | Layer 11 |
| Qdrant | 6333 | Layer 11 |
| MinIO | 9000/9001 | Layer 11 |
| Grafana | 3000 | Layer 10 |
| Prometheus | 9090 | Layer 10 |
| **Model Gateway** | **9090** | Layer 9 |
| **Knowledge Engine** | **9091** | Layer 8 |
| **MCP Registry** | **9092** | Layer 7 |
| **Secure Execution** | **9093** | Layer 6 |
| **Evolution Engine** | **9094** | Layer 5 |
| **Agent Runtime** | **9095** | Layer 4 |

---

## 常见问题

### Q: 服务启动失败怎么办？
```bash
# 查看具体错误
journalctl -u agentos-model-gateway --no-pager -n 50
# 或查看日志文件
tail -50 /var/log/agentos/model-gateway.log
```

### Q: 数据库连接失败？
```bash
# 检查 PostgreSQL 是否运行
docker ps | grep postgres
# 测试连接
docker exec agentos-postgres pg_isready -U agentos -d agentos
```

### Q: 端口被占用？
```bash
# 查找占用端口的进程
sudo lsof -i :9090
# 或
sudo ss -tlnp | grep 9090
```

### Q: 如何更新服务？
```bash
# 停止服务 → 重新编译 → 启动
cd agentos/model-gateway
git pull
mvn clean package -DskipTests -q
sudo systemctl restart agentos-model-gateway
```

### Q: MinIO 初始化
```bash
# 创建默认 bucket
docker exec agentos-minio mc alias set local http://localhost:9000 agentos $S3_SECRET_KEY
docker exec agentos-minio mc mb local/agentos-data
docker exec agentos-minio mc mb local/agentos-backups
```