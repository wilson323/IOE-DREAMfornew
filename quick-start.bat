@echo off
chcp 65001 >nul
echo ========================================
echo   IOE-DREAM Quick Start with Database
echo ========================================
echo.

echo [1/4] Stopping existing services...
docker-compose -f docker-compose-all.yml down

echo.
echo [2/4] Starting MySQL and Redis...
docker-compose -f docker-compose-all.yml up -d mysql redis

echo.
echo [3/4] Waiting for MySQL to be ready (30 seconds)...
timeout /t 30 /nobreak >nul

echo.
echo [4/4] Starting all services...
docker-compose -f docker-compose-all.yml up -d

echo.
echo ========================================
echo   Services Status
echo ========================================
docker-compose -f docker-compose-all.yml ps

echo.
echo ========================================
echo   Quick Links
echo ========================================
echo   Nacos Console: http://localhost:8848/nacos
echo   Username: nacos
echo   Password: nacos
echo.
echo   API Gateway: http://localhost:8080
echo ========================================
echo.
pause
