# IOE-DREAM集成测试执行脚本
# 需要MySQL和Redis环境支持

param(
    [string]$Service = "",
    [switch]$Docker = $false
)

$ErrorActionPreference = "Stop"

Write-Host "===== IOE-DREAM集成测试执行 =====" -ForegroundColor Cyan

# 检查环境
Write-Host "`n检查测试环境..." -ForegroundColor Yellow

# 检查MySQL
try {
    $mysqlTest = & mysql -u root -proot -e "SELECT 1" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✅ MySQL连接正常" -ForegroundColor Green
    } else {
        Write-Host "  ❌ MySQL连接失败" -ForegroundColor Red
        Write-Host "  请启动MySQL服务或使用 -Docker 参数" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  MySQL未安装或未启动" -ForegroundColor Yellow
    if (-not $Docker) {
        Write-Host "  建议使用 -Docker 参数启动测试环境" -ForegroundColor Yellow
    }
}

# 检查Redis
try {
    $redisTest = Test-NetConnection -ComputerName localhost -Port 6379 -WarningAction SilentlyContinue
    if ($redisTest.TcpTestSucceeded) {
        Write-Host "  ✅ Redis连接正常" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Redis连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "  ⚠️  Redis未启动" -ForegroundColor Yellow
}

# 如果使用Docker，启动测试环境
if ($Docker) {
    Write-Host "`n启动Docker测试环境..." -ForegroundColor Yellow

    # 启动MySQL和Redis容器
    docker-compose -f docker/docker-compose-test.yml up -d

    Write-Host "等待服务启动（30秒）..." -ForegroundColor Gray
    Start-Sleep -Seconds 30
}

# 执行集成测试
Write-Host "`n执行集成测试..." -ForegroundColor Yellow
$mvnCmd = "mvn verify -DskipUnitTests -P integration-test"

if ($Service -ne "") {
    $mvnCmd += " -pl microservices/$Service -am"
    Write-Host "仅测试服务: $Service" -ForegroundColor Cyan
}

Write-Host "执行命令: $mvnCmd`n" -ForegroundColor Gray
Write-Host "=" * 60 -ForegroundColor Gray

$startTime = Get-Date
Invoke-Expression $mvnCmd
$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host "`n" + "=" * 60 -ForegroundColor Gray
Write-Host "集成测试完成" -ForegroundColor Green
Write-Host "耗时: $($duration.TotalSeconds) 秒" -ForegroundColor Cyan

# 清理Docker环境
if ($Docker) {
    Write-Host "`n清理Docker测试环境..." -ForegroundColor Yellow
    docker-compose -f docker/docker-compose-test.yml down
}

