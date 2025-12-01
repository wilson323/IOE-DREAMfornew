@echo off
chcp 65001 >nul
title IOE-DREAM 微服务启动管理器
color 0A

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                🚀 IOE-DREAM 微服务启动管理器                    ║
echo ║                    微服务架构 v2.0                           ║
echo ║                      启动时间: %date% %time%                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: 检查Java环境
echo 🔍 检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java未安装或未配置到PATH，请先安装Java 17+
    pause
    exit /b 1
)
echo ✅ Java环境检查通过

:: 检查Maven环境
echo 🔍 检查Maven环境...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven未安装或未配置到PATH，请先安装Maven
    pause
    exit /b 1
)
echo ✅ Maven环境检查通过

:: 设置环境变量
set JAVA_HOME=%JAVA_HOME%
set MAVEN_HOME=%MAVEN_HOME%
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

:: 显示菜单
:menu
echo.
echo ══════════════════════════════════════════════════════════════
echo 🔧 选择启动方式：
echo.
echo 1️⃣  启动所有微服务（推荐生产环境）
echo 2️⃣  启动基础设施服务（网关+配置中心+注册中心）
echo 3️⃣  启动核心业务服务（认证+设备+门禁）
echo 4️⃣  启动单个微服务
echo 5️⃣  编译所有微服务
echo 6️⃣  停止所有微服务
echo 0️⃣  退出
echo ══════════════════════════════════════════════════════════════
echo.
set /p choice="请选择操作 (0-6): "

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto start_infra
if "%choice%"=="3" goto start_core
if "%choice%"=="4" goto start_single
if "%choice%"=="5" goto compile_all
if "%choice%"=="6" goto stop_all
if "%choice%"=="0" goto exit
echo ⚠️ 无效选择，请重新输入
goto menu

:compile_all
echo.
echo 🔨 开始编译所有微服务...
echo.

:: 设置服务列表
set services=ioedream-gateway-service ioedream-config-service ioedream-auth-service ioedream-identity-service ioedream-access-service ioedream-device-service ioedream-attendance-service ioedream-video-service ioedream-consume-service ioedream-enterprise-service ioedream-oa-service ioedream-notification-service ioedream-audit-service ioedream-report-service analytics-service ioedream-monitor-service ioedream-scheduler-service ioedream-integration-service ioedream-infrastructure-service

:: 编译所有服务
for %%s in (%services%) do (
    echo 📦 编译 %%s...
    cd "%%s"
    call mvn clean compile -DskipTests
    if !errorlevel! neq 0 (
        echo ❌ %%s 编译失败
    ) else (
        echo ✅ %%s 编译成功
    )
    cd ..
)

echo.
echo ✅ 所有微服务编译完成！
pause
goto menu

:start_all
echo.
echo 🚀 启动所有微服务...
echo.

:: 启动基础设施服务
echo 1️⃣ 启动API网关服务 (端口: 8080)
start "Gateway-8080" cmd /k "cd ioedream-gateway-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 2️⃣ 启动配置中心服务 (端口: 8888)
start "Config-8888" cmd /k "cd ioedream-config-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 3️⃣ 启动身份认证服务 (端口: 8081)
start "Auth-8081" cmd /k "cd ioedream-auth-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 4️⃣ 启动身份管理服务 (端口: 8082)
start "Identity-8082" cmd /k "cd ioedream-identity-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 5️⃣ 启动门禁管理服务 (端口: 8090)
start "Access-8090" cmd /k "cd ioedream-access-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 6️⃣ 启动设备管理服务 (端口: 8093)
start "Device-8093" cmd /k "cd ioedream-device-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 7️⃣ 启动考勤管理服务 (端口: 8091)
start "Attendance-8091" cmd /k "cd ioedream-attendance-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 8️⃣ 启动视频监控服务 (端口: 8092)
start "Video-8092" cmd /k "cd ioedream-video-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 9️⃣ 启动消费管理服务 (端口: 8094)
start "Consume-8094" cmd /k "cd ioedream-consume-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 🔟 启动企业服务 (端口: 8083)
start "Enterprise-8083" cmd /k "cd ioedream-enterprise-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 1️⃣1️⃣ 启动通知服务 (端口: 8096)
start "Notification-8096" cmd /k "cd ioedream-notification-service && mvn spring-boot:run"

