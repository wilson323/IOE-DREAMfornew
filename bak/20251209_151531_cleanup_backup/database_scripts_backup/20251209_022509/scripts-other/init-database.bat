@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo ============================================
echo 🗄️ IOE-DREAM 数据库初始化脚本
echo ============================================
echo.

REM 配置数据库连接信息
set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_NAME=ioedream_db
set DB_USER=root
set DB_PASS=root

echo 📋 配置信息:
echo    数据库地址: %DB_HOST%:%DB_PORT%
echo    数据库名称: %DB_NAME%
echo    用户名: %DB_USER%
echo.

REM 检查MySQL客户端
where mysql >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到MySQL客户端
    echo 请确保MySQL已安装并添加到系统PATH
    pause
    exit /b 1
)

echo ✅ MySQL客户端检测成功
echo.

REM 创建数据库
echo 📦 创建数据库...
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if errorlevel 1 (
    echo ❌ 数据库创建失败
    pause
    exit /b 1
)
echo ✅ 数据库创建成功
echo.

REM 执行SQL脚本
echo 🚀 开始执行SQL脚本...
echo.

cd /d "d:\IOE-DREAM\database-scripts\common-service"

for %%f in (*.sql) do (
    echo 📄 执行脚本: %%f
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < %%f
    if errorlevel 1 (
        echo ❌ 脚本执行失败: %%f
        pause
        exit /b 1
    )
    echo ✅ %%f 执行成功
)

echo.
echo ============================================
echo ✅ 数据库初始化完成！
echo ============================================
echo.
echo 📊 验证结果:
mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% -e "SHOW TABLES;"
echo.

echo 🎉 初始化成功，数据库已就绪！
pause
