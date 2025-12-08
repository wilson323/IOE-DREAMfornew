@echo off
REM ================================================================================
REM IOE-DREAM 开发环境启动脚本 (Windows批处理版本)
REM ================================================================================
REM 功能：按正确顺序启动后端网关 → 前端服务
REM ================================================================================

setlocal enabledelayedexpansion
chcp 65001 >nul

echo ========================================
echo IOE-DREAM 开发环境启动
echo ========================================
echo.

REM 1️⃣ 检查依赖服务
echo [1/4] 检查依赖服务...
echo.

REM 检查MySQL
docker ps --filter "name=ioedream-mysql" --format "{{.Names}}" > temp_mysql.txt
set /p MYSQL_RUNNING=<temp_mysql.txt
del temp_mysql.txt

if not "!MYSQL_RUNNING!"=="ioedream-mysql" (
    echo ❌ MySQL未运行，启动中...
    docker-compose -f docker-compose-services.yml up -d mysql
    timeout /t 10 /nobreak >nul
) else (
    echo ✅ MySQL运行中
)

REM 检查Redis
docker ps --filter "name=ioedream-redis" --format "{{.Names}}" > temp_redis.txt
set /p REDIS_RUNNING=<temp_redis.txt
del temp_redis.txt

if not "!REDIS_RUNNING!"=="ioedream-redis" (
    echo ❌ Redis未运行，启动中...
    docker-compose -f docker-compose-services.yml up -d redis
    timeout /t 5 /nobreak >nul
) else (
    echo ✅ Redis运行中
)

REM 2️⃣ 启动后端网关
echo.
echo [2/4] 启动后端网关服务...
cd /d "d:\IOE-DREAM\microservices\ioedream-gateway-service"

start "IOE-DREAM 网关服务" cmd /c "mvn spring-boot:run -DskipTests"
echo ✅ 网关服务启动中...

REM 3️⃣ 等待网关健康检查
echo.
echo [3/4] 等待网关服务就绪...
set MAX_RETRY=30
set RETRY_COUNT=0
set GATEWAY_READY=0

:CHECK_GATEWAY
if !RETRY_COUNT! GEQ !MAX_RETRY! goto GATEWAY_TIMEOUT

curl -s -o nul -w "%%{http_code}" http://127.0.0.1:8080/actuator/health > temp_status.txt 2>nul
set /p HTTP_STATUS=<temp_status.txt
del temp_status.txt 2>nul

if "!HTTP_STATUS!"=="200" (
    set GATEWAY_READY=1
    echo ✅ 网关服务就绪！
    goto GATEWAY_READY_LABEL
)

set /a RETRY_COUNT+=1
echo 等待网关启动... (!RETRY_COUNT!/!MAX_RETRY!)
timeout /t 2 /nobreak >nul
goto CHECK_GATEWAY

:GATEWAY_TIMEOUT
echo ❌ 网关启动超时，请检查日志
pause
exit /b 1

:GATEWAY_READY_LABEL

REM 4️⃣ 启动前端服务
echo.
echo [4/4] 启动前端服务...
cd /d "d:\IOE-DREAM\smart-admin-web-javascript"

start "IOE-DREAM 前端服务" cmd /c "npm run dev"
echo ✅ 前端服务启动中...

REM 5️⃣ 启动完成
echo.
echo ========================================
echo 🚀 IOE-DREAM 开发环境启动完成！
echo ========================================
echo 📱 前端访问: http://localhost:8081
echo 🌐 网关API: http://localhost:8080
echo 📊 监控面板: http://localhost:8080/actuator
echo.
echo 💡 按任意键退出此窗口（服务将继续运行）
echo 💡 关闭服务窗口可停止对应服务
echo ========================================
echo.

pause
