# =====================================================
# 所有微服务环境变量统一设置脚本
# 用途: 一次性设置所有微服务启动所需的环境变量
# 创建时间: 2025-12-14
# =====================================================

param(
    [string]$MysqlPassword = "123456",
    [string]$NacosPassword = "nacos",
    [string]$RedisPassword = "redis123"
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "所有微服务环境变量统一设置脚本" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 如果使用默认密码，显示提示；否则提示用户输入
if ($MysqlPassword -eq "123456") {
    Write-Host "[INFO] 使用默认MySQL密码: 123456" -ForegroundColor Yellow
    Write-Host "[INFO] 如需使用其他密码，请通过参数传递: -MysqlPassword 'your-password'" -ForegroundColor Gray
} elseif ([string]::IsNullOrWhiteSpace($MysqlPassword)) {
    Write-Host "[INFO] 请输入MySQL数据库密码:" -ForegroundColor Yellow
    $securePassword = Read-Host -AsSecureString
    if ($securePassword) {
        $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        $MysqlPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
    }
}

# MySQL配置
$env:MYSQL_HOST = "127.0.0.1"
$env:MYSQL_PORT = "3306"
$env:MYSQL_DATABASE = "ioedream"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = $MysqlPassword

# Nacos配置
$env:NACOS_SERVER_ADDR = "127.0.0.1:8848"
$env:NACOS_NAMESPACE = "dev"
$env:NACOS_GROUP = "IOE-DREAM"
$env:NACOS_USERNAME = "nacos"
$env:NACOS_PASSWORD = $NacosPassword
# Base64编码的密钥，长度>=32字节 (原文: IOE-DREAM-Nacos-Secret-Key-2024-Security)
$env:NACOS_AUTH_TOKEN = "SU9FLURSRUFNLU5hY29zLVNlY3JldC1LZXktMjAyNC1TZWN1cml0eQ=="

# Redis配置
$env:REDIS_HOST = "127.0.0.1"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = $RedisPassword
$env:REDIS_DATABASE = "0"
$env:REDIS_TIMEOUT = "3000"

# 服务端口配置
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:GATEWAY_URL = "http://localhost:8080"

Write-Host "[SUCCESS] 环境变量已设置" -ForegroundColor Green
Write-Host ""
Write-Host "MySQL配置:" -ForegroundColor Yellow
Write-Host "  Host: $env:MYSQL_HOST" -ForegroundColor Gray
Write-Host "  Port: $env:MYSQL_PORT" -ForegroundColor Gray
Write-Host "  Database: $env:MYSQL_DATABASE" -ForegroundColor Gray
Write-Host "  Username: $env:MYSQL_USERNAME" -ForegroundColor Gray
Write-Host "  Password: [已设置]" -ForegroundColor Gray
Write-Host ""
Write-Host "Nacos配置:" -ForegroundColor Yellow
Write-Host "  Server: $env:NACOS_SERVER_ADDR" -ForegroundColor Gray
Write-Host "  Namespace: $env:NACOS_NAMESPACE" -ForegroundColor Gray
Write-Host "  Group: $env:NACOS_GROUP" -ForegroundColor Gray
Write-Host "  Username: $env:NACOS_USERNAME" -ForegroundColor Gray
Write-Host "  Password: $env:NACOS_PASSWORD" -ForegroundColor Gray
Write-Host ""
Write-Host "Redis配置:" -ForegroundColor Yellow
Write-Host "  Host: $env:REDIS_HOST" -ForegroundColor Gray
Write-Host "  Port: $env:REDIS_PORT" -ForegroundColor Gray
Write-Host "  Password: $(if ([string]::IsNullOrWhiteSpace($env:REDIS_PASSWORD)) { '[未设置]' } else { '[已设置]' })" -ForegroundColor Gray
Write-Host "  Timeout: $env:REDIS_TIMEOUT" -ForegroundColor Gray
Write-Host ""
Write-Host "Nacos认证Token:" -ForegroundColor Yellow
Write-Host "  Token: [已设置]" -ForegroundColor Gray
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "现在可以启动任意微服务:" -ForegroundColor Green
Write-Host "  cd microservices\ioedream-access-service" -ForegroundColor Gray
Write-Host "  mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
