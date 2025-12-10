# IOE-DREAM Docker完整重建和部署脚本
# 修复JAR包清单问题后,重新构建所有Docker镜像并部署

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Docker重建和部署" -ForegroundColor Cyan  
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 1. 停止并清理现有容器
Write-Host "[1/5] 停止现有容器..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down -v
if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 已停止所有容器" -ForegroundColor Green
} else {
    Write-Host "✗ 停止容器失败" -ForegroundColor Red
}

Write-Host ""

# 2. 清理旧镜像(可选)
Write-Host "[2/5] 清理旧的Docker镜像..." -ForegroundColor Yellow
$oldImages = docker images "ioedream-*" -q
if ($oldImages) {
    Write-Host "找到 $($oldImages.Count) 个旧镜像,正在删除..." -ForegroundColor Gray
    docker rmi $oldImages -f 2>$null
    Write-Host "✓ 已清理旧镜像" -ForegroundColor Green
} else {
    Write-Host "✓ 没有旧镜像需要清理" -ForegroundColor Green
}

Write-Host ""

# 3. 构建所有Docker镜像
Write-Host "[3/5] 构建所有Docker镜像..." -ForegroundColor Yellow
Write-Host "这将需要20-30分钟..." -ForegroundColor Gray

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

$buildSuccess = 0
$buildFailed = @()

foreach ($service in $services) {
    Write-Host "`n  构建 $service..." -ForegroundColor Cyan
    
    docker build `
        --build-arg BUILDKIT_INLINE_CACHE=1 `
        -f "microservices/$service/Dockerfile" `
        -t "${service}:latest" `
        .
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ $service 构建成功" -ForegroundColor Green
        $buildSuccess++
    } else {
        Write-Host "  ✗ $service 构建失败" -ForegroundColor Red
        $buildFailed += $service
    }
}

Write-Host ""
Write-Host "构建完成: 成功 $buildSuccess / $($services.Count)" -ForegroundColor $(if ($buildSuccess -eq $services.Count) { "Green" } else { "Yellow" })

if ($buildFailed.Count -gt 0) {
    Write-Host "构建失败的服务: $($buildFailed -join ', ')" -ForegroundColor Red
    Write-Host ""
    $continue = Read-Host "是否继续部署? (y/n)"
    if ($continue -ne 'y') {
        exit 1
    }
}

Write-Host ""

# 4. 启动Docker Compose
Write-Host "[4/5] 启动Docker Compose..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml up -d

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Docker Compose启动成功" -ForegroundColor Green
} else {
    Write-Host "✗ Docker Compose启动失败" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 5. 等待并检查服务健康状态
Write-Host "[5/5] 检查服务健康状态..." -ForegroundColor Yellow
Write-Host "等待30秒让服务启动..." -ForegroundColor Gray
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "服务状态:" -ForegroundColor Cyan
docker ps --filter "name=ioedream" --format "table {{.Names}}\t{{.Status}}" | Write-Host

Write-Host ""

# 检查基础设施
Write-Host "基础设施健康检查:" -ForegroundColor Cyan

$infraServices = @{
    "MySQL" = "http://localhost:3306"
    "Redis" = "http://localhost:6379"  
    "Nacos" = "http://localhost:8848/nacos"
}

foreach ($service in $infraServices.Keys) {
    $container = docker ps --filter "name=$service" --format "{{.Names}}" 2>$null
    if ($container) {
        $status = docker inspect --format='{{.State.Health.Status}}' $container 2>$null
        if ($status -eq "healthy") {
            Write-Host "  ✓ $service - Healthy" -ForegroundColor Green
        } else {
            Write-Host "  ⚠ $service - $status" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ $service - Not Running" -ForegroundColor Red
    }
}

Write-Host ""

# 检查微服务
Write-Host "微服务健康检查:" -ForegroundColor Cyan

$microservicePorts = @{
    "gateway-service" = 8080
    "common-service" = 8088
    "device-comm-service" = 8087
    "oa-service" = 8089
    "access-service" = 8090
    "attendance-service" = 8091
    "video-service" = 8092
    "consume-service" = 8094
    "visitor-service" = 8095
}

Start-Sleep -Seconds 10

foreach ($service in $microservicePorts.Keys) {
    $port = $microservicePorts[$service]
    $containerName = "ioedream-$service"
    
    $container = docker ps --filter "name=$containerName" --format "{{.Names}}" 2>$null
    if ($container) {
        $running = docker inspect --format='{{.State.Running}}' $containerName 2>$null
        if ($running -eq "true") {
            Write-Host "  ✓ $service (端口:$port) - Running" -ForegroundColor Green
        } else {
            Write-Host "  ✗ $service (端口:$port) - Stopped" -ForegroundColor Red
        }
    } else {
        Write-Host "  ✗ $service (端口:$port) - Not Found" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "部署完成!" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "访问地址:" -ForegroundColor Yellow
Write-Host "  Nacos控制台: http://localhost:8848/nacos (nacos/nacos)" -ForegroundColor White
Write-Host "  API网关: http://localhost:8080" -ForegroundColor White
Write-Host "  公共服务: http://localhost:8088" -ForegroundColor White
Write-Host ""
Write-Host "查看日志命令:" -ForegroundColor Yellow
Write-Host "  docker logs -f ioedream-gateway-service" -ForegroundColor Gray
Write-Host "  docker logs -f ioedream-common-service" -ForegroundColor Gray
Write-Host ""
Write-Host "查看所有服务状态:" -ForegroundColor Yellow  
Write-Host "  docker-compose -f docker-compose-all.yml ps" -ForegroundColor Gray
Write-Host ""
