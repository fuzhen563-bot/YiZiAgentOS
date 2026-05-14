@echo off
chcp 65001 >nul
title 亦梓AgentOS 启动器
setlocal enabledelayedexpansion

set JAVA=C:\Program Files\Java\jdk-26.0.1\bin\java.exe
set BASE=D:\万岳AgentOS\agentos
set KEY=sk-2igVg3QbH8AOHVjuixJR7uDGTUwimoZ1BSjCNwYFTe3toLFa

echo ╔══════════════════════════════════════╗
echo ║       亦梓AgentOS 启动器             ║
echo ╚══════════════════════════════════════╝
echo.

:: 检查 JAR 是否存在，不存在则编译
if not exist "%BASE%\model-gateway\target\model-gateway-1.0.0.jar" (
    echo [编译] model-gateway...
    cd /d "%BASE%\model-gateway"
    call mvn clean package -DskipTests -q -Paliyun-first
)
if not exist "%BASE%\agent-runtime\target\agent-runtime-1.0.0.jar" (
    echo [编译] agent-runtime...
    cd /d "%BASE%\agent-runtime"
    call mvn clean package -DskipTests -q -Paliyun-first
)
if not exist "%BASE%\mcp-registry\target\mcp-registry-1.0.0.jar" (
    echo [编译] mcp-registry...
    cd /d "%BASE%\mcp-registry"
    call mvn clean package -DskipTests -q -Paliyun-first
)

echo.
echo [启动] 基础设施服务...
start "agent-runtime" /B "%JAVA%" -jar "%BASE%\agent-runtime\target\agent-runtime-1.0.0.jar" --server.port=9095
start "mcp-registry" /B "%JAVA%" -jar "%BASE%\mcp-registry\target\mcp-registry-1.0.0.jar" --server.port=9092
timeout /t 20 >nul

echo [启动] 其他服务...
start "goal-engine" /B "%JAVA%" -jar "%BASE%\goal-engine\target\goal-engine-1.0.0.jar" --server.port=9096
start "control-plane" /B "%JAVA%" -jar "%BASE%\control-plane\target\control-plane-1.0.0.jar" --server.port=9097
start "secure-exec" /B "%JAVA%" -jar "%BASE%\secure-execution\target\secure-execution-1.0.0.jar" --server.port=9093
start "knowledge" /B "%JAVA%" -jar "%BASE%\knowledge-engine\target\knowledge-engine-1.0.0.jar" --server.port=9091
start "evolution" /B "%JAVA%" -jar "%BASE%\evolution-engine\target\evolution-engine-1.0.0.jar" --server.port=9094
timeout /t 15 >nul

echo [启动] Model Gateway（可能需要较长时间）...
start "model-gateway" /B "%JAVA%" -jar "%BASE%\model-gateway\target\model-gateway-1.0.0.jar" --server.port=9090 --OPENAI_API_KEY=%KEY% --OPENAI_API_BASE=https://api.yiziyun.com --OPENAI_MODEL=deepseek-v4-flash

echo.
echo ╔══════════════════════════════════════╗
echo ║  服务启动中，请稍候...               ║
echo ║                                      ║
echo ║  Web UI: 打开 web-ui\index.html      ║
echo ║  Admin:  打开 web-ui\admin\index.html ║
echo ╚══════════════════════════════════════╝
echo.

:: 检查服务状态
:check
timeout /t 5 >nul
set up=0
for %%p in (9092 9095) do (
    curl -sf http://localhost:%%p/actuator/health >nul 2>&1 && set /a up+=1
)
if %up% lss 2 goto check

echo [完成] 核心服务已就绪 (MCP + Agent Runtime)
echo [状态] 查看 web-ui/index.html 即可开始使用
pause