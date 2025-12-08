@echo off
REM IOE-DREAM Docker启动问题快速修复脚本
REM 修复内容：
REM 1. Gateway服务Bean定义冲突
REM 2. 业务服务数据库配置缺失

echo ========================================
echo  IOE-DREAM Docker启动问题快速修复
echo ========================================
echo.

echo [1/5] 停止所有Docker容器...
docker-compose -f docker-compose-all.yml down
echo   完成 √
echo.

echo [2/5] 清理旧镜像...
docker rmi -f ioedream/gateway-service:latest 2>nul
docker rmi -f ioedream/common-service:latest 2>nul
echo   完成 √
echo.

echo [3/5] 重新构建microservices-common...
cd microservices
call mvn clean install -pl microservices-common -am -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo   错误：microservices-common构建失败！
    pause
    exit /b 1
)
cd ..
echo   完成 √
echo.

echo [4/5] 重新构建Gateway服务...
cd microservices
call mvn clean install -pl ioedream-gateway-service -am -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo   错误：Gateway服务构建失败！
    pause
    exit /b 1
)
cd ..
echo   完成 √
echo.

echo [5/5] 启动所有Docker服务...
docker-compose -f docker-compose-all.yml up -d
echo   完成 √
echo.

echo ========================================
echo  修复完成！正在查看服务状态...
echo ========================================
echo.

timeout /t 10 /nobreak > nul

echo 服务状态：
docker-compose -f docker-compose-all.yml ps
echo.

echo ========================================
echo  查看实时日志（Ctrl+C退出）
echo ========================================
docker-compose -f docker-compose-all.yml logs -f gateway-service common-service

pause
