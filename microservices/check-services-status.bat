@echo off
chcp 65001 >nul
title IOE-DREAM 微服务状态监控
color 0B

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                📊 IOE-DREAM 微服务状态监控                      ║
echo ║                    检查时间: %date% %time%                  ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

echo 🔍 检查微服务运行状态...
echo.

:: 服务列表和端口
set "gateway_port=8080"
set "config_port=8888"
set "auth_port=8081"
set "identity_port=8082"
set "enterprise_port=8083"
set "audit_port=8085"
set "oa_port=8084"
set "access_port=8090"
set "attendance_port=8091"
set "video_port=8092"
set "device_port=8093"
set "consume_port=8094"
set "report_port=8095"
set "notification_port=8096"
set "monitor_port=8097"
set "scheduler_port=8098"
set "integration_port=8099"

:: 检查服务状态
echo ══════════════════════════════════════════════════════════════
echo 🏗️  基础设施层服务
echo ══════════════════════════════════════════════════════════════

call :check_service "API网关服务" %gateway_port%
call :check_service "配置中心服务" %config_port%
call :check_service "审计服务" %audit_port%
call :check_service "报表分析服务" %config_port%
call :check_service "任务调度服务" %scheduler_port%
call :check_service "集成服务" %integration_port%
call :check_service "基础设施服务" %monitor_port%

echo ══════════════════════════════════════════════════════════════
echo 🔐 业务核心层服务
echo ══════════════════════════════════════════════════════════════

call :check_service "身份认证服务" %auth_port%
call :check_service "身份管理服务" %identity_port%
call :check_service "企业服务" %enterprise_port%
call :check_service "办公自动化服务" %oa_port%

echo ══════════════════════════════════════════════════════════════
echo 📱 业务应用层服务
echo ══════════════════════════════════════════════════════════════

call :check_service "门禁管理服务" %access_port%
call :check_service "考勤管理服务" %attendance_port%
call :check_service "视频监控服务" %video_port%
call :check_service "设备管理服务" %device_port%
call :check_service "消费管理服务" %consume_port%
call :check_service "报表服务" %report_port%

echo ══════════════════════════════════════════════════════════════
echo 📡 通信支撑层服务
echo ══════════════════════════════════════════════════════════════

call :check_service "通知服务" %notification_port%
call :check_service "监控服务" %monitor_port%

echo.
echo ══════════════════════════════════════════════════════════════
echo 🌐 访问地址汇总
echo ══════════════════════════════════════════════════════════════
echo API网关入口:      http://localhost:%gateway_port%
echo 配置中心:        http://localhost:%config_port%
echo 身份认证服务:     http://localhost:%auth_port%
echo 设备管理服务:     http://localhost:%device_port%
echo 门禁管理服务:     http://localhost:%access_port%
echo.
echo 📊 监控端点:
echo 网关路由信息:     http://localhost:%gateway_port%/actuator/gateway/routes
echo 健康检查:        http://localhost:%gateway_port%/actuator/health
echo 服务指标:        http://localhost:%gateway_port%/actuator/metrics
echo.

:: 统计Java进程数量
echo 📈 系统资源使用情况:
for /f "tokens=2" %%i in ('tasklist /fi "imagename eq java.exe" /fo table /nh ^| find /c "java.exe"') do set java_count=%%i
echo Java进程数: %java_count%

:: 检查内存使用
for /f "skip=1 tokens=4,5" %%a in ('wmic computersystem get TotalPhysicalMemory^,AvailablePhysicalMemory') do (
    set total_memory=%%a
    set available_memory=%%b
    goto :memory_done
)
:memory_done
if defined total_memory (
    echo 内存使用: 已使用 / 总计 = %total_memory% 字节
)

echo.
echo 💡 提示：端口显示绿色表示服务正常，红色表示服务未启动
echo 🔄 按任意键刷新状态，按ESC退出...
echo.

pause >nul
goto :eof

:check_service
set service_name=%1
set service_port=%2

:: 检查端口是否被占用
netstat -an | findstr ":%service_port% " >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ %service_name% (端口:%service_port%) - [运行中]
    color 0A
) else (
    echo ❌ %service_name% (端口:%service_port%) - [未运行]
    color 0C
)
goto :eof