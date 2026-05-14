@echo off
chcp 65001 >nul
title 亦梓AgentOS Web Server
echo ╔══════════════════════════════════════╗
echo ║   亦梓AgentOS Web UI 服务           ║
echo ╚══════════════════════════════════════╝
echo.
echo 启动 HTTP 服务: http://localhost:8080
echo.
echo 聊天界面: http://localhost:8080/index.html
echo 管理后台: http://localhost:8080/admin/index.html
echo.
echo 按 Ctrl+C 停止服务
echo.

cd /d "D:\万岳AgentOS\agentos\web-ui"

:: 尝试用 Python 启动 HTTP 服务
where python3 >nul 2>&1
if %ERRORLEVEL%==0 (
    python3 -m http.server 8080
    goto :eof
)

where python >nul 2>&1
if %ERRORLEVEL%==0 (
    python -m http.server 8080
    goto :eof
)

:: 如果没有 Python，用 node
where npx >nul 2>&1
if %ERRORLEVEL%==0 (
    npx http-server -p 8080 --cors
    goto :eof
)

echo 错误: 未找到 python 或 node，请安装 Python 3
pause