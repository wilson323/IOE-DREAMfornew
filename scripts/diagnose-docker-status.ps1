# Docker服务状态诊断脚本
# 先检查现有状态，再决定是否需要启动服务

$ErrorActionPreference = "Continue"

Write-Host "===== Docker服务状态诊断 =====" -ForegroundColor Cyan
Write-Host "执行时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n" -ForegroundColor Gray

# 1. 检查Docker是否安装和运行
Write-Host "[1] 检查Docker环境..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ Docker已安装: $dockerVersion" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Docker未正确安装" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ Docker未安装或未运行" -ForegroundColor Red
    exit 1
}

# 2. 检查所有容器状态
Write-Host "`n[2] 检查所有容器状态..." -ForegroundColor Yellow
$allContainers = docker ps -a --format "{{.Names}}|{{.Status}}|{{.Ports}}" 2>&1
if ($allContainers) {
    Write-Host "  发现容器:" -ForegroundColor Cyan
    $allContainers | ForEach-Object {
        $parts = $_ -split '\|'
        $name = $parts[0]
        $status = $parts[1]
        $ports = $parts[2]
        Write-Host "    - $name : $status" -ForegroundColor Gray
        if ($ports) {
            Write-Host "      端口: $ports" -ForegroundColor DarkGray
        }
    }
} else {
    Write-Host "  ⚠️  未发现任何容器" -ForegroundColor Yellow
}

