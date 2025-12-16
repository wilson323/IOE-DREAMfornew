# =====================================================
# 考勤服务快速启动脚本
# 用途: 快速设置环境变量并启动考勤服务
# 创建时间: 2025-12-14
# =====================================================

param(
    [string]$MysqlPassword = "123456",
    [string]$NacosPassword = "nacos"
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "考勤服务快速启动脚本" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 获取项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
$attendanceServicePath = Join-Path $projectRoot "microservices\ioedream-attendance-service"

if (-not (Test-Path $attendanceServicePath)) {
    Write-Host "[ERROR] 考勤服务目录不存在: $attendanceServicePath" -ForegroundColor Red
    exit 1
}

# 如果未提供MySQL密码，提示用户输入
if ([string]::IsNullOrWhiteSpace($MysqlPassword)) {
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

# Redis配置
$env:REDIS_HOST = "127.0.0.1"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = "123456"

# 服务端口
$env:ATTENDANCE_SERVICE_PORT = "8091"
$env:SPRING_PROFILES_ACTIVE = "dev"

Write-Host "[SUCCESS] 环境变量已设置" -ForegroundColor Green
Write-Host "  MySQL Host: $env:MYSQL_HOST" -ForegroundColor Gray
Write-Host "  MySQL Port: $env:MYSQL_PORT" -ForegroundColor Gray
Write-Host "  MySQL Database: $env:MYSQL_DATABASE" -ForegroundColor Gray
Write-Host "  MySQL Username: $env:MYSQL_USERNAME" -ForegroundColor Gray
Write-Host "  MySQL Password: [已设置]" -ForegroundColor Gray
Write-Host "  Nacos Server: $env:NACOS_SERVER_ADDR" -ForegroundColor Gray
Write-Host "  Nacos Namespace: $env:NACOS_NAMESPACE" -ForegroundColor Gray
Write-Host "  Nacos Username: $env:NACOS_USERNAME" -ForegroundColor Gray
Write-Host "  Nacos Password: $env:NACOS_PASSWORD" -ForegroundColor Gray
Write-Host ""

# 切换到服务目录
Set-Location $attendanceServicePath

Write-Host "[INFO] 切换到服务目录: $attendanceServicePath" -ForegroundColor Yellow
Write-Host ""

# 启动服务
Write-Host "[INFO] 启动考勤服务..." -ForegroundColor Yellow
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
    Write-Host "4. 端口8091是否被占用" -ForegroundColor White
    exit 1
}
