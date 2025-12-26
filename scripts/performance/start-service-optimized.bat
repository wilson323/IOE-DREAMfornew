@echo off
REM =====================================================
REM IOE-DREAM P0级优化启动脚本 (Windows版本)
REM 包含G1GC参数优化和性能监控
REM =====================================================

setlocal enabledelayedexpansion

REM 获取参数
set SERVICE_NAME=%1
set MEMORY_PROFILE=%2
set OPERATION=%3

if "%SERVICE_NAME%"=="" (
    echo 错误: 请指定服务名
    echo 用法: %0 ^<服务名^> ^<内存配置^> ^<操作^>
    echo 示例: %0 ioedream-gateway-service medium start
    exit /b 1
)

if "%MEMORY_PROFILE%"=="" (
    set MEMORY_PROFILE=medium
)

if "%OPERATION%"=="" (
    set OPERATION=start
)

echo =====================================================
echo IOE-DREAM 优化启动脚本
echo 服务名: %SERVICE_NAME%
echo 内存配置: %MEMORY_PROFILE%
echo 操作: %OPERATION%
echo =====================================================

REM 设置日志目录
set LOG_DIR=D:\ioedream\logs
set GC_LOG_DIR=D:\ioedream\gc
set DUMP_DIR=D:\ioedream\dump

REM 创建目录
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
if not exist "%GC_LOG_DIR%" mkdir "%GC_LOG_DIR%"
if not exist "%DUMP_DIR%" mkdir "%DUMP_DIR%"

REM =====================================================
REM JVM参数配置
REM =====================================================

if "%MEMORY_PROFILE%"=="large" (
    REM 大内存环境 (16GB堆内存)
    echo 使用大内存配置 (16GB堆内存)
    set JAVA_OPTS=-Xms16g -Xmx16g ^
-XX:+UseG1GC ^
-XX:MaxGCPauseMillis=150 ^
-XX:G1HeapRegionSize=32m ^
-XX:G1NewSizePercent=35 ^
-XX:G1MaxNewSizePercent=50 ^
-XX:G1MixedGCCountTarget=12 ^
-XX:G1MixedGCLiveThresholdPercent=80 ^
-XX:G1OldCSetRegionThresholdPercent=15 ^
-XX:G1ReservePercent=25 ^
-XX:SurvivorRatio=6 ^
-XX:MaxTenuringThreshold=20 ^
-XX:InitiatingHeapOccupancyPercent=40 ^
-XX:+G1UseAdaptiveIHOP ^
-XX:+ParallelRefProcEnabled ^
-XX:+UseStringDeduplication ^
-XX:StringDeduplicationAgeThreshold=5

) else if "%MEMORY_PROFILE%"=="medium" (
    REM 中等内存环境 (8GB堆内存) - 生产环境推荐
    echo 使用中等内存配置 (8GB堆内存)
    set JAVA_OPTS=-Xms8g -Xmx8g ^
-XX:+UseG1GC ^
-XX:MaxGCPauseMillis=200 ^
-XX:G1HeapRegionSize=16m ^
-XX:G1NewSizePercent=30 ^
-XX:G1MaxNewSizePercent=40 ^
-XX:G1MixedGCCountTarget=8 ^
-XX:G1MixedGCLiveThresholdPercent=85 ^
-XX:G1OldCSetRegionThresholdPercent=10 ^
-XX:G1ReservePercent=20 ^
-XX:SurvivorRatio=8 ^
-XX:MaxTenuringThreshold=15 ^
-XX:InitiatingHeapOccupancyPercent=45 ^
-XX:+G1UseAdaptiveIHOP ^
-XX:+ParallelRefProcEnabled ^
-XX:+UseStringDeduplication ^
-XX:StringDeduplicationAgeThreshold=3

) else if "%MEMORY_PROFILE%"=="small" (
    REM 小内存环境 (4GB堆内存)
    echo 使用小内存配置 (4GB堆内存)
    set JAVA_OPTS=-Xms4g -Xmx4g ^
-XX:+UseG1GC ^
-XX:MaxGCPauseMillis=100 ^
-XX:G1HeapRegionSize=8m ^
-XX:G1NewSizePercent=40 ^
-XX:G1MaxNewSizePercent=50 ^
-XX:InitiatingHeapOccupancyPercent=50 ^
-XX:+ParallelRefProcEnabled ^
-XX:+UseStringDeduplication ^
-XX:StringDeduplicationAgeThreshold=3

) else if "%MEMORY_PROFILE%"=="dev" (
    REM 开发环境
    echo 使用开发环境配置 (2GB堆内存)
    set JAVA_OPTS=-Xms2g -Xmx4g ^
-XX:+UseG1GC ^
-XX:MaxGCPauseMillis=100 ^
-XX:G1HeapRegionSize=8m ^
-XX:G1NewSizePercent=40 ^
-XX:G1MaxNewSizePercent=50 ^
-XX:InitiatingHeapOccupancyPercent=50 ^
-XX:+ParallelRefProcEnabled

) else (
    echo 警告: 未识别的内存配置 "%MEMORY_PROFILE%"，使用默认中等内存配置
    set MEMORY_PROFILE=medium
    set JAVA_OPTS=-Xms8g -Xmx8g ^
-XX:+UseG1GC ^
-XX:MaxGCPauseMillis=200 ^
-XX:G1HeapRegionSize=16m ^
-XX:InitiatingHeapOccupancyPercent=45 ^
-XX:+ParallelRefProcEnabled
)

