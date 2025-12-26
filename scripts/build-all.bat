@echo off
setlocal enabledelayedexpansion

:: IOE-DREAM 批量构建脚本
:: 替代GitHub Actions，支持Windows环境

echo ========================================
echo    IOE-DREAM 本地CI构建系统
echo ========================================
echo.

:: 检查参数
set CLEAN_BUILD=%1
set TARGET_SERVICE=%2

if "%CLEAN_BUILD%"=="--help" goto :show_help
if "%CLEAN_BUILD%"=="-h" goto :show_help
if "%CLEAN_BUILD%"=="-clean" (
    set CLEAN_FLAG=clean
) else (
    set CLEAN_FLAG=
)

if "%TARGET_SERVICE%"=="" (
    echo [%time%] 🚀 开始构建所有服务...
    set MAVEN_MODULE=
) else (
    echo [%time%] 🚀 开始构建服务: %TARGET_SERVICE%
    set MAVEN_MODULE=-pl %TARGET_SERVICE% -am
)

:: 检查Java和Maven
echo [%time%] 🔍 检查构建环境...

java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: Java未安装或不可用
    exit /b 1
)

mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: Maven未安装或不可用
    exit /b 1
)

echo ✅ 构建环境检查通过
echo.

:: 设置Maven参数
set MAVEN_ARGS=%CLEAN_FLAG% install -DskipTests -Dpmd.skip=true -q

:: 执行构建
echo [%time%] 📦 执行Maven构建...
echo 命令: mvn %MAVEN_ARGS% %MAVEN_MODULE%
echo.

set START_TIME=%time%

mvn %MAVEN_ARGS% -f microservices\pom.xml %MAVEN_MODULE%

if errorlevel 1 (
    echo.
    echo ❌ 构建失败！
    exit /b 1
)

set END_TIME=%time%

:: 计算构建时间
call :calc_duration "%START_TIME%" "%END_TIME%"

echo.
echo ✅ 构建成功！
echo ⏱️  构建耗时: !DURATION! 秒

:: 统计构建产物
if "%TARGET_SERVICE%"=="" (
    for /f %%i in ('dir /s /b microservices\*-1.0.0.jar 2^>nul ^| find /c /v ""') do set JAR_COUNT=%%i
    echo 📦 生成了 !JAR_COUNT! 个JAR文件
)

echo.
echo ========================================
echo    🎉 构建完成！
echo ========================================
goto :end

:show_help
echo 用法:
echo   build-all.bat [选项] [服务名]
echo.
echo 选项:
echo   -clean     清理后构建
echo   -h, --help 显示帮助信息
echo.
echo 示例:
echo   build-all.bat                    构建所有服务
echo   build-all.bat -clean              清理并构建所有服务
echo   build-all.bat ioedream-access-service  构建指定服务
echo.
goto :end

:calc_duration
set "start=%~1"
set "end=%~2"

:: 提取时分秒
set "start_h=!start:~0,2!"
set "start_m=!start:~3,2!"
set "start_s=!start:~6,2!"
set "end_h=!end:~0,2!"
set "end_m=!end:~3,2!"
set "end_s=!end:~6,2!"

:: 转换为秒
set /a "start_total=!start_h!*3600 + !start_m!*60 + !start_s!"
set /a "end_total=!end_h!*3600 + !end_m!*60 + !end_s!"

:: 计算差值
set /a "DURATION=!end_total! - !start_total!"
goto :eof

:end
exit /b 0