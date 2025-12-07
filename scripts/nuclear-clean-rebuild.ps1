# 核武器级清理和重建 - 彻底清理所有Docker缓存
# 解决Docker使用缓存旧版本Dockerfile的问题

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Red
Write-Host "⚠️  核武器级清理 - 将删除所有Docker缓存" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red
Write-Host ""
Write-Host "此操作将:" -ForegroundColor Yellow
Write-Host "  1. 停止并删除所有容器和卷" -ForegroundColor White
Write-Host "  2. 删除所有未使用的镜像" -ForegroundColor White
Write-Host "  3. 清理所有构建缓存" -ForegroundColor White
Write-Host "  4. 清理所有未使用的网络" -ForegroundColor White
Write-Host "  5. 清理所有未使用的卷" -ForegroundColor White
Write-Host ""
$confirm = Read-Host "确认执行? (输入 YES 继续)"

if ($confirm -ne "YES") {
    Write-Host "操作已取消" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "开始核武器级清理..." -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 停止并删除所有容器和卷
Write-Host "步骤1: 停止并删除所有容器和卷..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down -v --remove-orphans
Write-Host "✅ 容器清理完成" -ForegroundColor Green
Write-Host ""

# 步骤2: 删除所有未使用的镜像
Write-Host "步骤2: 删除所有未使用的镜像..." -ForegroundColor Yellow
docker image prune -af
Write-Host "✅ 镜像清理完成" -ForegroundColor Green
Write-Host ""

# 步骤3: 清理所有构建缓存
Write-Host "步骤3: 清理所有构建缓存..." -ForegroundColor Yellow
docker builder prune -af
Write-Host "✅ 构建缓存清理完成" -ForegroundColor Green
Write-Host ""

# 步骤4: 清理所有未使用的网络
Write-Host "步骤4: 清理所有未使用的网络..." -ForegroundColor Yellow
docker network prune -f
Write-Host "✅ 网络清理完成" -ForegroundColor Green
Write-Host ""

# 步骤5: 清理所有未使用的卷
Write-Host "步骤5: 清理所有未使用的卷..." -ForegroundColor Yellow
docker volume prune -f
Write-Host "✅ 卷清理完成" -ForegroundColor Green
Write-Host ""

# 步骤6: 验证Dockerfile修复
Write-Host "步骤6: 验证Dockerfile修复..." -ForegroundColor Yellow
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
            $content -match "mvn install:install-file -Dfile=pom\.xml" -and
            -not ($content -match "pom-temp\.xml")) {
            Write-Host "  ✅ $service - V5方案正确" -ForegroundColor Green
        } elseif ($content -match "pom-temp\.xml") {
            Write-Host "  ❌ $service - 仍使用V4方案（pom-temp.xml）" -ForegroundColor Red
            $allFixed = $false
        } elseif ($content -match "python3") {
            Write-Host "  ❌ $service - 仍使用V3方案（python3）" -ForegroundColor Red
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

# 步骤7: 强制重新构建
Write-Host "步骤7: 强制重新构建所有服务（不使用缓存，拉取最新基础镜像）..." -ForegroundColor Yellow
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
    
    # 步骤8: 启动服务
    Write-Host "步骤8: 启动所有服务..." -ForegroundColor Yellow
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
    $logCmd = "docker-compose -f docker-compose-all.yml build --progress=plain 2>&1 | Select-String -Pattern ERROR"
    Write-Host "  $logCmd" -ForegroundColor White
    exit 1
}
