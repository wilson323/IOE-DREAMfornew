# Nacos启动问题诊断和修复脚本
# 用途：诊断并修复Nacos容器启动失败问题

Write-Host "=== Nacos启动问题诊断和修复 ===" -ForegroundColor Cyan

$projectRoot = Get-Location
Set-Location $projectRoot

# 1. 检查Nacos容器状态
Write-Host "`n1. 检查Nacos容器状态..." -ForegroundColor Yellow
$nacosStatus = docker inspect ioedream-nacos --format='{{.State.Status}}' 2>$null
$nacosHealth = docker inspect ioedream-nacos --format='{{.State.Health.Status}}' 2>$null

if ($nacosStatus) {
    Write-Host "容器状态: $nacosStatus" -ForegroundColor Cyan
    if ($nacosHealth) {
        Write-Host "健康状态: $nacosHealth" -ForegroundColor $(if ($nacosHealth -eq "healthy") { "Green" } else { "Red" })
    }
} else {
    Write-Host "Nacos容器不存在" -ForegroundColor Yellow
}

# 2. 检查MySQL容器状态
Write-Host "`n2. 检查MySQL容器状态..." -ForegroundColor Yellow
$mysqlStatus = docker inspect ioedream-mysql --format='{{.State.Status}}' 2>$null
$mysqlHealth = docker inspect ioedream-mysql --format='{{.State.Health.Status}}' 2>$null

if ($mysqlStatus) {
    Write-Host "MySQL容器状态: $mysqlStatus" -ForegroundColor Cyan
    if ($mysqlHealth) {
        Write-Host "MySQL健康状态: $mysqlHealth" -ForegroundColor $(if ($mysqlHealth -eq "healthy") { "Green" } else { "Red" })
    }
} else {
    Write-Host "MySQL容器不存在" -ForegroundColor Red
    Write-Host "请先启动MySQL容器" -ForegroundColor Yellow
    exit 1
}

# 3. 检查nacos数据库是否存在
Write-Host "`n3. 检查nacos数据库..." -ForegroundColor Yellow
if ($mysqlHealth -eq "healthy") {
    $nacosDbExists = docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES LIKE 'nacos';" 2>&1 | Select-String "nacos"
    
    if ($nacosDbExists) {
        Write-Host "✓ nacos数据库已存在" -ForegroundColor Green
    } else {
        Write-Host "✗ nacos数据库不存在，需要初始化" -ForegroundColor Red
        
        # 检查初始化脚本是否存在
        $nacosSchema = "deployment\mysql\init\nacos-schema.sql"
        if (Test-Path $nacosSchema) {
            Write-Host "`n初始化nacos数据库..." -ForegroundColor Yellow
            Get-Content $nacosSchema | docker exec -i ioedream-mysql mysql -uroot -proot
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✓ nacos数据库初始化成功" -ForegroundColor Green
            } else {
                Write-Host "✗ nacos数据库初始化失败" -ForegroundColor Red
                Write-Host "错误代码: $LASTEXITCODE" -ForegroundColor Red
            }
        } else {
            Write-Host "✗ 找不到nacos初始化脚本: $nacosSchema" -ForegroundColor Red
        }
    }
} else {
    Write-Host "MySQL未就绪，等待MySQL启动..." -ForegroundColor Yellow
    Write-Host "请等待MySQL健康检查通过后再运行此脚本" -ForegroundColor Yellow
}

# 4. 查看Nacos日志
Write-Host "`n4. 查看Nacos最新日志..." -ForegroundColor Yellow
$nacosLogs = docker logs ioedream-nacos --tail 30 2>&1
if ($nacosLogs) {
    Write-Host $nacosLogs -ForegroundColor Gray
} else {
    Write-Host "无法获取Nacos日志" -ForegroundColor Yellow
}

# 5. 提供修复建议
Write-Host "`n=== 修复建议 ===" -ForegroundColor Cyan

if ($nacosHealth -ne "healthy") {
    Write-Host "`n方案1: 重新启动Nacos容器" -ForegroundColor Yellow
    Write-Host "docker-compose -f docker-compose-all.yml restart nacos" -ForegroundColor White
    
    Write-Host "`n方案2: 删除并重新创建Nacos容器" -ForegroundColor Yellow
    Write-Host "docker-compose -f docker-compose-all.yml stop nacos" -ForegroundColor White
    Write-Host "docker-compose -f docker-compose-all.yml rm -f nacos" -ForegroundColor White
    Write-Host "docker-compose -f docker-compose-all.yml up -d nacos" -ForegroundColor White
    
    Write-Host "`n方案3: 检查Nacos配置" -ForegroundColor Yellow
    Write-Host "- 确认MySQL连接配置正确" -ForegroundColor White
    Write-Host "- 确认nacos数据库已初始化" -ForegroundColor White
    Write-Host "- 检查Nacos日志中的错误信息" -ForegroundColor White
    
    Write-Host "`n方案4: 手动初始化nacos数据库" -ForegroundColor Yellow
    Write-Host "Get-Content deployment\mysql\init\nacos-schema.sql | docker exec -i ioedream-mysql mysql -uroot -proot" -ForegroundColor White
}

# 6. 检查常见问题
Write-Host "`n=== 常见问题检查 ===" -ForegroundColor Cyan

# 检查端口占用
Write-Host "`n检查端口占用..." -ForegroundColor Yellow
$port8848 = Get-NetTCPConnection -LocalPort 8848 -ErrorAction SilentlyContinue
$port9848 = Get-NetTCPConnection -LocalPort 9848 -ErrorAction SilentlyContinue

if ($port8848) {
    Write-Host "⚠ 端口8848已被占用" -ForegroundColor Yellow
    Write-Host "  占用进程: $($port8848.OwningProcess)" -ForegroundColor Gray
}

if ($port9848) {
    Write-Host "⚠ 端口9848已被占用" -ForegroundColor Yellow
    Write-Host "  占用进程: $($port9848.OwningProcess)" -ForegroundColor Gray
}

# 检查网络连接
Write-Host "`n检查网络连接..." -ForegroundColor Yellow
$networkExists = docker network ls | Select-String "ioedream-network"
if ($networkExists) {
    Write-Host "✓ Docker网络已创建" -ForegroundColor Green
} else {
    Write-Host "✗ Docker网络不存在" -ForegroundColor Red
    Write-Host "  运行: docker network create ioedream-network" -ForegroundColor White
}

Write-Host "`n=== 诊断完成 ===" -ForegroundColor Cyan
