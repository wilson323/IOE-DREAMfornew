@echo off
REM IOE-DREAM 微服务启动脚本 (Windows版本)
REM 作者: 老王 (AI工程师)
REM 版本: 1.0.0

setlocal enabledelayedexpansion

REM 颜色定义
set "RED=[91m"
set "GREEN=[92m"
set "YELLOW=[93m"
set "BLUE=[94m"
set "NC=[0m"

echo %BLUE%[INFO]%NC% IOE-DREAM 微服务启动脚本 (Windows版本)
echo %BLUE%[INFO]%NC% ============================================

REM 检查Java环境
echo %BLUE%[INFO]%NC% 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo %RED%[ERROR]%NC% Java未安装，请先安装Java 17+
    pause
    exit /b 1
)
echo %GREEN%[SUCCESS]%NC% Java环境检查通过

REM 检查Maven环境
echo %BLUE%[INFO]%NC% 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo %RED%[ERROR]%NC% Maven未安装，请先安装Maven
    pause
    exit /b 1
)
echo %GREEN%[SUCCESS]%NC% Maven环境检查通过

REM 切换到微服务目录
cd /d "%~dp0..\microservices"

REM 创建日志目录
if not exist logs mkdir logs

REM 编译通用模块
echo %BLUE%[INFO]%NC% 编译通用模块...
call mvn clean install -pl microservices-common -am -DskipTests -q
if %errorlevel% neq 0 (
    echo %RED%[ERROR]%NC% 通用模块编译失败
    pause
    exit /b 1
)
echo %GREEN%[SUCCESS]%NC% 通用模块编译完成

echo.
echo %BLUE%[INFO]%NC% 开始启动IOE-DREAM微服务...
echo %BLUE%[INFO]%NC% ============================================

REM 启动配置中心
echo %BLUE%[INFO]%NC% 启动配置中心服务 (端口: 8888)...
cd ioedream-config-service
start "Config Service" cmd /c "mvn spring-boot:run > ../logs/config.log 2>&1"
cd ..
timeout /t 5 >nul

REM 启动认证服务
echo %BLUE%[INFO]%NC% 启动认证服务 (端口: 8889)...
cd ioedream-auth-service
start "Auth Service" cmd /c "mvn spring-boot:run > ../logs/auth.log 2>&1"
cd ..
timeout /t 5 >nul

REM 启动API网关
echo %BLUE%[INFO]%NC% 启动API网关 (端口: 8080)...
cd ioedream-gateway-service
start "Gateway Service" cmd /c "mvn spring-boot:run > ../logs/gateway.log 2>&1"
cd ..
timeout /t 5 >nul

REM 启动其他核心服务
echo %BLUE%[INFO]%NC% 启动其他核心服务...

REM 启动设备管理服务
if exist ioedream-device-service (
    echo %BLUE%[INFO]%NC% 启动设备管理服务 (端口: 8081)...
    cd ioedream-device-service
    start "Device Service" cmd /c "mvn spring-boot:run > ../logs/device.log 2>&1"
    cd ..
    timeout /t 3 >nul
)

REM 启动监控服务
if exist ioedream-monitor-service (
    echo %BLUE%[INFO]%NC% 启动监控服务 (端口: 8083)...
    cd ioedream-monitor-service
    start "Monitor Service" cmd /c "mvn spring-boot:run > ../logs/monitor.log 2>&1"
    cd ..
    timeout /t 3 >nul
)

REM 启动OA服务
if exist ioedream-oa-service (
    echo %BLUE%[INFO]%NC% 启动OA服务 (端口: 8084)...
    cd ioedream-oa-service
    start "OA Service" cmd /c "mvn spring-boot:run > ../logs/oa.log 2>&1"
    cd ..
    timeout /t 3 >nul
)

REM 启动报表服务
if exist ioedream-report-service (
    echo %BLUE%[INFO]%NC% 启动报表服务 (端口: 8085)...
    cd ioedream-report-service
    start "Report Service" cmd /c "mvn spring-boot:run > ../logs/report.log 2>&1"
    cd ..
    timeout /t 3 >nul
)

REM 启动视频服务
if exist ioedream-video-service (
    echo %BLUE%[INFO]%NC% 启动视频监控服务 (端口: 8086)...
    cd ioedream-video-service
    start "Video Service" cmd /c "mvn spring-boot:run > ../logs/video.log 2>&1"
    cd ..
    timeout /t 3 >nul
)

echo.
echo %GREEN%[SUCCESS]%NC% IOE-DREAM微服务启动完成！
echo.
echo %BLUE%[INFO]%NC% 服务访问地址：
echo   - API网关: http://localhost:8080
echo   - 认证服务: http://localhost:8889
echo   - 配置中心: http://localhost:8888
echo   - 监控服务: http://localhost:8083
echo.
echo %YELLOW%[WARNING]%NC% 请等待服务完全启动（约1-2分钟）
echo %YELLOW%[WARNING]%NC% 服务日志位于 logs/ 目录
echo.
echo %BLUE%[INFO]%NC% 使用 Ctrl+C 停止脚本，服务将继续在后台运行

REM 等待用户输入
pause