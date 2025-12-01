@echo off
REM SmartAdmin Docker 部署脚本 (Windows版本)
REM 作者: SmartAdmin团队
REM 版本: v1.0.0
REM 更新: 2025-11-14

setlocal enabledelayedexpansion

echo.
echo ===== SmartAdmin Docker 部署脚本 =====
echo 部署时间: %date% %time%
echo 项目路径: %cd%
echo =======================================

REM 检查Docker是否安装
echo.
echo [1/6] 检查Docker环境...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker未安装或未在PATH中
    echo 请安装Docker Desktop并确保其正在运行
    pause
    exit /b 1
)
echo Docker已安装

docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: Docker Compose未安装或未在PATH中
    echo 请安装Docker Compose
    pause
    exit /b 1
)
echo Docker Compose已安装

REM 选择部署环境
echo.
echo [2/6] 选择部署环境:
echo 1) 开发环境 (包含热重载)
echo 2) 生产环境
echo 3) 自定义配置
set /p env_choice="请输入选项 [1-3]: "

if "%env_choice%"=="1" (
    set "ENV_FILE=docker-compose.dev.yml"
    set "ENV_NAME=开发环境"
) else if "%env_choice%"=="2" (
    set "ENV_FILE=docker-compose.yml"
    set "ENV_NAME=生产环境"
) else if "%env_choice%"=="3" (
    set /p custom_file="请输入自定义配置文件名: "
    set "ENV_FILE=%custom_file%"
    set "ENV_NAME=自定义环境"
) else (
    echo 无效选项，使用默认生产环境
    set "ENV_FILE=docker-compose.yml"
    set "ENV_NAME=生产环境"
)

echo 已选择: %ENV_NAME%
echo 配置文件: %ENV_FILE%

REM 检查配置文件
echo.
echo [3/6] 准备部署环境...

if not exist "%ENV_FILE%" (
    echo 错误: 配置文件不存在: %ENV_FILE%
    pause
    exit /b 1
)
echo 配置文件存在: %ENV_FILE%

REM 创建必要的目录
if not exist "logs" mkdir logs
if not exist "logs\backend" mkdir logs\backend
if not exist "logs\nginx" mkdir logs\nginx
if not exist "docker" mkdir docker
if not exist "docker\mysql" mkdir docker\mysql
if not exist "docker\mysql\conf.d" mkdir docker\mysql\conf.d
if not exist "docker\redis" mkdir docker\redis
if not exist "docker\nginx" mkdir docker\nginx
if not exist "docker\nginx\ssl" mkdir docker\nginx\ssl

echo 必要目录已创建

REM 检查数据库脚本
if not exist "数据库SQL脚本\mysql" (
    echo 警告: 数据库脚本目录不存在，将跳过数据库初始化
) else (
    echo 数据库脚本目录存在
)

REM 停止现有服务
echo 停止现有服务...
docker-compose -f "%ENV_FILE%" down >nul 2>&1

REM 构建和部署
echo.
echo [4/6] 构建镜像...
docker-compose -f "%ENV_FILE%" build --no-cache
if %errorlevel% neq 0 (
    echo 错误: 镜像构建失败
    pause
    exit /b 1
)
echo 镜像构建成功

echo.
echo [5/6] 启动服务...
docker-compose -f "%ENV_FILE%" up -d
if %errorlevel% neq 0 (
    echo 错误: 服务启动失败
    pause
    exit /b 1
)
echo 服务启动成功

REM 等待服务就绪
echo.
echo [6/6] 等待服务就绪...
timeout /t 30 /nobreak >nul

REM 显示服务状态
echo.
echo 服务状态:
docker-compose -f "%ENV_FILE%" ps

REM 显示访问信息
echo.
echo ===== 访问信息 =====
echo 前端应用: http://localhost:8080 (开发环境) 或 http://localhost (生产环境)
echo 后端API:  http://localhost:1024/api
echo API文档:  http://localhost:1024/doc.html
echo.
echo 数据库连接:
echo   MySQL:   localhost:3306
echo           用户: root / smartadmin
echo           密码: root1234 / smartadmin123
echo   Redis:   localhost:6379
echo           密码: zkteco3100
echo.
echo 常用命令:
echo   查看日志: docker-compose -f %ENV_FILE% logs -f
echo   停止服务: docker-compose -f %ENV_FILE% down
echo   重启服务: docker-compose -f %ENV_FILE% restart
echo   查看状态: docker-compose -f %ENV_FILE% ps
echo ====================

echo.
echo 部署完成！

REM 询问是否查看日志
set /p view_logs="是否查看实时日志？[y/N]: "
if /i "%view_logs%"=="y" (
    docker-compose -f "%ENV_FILE%" logs -f
)

pause