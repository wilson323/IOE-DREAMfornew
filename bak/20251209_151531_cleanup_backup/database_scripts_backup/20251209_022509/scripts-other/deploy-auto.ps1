# 自动化部署脚本 - 自动检测并初始化数据库
# 用途：智能部署，自动处理所有初始化步骤

param(
    [switch]$Force = $false,
    [switch]$SkipInit = $false
)

Write-Host "=== IOE-DREAM 自动化部署 ===" -ForegroundColor Cyan

$projectRoot = Get-Location
Set-Location $projectRoot

# ==================== 步骤1: 环境检查 ====================
Write-Host "`n[步骤1] 环境检查..." -ForegroundColor Yellow

# 检查Docker
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "✗ Docker未安装或未在PATH中" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Docker已安装" -ForegroundColor Green

# 检查docker-compose文件
if (-not (Test-Path "docker-compose-all.yml")) {
    Write-Host "✗ 找不到docker-compose-all.yml" -ForegroundColor Red
    exit 1
}
Write-Host "✓ docker-compose-all.yml存在" -ForegroundColor Green

# ==================== 步骤2: 启动基础设施 ====================
Write-Host "`n[步骤2] 启动基础设施..." -ForegroundColor Yellow

Write-Host "启动MySQL和Redis..." -ForegroundColor Cyan
docker-compose -f docker-compose-all.yml up -d mysql redis

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 启动基础设施失败" -ForegroundColor Red
    exit 1
}

# 等待MySQL就绪
Write-Host "等待MySQL就绪..." -ForegroundColor Cyan
$maxWait = 120
$waited = 0
$mysqlReady = $false

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 5
    $waited += 5
    
    $mysqlHealth = docker inspect ioedream-mysql --format='{{.State.Health.Status}}' 2>&1
    if ($mysqlHealth -eq "healthy") {
        Write-Host "✓ MySQL已就绪 ($waited秒)" -ForegroundColor Green
        $mysqlReady = $true
        break
    }
    
    Write-Host "  等待中... ($waited/$maxWait秒)" -ForegroundColor Gray
}

if (-not $mysqlReady) {
    Write-Host "✗ MySQL启动超时" -ForegroundColor Red
    Write-Host "查看日志: docker logs ioedream-mysql" -ForegroundColor Yellow
    exit 1
}

# ==================== 步骤3: 自动检测并初始化数据库 ====================
Write-Host "`n[步骤3] 自动检测并初始化数据库..." -ForegroundColor Yellow

