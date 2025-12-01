@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: IOE-DREAM 微服务启动脚本 (Windows版本)
:: 支持分阶段启动：基础设施 → 基础服务 → 业务服务 → 监控服务

title IOE-DREAM 微服务启动管理

:main
if "%1"=="" goto show_help
if "%1"=="start" goto start_services
if "%1"=="stop" goto stop_services
if "%1"=="restart" goto restart_services
if "%1"=="status" goto show_status
if "%1"=="logs" goto view_logs
if "%1"=="backup" goto backup_data
if "%1"=="help" goto show_help
goto unknown_command

:start_services
call :check_prerequisites
call :create_directories

if "%2"=="infra" (
    call :start_infrastructure
) else if "%2"=="basic" (
    call :start_infrastructure
    call :start_basic_services
) else if "%2"=="business" (
    call :start_infrastructure
    call :start_basic_services
    call :start_business_services
) else if "%2"=="monitoring" (
    call :start_infrastructure
    call :start_basic_services
    call :start_business_services
    call :start_monitoring
) else (
    call :start_infrastructure
    call :start_basic_services
    call :start_business_services
    call :start_monitoring
)
call :show_services_status
goto end

:stop_services
call :stop_all_services
goto end

:restart_services
call :stop_all_services
timeout /t 10 /nobreak >nul
call :start_services
goto end

:show_status
call :show_services_status
goto end

:view_logs
if "%2"=="" (
    echo [错误] 请指定服务名称
    echo 使用方法: %0 logs ^<service-name^>
    goto end
)
call :view_service_logs %2
goto end

:backup_data
call :backup_data
goto end

:show_help
echo.
echo ========================================================
echo           IOE-DREAM 微服务管理脚本 (Windows)
echo ========================================================
echo.
echo 使用方法: %0 [命令] [选项]
echo.
echo 可用命令:
echo   start              启动所有服务
echo   start infra         只启动基础设施服务
echo   start basic        只启动基础服务
echo   start business     只启动业务服务
echo   start monitoring    只启动监控服务
echo   stop               停止所有服务
echo   restart            重启所有服务
echo   status             显示服务状态
echo   logs ^<service^>     查看指定服务日志
echo   backup             备份重要数据
echo   help               显示帮助信息
echo.
echo 示例:
echo   %0 start           # 启动所有服务
echo   %0 logs nacos      # 查看Nacos日志
echo   %0 status          # 显示服务状态
echo.
goto end

:unknown_command
echo [错误] 未知命令: %1
goto show_help
goto end

:: ========== 函数定义 ==========

:check_prerequisites
echo [INFO] 检查系统环境...

:: 检查Docker
docker --version >nul 2>&1
if !errorlevel 1 (
    echo [错误] Docker 未安装，请先安装 Docker
    exit /b 1
)

:: 检查Docker Compose
docker-compose --version >nul 2>&1
if !errorlevel 1 (
    echo [错误] Docker Compose 未安装，请先安装 Docker Compose
    exit /b 1
)

:: 检查Docker服务
docker info >nul 2>&1
if !errorlevel 1 (
    echo [错误] Docker 服务未启动，请先启动 Docker
    exit /b 1
)

echo [成功] 系统环境检查通过
goto :eof

:create_directories
echo [INFO] 创建必要的目录结构...

:: 创建日志目录
if not exist "C:\logs\ioedream" mkdir "C:\logs\ioedream"
if not exist ".\logs\infrastructure" mkdir ".\logs\infrastructure"
if not exist ".\logs\basic" mkdir ".\logs\basic"
if not exist ".\logs\business" mkdir ".\logs\business"
if not exist ".\logs\monitoring" mkdir ".\logs\monitoring"

:: 创建配置目录
if not exist ".\config\mysql" mkdir ".\config\mysql"
if not exist ".\config\redis" mkdir ".\config\redis"
if not exist ".\config\nacos" mkdir ".\config\nacos"
if not exist ".\config\rabbitmq" mkdir ".\config\rabbitmq"
if not exist ".\config\nginx" mkdir ".\config\nginx"
if not exist ".\config\monitoring" mkdir ".\config\monitoring"
if not exist ".\config\prometheus" mkdir ".\config\prometheus"
if not exist ".\config\grafana" mkdir ".\config\grafana"

:: 创建数据目录
if not exist ".\data\mysql" mkdir ".\data\mysql"
if not exist ".\data\redis" mkdir ".\data\redis"
if not exist ".\data\rabbitmq" mkdir ".\data\rabbitmq"
if not exist ".\data\elasticsearch" mkdir ".\data\elasticsearch"
if not exist ".\data\minio" mkdir ".\data\minio"

:: 创建备份目录
if not exist ".\backup\mysql" mkdir ".\backup\mysql"
if not exist ".\backup\redis" mkdir ".\backup\redis"
if not exist ".\backup\config" mkdir ".\backup\config"

echo [成功] 目录结构创建完成
goto :eof

:start_infrastructure
echo [STEP] 启动基础设施服务...

echo [INFO] 启动 MySQL、Redis、Nacos、RabbitMQ、Elasticsearch...
docker-compose -f infrastructure.yml up -d

echo [INFO] 等待基础设施服务启动...
timeout /t 30 /nobreak >nul

:: 检查服务状态
call :check_service_health "mysql-master" "MySQL主库"
call :check_service_health "redis-master" "Redis主库"
call :check_service_health "nacos" "Nacos注册中心"
call :check_service_health "rabbitmq" "RabbitMQ消息队列"
call :check_service_health "elasticsearch" "Elasticsearch搜索引擎"

echo [成功] 基础设施服务启动完成
goto :eof

