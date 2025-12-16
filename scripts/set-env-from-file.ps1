# =====================================================
# 从环境变量文件加载配置脚本
# 用途: 从.env文件或环境变量文件加载配置
# 创建时间: 2025-12-14
# =====================================================

param(
    [string]$EnvFile = ".env"
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "从环境变量文件加载配置" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 获取项目根目录（优先使用load-env.ps1的逻辑）
$loadEnvScript = Join-Path $PSScriptRoot "load-env.ps1"
if (Test-Path $loadEnvScript) {
    # 如果load-env.ps1存在，使用它来加载
    & $loadEnvScript
    return
}

# 否则使用原有逻辑
$projectRoot = Split-Path -Parent $PSScriptRoot
$envFilePath = Join-Path $projectRoot $EnvFile

if (-not (Test-Path $envFilePath)) {
    Write-Host "[WARN] 环境变量文件不存在: $envFilePath" -ForegroundColor Yellow
    Write-Host "[INFO] 使用默认配置值..." -ForegroundColor Yellow
    Write-Host ""

    # 使用默认值
    $env:MYSQL_HOST = "127.0.0.1"
    $env:MYSQL_PORT = "3306"
    $env:MYSQL_DATABASE = "ioedream"
    $env:MYSQL_USERNAME = "root"
    $env:MYSQL_PASSWORD = "123456"
    $env:MYSQL_ROOT_PASSWORD = "123456"

    $env:REDIS_HOST = "127.0.0.1"
    $env:REDIS_PORT = "6379"
    $env:REDIS_PASSWORD = "redis123"
    $env:REDIS_DATABASE = "0"
    $env:REDIS_TIMEOUT = "3000"

    $env:NACOS_SERVER_ADDR = "127.0.0.1:8848"
    $env:NACOS_NAMESPACE = "dev"
    $env:NACOS_GROUP = "IOE-DREAM"
    $env:NACOS_USERNAME = "nacos"
    $env:NACOS_PASSWORD = "nacos"
    $env:NACOS_AUTH_TOKEN = "SU9FLURSRUFNLU5hY29zLVNlY3JldC1LZXktMjAyNC1TZWN1cml0eQ=="
} else {
    Write-Host "[INFO] 从文件加载环境变量: $envFilePath" -ForegroundColor Yellow
    Write-Host ""

    # 读取环境变量文件
    $lines = Get-Content $envFilePath -Encoding UTF8

    foreach ($line in $lines) {
        # 跳过空行和注释行
        if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith("#")) {
            continue
        }

        # 解析 KEY=VALUE 格式
        if ($line -match '^([^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()

            # 移除引号（如果存在）
            if ($value.StartsWith('"') -and $value.EndsWith('"')) {
                $value = $value.Substring(1, $value.Length - 2)
            } elseif ($value.StartsWith("'") -and $value.EndsWith("'")) {
                $value = $value.Substring(1, $value.Length - 2)
            }

            # 设置环境变量
            Set-Item -Path "env:$key" -Value $value
            Write-Host "  [SET] $key = $(if ($key -match 'PASSWORD|TOKEN|SECRET') { '[已设置]' } else { $value })" -ForegroundColor Gray
        }
    }
}

# 服务端口配置
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:GATEWAY_URL = "http://localhost:8080"

Write-Host ""
Write-Host "[SUCCESS] 环境变量已加载" -ForegroundColor Green
Write-Host ""
Write-Host "MySQL配置:" -ForegroundColor Yellow
Write-Host "  Host: $env:MYSQL_HOST" -ForegroundColor Gray
Write-Host "  Port: $env:MYSQL_PORT" -ForegroundColor Gray
Write-Host "  Database: $env:MYSQL_DATABASE" -ForegroundColor Gray
Write-Host "  Username: $env:MYSQL_USERNAME" -ForegroundColor Gray
Write-Host "  Password: [已设置]" -ForegroundColor Gray
Write-Host ""
Write-Host "Redis配置:" -ForegroundColor Yellow
Write-Host "  Host: $env:REDIS_HOST" -ForegroundColor Gray
Write-Host "  Port: $env:REDIS_PORT" -ForegroundColor Gray
Write-Host "  Password: [已设置]" -ForegroundColor Gray
Write-Host "  Timeout: $env:REDIS_TIMEOUT" -ForegroundColor Gray
Write-Host ""
Write-Host "Nacos配置:" -ForegroundColor Yellow
Write-Host "  Server: $env:NACOS_SERVER_ADDR" -ForegroundColor Gray
Write-Host "  Namespace: $env:NACOS_NAMESPACE" -ForegroundColor Gray
Write-Host "  Group: $env:NACOS_GROUP" -ForegroundColor Gray
Write-Host "  Username: $env:NACOS_USERNAME" -ForegroundColor Gray
Write-Host "  Password: $env:NACOS_PASSWORD" -ForegroundColor Gray
Write-Host "  Auth Token: [已设置]" -ForegroundColor Gray
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "现在可以启动任意微服务:" -ForegroundColor Green
Write-Host "  cd microservices\ioedream-access-service" -ForegroundColor Gray
Write-Host "  mvn spring-boot:run" -ForegroundColor Gray
Write-Host ""
