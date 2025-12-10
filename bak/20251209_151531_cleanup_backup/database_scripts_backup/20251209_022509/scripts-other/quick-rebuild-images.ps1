# 快速重建所有Docker镜像
# 使用修复后的Dockerfile重新构建

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "快速重建Docker镜像" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 停止所有容器
Write-Host "[1/4] 停止所有容器..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down
Write-Host "✓ 已停止" -ForegroundColor Green
Write-Host ""

# 删除旧镜像
Write-Host "[2/4] 删除旧镜像..." -ForegroundColor Yellow
$images = docker images "ioedream/*" -q
if ($images) {
    docker rmi -f $images
    Write-Host "✓ 已删除 $($images.Count) 个旧镜像" -ForegroundColor Green
} else {
    Write-Host "✓ 无需删除" -ForegroundColor Green
}
Write-Host ""

# 重新构建镜像
Write-Host "[3/4] 重新构建镜像 (预计20-25分钟)..." -ForegroundColor Yellow

$services = @(
    "gateway-service",
    "common-service", 
    "device-comm-service",
    "oa-service",
    "access-service",
    "attendance-service",
    "video-service",
    "consume-service",
    "visitor-service"
)

$success = 0
$failed = @()

foreach ($service in $services) {
    Write-Host "`n  构建 $service..." -ForegroundColor Cyan
    
    docker build `
        -f "microservices/ioedream-$service/Dockerfile" `
        -t "ioedream/${service}:latest" `
        . 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ $service 成功" -ForegroundColor Green
        $success++
    } else {
        Write-Host "  ✗ $service 失败" -ForegroundColor Red
        $failed += $service
    }
}

Write-Host ""
Write-Host "构建结果: $success 成功 / $($services.Count) 总计" -ForegroundColor $(if ($success -eq $services.Count) { "Green" } else { "Yellow" })

if ($failed.Count -gt 0) {
    Write-Host "失败: $($failed -join ', ')" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 启动服务
Write-Host "[4/4] 启动服务..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml up -d

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "✓ 重建完成!" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "等待30秒后检查状态..." -ForegroundColor Gray
Start-Sleep -Seconds 30

docker ps --filter "name=ioedream" --format "table {{.Names}}\t{{.Status}}"
Write-Host ""
