# IOE-DREAM Docker状态检查脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Docker服务状态检查" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker是否运行
Write-Host "[1] 检查Docker服务..." -ForegroundColor Yellow
try {
    $dockerInfo = docker info 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ Docker正在运行" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Docker未运行或无法访问" -ForegroundColor Red
        Write-Host "  错误信息: $dockerInfo" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ Docker检查失败: $_" -ForegroundColor Red
    exit 1
}

# 检查所有容器
Write-Host "`n[2] 检查所有容器..." -ForegroundColor Yellow
$allContainers = docker ps -a 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  容器总数: $($allContainers.Count - 1)" -ForegroundColor Cyan
} else {
    Write-Host "  ❌ 无法获取容器列表" -ForegroundColor Red
    exit 1
}

# 检查IOE-DREAM相关容器
Write-Host "`n[3] 检查IOE-DREAM容器..." -ForegroundColor Yellow
$ioedreamContainers = docker ps -a --filter "name=ioedream" 2>&1
if ($ioedreamContainers -and $ioedreamContainers.Count -gt 1) {
    Write-Host "  发现的IOE-DREAM容器:" -ForegroundColor Cyan
    $ioedreamContainers | Select-Object -Skip 1 | ForEach-Object {
        Write-Host "    $_" -ForegroundColor Gray
    }
    
    # 检查运行中的容器
    $runningContainers = docker ps --filter "name=ioedream" --format "{{.Names}}" 2>&1
    if ($runningContainers) {
        Write-Host "`n  运行中的容器 ($($runningContainers.Count)):" -ForegroundColor Green
        $runningContainers | ForEach-Object {
            Write-Host "    ✅ $_" -ForegroundColor Green
        }
    } else {
        Write-Host "`n  ⚠️  没有运行中的IOE-DREAM容器" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ⚠️  未发现IOE-DREAM容器" -ForegroundColor Yellow
    Write-Host "  提示: 可能需要先启动Docker Compose服务" -ForegroundColor Gray
}

# 检查Docker Compose服务状态
Write-Host "`n[4] 检查Docker Compose服务状态..." -ForegroundColor Yellow
$composeFile = "D:\IOE-DREAM\docker-compose-all.yml"
if (Test-Path $composeFile) {
    Write-Host "  ✅ docker-compose-all.yml文件存在" -ForegroundColor Green
    
    # 尝试获取服务状态
    Push-Location "D:\IOE-DREAM"
    try {
        $composeStatus = docker compose -f docker-compose-all.yml ps 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "`n  Docker Compose服务状态:" -ForegroundColor Cyan
            Write-Host $composeStatus
        } else {
            Write-Host "  ⚠️  无法获取Docker Compose状态" -ForegroundColor Yellow
            Write-Host "  输出: $composeStatus" -ForegroundColor Gray
        }
    } catch {
        Write-Host "  ❌ 检查Docker Compose状态失败: $_" -ForegroundColor Red
    } finally {
        Pop-Location
    }
} else {
    Write-Host "  ❌ docker-compose-all.yml文件不存在" -ForegroundColor Red
}

# 检查关键端口
Write-Host "`n[5] 检查关键端口..." -ForegroundColor Yellow
$ports = @(
    @{ Name = "MySQL"; Port = 3306 }
    @{ Name = "Redis"; Port = 6379 }
    @{ Name = "Nacos"; Port = 8848 }
    @{ Name = "Gateway"; Port = 8080 }
)

foreach ($portInfo in $ports) {
    try {
        $result = Test-NetConnection -ComputerName localhost -Port $portInfo.Port -InformationLevel Quiet -WarningAction SilentlyContinue -ErrorAction SilentlyContinue
        if ($result) {
            Write-Host "  ✅ $($portInfo.Name) (端口 $($portInfo.Port)) 已开放" -ForegroundColor Green
        } else {
            Write-Host "  ❌ $($portInfo.Name) (端口 $($portInfo.Port)) 未开放" -ForegroundColor Red
        }
    } catch {
        Write-Host "  ❌ 检查 $($portInfo.Name) 端口失败" -ForegroundColor Red
    }
}

# 提供建议
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  建议操作" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$runningCount = (docker ps --filter "name=ioedream" --format "{{.Names}}" 2>&1 | Measure-Object -Line).Lines
if ($runningCount -eq 0) {
    Write-Host "`n⚠️  没有运行中的服务，建议执行:" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-all.yml up -d" -ForegroundColor Gray
} else {
    Write-Host "`n✅ 发现 $runningCount 个运行中的服务" -ForegroundColor Green
    Write-Host "`n查看日志命令:" -ForegroundColor Cyan
    Write-Host "  docker-compose -f docker-compose-all.yml logs -f [服务名]" -ForegroundColor Gray
    Write-Host "`n查看所有服务日志:" -ForegroundColor Cyan
    Write-Host "  docker-compose -f docker-compose-all.yml logs -f" -ForegroundColor Gray
}

Write-Host ""
