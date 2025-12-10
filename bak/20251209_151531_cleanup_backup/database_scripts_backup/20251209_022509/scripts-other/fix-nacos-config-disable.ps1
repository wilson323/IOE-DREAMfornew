# ============================================================
# Nacos配置中心禁用修复脚本
# 
# 功能: 重新构建所有微服务并重启Docker容器
# 用途: 应用Nacos配置中心禁用修复
# 
# @Author: IOE-DREAM Team
# @Date: 2025-12-08
# ============================================================

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Nacos配置中心禁用修复脚本" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否在正确的目录
if (-not (Test-Path "microservices\pom.xml")) {
    Write-Host "错误: 请在项目根目录执行此脚本" -ForegroundColor Red
    exit 1
}

# 步骤1: 停止所有服务
Write-Host "[1/4] 停止所有Docker服务..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down
if ($LASTEXITCODE -ne 0) {
    Write-Host "警告: 停止服务时出现错误，继续执行..." -ForegroundColor Yellow
}

# 步骤2: 清理并重新构建所有微服务
Write-Host "[2/4] 清理并重新构建所有微服务..." -ForegroundColor Yellow
Set-Location microservices
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: Maven构建失败" -ForegroundColor Red
    Set-Location ..
    exit 1
}
Set-Location ..

# 步骤3: 重新构建Docker镜像
Write-Host "[3/4] 重新构建Docker镜像..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml build
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: Docker镜像构建失败" -ForegroundColor Red
    exit 1
}

# 步骤4: 启动所有服务
Write-Host "[4/4] 启动所有Docker服务..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: Docker服务启动失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "等待服务启动（约30秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 检查服务状态
Write-Host ""
Write-Host "检查服务状态..." -ForegroundColor Cyan
docker-compose -f docker-compose-all.yml ps

# 检查错误日志
Write-Host ""
Write-Host "检查错误日志（dataId相关）..." -ForegroundColor Cyan
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

$hasError = $false
foreach ($service in $services) {
    $logs = docker logs $service --tail 20 2>&1 | Select-String "dataId must be specified"
    if ($logs) {
        Write-Host "  ❌ $service : 仍有dataId错误" -ForegroundColor Red
        $hasError = $true
    } else {
        Write-Host "  ✅ $service : 无dataId错误" -ForegroundColor Green
    }
}

if ($hasError) {
    Write-Host ""
    Write-Host "警告: 部分服务仍有错误，请检查日志" -ForegroundColor Yellow
    Write-Host "查看详细日志: docker logs <service-name> --tail 50" -ForegroundColor Yellow
} else {
    Write-Host ""
    Write-Host "✅ 所有服务修复成功！" -ForegroundColor Green
}

Write-Host ""
Write-Host "查看所有服务日志: docker-compose -f docker-compose-all.yml logs --tail=50" -ForegroundColor Cyan
