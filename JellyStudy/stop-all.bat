@echo off
chcp 65001 >nul
title JellyStudy 一键停止系统

echo.
echo ╔══════════════════════════════════════════╗
echo ║     🛑 JellyStudy 一键停止系统 v1.0      ║
echo ╚══════════════════════════════════════════╝
echo.

set DOCKER_COMPOSE_PATH=e:\大三下\移动应用\11\JellyStudy

echo ========================================
echo  🛑 第一步：停止 Docker 容器
echo ========================================
echo.

cd /d "%DOCKER_COMPOSE_PATH%"

echo [停止] 正在停止所有容器...
docker-compose down

echo.
echo [✅] 所有Docker容器已停止！

echo.
echo ========================================
echo  🛑 第二步：停止 Nacos
echo ========================================
echo.

cd /d "E:\tools\nacos\2.5.1\nacos\bin"

if exist "%NACOS_PATH%\shutdown.cmd" (
    echo [停止] 正在停止 Nacos...
    call shutdown.cmd
    echo [✅] Nacos 已停止！
) else (
    echo [提示] 请手动关闭 Nacos 命令行窗口（如果正在运行）
)

echo.
echo ========================================
echo  🛑 第三步：停止 MongoDB
echo ========================================
echo.

tasklist /FI "IMAGENAME eq mongod.exe" 2>NUL | find /I /N "mongod.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [停止] 正在停止 MongoDB...
    taskkill /F /IM mongod.exe >nul 2>&1
    timeout /t 2 /nobreak >nul
    echo [✅] MongoDB 已停止！
) else (
    echo [提示] MongoDB 未在运行
)

echo.
echo ╔══════════════════════════════════════════╗
echo ║  ✅ 所有服务已成功停止！                   ║
echo ╚══════════════════════════════════════════╝
echo.

pause
