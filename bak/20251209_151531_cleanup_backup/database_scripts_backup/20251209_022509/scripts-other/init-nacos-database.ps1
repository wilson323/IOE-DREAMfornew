# Nacos数据库初始化脚本
# 用途：确保nacos数据库正确初始化

Write-Host "=== Nacos数据库初始化 ===" -ForegroundColor Cyan

$projectRoot = Get-Location
Set-Location $projectRoot

# 1. 检查MySQL容器状态
Write-Host "`n1. 检查MySQL容器状态..." -ForegroundColor Yellow
$mysqlStatus = docker inspect ioedream-mysql --format='{{.State.Status}}' 2>$null
$mysqlHealth = docker inspect ioedream-mysql --format='{{.State.Health.Status}}' 2>$null

if (-not $mysqlStatus -or $mysqlStatus -ne "running") {
    Write-Host "✗ MySQL容器未运行" -ForegroundColor Red
    Write-Host "请先启动MySQL容器: docker-compose -f docker-compose-all.yml up -d mysql" -ForegroundColor Yellow
    exit 1
}

if ($mysqlHealth -ne "healthy") {
    Write-Host "⚠ MySQL容器未就绪，等待健康检查..." -ForegroundColor Yellow
    $maxWait = 60
    $waited = 0
    while ($waited -lt $maxWait) {
        Start-Sleep -Seconds 5
        $waited += 5
        $mysqlHealth = docker inspect ioedream-mysql --format='{{.State.Health.Status}}' 2>$null
        if ($mysqlHealth -eq "healthy") {
            Write-Host "✓ MySQL已就绪" -ForegroundColor Green
            break
        }
        Write-Host "  等待中... ($waited/$maxWait秒)" -ForegroundColor Gray
    }
    
    if ($mysqlHealth -ne "healthy") {
        Write-Host "✗ MySQL健康检查超时" -ForegroundColor Red
        exit 1
    }
}

Write-Host "✓ MySQL容器运行正常" -ForegroundColor Green

# 2. 检查nacos数据库是否存在
Write-Host "`n2. 检查nacos数据库..." -ForegroundColor Yellow
$nacosDbCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES LIKE 'nacos';" 2>&1
$nacosDbExists = $nacosDbCheck | Select-String "nacos"

if ($nacosDbExists) {
    Write-Host "✓ nacos数据库已存在" -ForegroundColor Green
    
    # 检查表数量
    $tableCount = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1 | Select-String -Pattern "\d+"
    if ($tableCount) {
        Write-Host "  表数量: $tableCount" -ForegroundColor Gray
        if ([int]$tableCount -gt 0) {
            Write-Host "✓ nacos数据库已初始化" -ForegroundColor Green
            exit 0
        }
    }
} else {
    Write-Host "✗ nacos数据库不存在" -ForegroundColor Red
}

# 3. 初始化nacos数据库
Write-Host "`n3. 初始化nacos数据库..." -ForegroundColor Yellow

$nacosSchema = "deployment\mysql\init\nacos-schema.sql"
if (-not (Test-Path $nacosSchema)) {
    Write-Host "✗ 找不到nacos初始化脚本: $nacosSchema" -ForegroundColor Red
    exit 1
}

Write-Host "执行SQL脚本: $nacosSchema" -ForegroundColor Gray

try {
    # 使用Get-Content和管道执行SQL
    Get-Content $nacosSchema -Raw | docker exec -i ioedream-mysql mysql -uroot -proot
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ nacos数据库初始化成功" -ForegroundColor Green
        
        # 验证初始化结果
        $tableCount = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1 | Select-String -Pattern "\d+"
        if ($tableCount) {
            Write-Host "  创建表数量: $tableCount" -ForegroundColor Gray
        }
    } else {
        Write-Host "✗ nacos数据库初始化失败" -ForegroundColor Red
        Write-Host "错误代码: $LASTEXITCODE" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "✗ 执行SQL脚本时出错: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "`n=== 初始化完成 ===" -ForegroundColor Cyan
Write-Host "现在可以重新启动Nacos容器:" -ForegroundColor Yellow
Write-Host "docker-compose -f docker-compose-all.yml restart nacos" -ForegroundColor White