echo.
echo ✅ 所有微服务启动命令已执行！
echo 📊 服务启动状态：请等待1-2分钟后检查各服务端口
echo.
echo 🔗 关键访问地址：
echo 🌐 API网关: http://localhost:8080
echo ⚙️ 配置中心: http://localhost:8888
echo 🔐 认证服务: http://localhost:8081
echo.
pause
goto menu

:start_infra
echo.
echo 🏗️ 启动基础设施服务...
echo.

echo 1️⃣ 启动API网关服务
start "Gateway-8080" cmd /k "cd ioedream-gateway-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 2️⃣ 启动配置中心服务
start "Config-8888" cmd /k "cd ioedream-config-service && mvn spring-boot:run"

echo.
echo ✅ 基础设施服务启动完成！
echo.
pause
goto menu

:start_core
echo.
echo 💼 启动核心业务服务...
echo.

echo 1️⃣ 启动身份认证服务
start "Auth-8081" cmd /k "cd ioedream-auth-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 2️⃣ 启动设备管理服务
start "Device-8093" cmd /k "cd ioedream-device-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

echo 3️⃣ 启动门禁管理服务
start "Access-8090" cmd /k "cd ioedream-access-service && mvn spring-boot:run"

echo.
echo ✅ 核心业务服务启动完成！
echo.
pause
goto menu

:start_single
echo.
echo 🎯 启动单个微服务...
echo.
echo 可用服务列表：
echo 1. ioedream-gateway-service (网关:8080)
echo 2. ioedream-config-service (配置:8888)
echo 3. ioedream-auth-service (认证:8081)
echo 4. ioedream-identity-service (身份:8082)
echo 5. ioedream-access-service (门禁:8090)
echo 6. ioedream-device-service (设备:8093)
echo 7. ioedream-attendance-service (考勤:8091)
echo 8. ioedream-video-service (视频:8092)
echo 9. ioedream-consume-service (消费:8094)
echo 10. ioedream-enterprise-service (企业:8083)
echo.
set /p service_choice="请选择服务编号 (1-10): "

if "%service_choice%"=="1" (
    start "Gateway-8080" cmd /k "cd ioedream-gateway-service && mvn spring-boot:run"
)
if "%service_choice%"=="2" (
    start "Config-8888" cmd /k "cd ioedream-config-service && mvn spring-boot:run"
)
if "%service_choice%"=="3" (
    start "Auth-8081" cmd /k "cd ioedream-auth-service && mvn spring-boot:run"
)
if "%service_choice%"=="4" (
    start "Identity-8082" cmd /k "cd ioedream-identity-service && mvn spring-boot:run"
)
if "%service_choice%"=="5" (
    start "Access-8090" cmd /k "cd ioedream-access-service && mvn spring-boot:run"
)
if "%service_choice%"=="6" (
    start "Device-8093" cmd /k "cd ioedream-device-service && mvn spring-boot:run"
)
if "%service_choice%"=="7" (
    start "Attendance-8091" cmd /k "cd ioedream-attendance-service && mvn spring-boot:run"
)
if "%service_choice%"=="8" (
    start "Video-8092" cmd /k "cd ioedream-video-service && mvn spring-boot:run"
)
if "%service_choice%"=="9" (
    start "Consume-8094" cmd /k "cd ioedream-consume-service && mvn spring-boot:run"
)
if "%service_choice%"=="10" (
    start "Enterprise-8083" cmd /k "cd ioedream-enterprise-service && mvn spring-boot:run"
)

echo ✅ 服务启动命令已执行！
pause
goto menu

:stop_all
echo.
echo 🛑 停止所有微服务...
echo.

:: 杀死所有Java进程
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im mvn.exe >nul 2>&1

echo ✅ 所有微服务已停止！
echo.
pause
goto menu

:exit
echo.
echo 👋 感谢使用IOE-DREAM微服务管理器！
echo 📧 如有问题请联系技术团队
echo.
timeout /t 3 /nobreak >nul
exit /b 0