# 3. 检查MySQL相关容器
Write-Host "`n[3] 检查MySQL服务..." -ForegroundColor Yellow
$mysqlContainers = docker ps -a --filter "name=mysql" --format "{{.Names}}|{{.Status}}|{{.Ports}}" 2>&1
if ($mysqlContainers) {
    $mysqlContainers | ForEach-Object {
        $parts = $_ -split '\|'
        $name = $parts[0]
        $status = $parts[1]
        $ports = $parts[2]
        if ($status -match "Up") {
            Write-Host "  ✅ MySQL容器运行中: $name" -ForegroundColor Green
            Write-Host "    状态: $status" -ForegroundColor Gray
            Write-Host "    端口: $ports" -ForegroundColor Gray
        } else {
            Write-Host "  ⚠️  MySQL容器未运行: $name" -ForegroundColor Yellow
            Write-Host "    状态: $status" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "  ❌ 未发现MySQL容器" -ForegroundColor Red
}

# 4. 检查Redis相关容器
Write-Host "`n[4] 检查Redis服务..." -ForegroundColor Yellow
$redisContainers = docker ps -a --filter "name=redis" --format "{{.Names}}|{{.Status}}|{{.Ports}}" 2>&1
if ($redisContainers) {
    $redisContainers | ForEach-Object {
        $parts = $_ -split '\|'
        $name = $parts[0]
        $status = $parts[1]
        $ports = $parts[2]
        if ($status -match "Up") {
            Write-Host "  ✅ Redis容器运行中: $name" -ForegroundColor Green
            Write-Host "    状态: $status" -ForegroundColor Gray
            Write-Host "    端口: $ports" -ForegroundColor Gray
        } else {
            Write-Host "  ⚠️  Redis容器未运行: $name" -ForegroundColor Yellow
            Write-Host "    状态: $status" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "  ❌ 未发现Redis容器" -ForegroundColor Red
}

# 5. 检查Nacos相关容器
Write-Host "`n[5] 检查Nacos服务..." -ForegroundColor Yellow
$nacosContainers = docker ps -a --filter "name=nacos" --format "{{.Names}}|{{.Status}}|{{.Ports}}" 2>&1
if ($nacosContainers) {
    $nacosContainers | ForEach-Object {
        $parts = $_ -split '\|'
        $name = $parts[0]
        $status = $parts[1]
        $ports = $parts[2]
        if ($status -match "Up") {
            Write-Host "  ✅ Nacos容器运行中: $name" -ForegroundColor Green
            Write-Host "    状态: $status" -ForegroundColor Gray
            Write-Host "    端口: $ports" -ForegroundColor Gray
        } else {
            Write-Host "  ⚠️  Nacos容器未运行: $name" -ForegroundColor Yellow
            Write-Host "    状态: $status" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "  ❌ 未发现Nacos容器" -ForegroundColor Red
}

# 6. 检查端口占用情况
Write-Host "`n[6] 检查端口占用情况..." -ForegroundColor Yellow

# MySQL 3306
Write-Host "  检查MySQL端口 (3306)..." -ForegroundColor Cyan
$mysqlPort = Test-NetConnection -ComputerName localhost -Port 3306 -WarningAction SilentlyContinue -InformationLevel Quiet
if ($mysqlPort) {
    Write-Host "    ✅ 端口3306已开放" -ForegroundColor Green
    # 检查是Docker容器还是其他服务
    $port3306 = netstat -ano | findstr ":3306" | Select-Object -First 1
    if ($port3306) {
        Write-Host "    占用信息: $port3306" -ForegroundColor Gray
    }
} else {
    Write-Host "    ❌ 端口3306未开放" -ForegroundColor Red
}

# Redis 6379
Write-Host "  检查Redis端口 (6379)..." -ForegroundColor Cyan
$redisPort = Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue -InformationLevel Quiet
if ($redisPort) {
    Write-Host "    ✅ 端口6379已开放" -ForegroundColor Green
    $port6379 = netstat -ano | findstr ":6379" | Select-Object -First 1
    if ($port6379) {
        Write-Host "    占用信息: $port6379" -ForegroundColor Gray
    }
} else {
    Write-Host "    ❌ 端口6379未开放" -ForegroundColor Red
}

# Nacos 8848
Write-Host "  检查Nacos端口 (8848)..." -ForegroundColor Cyan
$nacosPort = Test-NetConnection -ComputerName localhost -Port 8848 -WarningAction SilentlyContinue -InformationLevel Quiet
if ($nacosPort) {
    Write-Host "    ✅ 端口8848已开放" -ForegroundColor Green
    Write-Host "    访问地址: http://localhost:8848/nacos" -ForegroundColor Gray
    $port8848 = netstat -ano | findstr ":8848" | Select-Object -First 1
    if ($port8848) {
        Write-Host "    占用信息: $port8848" -ForegroundColor Gray
    }
} else {
    Write-Host "    ❌ 端口8848未开放" -ForegroundColor Red
}

# 7. 检查Docker网络
Write-Host "`n[7] 检查Docker网络..." -ForegroundColor Yellow
$networks = docker network ls --format "{{.Name}}|{{.Driver}}|{{.Scope}}" 2>&1
if ($networks) {
    Write-Host "  发现的网络:" -ForegroundColor Cyan
    $networks | ForEach-Object {
        $parts = $_ -split '\|'
        $name = $parts[0]
        $driver = $parts[1]
        $scope = $parts[2]
        if ($name -match "ioedream") {
            Write-Host "    ✅ $name ($driver, $scope)" -ForegroundColor Green
        } else {
            Write-Host "    - $name ($driver, $scope)" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "  ⚠️  未发现任何网络" -ForegroundColor Yellow
}

# 8. 检查docker-compose配置
Write-Host "`n[8] 检查docker-compose配置..." -ForegroundColor Yellow
$composeFile = "D:\IOE-DREAM\documentation\technical\verification\docker\docker-compose.yml"
if (Test-Path $composeFile) {
    Write-Host "  ✅ docker-compose.yml文件存在" -ForegroundColor Green
    Push-Location D:\IOE-DREAM\documentation\technical\verification\docker
    try {
        $services = docker-compose config --services 2>&1
        if ($services) {
            Write-Host "  定义的服务: $($services -join ', ')" -ForegroundColor Gray

            # 检查docker-compose管理的容器状态
            Write-Host "`n  docker-compose管理的容器状态:" -ForegroundColor Cyan
            $composeStatus = docker-compose ps 2>&1
            Write-Host $composeStatus
        }
    } catch {
        Write-Host "  ⚠️  无法读取docker-compose配置: $_" -ForegroundColor Yellow
    } finally {
        Pop-Location
    }
} else {
    Write-Host "  ❌ docker-compose.yml文件不存在" -ForegroundColor Red
}

# 9. 总结和建议
Write-Host "`n===== 诊断总结 =====" -ForegroundColor Cyan

$mysqlRunning = $mysqlPort -or ($mysqlContainers -and ($mysqlContainers -match "Up"))
$redisRunning = $redisPort -or ($redisContainers -and ($redisContainers -match "Up"))
$nacosRunning = $nacosPort -or ($nacosContainers -and ($nacosContainers -match "Up"))

Write-Host "`n服务状态:" -ForegroundColor Yellow
Write-Host "  MySQL:  $(if ($mysqlRunning) { '✅ 运行中' } else { '❌ 未运行' })" -ForegroundColor $(if ($mysqlRunning) { 'Green' } else { 'Red' })
Write-Host "  Redis:  $(if ($redisRunning) { '✅ 运行中' } else { '❌ 未运行' })" -ForegroundColor $(if ($redisRunning) { 'Green' } else { 'Red' })
Write-Host "  Nacos:  $(if ($nacosRunning) { '✅ 运行中' } else { '❌ 未运行' })" -ForegroundColor $(if ($nacosRunning) { 'Green' } else { 'Red' })

if (-not ($mysqlRunning -and $redisRunning -and $nacosRunning)) {
    Write-Host "`n建议操作:" -ForegroundColor Yellow
    Write-Host "  启动缺失的服务:" -ForegroundColor Cyan
    Write-Host "    cd D:\IOE-DREAM\documentation\technical\verification\docker" -ForegroundColor Gray
    Write-Host "    docker-compose up -d mysql redis nacos" -ForegroundColor Gray
} else {
    Write-Host "`n✅ 所有服务已运行，可以启动微服务进行验证" -ForegroundColor Green
}

Write-Host "`n===== 诊断完成 =====" -ForegroundColor Cyan
