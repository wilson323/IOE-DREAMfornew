@echo off
REM ================================================================================
REM IOE-DREAM 开发环境停止脚本
REM ================================================================================
REM 功能：优雅停止所有开发服务
REM ================================================================================

setlocal enabledelayedexpansion
chcp 65001 >nul

echo ========================================
echo IOE-DREAM 开发环境停止
echo ========================================
echo.

REM 1️⃣ 停止Maven进程（网关服务）
echo [1/3] 停止后端网关服务...
for /f "tokens=2" %%i in ('tasklist /fi "windowtitle eq IOE-DREAM 网关服务*" /fo list ^| findstr "PID"') do (
    echo 停止网关服务 PID: %%i
    taskkill /PID %%i /F >nul 2>&1
)

REM 也可以通过进程名停止
taskkill /FI "WINDOWTITLE eq IOE-DREAM 网关服务*" /F >nul 2>&1
echo ✅ 网关服务已停止

REM 2️⃣ 停止npm进程（前端服务）
echo.
echo [2/3] 停止前端服务...
for /f "tokens=2" %%i in ('tasklist /fi "windowtitle eq IOE-DREAM 前端服务*" /fo list ^| findstr "PID"') do (
    echo 停止前端服务 PID: %%i
    taskkill /PID %%i /F >nul 2>&1
)

REM 也可以通过进程名停止
taskkill /FI "WINDOWTITLE eq IOE-DREAM 前端服务*" /F >nul 2>&1

REM 停止所有node进程（如果需要）
REM taskkill /IM node.exe /F >nul 2>&1

echo ✅ 前端服务已停止

REM 3️⃣ 清理临时文件
echo.
echo [3/3] 清理临时文件...
del temp_*.txt >nul 2>&1
echo ✅ 临时文件已清理

REM 4️⃣ 完成
echo.
echo ========================================
echo ✅ IOE-DREAM 开发环境已停止！
echo ========================================
echo.
echo 💡 Docker服务（MySQL/Redis）仍在运行
echo 💡 如需停止Docker服务，请执行：
echo    docker-compose -f docker-compose-services.yml down
echo ========================================
echo.

pause
