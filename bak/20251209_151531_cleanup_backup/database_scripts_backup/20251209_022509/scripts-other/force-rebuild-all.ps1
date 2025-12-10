# 强制重新构建所有服务（清理Docker缓存）
# 解决Docker使用旧版本Dockerfile的问题

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 强制重新构建脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 停止并删除所有容器
Write-Host "步骤1: 停止并删除所有容器..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down -v
Write-Host "✅ 容器清理完成" -ForegroundColor Green
Write-Host ""

# 步骤2: 清理Docker构建缓存
Write-Host "步骤2: 清理Docker构建缓存..." -ForegroundColor Yellow
docker builder prune -af --filter "until=24h"
Write-Host "✅ 构建缓存清理完成" -ForegroundColor Green
Write-Host ""

# 步骤3: 验证Dockerfile修复
Write-Host "步骤3: 验证Dockerfile修复..." -ForegroundColor Yellow
$allFixed = $true
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

foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    if (Test-Path $dockerfilePath) {
        $content = Get-Content $dockerfilePath -Raw
        if ($content -match "cp pom\.xml pom-original\.xml" -and 
            $content -match "awk.*pom-original\.xml > pom\.xml" -and
            $content -match "mvn install:install-file -Dfile=pom\.xml") {
            Write-Host "  ✅ $service - V5方案已应用" -ForegroundColor Green
        } elseif ($content -match "pom-temp\.xml") {
            Write-Host "  ❌ $service - 仍使用V4方案（pom-temp.xml）" -ForegroundColor Red
            $allFixed = $false
        } else {
            Write-Host "  ⚠️  $service - 未找到V5方案" -ForegroundColor Yellow
            $allFixed = $false
        }
    } else {
        Write-Host "  ❌ $service - Dockerfile不存在" -ForegroundColor Red
        $allFixed = $false
    }
}

Write-Host ""

if (-not $allFixed) {
    Write-Host "❌ 部分Dockerfile未正确修复，请先修复后再构建" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 所有Dockerfile验证通过" -ForegroundColor Green
Write-Host ""

# 步骤4: 构建所有服务
Write-Host "步骤4: 构建所有服务镜像（不使用缓存）..." -ForegroundColor Yellow
Write-Host "这可能需要10-30分钟，请耐心等待..." -ForegroundColor Cyan
Write-Host ""

$buildStartTime = Get-Date
docker-compose -f docker-compose-all.yml build --no-cache --pull

if ($LASTEXITCODE -eq 0) {
    $buildEndTime = Get-Date
    $buildDuration = $buildEndTime - $buildStartTime
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 构建成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "构建耗时: $($buildDuration.TotalMinutes.ToString('F2')) 分钟" -ForegroundColor Cyan
    Write-Host ""
    
    # 步骤5: 启动服务
    Write-Host "步骤5: 启动所有服务..." -ForegroundColor Yellow
    docker-compose -f docker-compose-all.yml up -d
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 服务启动命令执行成功" -ForegroundColor Green
        Write-Host ""
        Write-Host "等待服务启动（180秒）..." -ForegroundColor Yellow
        Start-Sleep -Seconds 180
        
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✅ 构建和部署完成！" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "服务访问地址:" -ForegroundColor Cyan
        Write-Host "  - 网关服务: http://localhost:8080" -ForegroundColor White
        Write-Host "  - Nacos控制台: http://localhost:8848/nacos" -ForegroundColor White
        Write-Host "  - 公共服务: http://localhost:8088" -ForegroundColor White
        Write-Host ""
        Write-Host "查看服务状态:" -ForegroundColor Cyan
        Write-Host "  docker-compose -f docker-compose-all.yml ps" -ForegroundColor White
    } else {
        Write-Host "❌ 服务启动失败" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 构建失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查构建日志:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-all.yml build --progress=plain 2>&1 | Select-String -Pattern ERROR" -ForegroundColor White
    exit 1
}
