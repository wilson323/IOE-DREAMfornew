# Docker服务状态检查脚本

Write-Host "===== Docker服务状态检查 =====" -ForegroundColor Cyan
Write-Host "执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n" -ForegroundColor Gray

# 检查Docker是否运行
Write-Host "[1] 检查Docker服务..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    Write-Host "  ✅ Docker已安装: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Docker未安装或未运行" -ForegroundColor Red
    exit 1
}

# 检查docker-compose文件
Write-Host "`n[2] 检查docker-compose配置..." -ForegroundColor Yellow
$composeFile = "D:\IOE-DREAM\documentation\technical\verification\docker\docker-compose.yml"
if (Test-Path $composeFile) {
    Write-Host "  ✅ docker-compose.yml文件存在" -ForegroundColor Green
    cd D:\IOE-DREAM\documentation\technical\verification\docker

    # 检查服务定义
    $services = docker-compose config --services 2>&1
    Write-Host "  定义的服务: $($services -join ', ')" -ForegroundColor Gray
} else {
    Write-Host "  ❌ docker-compose.yml文件不存在" -ForegroundColor Red
    exit 1
}

# 检查容器状态
Write-Host "`n[3] 检查容器状态..." -ForegroundColor Yellow
$containers = docker-compose ps 2>&1
Write-Host $containers

# 检查端口
Write-Host "`n[4] 检查服务端口..." -ForegroundColor Yellow

# MySQL
Write-Host "  检查MySQL (3306)..." -ForegroundColor Cyan
$mysql = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue -InformationLevel Quiet
if ($mysql) {
    Write-Host "    ✅ MySQL端口3306已开放" -ForegroundColor Green
} else {
    Write-Host "    ❌ MySQL端口3306未开放" -ForegroundColor Red
}

# Redis
Write-Host "  检查Redis (6379)..." -ForegroundColor Cyan
$redis = Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue -InformationLevel Quiet
if ($redis) {
    Write-Host "    ✅ Redis端口6379已开放" -ForegroundColor Green
} else {
    Write-Host "    ❌ Redis端口6379未开放" -ForegroundColor Red
}

# Nacos
Write-Host "  检查Nacos (8848)..." -ForegroundColor Cyan
$nacos = Test-NetConnection -ComputerName localhost -Port 8848 -WarningAction SilentlyContinue -InformationLevel Quiet
if ($nacos) {
    Write-Host "    ✅ Nacos端口8848已开放" -ForegroundColor Green
    Write-Host "    访问地址: http://localhost:8848/nacos" -ForegroundColor Gray
} else {
    Write-Host "    ❌ Nacos端口8848未开放" -ForegroundColor Red
}

# 检查网络
Write-Host "`n[5] 检查Docker网络..." -ForegroundColor Yellow
$networks = docker network ls --format "{{.Name}}" | Select-String "ioedream"
if ($networks) {
    Write-Host "  发现的网络:" -ForegroundColor Cyan
    $networks | ForEach-Object { Write-Host "    - $_" -ForegroundColor Gray }
} else {
    Write-Host "  ⚠️  未发现ioedream网络" -ForegroundColor Yellow
}

Write-Host "`n===== 检查完成 =====" -ForegroundColor Cyan
