# Docker Compose 诊断脚本
# 用途：诊断docker-compose-all.yml启动问题

Write-Host "=== Docker Compose 诊断工具 ===" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Get-Location
Set-Location $projectRoot

# 1. 检查Docker环境
Write-Host "1. 检查Docker环境..." -ForegroundColor Yellow
$dockerVersion = docker --version 2>&1
$composeVersion = docker-compose --version 2>&1

if ($dockerVersion) {
    Write-Host "  ✓ Docker: $dockerVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ Docker未安装或未启动" -ForegroundColor Red
    exit 1
}

if ($composeVersion) {
    Write-Host "  ✓ Docker Compose: $composeVersion" -ForegroundColor Green
} else {
    Write-Host "  ✗ Docker Compose未安装" -ForegroundColor Red
    exit 1
}

# 2. 检查端口占用
Write-Host "`n2. 检查端口占用..." -ForegroundColor Yellow
$ports = @(3306, 6379, 8848, 8080, 8088, 8087, 8089, 8090, 8091, 8092, 8094, 8095)
$conflicts = @()

foreach ($port in $ports) {
    $listening = netstat -ano | Select-String ":$port\s" | Select-String "LISTENING"
    if ($listening) {
        $conflicts += $port
        Write-Host "  ⚠ 端口 $port 被占用" -ForegroundColor Yellow
        $listening | ForEach-Object {
            Write-Host "    $_" -ForegroundColor Gray
        }
    } else {
        Write-Host "  ✓ 端口 $port 可用" -ForegroundColor Green
    }
}

if ($conflicts.Count -gt 0) {
    Write-Host "`n  ⚠ 发现 $($conflicts.Count) 个端口冲突" -ForegroundColor Yellow
    Write-Host "  建议：停止占用端口的服务或修改docker-compose-all.yml中的端口映射" -ForegroundColor Yellow
}