:start_basic_services
echo [STEP] 启动基础服务...

echo [INFO] 启动认证服务、身份服务、设备服务、区域服务...
docker-compose -f basic-services.yml up -d

echo [INFO] 等待基础服务启动...
timeout /t 60 /nobreak >nul

:: 检查基础服务状态
call :check_service_health "ioedream-auth-service" "认证服务"
call :check_service_health "ioedream-identity-service" "身份服务"
call :check_service_health "ioedream-device-service" "设备服务"
call :check_service_health "ioedream-area-service" "区域服务"

echo [成功] 基础服务启动完成
goto :eof

:start_business_services
echo [STEP] 启动业务服务...

echo [INFO] 启动智能网关和所有业务服务...
docker-compose -f business-services.yml up -d

echo [INFO] 等待业务服务启动...
timeout /t 90 /nobreak >nul

:: 检查业务服务状态
call :check_service_health "smart-gateway" "智能网关"
call :check_service_health "ioedream-access-service" "门禁服务"
call :check_service_health "ioedream-consume-service" "消费服务"
call :check_service_health "ioedream-attendance-service" "考勤服务"
call :check_service_health "ioedream-video-service" "视频服务"
call :check_service_health "ioedream-visitor-service" "访客服务"
call :check_service_health "ioedream-notification-service" "通知服务"
call :check_service_health "ioedream-file-service" "文件服务"
call :check_service_health "ioedream-report-service" "报表服务"

echo [成功] 业务服务启动完成
goto :eof

:start_monitoring
echo [STEP] 启动监控服务...

echo [INFO] 启动监控、日志、链路追踪等服务...
docker-compose -f monitoring.yml up -d

echo [INFO] 等待监控服务启动...
timeout /t 60 /nobreak >nul

:: 检查监控服务状态
call :check_service_health "prometheus" "Prometheus监控"
call :check_service_health "grafana" "Grafana可视化"
call :check_service_health "zipkin" "Zipkin链路追踪"
call :check_service_health "jaeger" "Jaeger分布式追踪"
call :check_service_health "minio" "MinIO对象存储"

echo [成功] 监控服务启动完成
goto :eof

:stop_all_services
echo [STEP] 停止所有服务...

echo [INFO] 停止监控服务...
docker-compose -f monitoring.yml down

echo [INFO] 停止业务服务...
docker-compose -f business-services.yml down

echo [INFO] 停止基础服务...
docker-compose -f basic-services.yml down

echo [INFO] 停止基础设施服务...
docker-compose -f infrastructure.yml down

echo [成功] 所有服务已停止
goto :eof

:check_service_health
setlocal service_name=%1
setlocal display_name=%2
setlocal max_attempts=30
setlocal attempt=1

echo [INFO] 检查 %display_name% 健康状态...

:check_loop
docker-compose ps | findstr /i "%service_name%.*Up" >nul
if !errorlevel 1 (
    echo [成功] %display_name% 启动成功
    goto :eof
)

if %attempt% geq %max_attempts% (
    echo [错误] %display_name% 启动失败或超时
    goto :eof
)

echo [警告] %display_name% 启动中... (%attempt%/%max_attempts%)
timeout /t 10 /nobreak >nul
set /a attempt+=1
goto check_loop

:view_service_logs
setlocal service_name=%1

echo [INFO] 查看 %service_name% 日志...

:: 尝试在所有compose文件中查找服务
for %%f in (infrastructure.yml basic-services.yml business-services.yml monitoring.yml) do (
    docker-compose -f %%f config --services 2>nul | findstr /i "^%service_name%$" >nul
    if !errorlevel 1 (
        docker-compose -f %%f logs -f %service_name%
        goto :eof
    )
)

echo [错误] 未找到服务: %service_name%
goto :eof

:show_services_status
echo [STEP] 显示服务状态...

echo.
echo ========================================================
echo                   服务状态概览
echo ========================================================
echo.
echo 基础设施服务:
docker-compose -f infrastructure.yml ps
echo.
echo 基础服务:
docker-compose -f basic-services.yml ps
echo.
echo 业务服务:
docker-compose -f business-services.yml ps
echo.
echo 监控服务:
docker-compose -f monitoring.yml ps
echo.
echo ========================================================
echo                   服务访问地址
echo ========================================================
echo.
echo • 智能网关: http://localhost:8080
echo • Nacos控制台: http://localhost:8848/nacos
echo • RabbitMQ管理: http://localhost:15672
echo • Grafana监控: http://localhost:3000
echo • Prometheus: http://localhost:9090
echo • Zipkin链路追踪: http://localhost:9411
echo • Jaeger分布式追踪: http://localhost:16686
echo • Kibana日志分析: http://localhost:5601
echo • MinIO对象存储: http://localhost:9001
echo.

goto :eof

:backup_data
echo [STEP] 备份重要数据...

set backup_dir=.\backup\%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
mkdir "%backup_dir%"

echo [INFO] 备份数据到 %backup_dir%

:: 备份MySQL数据
echo [INFO] 备份MySQL数据...
docker exec ioedream-mysql-master mysqldump -u root -pioedream@2024 --all-databases > "%backup_dir%\mysql_backup.sql"

:: 备份Redis数据
echo [INFO] 备份Redis数据...
docker exec redis-master redis-cli BGSAVE
docker cp ioedream-redis-master:/data/dump.rdb "%backup_dir%\redis_backup.rdb" 2>nul

:: 备份配置文件
echo [INFO] 备份配置文件...
xcopy .\config "%backup_dir%\config" /E /I /Q >nul

echo [成功] 数据备份完成: %backup_dir%
goto :eof

:end
echo.
echo 操作完成！
pause