@echo off
REM IOE-DREAM 一键编译脚本 - Windows Batch
REM 用途：自动设置编译环境并执行 Maven 编译
REM 使用方法：compile.bat [模块] [选项]
REM   模块: all (默认), base, support, admin
REM   选项: /clean (默认), /no-clean, /test, /no-test, /profile:dev (默认)

setlocal enabledelayedexpansion

set PROJECT_ROOT=D:\IOE-DREAM
set BACKEND_PATH=%PROJECT_ROOT%\smart-admin-api-java17-springboot3

echo.
echo === IOE-DREAM 一键编译脚本 ===
echo 时间: %date% %time%
echo.

REM 解析参数
set MODULE=all
set CLEAN=clean
set SKIP_TESTS=-DskipTests
set PROFILE=dev

:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="base" set MODULE=base
if /i "%~1"=="support" set MODULE=support
if /i "%~1"=="admin" set MODULE=admin
if /i "%~1"=="all" set MODULE=all
if /i "%~1"=="/clean" set CLEAN=clean
if /i "%~1"=="/no-clean" set CLEAN=
if /i "%~1"=="/test" set SKIP_TESTS=
if /i "%~1"=="/no-test" set SKIP_TESTS=-DskipTests
if "%~1"=="/profile" (
    set PROFILE=%~2
    shift
)
shift
goto parse_args
:end_parse

REM 步骤1: 检查环境
echo 步骤1: 检查编译环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] Java 未安装或未配置到 PATH
    exit /b 1
)
echo   [PASS] Java 已安装

mvn -version >nul 2>&1
if errorlevel 1 (
    echo   [FAIL] Maven 未安装或未配置到 PATH
    exit /b 1
)
echo   [PASS] Maven 已安装

REM 步骤2: 设置 UTF-8 编码环境变量
echo.
echo 步骤2: 设置 UTF-8 编码环境变量...
set MAVEN_OPTS=-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN
set JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8
echo   [DONE] UTF-8 编码环境变量已设置
echo     MAVEN_OPTS: %MAVEN_OPTS%
echo     JAVA_TOOL_OPTIONS: %JAVA_TOOL_OPTIONS%

REM 步骤3: 切换到后端项目目录
echo.
echo 步骤3: 切换到后端项目目录...
if not exist "%BACKEND_PATH%" (
    echo   [FAIL] 找不到后端项目目录: %BACKEND_PATH%
    exit /b 1
)
cd /d "%BACKEND_PATH%"
echo   [DONE] 当前目录: %CD%

REM 步骤4: 构建 Maven 编译命令
echo.
echo 步骤4: 构建编译命令...
set MAVEN_GOALS=
if not "%CLEAN%"=="" (
    set MAVEN_GOALS=%CLEAN% compile
) else (
    set MAVEN_GOALS=compile
)

set MAVEN_ARGS=-P%PROFILE% %SKIP_TESTS%

if /i "%MODULE%"=="base" (
    set MAVEN_ARGS=-pl sa-base -am %MAVEN_ARGS%
    echo   [INFO] 编译模块: sa-base (含依赖)
) else if /i "%MODULE%"=="support" (
    set MAVEN_ARGS=-pl sa-support -am %MAVEN_ARGS%
    echo   [INFO] 编译模块: sa-support (含依赖)
) else if /i "%MODULE%"=="admin" (
    set MAVEN_ARGS=-pl sa-admin -am %MAVEN_ARGS%
    echo   [INFO] 编译模块: sa-admin (含依赖)
) else (
    echo   [INFO] 编译所有模块
)

echo   [INFO] 使用 Profile: %PROFILE%
echo   [DONE] 编译命令: mvn %MAVEN_GOALS% %MAVEN_ARGS%

REM 步骤5: 执行编译
echo.
echo 步骤5: 执行编译...
echo 开始时间: %date% %time%
echo.

call mvn %MAVEN_GOALS% %MAVEN_ARGS%

if errorlevel 1 (
    echo.
    echo === 编译失败 ===
    echo 结束时间: %date% %time%
    echo 请检查错误信息并重试
    exit /b 1
) else (
    echo.
    echo === 编译成功 ===
    echo 结束时间: %date% %time%
    exit /b 0
)

