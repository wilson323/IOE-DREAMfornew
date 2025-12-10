# Nacos启动问题完整修复脚本
# 用途：系统性诊断和修复Nacos启动失败问题

Write-Host "=== Nacos启动问题完整修复 ===" -ForegroundColor Cyan

$projectRoot = Get-Location
Set-Location $projectRoot

# ==================== 步骤1: 诊断问题 ====================
Write-Host "`n[步骤1] 诊断问题..." -ForegroundColor Yellow

# 1.1 检查MySQL容器
Write-Host "`n1.1 检查MySQL容器..." -ForegroundColor Cyan
$mysqlStatus = docker inspect ioedream-mysql --format='{{.State.Status}}' 2>$null
$mysqlHealth = docker inspect ioedream-mysql --format='{{.State.Health.Status}}' 2>$null

if (-not $mysqlStatus) {
    Write-Host "✗ MySQL容器不存在" -ForegroundColor Red
    Write-Host "请先启动MySQL: docker-compose -f docker-compose-all.yml up -d mysql" -ForegroundColor Yellow
    exit 1
}

Write-Host "MySQL状态: $mysqlStatus" -ForegroundColor $(if ($mysqlStatus -eq "running") { "Green" } else { "Red" })
Write-Host "MySQL健康: $mysqlHealth" -ForegroundColor $(if ($mysqlHealth -eq "healthy") { "Green" } else { "Yellow" })

# 1.2 检查nacos数据库
Write-Host "`n1.2 检查nacos数据库..." -ForegroundColor Cyan
$dbCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES;" 2>&1
$nacosDbExists = $dbCheck | Select-String "nacos"

if ($nacosDbExists) {
    Write-Host "✓ nacos数据库存在" -ForegroundColor Green
    
    # 检查表数量
    $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1
    $tableCount = [regex]::Match($tableCheck, '\d+').Value
    if ($tableCount -and [int]$tableCount -gt 0) {
        Write-Host "✓ nacos数据库已初始化 (表数量: $tableCount)" -ForegroundColor Green
    } else {
        Write-Host "✗ nacos数据库为空，需要初始化" -ForegroundColor Red
        $needInit = $true
    }
} else {
    Write-Host "✗ nacos数据库不存在" -ForegroundColor Red
    $needInit = $true
}

# 1.3 检查Nacos容器
Write-Host "`n1.3 检查Nacos容器..." -ForegroundColor Cyan
$nacosStatus = docker inspect ioedream-nacos --format='{{.State.Status}}' 2>$null
$nacosHealth = docker inspect ioedream-nacos --format='{{.State.Health.Status}}' 2>$null

if ($nacosStatus) {
    Write-Host "Nacos状态: $nacosStatus" -ForegroundColor $(if ($nacosStatus -eq "running") { "Green" } else { "Red" })
    if ($nacosHealth) {
        Write-Host "Nacos健康: $nacosHealth" -ForegroundColor $(if ($nacosHealth -eq "healthy") { "Green" } else { "Red" })
    }
}

# 1.4 查看Nacos错误日志
Write-Host "`n1.4 查看Nacos错误日志..." -ForegroundColor Cyan
$nacosLogs = docker logs ioedream-nacos --tail 20 2>&1 | Select-String -Pattern "ERROR|Exception|error" -Context 0,2
if ($nacosLogs) {
    Write-Host "发现错误:" -ForegroundColor Red
    $nacosLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
}

# ==================== 步骤2: 修复问题 ====================
Write-Host "`n[步骤2] 修复问题..." -ForegroundColor Yellow

if ($needInit) {
    Write-Host "`n2.1 初始化nacos数据库..." -ForegroundColor Cyan
    
    $nacosSchema = "deployment\mysql\init\nacos-schema.sql"
    if (Test-Path $nacosSchema) {
        Write-Host "执行SQL脚本: $nacosSchema" -ForegroundColor Gray
        
        try {
            Get-Content $nacosSchema -Raw | docker exec -i ioedream-mysql mysql -uroot -proot
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✓ nacos数据库初始化成功" -ForegroundColor Green
                
                # 验证
                $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1
                $tableCount = [regex]::Match($tableCheck, '\d+').Value
                Write-Host "  表数量: $tableCount" -ForegroundColor Gray
            } else {
                Write-Host "✗ 初始化失败，错误代码: $LASTEXITCODE" -ForegroundColor Red
                exit 1
            }
        }
        catch {
            Write-Host "✗ 初始化异常: $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "✗ 找不到SQL脚本: $nacosSchema" -ForegroundColor Red
        exit 1
    }
}

