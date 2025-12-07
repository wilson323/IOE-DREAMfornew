@echo off
REM IOE-DREAM 快速验证脚本
REM 快速检查部署状态

echo ========================================
echo   IOE-DREAM 快速部署验证
echo ========================================
echo.

echo [1] 检查Docker容器状态...
docker ps --filter "name=ioedream" --format "table {{.Names}}\t{{.Status}}"
echo.

echo [2] 检查关键端口...
powershell -Command "Test-NetConnection -ComputerName localhost -Port 8848 -InformationLevel Quiet -WarningAction SilentlyContinue | Out-Null; if ($?) { Write-Host '  Nacos (8848): 已开放' -ForegroundColor Green } else { Write-Host '  Nacos (8848): 未开放' -ForegroundColor Red }"
powershell -Command "Test-NetConnection -ComputerName localhost -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue | Out-Null; if ($?) { Write-Host '  Gateway (8080): 已开放' -ForegroundColor Green } else { Write-Host '  Gateway (8080): 未开放' -ForegroundColor Red }"
powershell -Command "Test-NetConnection -ComputerName localhost -Port 8088 -InformationLevel Quiet -WarningAction SilentlyContinue | Out-Null; if ($?) { Write-Host '  Common (8088): 已开放' -ForegroundColor Green } else { Write-Host '  Common (8088): 未开放' -ForegroundColor Red }"
echo.

echo [3] 检查Nacos控制台...
echo   访问地址: http://localhost:8848/nacos
echo   用户名/密码: nacos/nacos
echo.

echo [4] 运行完整验证脚本...
powershell -ExecutionPolicy Bypass -File "%~dp0verify-deployment-step-by-step.ps1"

pause
