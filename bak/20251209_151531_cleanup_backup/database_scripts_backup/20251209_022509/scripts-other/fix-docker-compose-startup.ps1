# ============================================================================
# IOE-DREAM Docker Compose 启动问题修复脚本
# 功能: 诊断和修复常见启动问题
# 创建日期: 2025-01-31
# ============================================================================

param(
    [switch]$CleanStart,      # 完全清理后重新启动
    [switch]$DiagnoseOnly,    # 仅诊断不修复
    [switch]$Force            # 强制执行（不询问确认）
)

# 设置控制台输出编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Docker Compose 启动诊断与修复" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker是否运行
Write-Host "[1/6] 检查Docker服务状态..." -ForegroundColor Yellow
$dockerStatus = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: Docker服务未运行！请先启动Docker Desktop。" -ForegroundColor Red
    exit 1
}
Write-Host "Docker服务正常运行" -ForegroundColor Green

# 检查当前容器状态
Write-Host ""
Write-Host "[2/6] 检查现有容器状态..." -ForegroundColor Yellow
$containers = docker ps -a --filter "name=ioedream" --format "{{.Names}}:{{.Status}}"
if ($containers) {
    Write-Host "发现以下IOE-DREAM容器:" -ForegroundColor Cyan
    foreach ($container in $containers) {
        $parts = $container -split ":"
        $name = $parts[0]
        $status = $parts[1]
        if ($status -match "Up") {
            Write-Host "  ✓ $name - 运行中" -ForegroundColor Green
        } elseif ($status -match "Exited \(0\)") {
            Write-Host "  ○ $name - 已正常退出" -ForegroundColor Gray
        } else {
            Write-Host "  ✗ $name - $status" -ForegroundColor Red
        }
    }
} else {
    Write-Host "未发现IOE-DREAM容器" -ForegroundColor Gray
}

# 获取问题容器日志
Write-Host ""
Write-Host "[3/6] 检查问题容器日志..." -ForegroundColor Yellow

$problemContainers = @("ioedream-nacos", "ioedream-db-init")
foreach ($containerName in $problemContainers) {
    $exists = docker ps -a --filter "name=$containerName" --format "{{.Names}}" 2>$null
    if ($exists) {
        Write-Host ""
        Write-Host "--- $containerName 最近日志 ---" -ForegroundColor Cyan
        docker logs $containerName --tail 30 2>&1 | ForEach-Object {
            if ($_ -match "ERROR|error|Exception|failed|Failed") {
                Write-Host $_ -ForegroundColor Red
            } elseif ($_ -match "WARN|warn|Warning") {
                Write-Host $_ -ForegroundColor Yellow
            } else {
                Write-Host $_ -ForegroundColor Gray
            }
        }
    }
}

if ($DiagnoseOnly) {
    Write-Host ""
    Write-Host "诊断模式完成，未执行任何修复操作。" -ForegroundColor Cyan
    exit 0
}

# 清理启动
if ($CleanStart) {
    Write-Host ""
    Write-Host "[4/6] 执行清理启动..." -ForegroundColor Yellow
    
    if (-not $Force) {
        $confirm = Read-Host "确认要停止并删除所有IOE-DREAM容器和卷吗？(y/N)"
        if ($confirm -ne "y" -and $confirm -ne "Y") {
            Write-Host "已取消操作" -ForegroundColor Yellow
            exit 0
        }
    }
    
    Write-Host "停止所有容器..." -ForegroundColor Gray
    docker-compose -f docker-compose-all.yml down -v 2>$null
    
    Write-Host "删除孤立网络..." -ForegroundColor Gray
    docker network prune -f 2>$null
    
    Write-Host "清理完成" -ForegroundColor Green
}

# 启动服务
Write-Host ""
Write-Host "[5/6] 启动Docker Compose服务..." -ForegroundColor Yellow

# 设置默认环境变量
$env:MYSQL_ROOT_PASSWORD = "root1234"
$env:REDIS_PASSWORD = "redis123"

Write-Host "正在启动服务（此过程可能需要2-5分钟）..." -ForegroundColor Gray
docker-compose -f docker-compose-all.yml up -d 2>&1 | ForEach-Object {
    if ($_ -match "Error|error|failed|Failed") {
        Write-Host $_ -ForegroundColor Red
    } elseif ($_ -match "Started|Created|Healthy") {
        Write-Host $_ -ForegroundColor Green
    } else {
        Write-Host $_ -ForegroundColor Gray
    }
}

# 等待服务健康
Write-Host ""
Write-Host "[6/6] 等待服务健康检查..." -ForegroundColor Yellow

$maxWait = 180  # 最大等待时间（秒）
$checkInterval = 10
$elapsed = 0

while ($elapsed -lt $maxWait) {
    Start-Sleep -Seconds $checkInterval
    $elapsed += $checkInterval
    
    # 检查Nacos状态
    $nacosStatus = docker ps --filter "name=ioedream-nacos" --filter "health=healthy" --format "{{.Names}}" 2>$null
    
    if ($nacosStatus) {
        Write-Host ""
        Write-Host "✓ Nacos服务已健康！" -ForegroundColor Green
        break
    }
    
    # 检查是否有错误退出的容器
    $exitedContainers = docker ps -a --filter "name=ioedream" --filter "status=exited" --format "{{.Names}}:{{.Status}}" 2>$null | Where-Object { $_ -notmatch "Exited \(0\)" -and $_ -notmatch "db-init" }
    
    if ($exitedContainers) {
        Write-Host ""
        Write-Host "检测到异常退出的容器:" -ForegroundColor Red
        foreach ($c in $exitedContainers) {
            Write-Host "  $c" -ForegroundColor Red
        }
    }
    
    Write-Host "等待中... ($elapsed/$maxWait 秒)" -ForegroundColor Gray
}

# 最终状态报告
Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "最终服务状态:" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
docker ps -a --filter "name=ioedream" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

Write-Host ""
Write-Host "如果Nacos仍然不健康，请执行以下命令查看详细日志:" -ForegroundColor Yellow
Write-Host "  docker logs ioedream-nacos" -ForegroundColor White
Write-Host ""
Write-Host "如需完全重新启动，请执行:" -ForegroundColor Yellow
Write-Host "  .\scripts\fix-docker-compose-startup.ps1 -CleanStart" -ForegroundColor White