# 3. 检查容器状态
Write-Host "`n3. 检查容器状态..." -ForegroundColor Yellow
$containers = @(
    "ioedream-mysql",
    "ioedream-redis",
    "ioedream-nacos",
    "ioedream-db-init",
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

$runningCount = 0
$stoppedCount = 0
$notFoundCount = 0

foreach ($container in $containers) {
    $status = docker inspect $container --format='{{.State.Status}}' 2>$null
    $health = docker inspect $container --format='{{.State.Health.Status}}' 2>$null
    
    if ($status) {
        if ($status -eq "running") {
            $runningCount++
            if ($health) {
                if ($health -eq "healthy") {
                    Write-Host "  ✓ $container : $status ($health)" -ForegroundColor Green
                } elseif ($health -eq "unhealthy") {
                    Write-Host "  ✗ $container : $status ($health)" -ForegroundColor Red
                } else {
                    Write-Host "  △ $container : $status ($health)" -ForegroundColor Yellow
                }
            } else {
                Write-Host "  ✓ $container : $status" -ForegroundColor Green
            }
        } else {
            $stoppedCount++
            Write-Host "  ✗ $container : $status" -ForegroundColor Red
        }
    } else {
        $notFoundCount++
        Write-Host "  - $container : 不存在" -ForegroundColor Gray
    }
}

Write-Host "`n  统计: 运行中($runningCount) 已停止($stoppedCount) 不存在($notFoundCount)" -ForegroundColor Cyan

# 4. 检查Redis健康检查配置
Write-Host "`n4. 检查Redis健康检查配置..." -ForegroundColor Yellow
$redisHealthCheck = Get-Content docker-compose-all.yml | Select-String -Pattern "redis:" -Context 0,15 | Select-String -Pattern "test:"
if ($redisHealthCheck -match "redis-cli.*ping") {
    if ($redisHealthCheck -match "-a|password") {
        Write-Host "  ✓ Redis健康检查包含密码" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Redis健康检查缺少密码参数" -ForegroundColor Red
        Write-Host "    修复: 将 'test: [\"CMD\", \"redis-cli\", \"ping\"]' 改为" -ForegroundColor Yellow
        Write-Host "          'test: [\"CMD-SHELL\", \"redis-cli -a \${REDIS_PASSWORD:-redis123} ping || exit 1\"]'" -ForegroundColor Yellow
    }
}

# 5. 检查微服务镜像
Write-Host "`n5. 检查微服务镜像..." -ForegroundColor Yellow
$images = @(
    "ioedream/gateway-service:latest",
    "ioedream/common-service:latest",
    "ioedream/device-comm-service:latest",
    "ioedream/oa-service:latest",
    "ioedream/access-service:latest",
    "ioedream/attendance-service:latest",
    "ioedream/video-service:latest",
    "ioedream/consume-service:latest",
    "ioedream/visitor-service:latest"
)

$missingImages = @()
foreach ($image in $images) {
    $exists = docker images $image --format "{{.Repository}}:{{.Tag}}" 2>$null
    if ($exists) {
        Write-Host "  ✓ $image" -ForegroundColor Green
    } else {
        $missingImages += $image
        Write-Host "  ✗ $image (不存在，需要构建)" -ForegroundColor Red
    }
}

if ($missingImages.Count -gt 0) {
    Write-Host "`n  ⚠ 发现 $($missingImages.Count) 个镜像缺失" -ForegroundColor Yellow
    Write-Host "  执行构建: docker-compose -f docker-compose-all.yml build" -ForegroundColor Yellow
}

# 6. 检查Dockerfile
Write-Host "`n6. 检查Dockerfile..." -ForegroundColor Yellow
$dockerfiles = @(
    "microservices/ioedream-gateway-service/Dockerfile",
    "microservices/ioedream-common-service/Dockerfile",
    "microservices/ioedream-device-comm-service/Dockerfile",
    "microservices/ioedream-oa-service/Dockerfile",
    "microservices/ioedream-access-service/Dockerfile",
    "microservices/ioedream-attendance-service/Dockerfile",
    "microservices/ioedream-video-service/Dockerfile",
    "microservices/ioedream-consume-service/Dockerfile",
    "microservices/ioedream-visitor-service/Dockerfile"
)

$missingDockerfiles = @()
foreach ($dockerfile in $dockerfiles) {
    if (Test-Path $dockerfile) {
        Write-Host "  ✓ $dockerfile" -ForegroundColor Green
    } else {
        $missingDockerfiles += $dockerfile
        Write-Host "  ✗ $dockerfile (不存在)" -ForegroundColor Red
    }
}

if ($missingDockerfiles.Count -gt 0) {
    Write-Host "`n  ✗ 发现 $($missingDockerfiles.Count) 个Dockerfile缺失" -ForegroundColor Red
}

# 7. 检查网络
Write-Host "`n7. 检查网络..." -ForegroundColor Yellow
$networkExists = docker network inspect ioedream-network 2>$null
if ($networkExists) {
    Write-Host "  ✓ ioedream-network 网络存在" -ForegroundColor Green
    $networkInfo = docker network inspect ioedream-network --format='{{.Driver}}' 2>$null
    Write-Host "    驱动: $networkInfo" -ForegroundColor Gray
} else {
    Write-Host "  - ioedream-network 网络不存在（启动时会自动创建）" -ForegroundColor Gray
}

# 8. 检查数据卷
Write-Host "`n8. 检查数据卷..." -ForegroundColor Yellow
$volumes = @("ioedream_mysql_data", "ioedream_redis_data", "ioedream_nacos_data", "ioedream_nacos_logs")
foreach ($volume in $volumes) {
    $exists = docker volume inspect $volume 2>$null
    if ($exists) {
        Write-Host "  ✓ $volume" -ForegroundColor Green
    } else {
        Write-Host "  - $volume (不存在，启动时会自动创建)" -ForegroundColor Gray
    }
}

# 9. 生成诊断报告
Write-Host "`n=== 诊断完成 ===" -ForegroundColor Cyan

$issues = @()
if ($conflicts.Count -gt 0) { $issues += "端口冲突: $($conflicts.Count) 个" }
if ($stoppedCount -gt 0) { $issues += "容器停止: $stoppedCount 个" }
if ($missingImages.Count -gt 0) { $issues += "镜像缺失: $($missingImages.Count) 个" }
if ($missingDockerfiles.Count -gt 0) { $issues += "Dockerfile缺失: $($missingDockerfiles.Count) 个" }

if ($issues.Count -eq 0) {
    Write-Host "✓ 未发现明显问题" -ForegroundColor Green
    Write-Host "`n建议执行: docker-compose -f docker-compose-all.yml up -d" -ForegroundColor Yellow
} else {
    Write-Host "⚠ 发现以下问题:" -ForegroundColor Yellow
    foreach ($issue in $issues) {
        Write-Host "  - $issue" -ForegroundColor Yellow
    }
    Write-Host "`n请根据上述问题逐一修复后重试" -ForegroundColor Yellow
}

Write-Host "`n详细诊断报告: documentation/deployment/docker/docker-compose-issue-diagnosis.md" -ForegroundColor Cyan