if ($SkipInit) {
    Write-Host "跳过数据库初始化（用户指定）" -ForegroundColor Yellow
} else {
    # 3.1 检测nacos数据库
    Write-Host "`n3.1 检测nacos数据库..." -ForegroundColor Cyan
    $dbCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES LIKE 'nacos';" 2>&1
    $nacosDbExists = $dbCheck | Select-String "nacos"
    
    $needInitNacos = $false
    
    if ($nacosDbExists) {
        Write-Host "✓ nacos数据库已存在" -ForegroundColor Green
        
        # 检查表数量
        $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1
        $tableCount = [regex]::Match($tableCheck, '\d+').Value
        
        if ($tableCount -and [int]$tableCount -gt 0) {
            Write-Host "✓ nacos数据库已初始化 (表数量: $tableCount)" -ForegroundColor Green
        } else {
            Write-Host "✗ nacos数据库为空，需要初始化表结构" -ForegroundColor Red
            $needInitNacos = $true
        }
    } else {
        Write-Host "✗ nacos数据库不存在，需要创建并初始化" -ForegroundColor Red
        $needInitNacos = $true
    }
    
    # 3.2 初始化nacos数据库（如果需要）
    if ($needInitNacos -or $Force) {
        Write-Host "`n3.2 初始化nacos数据库..." -ForegroundColor Cyan
        
        $nacosSchema = "deployment\mysql\init\nacos-schema.sql"
        if (-not (Test-Path $nacosSchema)) {
            Write-Host "✗ 找不到nacos初始化脚本: $nacosSchema" -ForegroundColor Red
            exit 1
        }
        
        try {
            # 创建数据库（如果不存在）
            Write-Host "创建nacos数据库..." -ForegroundColor Gray
            docker exec ioedream-mysql mysql -uroot -proot -e "CREATE DATABASE IF NOT EXISTS nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>&1 | Out-Null
            
            # 初始化表结构
            Write-Host "执行SQL初始化脚本..." -ForegroundColor Gray
            Get-Content $nacosSchema -Raw | docker exec -i ioedream-mysql mysql -uroot -proot nacos 2>&1 | Out-Null
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✓ nacos数据库初始化成功" -ForegroundColor Green
                
                # 验证
                $tableCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SELECT COUNT(*) as cnt FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" 2>&1
                $tableCount = [regex]::Match($tableCheck, '\d+').Value
                Write-Host "  表数量: $tableCount" -ForegroundColor Gray
            } else {
                Write-Host "✗ 初始化失败，错误代码: $LASTEXITCODE" -ForegroundColor Red
                Write-Host "查看MySQL日志: docker logs ioedream-mysql" -ForegroundColor Yellow
                exit 1
            }
        }
        catch {
            Write-Host "✗ 初始化异常: $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    }
    
    # 3.3 检测ioedream数据库（如果需要）
    Write-Host "`n3.3 检测ioedream数据库..." -ForegroundColor Cyan
    $ioedreamDbCheck = docker exec ioedream-mysql mysql -uroot -proot -e "SHOW DATABASES LIKE 'ioedream';" 2>&1
    $ioedreamDbExists = $ioedreamDbCheck | Select-String "ioedream"
    
    if (-not $ioedreamDbExists) {
        Write-Host "⚠ ioedream数据库不存在（如果项目需要，请手动初始化）" -ForegroundColor Yellow
    } else {
        Write-Host "✓ ioedream数据库已存在" -ForegroundColor Green
    }
}

# ==================== 步骤4: 启动Nacos ====================
Write-Host "`n[步骤4] 启动Nacos..." -ForegroundColor Yellow

# 停止并删除旧容器（如果存在）
$nacosExists = docker ps -a --filter "name=ioedream-nacos" --format "{{.Names}}" 2>&1
if ($nacosExists -eq "ioedream-nacos") {
    Write-Host "停止并删除旧Nacos容器..." -ForegroundColor Gray
    docker stop ioedream-nacos 2>&1 | Out-Null
    docker rm ioedream-nacos 2>&1 | Out-Null
}

Write-Host "启动Nacos容器..." -ForegroundColor Cyan
docker-compose -f docker-compose-all.yml up -d nacos

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 启动Nacos失败" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Nacos容器已启动" -ForegroundColor Green

# 等待Nacos就绪
Write-Host "等待Nacos就绪（这可能需要60-120秒）..." -ForegroundColor Cyan
$maxWait = 180
$waited = 0
$nacosReady = $false

while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 10
    $waited += 10
    
    $nacosHealth = docker inspect ioedream-nacos --format='{{.State.Health.Status}}' 2>&1
    if ($nacosHealth -eq "healthy") {
        Write-Host "✓ Nacos已就绪 ($waited秒)" -ForegroundColor Green
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
    Write-Host "`n⚠ Nacos可能仍未就绪，检查日志..." -ForegroundColor Yellow
    $errorLogs = docker logs ioedream-nacos --tail 20 2>&1 | Select-String -Pattern "ERROR|Exception|error" -Context 0,1
    if ($errorLogs) {
        Write-Host "发现错误:" -ForegroundColor Red
        $errorLogs | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
    }
    Write-Host "`n可以稍后手动检查: docker logs ioedream-nacos" -ForegroundColor Yellow
}

# ==================== 步骤5: 启动所有微服务 ====================
Write-Host "`n[步骤5] 启动所有微服务..." -ForegroundColor Yellow

Write-Host "启动所有服务..." -ForegroundColor Cyan
docker-compose -f docker-compose-all.yml up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ 启动服务失败" -ForegroundColor Red
    exit 1
}

Write-Host "✓ 所有服务已启动" -ForegroundColor Green

# ==================== 步骤6: 验证部署 ====================
Write-Host "`n[步骤6] 验证部署..." -ForegroundColor Yellow

Start-Sleep -Seconds 10

Write-Host "`n服务状态:" -ForegroundColor Cyan
docker-compose -f docker-compose-all.yml ps

Write-Host "`n关键服务健康状态:" -ForegroundColor Cyan
$services = @("mysql", "redis", "nacos")
foreach ($service in $services) {
    $containerName = "ioedream-$service"
    $health = docker inspect $containerName --format='{{.State.Health.Status}}' 2>&1
    $status = docker inspect $containerName --format='{{.State.Status}}' 2>&1
    
    if ($health -eq "healthy" -or ($status -eq "running" -and -not $health)) {
        Write-Host "  ✓ $containerName : $status" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $containerName : $status ($health)" -ForegroundColor Red
    }
}

# ==================== 完成 ====================
Write-Host "`n=== 部署完成 ===" -ForegroundColor Cyan

if ($nacosReady) {
    Write-Host "`n✅ 部署成功！" -ForegroundColor Green
    Write-Host "`n访问地址:" -ForegroundColor Yellow
    Write-Host "  Nacos控制台: http://localhost:8848/nacos" -ForegroundColor Cyan
    Write-Host "  默认账号: nacos / nacos" -ForegroundColor Cyan
} else {
    Write-Host "`n⚠ 部署完成，但Nacos可能仍在启动中" -ForegroundColor Yellow
    Write-Host "请稍后检查: docker logs ioedream-nacos" -ForegroundColor White
}

Write-Host "`n查看所有服务状态:" -ForegroundColor Yellow
Write-Host "  docker-compose -f docker-compose-all.yml ps" -ForegroundColor White

Write-Host "`n查看服务日志:" -ForegroundColor Yellow
Write-Host "  docker-compose -f docker-compose-all.yml logs -f [服务名]" -ForegroundColor White