REM =====================================================
REM 通用JVM参数
REM =====================================================

set COMMON_OPTS=-XX:+PrintGC ^
-XX:+PrintGCDetails ^
-XX:+PrintGCTimeStamps ^
-XX:+PrintGCApplicationStoppedTime ^
-XX:+PrintGCDateStamps ^
-XX:+UseGCLogFileRotation ^
-XX:NumberOfGCLogFiles=5 ^
-XX:GCLogFileSize=10M ^
-XX:+HeapDumpOnOutOfMemoryError ^
-XX:HeapDumpPath=%DUMP_DIR%\heapdump-%SERVICE_NAME%.hprof ^
-Xloggc:%GC_LOG_DIR%\gc-%SERVICE_NAME%.log ^
-Dfile.encoding=UTF-8 ^
-Duser.timezone=Asia/Shanghai ^
-Djava.security.egd=file:/dev/./urandom ^
-Dspring.profiles.active=prod,performance-optimized

REM 监控参数
set MONITORING_OPTS=-XX:+UnlockDiagnosticVMOptions ^
-XX:+PrintReferenceGC ^
-XX:+PrintHeapAtGC ^
-XX:+PrintGCApplicationConcurrentTime ^
-XX:+PrintTenuringDistribution ^
-XX:+LogVMOutput ^
-XX:+PrintGCDetails

REM 最终JVM参数
set FINAL_JAVA_OPTS=%JAVA_OPTS% %COMMON_OPTS% %MONITORING_OPTS%

REM =====================================================
REM 应用程序配置
REM =====================================================

set APP_JAR=D:\ioedream\services\%SERVICE_NAME%.jar
set LOG_FILE=%LOG_DIR%\%SERVICE_NAME%-startup.log

REM =====================================================
REM 操作函数
REM =====================================================

if "%OPERATION%"=="start" (
    echo 正在启动服务: %SERVICE_NAME%
    echo 内存配置: %MEMORY_PROFILE%

    if not exist "%APP_JAR%" (
        echo 错误: JAR文件不存在: %APP_JAR%
        exit /b 1
    )

    echo 执行命令: java %FINAL_JAVA_OPTS% -jar %APP_JAR%
    echo.
    echo 日志文件: %LOG_FILE%
    echo GC日志: %GC_LOG_DIR%\gc-%SERVICE_NAME%.log
    echo HeapDump路径: %DUMP_DIR%\heapdump-%SERVICE_NAME%.hprof
    echo.
    echo 访问地址:
    echo   Druid监控: http://localhost:8080/druid/
    echo   健康检查: http://localhost:8080/actuator/health
    echo   性能指标: http://localhost:8080/actuator/metrics
    echo   Prometheus: http://localhost:8080/actuator/prometheus
    echo.

    REM 启动服务
    start "IOE-DREAM %SERVICE_NAME%" java %FINAL_JAVA_OPTS% -jar %APP_JAR% > "%LOG_FILE%" 2>&1

    echo 服务启动中...
    timeout /t 5 >nul

    echo 服务已启动! 请查看日志确认启动状态。

) else if "%OPERATION%"=="status" (
    echo 检查服务状态: %SERVICE_NAME%

    REM 检查Java进程
    tasklist /fi "windowtitle eq IOE-DREAM %SERVICE_NAME%" | find "java.exe" >nul
    if !errorlevel! equ 0 (
        echo 服务运行中
    ) else (
        echo 服务未运行
    )

    REM 显示日志文件的最后几行
    if exist "%LOG_FILE%" (
        echo.
        echo 最近日志:
        echo ----------------------------------------
        powershell "Get-Content '%LOG_FILE%' -Tail 10"
        echo ----------------------------------------
    )

) else if "%OPERATION%"=="stop" (
    echo 正在停止服务: %SERVICE_NAME%

    REM 通过窗口标题停止进程
    taskkill /fi "windowtitle eq IOE-DREAM %SERVICE_NAME%" /f >nul 2>&1

    echo 服务已停止

) else if "%OPERATION%"=="restart" (
    echo 重启服务: %SERVICE_NAME%

    REM 停止
    taskkill /fi "windowtitle eq IOE-DREAM %SERVICE_NAME%" /f >nul 2>&1
    timeout /t 2 >nul

    REM 启动
    echo 重新启动服务...
    start "IOE-DREAM %SERVICE_NAME%" java %FINAL_JAVA_OPTS% -jar %APP_JAR% > "%LOG_FILE%" 2>&1

    echo 服务重启完成!

) else (
    echo 用法: %0 ^<服务名^> ^<内存配置^> ^<操作^>
    echo.
    echo 可用操作:
    echo   start   - 启动服务
    echo   stop    - 停止服务
    echo   restart - 重启服务
    echo   status  - 检查状态
    echo.
    echo 内存配置:
    echo   small   - 4GB堆内存 (轻量服务)
    echo   medium  - 8GB堆内存 (生产推荐)
    echo   large   - 16GB堆内存 (高负载)
    echo   dev     - 2-4GB堆内存 (开发环境)
    echo.
    echo 示例:
    echo   %0 ioedream-gateway-service medium start
    echo   %0 ioedream-common-service large restart
    echo   %0 ioedream-access-service small status
    exit /b 1
)

echo.
echo 操作完成!
pause