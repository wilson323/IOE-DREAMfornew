# ==========================================
# IOE-DREAM 数据库初始化验证脚本
# 版本: v1.0.0
# 日期: 2025-01-31
# ==========================================

param(
    [switch]$Reinitialize,
    [switch]$ShowDetails
)

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 数据库初始化验证" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查Docker是否运行
Write-Host "[1/5] 检查Docker状态..." -ForegroundColor Yellow
$dockerRunning = docker info 2>$null
if (-not $dockerRunning) {
    Write-Host "  错误: Docker未运行!" -ForegroundColor Red
    exit 1
}
Write-Host "  Docker运行正常 ✓" -ForegroundColor Green

# 检查MySQL容器状态
Write-Host ""
Write-Host "[2/5] 检查MySQL容器状态..." -ForegroundColor Yellow
$mysqlStatus = docker inspect --format='{{.State.Status}}' ioedream-mysql 2>$null
if ($mysqlStatus -ne "running") {
    Write-Host "  错误: MySQL容器未运行 (状态: $mysqlStatus)" -ForegroundColor Red
    Write-Host "  请先启动MySQL: docker-compose -f docker-compose-all.yml up -d mysql" -ForegroundColor Yellow
    exit 1
}
Write-Host "  MySQL容器运行中 ✓" -ForegroundColor Green

# 检查MySQL健康状态
$mysqlHealth = docker inspect --format='{{.State.Health.Status}}' ioedream-mysql 2>$null
if ($mysqlHealth -ne "healthy") {
    Write-Host "  警告: MySQL健康状态: $mysqlHealth" -ForegroundColor Yellow
}

# 检查SQL文件
Write-Host ""
Write-Host "[3/5] 检查SQL初始化文件..." -ForegroundColor Yellow
$initDir = "D:\IOE-DREAM\deployment\mysql\init"
$sqlFiles = Get-ChildItem -Path $initDir -Filter "*.sql" -ErrorAction SilentlyContinue

if ($sqlFiles.Count -eq 0) {
    Write-Host "  错误: 未找到SQL初始化文件!" -ForegroundColor Red
    exit 1
}

foreach ($file in $sqlFiles) {
    Write-Host "  - $($file.Name) ($([math]::Round($file.Length/1024, 1)) KB)" -ForegroundColor White
}
Write-Host "  共 $($sqlFiles.Count) 个SQL文件 ✓" -ForegroundColor Green

# 检查数据库状态
Write-Host ""
Write-Host "[4/5] 检查数据库状态..." -ForegroundColor Yellow

# 检查ioedream数据库
$ioedreamExists = docker exec ioedream-mysql mysql -uroot -p"root1234" -N -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='ioedream';" 2>$null
$ioedreamTables = docker exec ioedream-mysql mysql -uroot -p"root1234" -N -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='ioedream';" 2>$null

Write-Host "  ioedream数据库:" -ForegroundColor White
if ($ioedreamExists -gt 0) {
    Write-Host "    存在: 是 ✓" -ForegroundColor Green
    Write-Host "    表数量: $ioedreamTables" -ForegroundColor White
} else {
    Write-Host "    存在: 否 ✗" -ForegroundColor Red
}

# 检查nacos数据库
$nacosExists = docker exec ioedream-mysql mysql -uroot -p"root1234" -N -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='nacos';" 2>$null
$nacosTables = docker exec ioedream-mysql mysql -uroot -p"root1234" -N -e "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>$null

Write-Host "  nacos数据库:" -ForegroundColor White
if ($nacosExists -gt 0) {
    Write-Host "    存在: 是 ✓" -ForegroundColor Green
    Write-Host "    表数量: $nacosTables" -ForegroundColor White
    
    if ($nacosTables -lt 10) {
        Write-Host "    警告: 表数量不足,可能初始化不完整!" -ForegroundColor Yellow
    }
} else {
    Write-Host "    存在: 否 ✗" -ForegroundColor Red
}

# 显示详细信息
if ($ShowDetails) {
    Write-Host ""
    Write-Host "[详细信息] Nacos表列表:" -ForegroundColor Yellow
    docker exec ioedream-mysql mysql -uroot -p"root1234" -e "SHOW TABLES FROM nacos;" 2>$null
}

# 重新初始化选项
if ($Reinitialize) {
    Write-Host ""
    Write-Host "[5/5] 重新初始化数据库..." -ForegroundColor Yellow
    
    # 停止依赖服务
    Write-Host "  停止Nacos服务..." -ForegroundColor White
    docker stop ioedream-nacos 2>$null
    docker rm ioedream-nacos 2>$null
    
    # 删除db-init容器
    Write-Host "  清理db-init容器..." -ForegroundColor White
    docker rm ioedream-db-init 2>$null
    
    # 重新运行db-init
    Write-Host "  执行数据库初始化..." -ForegroundColor White
    docker-compose -f D:\IOE-DREAM\docker-compose-all.yml up db-init
    
    Write-Host "  重新启动Nacos..." -ForegroundColor White
    docker-compose -f D:\IOE-DREAM\docker-compose-all.yml up -d nacos
    
    Write-Host "  等待Nacos启动 (30秒)..." -ForegroundColor White
    Start-Sleep -Seconds 30
    
    # 检查Nacos状态
    $nacosStatus = docker inspect --format='{{.State.Status}}' ioedream-nacos 2>$null
    $nacosHealth = docker inspect --format='{{.State.Health.Status}}' ioedream-nacos 2>$null
    
    Write-Host "  Nacos状态: $nacosStatus (健康: $nacosHealth)" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "[5/5] 跳过重新初始化 (使用 -Reinitialize 参数启用)" -ForegroundColor Gray
}

# 总结
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  验证完成" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$allOk = ($ioedreamExists -gt 0) -and ($nacosExists -gt 0) -and ($nacosTables -ge 10)

if ($allOk) {
    Write-Host "状态: 数据库初始化正常 ✓" -ForegroundColor Green
} else {
    Write-Host "状态: 数据库需要初始化" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "建议操作:" -ForegroundColor White
    Write-Host "  1. 运行: docker-compose -f docker-compose-all.yml up db-init" -ForegroundColor Gray
    Write-Host "  2. 或使用: .\scripts\verify-database-init.ps1 -Reinitialize" -ForegroundColor Gray
}

Write-Host ""
