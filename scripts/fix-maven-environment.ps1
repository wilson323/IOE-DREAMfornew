@echo off
setlocal enabledelayedexpansion

echo ============================================================
echo IOE-DREAM Maven环境问题修复工具
echo ============================================================
echo.

REM 设置颜色
color 0A

echo 1. 检查Java环境...
java -version
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java环境未配置或版本过低
    pause
    exit /b 1
)

echo.
echo 2. 检查Maven环境...
mvn -version
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Maven环境未配置
    pause
    exit /b 1
)

echo.
echo 3. 检查YAML文件编码问题...
cd /d "%~dp0"

REM 检查关键YAML文件是否有BOM头
echo 检查microservices-common的YAML文件...
cd microservices\microservices-common

REM 检查application-shared.yml是否有BOM头
powershell -Command "$bytes = Get-Content 'src\main\resources\application-shared.yml' -Encoding Byte; if ($bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) { Write-Host 'Found BOM header, removing...'; $bytes = $bytes[3..($bytes.Length-1)]; Set-Content -Value $bytes -Encoding Byte 'src\main\resources\application-shared.yml' }"

echo.
echo 4. 清理编译缓存...
call mvn clean

echo.
echo 5. 修复YAML文件格式...

REM 创建临时的正确格式配置文件
echo spring.application.name=microservices-common > temp.properties
echo spring.profiles.active=dev >> temp.properties

echo.
echo 6. 使用Properties格式测试编译...
call mvn compile -Dspring.config.location=temp.properties

echo.
echo 7. 如果编译成功，说明是YAML文件问题...

REM 删除临时文件
if exist temp.properties del temp.properties

echo.
echo ============================================================
echo 修复完成，请检查编译结果
echo ============================================================
pause