# 2.2 重启Nacos容器
Write-Host "`n2.2 重启Nacos容器..." -ForegroundColor Cyan
Write-Host "停止Nacos容器..." -ForegroundColor Gray
docker stop ioedream-nacos 2>&1 | Out-Null
Start-Sleep -Seconds 2

Write-Host "删除Nacos容器..." -ForegroundColor Gray
docker rm ioedream-nacos 2>&1 | Out-Null

Write-Host "重新创建Nacos容器..." -ForegroundColor Gray
docker-compose -f docker-compose-all.yml up -d nacos

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Nacos容器已重新创建" -ForegroundColor Green
} else {
    Write-Host "✗ 创建Nacos容器失败" -ForegroundColor Red
    exit 1
}

# ==================== 步骤3: 验证修复 ====================
Write-Host "`n[步骤3] 验证修复..." -ForegroundColor Yellow

Write-Host "`n3.1 等待Nacos启动..." -ForegroundColor Cyan
$maxWait = 120
$waited = 0
$nacosReady = $false

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 5
    $waited += 5
    
    $nacosHealth = docker inspect ioedream-nacos --format='{{.State.Health.Status}}' 2>&1
    if ($nacosHealth -eq "healthy") {
        Write-Host "✓ Nacos健康检查通过" -ForegroundColor Green
        $nacosReady = $true
        break
    }
    
    # 检查是否启动失败
    $nacosStatus = docker inspect ioedream-nacos --format='{{.State.Status}}' 2>&1
    if ($nacosStatus -eq "exited") {
        Write-Host "✗ Nacos容器已退出" -ForegroundColor Red
        Write-Host "查看日志: docker logs ioedream-nacos" -ForegroundColor Yellow
        break
    }
    
    Write-Host "  等待中... ($waited/$maxWait秒)" -ForegroundColor Gray
}

if (-not $nacosReady) {
    Write-Host "`n3.2 检查Nacos日志..." -ForegroundColor Cyan
    $errorLogs = docker logs ioedream-nacos --tail 30 2>&1 | Select-String -Pattern "ERROR|Exception|error" -Context 1,1
    if ($errorLogs) {
        Write-Host "发现错误:" -ForegroundColor Red
        $errorLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
    }
    
    Write-Host "`n⚠ Nacos可能仍未就绪，请检查日志" -ForegroundColor Yellow
    Write-Host "查看完整日志: docker logs ioedream-nacos" -ForegroundColor White
} else {
    Write-Host "`n3.3 测试Nacos连接..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8848/nacos/v1/console/health/readiness" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✓ Nacos API可访问" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "⚠ Nacos API暂时不可访问（可能仍在启动中）" -ForegroundColor Yellow
    }
}

# ==================== 步骤4: 总结 ====================
Write-Host "`n=== 修复完成 ===" -ForegroundColor Cyan

if ($nacosReady) {
    Write-Host "`n✅ Nacos已成功启动！" -ForegroundColor Green
    Write-Host "访问地址: http://localhost:8848/nacos" -ForegroundColor Cyan
    Write-Host "默认账号: nacos / nacos" -ForegroundColor Cyan
} else {
    Write-Host "`n⚠ Nacos可能仍有问题，请检查:" -ForegroundColor Yellow
    Write-Host "1. 查看日志: docker logs ioedream-nacos" -ForegroundColor White
    Write-Host "2. 检查数据库: docker exec ioedream-mysql mysql -uroot -proot -e 'USE nacos; SHOW TABLES;'" -ForegroundColor White
    Write-Host "3. 检查网络: docker network inspect ioedream-network" -ForegroundColor White
}

Write-Host "`n下一步操作:" -ForegroundColor Yellow
Write-Host "如果Nacos已启动，可以继续启动其他服务:" -ForegroundColor White
Write-Host "docker-compose -f docker-compose-all.yml up -d" -ForegroundColor Cyan
