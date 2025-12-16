@echo off
chcp 65001 >nul
echo ========================================
echo   IOE-DREAM 加密配置生成工具
echo ========================================
echo.

REM 检查Java环境
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误: 未找到Java环境，请先安装Java
    pause
    exit /b 1
)

echo ✅ Java环境检查通过
echo.

REM 检查Maven仓库中是否有Jasypt
set JASYPT_JAR=%USERPROFILE%\.m2\repository\org\jasypt\jasypt\1.9.3\jasypt-1.9.3.jar
if not exist "%JASYPT_JAR%" (
    echo 🔍 正在查找Jasypt库...

    REM 检查项目中是否已有Jasypt
    if exist "lib\jasypt-1.9.3.jar" (
        set JASYPT_JAR=lib\jasypt-1.9.3.jar
        echo ✅ 使用项目中的Jasypt库
    ) else (
        echo ❌ 未找到Jasypt库，请先运行 Maven 下载依赖
        echo 💡 提示: 运行 mvn dependency:resolve
        pause
        exit /b 1
    )
) else (
    echo ✅ 使用Maven仓库中的Jasypt库
)

echo 📚 Jasypt库路径: %JASYPT_JAR%
echo.

REM 生成加密配置
echo 🔐 开始生成加密配置...
echo.

set MYSQL_SECRET=IOE-DREAM-MySQL-Secret-2024
set REDIS_SECRET=IOE-DREAM-Redis-Secret-2024
set NACOS_SECRET=IOE-DREAM-Nacos-Secret-2024
set RABBITMQ_SECRET=IOE-DREAM-RabbitMQ-Secret-2024
set JASYPT_SECRET=IOE-DREAM-Jasypt-Secret-2024
set JWT_SECRET=IOE-DREAM-JWT-Secret-2024
set MFA_SECRET=IOE-DREAM-MFA-TOTP-Secret-2024

echo 📝 正在加密 MySQL 密码 (123456)...
java -cp "%JASYPT_JAR%" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="123456" password="%MYSQL_SECRET%" algorithm=PBEWithMD5AndDES > mysql_encrypted.tmp

echo 📝 正在加密 Redis 密码 (redis123)...
java -cp "%JASYPT_JAR%" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="redis123" password="%REDIS_SECRET%" algorithm=PBEWithMD5AndDES > redis_encrypted.tmp

echo 📝 正在加密 Nacos 密码 (nacos)...
java -cp "%JASYPT_JAR%" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="nacos" password="%NACOS_SECRET%" algorithm=PBEWithMD5AndDES > nacos_encrypted.tmp

echo 📝 正在加密 RabbitMQ 密码 (guest)...
java -cp "%JASYPT_JAR%" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="guest" password="%RABBITMQ_SECRET%" algorithm=PBEWithMD5AndDES > rabbitmq_encrypted.tmp

echo 📝 正在加密 Jasypt 密钥...
java -cp "%JASYPT_JAR%" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="%JASYPT_SECRET%" password="%JASYPT_SECRET%" algorithm=PBEWithMD5AndDES > jasypt_encrypted.tmp

echo 📝 正在加密 JWT 密钥...
java -cp "%JASYPT_JAR%" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="%JWT_SECRET%" password="%JWT_SECRET%" algorithm=PBEWithMD5AndDES > jwt_encrypted.tmp

echo 📝 正在加密 MFA 密钥...
java -cp "%JASYPT_JAR%" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input="%MFA_SECRET%" password="%MFA_SECRET%" algorithm=PBEWithMD5AndDES > mfa_encrypted.tmp

echo.
echo ========================================
echo   加密结果
echo ========================================
echo.

REM 提取并显示加密结果
echo 🗄️ MySQL配置:
for /f "tokens=*" %%i in ('findstr "ENC" mysql_encrypted.tmp') do echo   MYSQL_PASSWORD=%%i

echo 🔴 Redis配置:
for /f "tokens=*" %%i in ('findstr "ENC" redis_encrypted.tmp') do echo   REDIS_PASSWORD=%%i

echo 🏢 Nacos配置:
for /f "tokens=*" %%i in ('findstr "ENC" nacos_encrypted.tmp') do echo   NACOS_PASSWORD=%%i

echo 📮 RabbitMQ配置:
for /f "tokens=*" %%i in ('findstr "ENC" rabbitmq_encrypted.tmp') do echo   RABBITMQ_PASSWORD=%%i

echo 🔐 Jasypt配置:
for /f "tokens=*" %%i in ('findstr "ENC" jasypt_encrypted.tmp') do echo   JASYPT_PASSWORD=%%i

echo 🎫 JWT配置:
for /f "tokens=*" %%i in ('findstr "ENC" jwt_encrypted.tmp') do echo   JWT_SECRET=%%i

echo 🔐 MFA配置:
for /f "tokens=*" %%i in ('findstr "ENC" mfa_encrypted.tmp') do echo   MFA_TOTP_SECRET=%%i

echo.
echo ========================================
echo   创建加密配置文件
echo ========================================

REM 创建.env.encrypted文件
echo # IOE-DREAM 加密环境配置文件 > .env.encrypted
echo # 生成时间: %date% %time% >> .env.encrypted
echo. >> .env.encrypted
echo # 数据库配置 >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" mysql_encrypted.tmp') do echo MYSQL_PASSWORD=%%i >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" mysql_encrypted.tmp') do echo MYSQL_ROOT_PASSWORD=%%i >> .env.encrypted
echo. >> .env.encrypted

echo # Redis配置 >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" redis_encrypted.tmp') do echo REDIS_PASSWORD=%%i >> .env.encrypted
echo. >> .env.encrypted

echo # Nacos配置 >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" nacos_encrypted.tmp') do echo NACOS_PASSWORD=%%i >> .env.encrypted
echo. >> .env.encrypted

echo # RabbitMQ配置 >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" rabbitmq_encrypted.tmp') do echo RABBITMQ_PASSWORD=%%i >> .env.encrypted
echo. >> .env.encrypted

echo # 安全配置 >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" jasypt_encrypted.tmp') do echo JASYPT_PASSWORD=%%i >> .env.encrypted
echo JASYPT_ALGORITHM=PBEWithMD5AndDES >> .env.encrypted
echo. >> .env.encrypted

echo # JWT配置 >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" jwt_encrypted.tmp') do echo JWT_SECRET=%%i >> .env.encrypted
echo JWT_EXPIRATION=86400 >> .env.encrypted
echo. >> .env.encrypted

echo # MFA配置 >> .env.encrypted
for /f "tokens=*" %%i in ('findstr "ENC" mfa_encrypted.tmp') do echo MFA_TOTP_SECRET=%%i >> .env.encrypted
echo MFA_TOTP_ISSUER=IOE-DREAM >> .env.encrypted
echo MFA_TOTP_DIGITS=6 >> .env.encrypted
echo MFA_TOTP_PERIOD=30 >> .env.encrypted
echo. >> .env.encrypted

echo ✅ 加密配置文件已创建: .env.encrypted
echo.
echo 📋 下一步操作:
echo    1. 复制 .env.encrypted 中的配置到 .env 文件
echo    2. 或者直接替换 .env 文件中的占位符
echo    3. 重启相关服务以应用新配置
echo.

REM 清理临时文件
del *.tmp 2>nul

echo ✅ 完成！
pause