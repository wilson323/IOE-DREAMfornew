# =====================================================
# 微服务快速启动脚本（通用版）
# 用途: 设置环境变量并启动指定的微服务
# 创建时间: 2025-12-14
# =====================================================

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("attendance", "visitor", "consume", "access", "oa", "video", "device-comm", "common", "gateway")]
    [string]$Service,

    [string]$MysqlPassword = "123456",
    [string]$NacosPassword = "nacos",
    [string]$RedisPassword = "redis123"
)

$ErrorActionPreference = "Stop"

# 服务名称映射
$serviceMap = @{
    "attendance" = "ioedream-attendance-service"
    "visitor" = "ioedream-visitor-service"
    "consume" = "ioedream-consume-service"
    "access" = "ioedream-access-service"
    "oa" = "ioedream-oa-service"
    "video" = "ioedream-video-service"
    "device-comm" = "ioedream-device-comm-service"
    "common" = "ioedream-common-service"
    "gateway" = "ioedream-gateway-service"
}

$serviceName = $serviceMap[$Service]

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "微服务快速启动脚本" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  服务: $serviceName" -ForegroundColor Yellow
Write-Host ""

# 获取项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
$servicePath = Join-Path $projectRoot "microservices\$serviceName"

if (-not (Test-Path $servicePath)) {
    Write-Host "[ERROR] 服务目录不存在: $servicePath" -ForegroundColor Red
    exit 1
}

# 如果使用默认密码，显示提示；否则提示用户输入
if ($MysqlPassword -eq "123456") {
    Write-Host "[INFO] 使用默认MySQL密码: 123456" -ForegroundColor Yellow
    Write-Host "[INFO] 如需使用其他密码，请通过参数传递: -MysqlPassword 'your-password'" -ForegroundColor Gray
} elseif ([string]::IsNullOrWhiteSpace($MysqlPassword)) {
    Write-Host "[INFO] 请输入MySQL数据库密码 (直接回车使用空密码):" -ForegroundColor Yellow
    $securePassword = Read-Host -AsSecureString
    if ($securePassword) {
        $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        $MysqlPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
    }
}

# 设置环境变量
Write-Host "[INFO] 设置环境变量..." -ForegroundColor Yellow

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

# 设置Maven JVM参数（解决编译和启动时内存不足问题）
if (-not $env:MAVEN_OPTS) {
    $env:MAVEN_OPTS = "-Xms1024m -Xmx2048m -XX:MaxMetaspaceSize=512m"
}

# 设置Spring Boot JVM参数（针对公共服务等内存需求较大的服务）
if ($Service -eq "common") {
    $env:JAVA_OPTS = "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
} else {
    $env:JAVA_OPTS = "-Xms256m -Xmx512m -XX:+UseG1GC"
}

Write-Host "[SUCCESS] 环境变量已设置" -ForegroundColor Green
Write-Host "  MySQL Password: [已设置]" -ForegroundColor Gray
Write-Host "  Nacos Password: $env:NACOS_PASSWORD" -ForegroundColor Gray
Write-Host "  MAVEN_OPTS: $env:MAVEN_OPTS" -ForegroundColor Gray
Write-Host "  JAVA_OPTS: $env:JAVA_OPTS" -ForegroundColor Gray
Write-Host ""

# 切换到服务目录
Set-Location $servicePath

Write-Host "[INFO] 切换到服务目录: $servicePath" -ForegroundColor Yellow
Write-Host ""

# 启动服务
Write-Host "[INFO] 启动 $serviceName ..." -ForegroundColor Yellow
Write-Host "  执行命令: mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""

try {
    mvn spring-boot:run
} catch {
    Write-Host "[ERROR] 启动失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "1. MySQL服务是否启动" -ForegroundColor White
    Write-Host "2. Nacos服务是否启动" -ForegroundColor White
    Write-Host "3. 环境变量是否正确设置" -ForegroundColor White
    Write-Host "4. 端口是否被占用" -ForegroundColor White
    exit 1
}
