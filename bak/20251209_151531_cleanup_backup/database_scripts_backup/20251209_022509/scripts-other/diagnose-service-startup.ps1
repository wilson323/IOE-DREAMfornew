# 诊断服务启动问题

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 服务启动诊断" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查所有容器状态
Write-Host "[1] 容器状态检查" -ForegroundColor Yellow
Write-Host ""
$allContainers = docker ps -a --filter "name=ioedream" --format "{{.Names}}|{{.Status}}|{{.Ports}}"
if ($allContainers) {
    $allContainers | ForEach-Object {
        $parts = $_ -split '\|'
        $name = $parts[0]
        $status = $parts[1]
        $ports = $parts[2]
        
        if ($status -match "Up") {
            Write-Host "  ✅ $name : $status" -ForegroundColor Green
        } elseif ($status -match "Exited") {
            Write-Host "  ❌ $name : $status" -ForegroundColor Red
        } else {
            Write-Host "  ⚠️  $name : $status" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  ⚠️  未找到任何容器" -ForegroundColor Yellow
}
Write-Host ""

# 2. 检查Nacos健康状态
Write-Host "[2] Nacos健康状态检查" -ForegroundColor Yellow
$nacosHealth = docker inspect ioedream-nacos --format='{{.State.Health.Status}}' 2>$null
if ($nacosHealth -eq "healthy") {
    Write-Host "  ✅ Nacos健康检查: $nacosHealth" -ForegroundColor Green
} elseif ($nacosHealth -eq "unhealthy") {
    Write-Host "  ❌ Nacos健康检查: $nacosHealth" -ForegroundColor Red
    Write-Host "  原因: 健康检查命令可能失败" -ForegroundColor Gray
} elseif ($nacosHealth -eq "starting") {
    Write-Host "  ⏳ Nacos健康检查: $nacosHealth (正在启动)" -ForegroundColor Yellow
} else {
    Write-Host "  ⚠️  Nacos健康检查状态: $nacosHealth" -ForegroundColor Yellow
}

# 检查Nacos是否在运行
$nacosRunning = docker ps --filter "name=ioedream-nacos" --format "{{.Names}}"
if ($nacosRunning) {
    Write-Host "  ✅ Nacos容器正在运行" -ForegroundColor Green
} else {
    Write-Host "  ❌ Nacos容器未运行" -ForegroundColor Red
}
Write-Host ""

# 3. 检查依赖关系
Write-Host "[3] 服务依赖关系检查" -ForegroundColor Yellow
Write-Host "  微服务依赖:" -ForegroundColor Gray
Write-Host "    - Nacos (condition: service_healthy)" -ForegroundColor Gray
Write-Host "    - Redis (condition: service_healthy)" -ForegroundColor Gray
Write-Host "    - MySQL (condition: service_healthy)" -ForegroundColor Gray
Write-Host ""

# 4. 检查基础设施服务
Write-Host "[4] 基础设施服务检查" -ForegroundColor Yellow
$infraServices = @("ioedream-mysql", "ioedream-redis", "ioedream-nacos")
foreach ($service in $infraServices) {
    $status = docker inspect $service --format='{{.State.Status}}' 2>$null
    $health = docker inspect $service --format='{{.State.Health.Status}}' 2>$null
    
    if ($status -eq "running") {
        if ($health -eq "healthy" -or $health -eq "") {
            Write-Host "  ✅ $service : 运行中 (健康)" -ForegroundColor Green
        } elseif ($health -eq "unhealthy") {
            Write-Host "  ❌ $service : 运行中 (不健康)" -ForegroundColor Red
        } else {
            Write-Host "  ⚠️  $service : 运行中 ($health)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ❌ $service : $status" -ForegroundColor Red
    }
}
Write-Host ""

# 5. 检查微服务启动状态
Write-Host "[5] 微服务启动状态检查" -ForegroundColor Yellow
$microservices = @(
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

foreach ($service in $microservices) {
    $status = docker inspect $service --format='{{.State.Status}}' 2>$null
    if ($status -eq "running") {
        Write-Host "  ✅ $service : 运行中" -ForegroundColor Green
    } elseif ($status -eq "created") {
        Write-Host "  ⏳ $service : 已创建 (等待依赖服务)" -ForegroundColor Yellow
    } elseif ($status -eq "exited") {
        $exitCode = docker inspect $service --format='{{.State.ExitCode}}' 2>$null
        Write-Host "  ❌ $service : 已退出 (退出码: $exitCode)" -ForegroundColor Red
    } else {
        Write-Host "  ⚠️  $service : $status" -ForegroundColor Yellow
    }
}
Write-Host ""

# 6. 检查Nacos健康检查配置
Write-Host "[6] Nacos健康检查配置" -ForegroundColor Yellow
$healthcheck = docker inspect ioedream-nacos --format='{{json .Config.Healthcheck}}' 2>$null | ConvertFrom-Json
if ($healthcheck) {
    Write-Host "  健康检查命令: $($healthcheck.Test -join ' ')" -ForegroundColor Gray
    Write-Host "  检查间隔: $($healthcheck.Interval)" -ForegroundColor Gray
    Write-Host "  超时时间: $($healthcheck.Timeout)" -ForegroundColor Gray
    Write-Host "  重试次数: $($healthcheck.Retries)" -ForegroundColor Gray
    Write-Host "  启动等待: $($healthcheck.StartPeriod)" -ForegroundColor Gray
} else {
    Write-Host "  ⚠️  未找到健康检查配置" -ForegroundColor Yellow
}
Write-Host ""

# 7. 建议操作
Write-Host "[7] 建议操作" -ForegroundColor Yellow
if ($nacosHealth -ne "healthy") {
    Write-Host "  🔧 Nacos健康检查未通过，建议:" -ForegroundColor Yellow
    Write-Host "    1. 检查Nacos日志: docker logs ioedream-nacos --tail 50" -ForegroundColor Gray
    Write-Host "    2. 手动测试健康检查: docker exec ioedream-nacos sh -c 'timeout 3 bash -c \"</dev/tcp/localhost/8848\"'" -ForegroundColor Gray
    Write-Host "    3. 如果Nacos正常运行但健康检查失败，可能需要调整健康检查配置" -ForegroundColor Gray
}

$createdServices = docker ps -a --filter "name=ioedream" --filter "status=created" --format "{{.Names}}"
if ($createdServices) {
    Write-Host "  🔧 有服务处于'created'状态（等待依赖），建议:" -ForegroundColor Yellow
    Write-Host "    1. 等待Nacos健康检查通过（通常需要1-2分钟）" -ForegroundColor Gray
    Write-Host "    2. 检查依赖服务状态: docker-compose -f docker-compose-all.yml ps" -ForegroundColor Gray
    Write-Host "    3. 如果等待时间过长，检查Nacos健康检查配置" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  诊断完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
