@echo off
chcp 65001 >nul
title JellyStudy 一键启动系统

echo.
echo ╔══════════════════════════════════════════╗
echo ║     🚀 JellyStudy 一键启动系统 v1.0      ║
echo ║     作者: AI Assistant                    ║
echo ╚══════════════════════════════════════════╝
echo.

:: 设置变量
set MONGO_PATH=C:\Program Files\MongoDB\Server\8.0\bin
set NACOS_PATH=E:\tools\nacos\2.5.1\nacos\bin
set DOCKER_COMPOSE_PATH=e:\大三下\移动应用\11\JellyStudy
set DATA_DB_PATH=C:\data\db

:: 检查MongoDB数据目录
if not exist "%DATA_DB_PATH%" (
    echo [INFO] 创建 MongoDB 数据目录...
    mkdir "%DATA_DB_PATH%"
)

echo.
echo ========================================
echo  📦 第一步：启动 MongoDB 数据库
echo ========================================
echo.

:: 检查MongoDB是否已在运行
tasklist /FI "IMAGENAME eq mongod.exe" 2>NUL | find /I /N "mongod.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo [✅] MongoDB 已经在运行中！
) else (
    echo [启动] 正在启动 MongoDB...
    start "MongoDB Server" /MIN cmd /c ""%MONGO_PATH%\mongod.exe" --dbpath "%DATA_DB_PATH%" --logpath "%DATA_DB_PATH%\mongod.log""
    
    :: 等待MongoDB启动（最多等待10秒）
    echo [等待] 等待 MongoDB 启动...
    timeout /t 5 /nobreak >nul
    
    :: 再次检查
    tasklist /FI "IMAGENAME eq mongod.exe" 2>NUL | find /I /N "mongod.exe">NUL
    if "%ERRORLEVEL%"=="0" (
        echo [✅] MongoDB 启动成功！端口: 27017
    ) else (
        echo [❌] MongoDB 启动失败，请检查路径：%MONGO_PATH%
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo  ⚙️  第二步：启动 Nacos 配置中心
echo ========================================
echo.

:: 检查Nacos是否已在运行（通过Java进程）
wmic process where "commandline like '%%nacos%%'" get processid 2>NUL | findstr /R "[0-9]" >NUL
if "%ERRORLEVEL%"=="0" (
    echo [✅] Nacos 已经在运行中！
) else (
    echo [启动] 正在启动 Nacos (standalone模式)...
    cd /d "%NACOS_PATH%"
    start "Nacos Server" /MIN cmd /c "startup.cmd -m standalone"
    
    :: 等待Nacos启动（最多等待30秒）
    echo [等待] 等待 Nacos 启动（约15-20秒）...
    timeout /t 20 /nobreak >nul
    
    :: 检查8848端口
netstat -an | findstr ":8848.*LISTENING">NUL
    if "%ERRORLEVEL%"=="0" (
        echo [✅] Nacos 启动成功！端口: 8848
    ) else (
        echo [⏳] Nacos 可能还在启动中，继续执行...
    )
)

echo.
echo ========================================
echo  🐳 第三步：启动 Docker 服务
echo ========================================
echo.

cd /d "%DOCKER_COMPOSE_PATH%"

echo [检查] 检查 Docker 是否运行...
docker info >NUL 2>&1
if "%ERRORLEVEL%" NEQ "0" (
    echo [❌] Docker 未运行，请先启动 Docker Desktop！
    pause
    exit /b 1
)
echo [✅] Docker 运行正常！

echo.
echo [构建] 停止旧容器并重新构建...
docker-compose down --remove-orphans

echo.
echo [启动] 启动所有容器 (Redis + RabbitMQ + 应用服务)...
docker-compose up -d

echo.
echo [等待] 等待服务启动（约10-15秒）...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo  ✅ 第四步：验证服务状态
echo ========================================
echo.

echo.
echo 📊 容器运行状态：
echo ──────────────────────────────
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr "jelly"
echo ──────────────────────────────

echo.
echo 🔗 服务访问地址：
echo   ├─ 🌐 前端页面: qianduan\index.html
echo   ├─ 💻 主服务实例1: http://localhost:8086
echo   ├─ 💻 主服务实例2: http://localhost:8087
echo   ├─ 📊 评估服务:   http://localhost:8089
echo   ├─ 🗄️  MongoDB:   localhost:27017
echo   ├─ ⚙️  Nacos:     http://localhost:8848/nacos
echo   ├─ 🔴 Redis:     localhost:6379
echo   └─ 🐰 RabbitMQ:  http://localhost:15672 (admin/admin123)

echo.
echo ╔══════════════════════════════════════════╗
echo ║  🎉 所有服务启动完成！                     ║
echo ║  现在可以打开浏览器访问前端页面了！         ║
echo ╚══════════════════════════════════════════╝
echo.

pause
