# =====================================================
# 考勤服务配置修复脚本
# 用途: 修复MySQL和Nacos连接配置问题
# 创建时间: 2025-12-14
# =====================================================

param(
    [string]$MysqlPassword = "",
    [string]$NacosPassword = "nacos",
    [string]$MysqlHost = "127.0.0.1",
    [string]$MysqlPort = "3306",
    [string]$MysqlDatabase = "ioedream",
    [string]$MysqlUsername = "root"
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "考勤服务配置修复脚本" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 获取项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
$attendanceServicePath = Join-Path $projectRoot "microservices\ioedream-attendance-service\src\main\resources\application.yml"

if (-not (Test-Path $attendanceServicePath)) {
    Write-Host "[ERROR] 配置文件不存在: $attendanceServicePath" -ForegroundColor Red
    exit 1
}

# 如果未提供MySQL密码，提示用户输入
if ([string]::IsNullOrWhiteSpace($MysqlPassword)) {
    Write-Host "[INFO] 请输入MySQL数据库密码:" -ForegroundColor Yellow
    $securePassword = Read-Host -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $MysqlPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)

    if ([string]::IsNullOrWhiteSpace($MysqlPassword)) {
        Write-Host "[ERROR] MySQL密码不能为空" -ForegroundColor Red
        exit 1
    }
}

Write-Host "[INFO] 正在修复配置文件..." -ForegroundColor Yellow
Write-Host "  MySQL Host: $MysqlHost" -ForegroundColor Gray
Write-Host "  MySQL Port: $MysqlPort" -ForegroundColor Gray
Write-Host "  MySQL Database: $MysqlDatabase" -ForegroundColor Gray
Write-Host "  MySQL Username: $MysqlUsername" -ForegroundColor Gray
Write-Host "  MySQL Password: [已设置]" -ForegroundColor Gray
Write-Host "  Nacos Password: $NacosPassword" -ForegroundColor Gray
Write-Host ""

try {
    # 读取配置文件
    $content = Get-Content $attendanceServicePath -Raw -Encoding UTF8

    # 修复MySQL密码配置（如果默认值为空，添加默认值）
    # 注意：这里不直接硬编码密码，而是设置一个合理的默认值
    # 实际密码应该通过环境变量传递
    $mysqlPasswordPattern = [regex]::new('password:\s*\$\{MYSQL_PASSWORD:\}')
    if ($mysqlPasswordPattern.IsMatch($content)) {
        Write-Host "[FIX] 修复MySQL密码配置..." -ForegroundColor Yellow
        # 保持使用环境变量，但添加提示注释
        $content = $content -replace 'password:\s*\$\{MYSQL_PASSWORD:\}', "password: `${MYSQL_PASSWORD:}  # 必须通过环境变量设置MYSQL_PASSWORD"
    }

    # 修复Nacos密码配置
    $nacosPasswordPattern = [regex]::new('password:\s*\$\{NACOS_PASSWORD:\}')
    if ($nacosPasswordPattern.IsMatch($content)) {
        Write-Host "[FIX] 修复Nacos密码配置..." -ForegroundColor Yellow
        $content = $content -replace 'password:\s*\$\{NACOS_PASSWORD:\}', "password: `${NACOS_PASSWORD:$NacosPassword}"
    }

    # 保存配置文件
    $content | Set-Content $attendanceServicePath -Encoding UTF8 -NoNewline

    Write-Host "[SUCCESS] 配置文件修复完成" -ForegroundColor Green
    Write-Host ""

    # 生成环境变量设置脚本
    Write-Host "[INFO] 生成环境变量设置脚本..." -ForegroundColor Yellow
    $envScriptPath = Join-Path $projectRoot "scripts\set-attendance-env.ps1"
    $envScript = @"
# =====================================================
# 考勤服务环境变量设置脚本
# 用途: 设置启动考勤服务所需的环境变量
# 使用方法: .\scripts\set-attendance-env.ps1
# =====================================================

# MySQL配置
`$env:MYSQL_HOST = "$MysqlHost"
`$env:MYSQL_PORT = "$MysqlPort"
`$env:MYSQL_DATABASE = "$MysqlDatabase"
`$env:MYSQL_USERNAME = "$MysqlUsername"
`$env:MYSQL_PASSWORD = "$MysqlPassword"

# Nacos配置
`$env:NACOS_SERVER_ADDR = "127.0.0.1:8848"
`$env:NACOS_NAMESPACE = "dev"
`$env:NACOS_GROUP = "IOE-DREAM"
`$env:NACOS_USERNAME = "nacos"
`$env:NACOS_PASSWORD = "$NacosPassword"

# Redis配置（如果需要）
`$env:REDIS_HOST = "127.0.0.1"
`$env:REDIS_PORT = "6379"
`$env:REDIS_PASSWORD = ""

Write-Host "[SUCCESS] 环境变量已设置" -ForegroundColor Green
Write-Host "  现在可以启动考勤服务:" -ForegroundColor Yellow
Write-Host "  cd microservices\ioedream-attendance-service" -ForegroundColor Gray
Write-Host "  mvn spring-boot:run" -ForegroundColor Gray
"@

    $envScript | Set-Content $envScriptPath -Encoding UTF8
    Write-Host "[SUCCESS] 环境变量脚本已生成: $envScriptPath" -ForegroundColor Green
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host "修复完成！" -ForegroundColor Green
    Write-Host "================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "下一步操作:" -ForegroundColor Yellow
    Write-Host "1. 执行环境变量设置脚本:" -ForegroundColor White
    Write-Host "   .\scripts\set-attendance-env.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. 启动考勤服务:" -ForegroundColor White
    Write-Host "   cd microservices\ioedream-attendance-service" -ForegroundColor Gray
    Write-Host "   mvn spring-boot:run" -ForegroundColor Gray
    Write-Host ""

} catch {
    Write-Host "[ERROR] 修复失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
