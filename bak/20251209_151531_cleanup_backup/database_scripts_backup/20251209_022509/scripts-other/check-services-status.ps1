# 检查IOE-DREAM所有服务状态

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 服务状态检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查所有容器状态
Write-Host "[1] 容器状态检查" -ForegroundColor Yellow
Write-Host ""
$containers = @(
    "ioedream-mysql",
    "ioedream-redis", 
    "ioedream-nacos",
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

foreach ($container in $containers) {
    $status = docker inspect --format='{{.State.Status}}' $container 2>$null
    $health = docker inspect --format='{{.State.Health.Status}}' $container 2>$null
    
    if ($status -eq "running") {
        if ($health -eq "healthy" -or $health -eq "") {
            Write-Host "  ✅ $container : $status" -ForegroundColor Green
        } elseif ($health -eq "unhealthy") {
            Write-Host "  ⚠️  $container : $status ($health)" -ForegroundColor Yellow
        } else {
            Write-Host "  ✅ $container : $status" -ForegroundColor Green
        }
    } elseif ($status -eq "exited") {
        $exitCode = docker inspect --format='{{.State.ExitCode}}' $container 2>$null
        Write-Host "  ❌ $container : $status (退出码: $exitCode)" -ForegroundColor Red
    } else {
        Write-Host "  ⚠️  $container : $status" -ForegroundColor Yellow
    }
}

Write-Host ""

# 2. 检查Nacos健康状态
Write-Host "[2] Nacos健康检查" -ForegroundColor Yellow
$nacosHealth = docker exec ioedream-nacos wget -qO- http://localhost:8848/nacos/v2/console/health/readiness 2>$null
if ($nacosHealth -match '"status":"UP"') {
    Write-Host "  ✅ Nacos健康检查通过" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Nacos健康检查未通过或无法访问" -ForegroundColor Yellow
    Write-Host "  响应: $nacosHealth" -ForegroundColor Gray
}
Write-Host ""

# 3. 检查关键服务日志（错误）
Write-Host "[3] 关键服务错误检查" -ForegroundColor Yellow
$testServices = @("ioedream-attendance-service", "ioedream-common-service", "ioedream-gateway-service")
foreach ($service in $testServices) {
    $errors = docker logs $service --tail 100 2>&1 | Select-String -Pattern "No spring.config.import|APPLICATION FAILED|Invalid bean definition|Error|ERROR" | Select-Object -First 3
    if ($errors) {
        Write-Host "  ⚠️  $service 发现错误:" -ForegroundColor Yellow
        $errors | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
    } else {
        Write-Host "  ✅ $service 无关键错误" -ForegroundColor Green
    }
}
Write-Host ""

# 4. 检查服务启动状态
Write-Host "[4] 服务启动状态检查" -ForegroundColor Yellow
foreach ($service in $testServices) {
    $started = docker logs $service --tail 50 2>&1 | Select-String -Pattern "Started.*in.*seconds|Application startup completed" | Select-Object -First 1
    if ($started) {
        Write-Host "  ✅ $service 已启动" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  $service 可能还在启动中" -ForegroundColor Yellow
    }
}
Write-Host ""

# 5. 检查内存使用
Write-Host "[5] 内存使用情况" -ForegroundColor Yellow
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}" | Select-String -Pattern "ioedream|NAME" | ForEach-Object {
    Write-Host "  $_" -ForegroundColor Gray
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  检查完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
