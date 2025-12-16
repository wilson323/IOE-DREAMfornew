@echo off
chcp 65001 >nul
echo ========================================
echo   IOE-DREAM 加密配置检查
echo ========================================
echo.

if not exist ".env" (
    echo ❌ 错误: .env 文件不存在
    pause
    exit /b 1
)

echo 🔍 检查加密配置项...
echo.

REM 检查 MySQL 密码
findstr /C:"MYSQL_PASSWORD=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ MYSQL_PASSWORD - 未加密
) else (
    echo ✅ MYSQL_PASSWORD - 已加密
)

REM 检查 MySQL Root 密码
findstr /C:"MYSQL_ROOT_PASSWORD=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ MYSQL_ROOT_PASSWORD - 未加密
) else (
    echo ✅ MYSQL_ROOT_PASSWORD - 已加密
)

REM 检查 Redis 密码
findstr /C:"REDIS_PASSWORD=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ REDIS_PASSWORD - 未加密
) else (
    echo ✅ REDIS_PASSWORD - 已加密
)

REM 检查 Nacos 密码
findstr /C:"NACOS_PASSWORD=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ NACOS_PASSWORD - 未加密
) else (
    echo ✅ NACOS_PASSWORD - 已加密
)

REM 检查 RabbitMQ 密码
findstr /C:"RABBITMQ_PASSWORD=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ RABBITMQ_PASSWORD - 未加密
) else (
    echo ✅ RABBITMQ_PASSWORD - 已加密
)

REM 检查 Jasypt 密钥
findstr /C:"JASYPT_PASSWORD=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ JASYPT_PASSWORD - 未加密
) else (
    echo ✅ JASYPT_PASSWORD - 已加密
)

REM 检查 JWT 密钥
findstr /C:"JWT_SECRET=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ JWT_SECRET - 未加密
) else (
    echo ✅ JWT_SECRET - 已加密
)

REM 检查 MFA 密钥
findstr /C:"MFA_TOTP_SECRET=ENC(AES256:" .env >nul
if errorlevel 1 (
    echo ❌ MFA_TOTP_SECRET - 未加密
) else (
    echo ✅ MFA_TOTP_SECRET - 已加密
)

echo.
echo 🔍 检查明文密码...
echo.

REM 检查常见明文密码
findstr /C:"PASSWORD=123456" .env >nul
if not errorlevel 1 (
    echo ❌ 发现明文密码: 123456
)

findstr /C:"PASSWORD=redis123" .env >nul
if not errorlevel 1 (
    echo ❌ 发现明文密码: redis123
)

findstr /C:"PASSWORD=nacos" .env >nul
if not errorlevel 1 (
    echo ❌ 发现明文密码: nacos
)

findstr /C:"PASSWORD=guest" .env >nul
if not errorlevel 1 (
    echo ❌ 发现明文密码: guest
)

if not errorlevel 1 (
    echo.
) else (
    echo ✅ 未发现常见明文密码
    echo.
)

echo 💡 建议事项:
echo   • 生产环境请使用强密码和随机密钥
echo   • 定期轮换加密密钥，建议每季度一次
echo   • 使用专业的密钥管理服务
echo.

echo ========================================
echo   检查完成
echo ========================================
pause