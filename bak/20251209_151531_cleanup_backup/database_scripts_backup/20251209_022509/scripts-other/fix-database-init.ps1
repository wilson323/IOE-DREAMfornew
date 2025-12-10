# ============================================================
# IOE-DREAM 数据库初始化修复脚本
# 
# @功能: 修复Nacos和业务数据库初始化问题
# @作者: IOE-DREAM Team
# @日期: 2025-12-07
# ============================================================

Write-Host "========================================" -ForegroundColor Green
Write-Host "  IOE-DREAM 数据库初始化修复工具" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

$ErrorActionPreference = "Stop"

# ==================== 步骤1: 下载正确的Nacos Schema ====================
Write-Host "[1/6] 下载Nacos Schema..." -ForegroundColor Cyan

$nacosSchemaUrl = "https://raw.githubusercontent.com/alibaba/nacos/2.3.0/distribution/conf/mysql-schema.sql"
$nacosSchemaPath = "deployment/mysql/init/nacos-schema-official.sql"

try {
    Write-Host "  - 从GitHub下载: $nacosSchemaUrl" -ForegroundColor Gray
    Invoke-WebRequest -Uri $nacosSchemaUrl -OutFile $nacosSchemaPath -UseBasicParsing
    Write-Host "  ✅ Nacos Schema下载成功" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️ 从GitHub下载失败，尝试从Gitee下载..." -ForegroundColor Yellow
    $nacosSchemaUrlGitee = "https://gitee.com/mirrors/nacos/raw/2.3.0/distribution/conf/mysql-schema.sql"
    try {
        Invoke-WebRequest -Uri $nacosSchemaUrlGitee -OutFile $nacosSchemaPath -UseBasicParsing
        Write-Host "  ✅ Nacos Schema下载成功（Gitee）" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ 下载失败，请手动下载Nacos Schema" -ForegroundColor Red
        Write-Host "     URL: $nacosSchemaUrl" -ForegroundColor Yellow
        Write-Host "     保存到: $nacosSchemaPath" -ForegroundColor Yellow
        exit 1
    }
}

# ==================== 步骤2: 合并业务SQL脚本 ====================
Write-Host "`n[2/6] 合并业务SQL脚本..." -ForegroundColor Cyan

$sqlScriptsDir = "database-scripts/common-service"
$mergedSqlPath = "deployment/mysql/init/01-ioedream-init.sql"

if (Test-Path $sqlScriptsDir) {
    Write-Host "  - 扫描SQL脚本目录: $sqlScriptsDir" -ForegroundColor Gray
    $sqlFiles = Get-ChildItem -Path $sqlScriptsDir -Filter "*.sql" | Sort-Object Name
    
    $mergedContent = "-- IOE-DREAM Business Database Initialization Script`r`n"
    $mergedContent += "-- Auto-generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`r`n"
    $mergedContent += "-- Total scripts: $($sqlFiles.Count)`r`n"
    $mergedContent += "`r`n"
    $mergedContent += "CREATE DATABASE IF NOT EXISTS ioedream CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`r`n"
    $mergedContent += "USE ioedream;`r`n"
    $mergedContent += "`r`n"

    foreach ($file in $sqlFiles) {
        Write-Host "    • Adding: $($file.Name)" -ForegroundColor Gray
        $mergedContent += "`r`n-- Source file: $($file.Name)`r`n"
        $mergedContent += Get-Content $file.FullName -Raw -Encoding UTF8
        $mergedContent += "`r`n"
    }
    
    $mergedContent | Out-File $mergedSqlPath -Encoding UTF8
    Write-Host "  ✅ 业务SQL脚本合并成功 ($($sqlFiles.Count) 个文件)" -ForegroundColor Green
    Write-Host "     保存到: $mergedSqlPath" -ForegroundColor Gray
} else {
    Write-Host "  ⚠️ 未找到业务SQL脚本目录: $sqlScriptsDir" -ForegroundColor Yellow
}

# ==================== 步骤3: 停止现有服务 ====================
Write-Host "`n[3/6] 停止现有服务..." -ForegroundColor Cyan

try {
    Write-Host "  - 停止所有容器..." -ForegroundColor Gray
    docker-compose -f docker-compose-all.yml down 2>&1 | Out-Null
    Write-Host "  ✅ 服务已停止" -ForegroundColor Green
} catch {
    Write-Host "  ⚠️ 停止服务时出现警告（可忽略）" -ForegroundColor Yellow
}

# ==================== 步骤4: 启动MySQL和Redis ====================
Write-Host "`n[4/6] 启动基础服务（MySQL + Redis）..." -ForegroundColor Cyan

try {
    Write-Host "  - 启动MySQL..." -ForegroundColor Gray
    docker-compose -f docker-compose-all.yml up -d mysql redis
    
    Write-Host "  - 等待MySQL就绪（最多60秒）..." -ForegroundColor Gray
    $maxWait = 60
    $waited = 0
    while ($waited -lt $maxWait) {
        try {
            $result = docker exec ioedream-mysql mysqladmin ping -h localhost -uroot -proot 2>&1
            if ($result -match "mysqld is alive") {
                Write-Host "  ✅ MySQL已就绪" -ForegroundColor Green
                break
            }
        } catch {}
        
        Start-Sleep -Seconds 2
        $waited += 2
        Write-Host "    等待中... ($waited/$maxWait 秒)" -ForegroundColor Gray -NoNewline
        Write-Host "`r" -NoNewline
    }
    
    if ($waited -ge $maxWait) {
        Write-Host "  ❌ MySQL启动超时" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "  ❌ 启动MySQL失败: $_" -ForegroundColor Red
    exit 1
}

# ==================== 步骤5: 初始化数据库 ====================
Write-Host "`n[5/6] 初始化数据库..." -ForegroundColor Cyan

# 5.1 初始化Nacos数据库
Write-Host "  - 初始化Nacos数据库..." -ForegroundColor Gray
try {
    # 创建nacos数据库
    $createNacosDb = @"
CREATE DATABASE IF NOT EXISTS nacos 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
"@
    $createNacosDb | docker exec -i ioedream-mysql mysql -uroot -proot 2>&1 | Out-Null
    
    # 导入nacos schema
    Get-Content $nacosSchemaPath | docker exec -i ioedream-mysql mysql -uroot -proot nacos 2>&1 | Out-Null
    
    # 验证表数量
    $tableCount = docker exec ioedream-mysql mysql -uroot -proot nacos -e "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema='nacos';" 2>&1 | Select-String "count" -NotMatch | Select-Object -Last 1
    Write-Host "  ✅ Nacos数据库初始化成功 (表数量: $tableCount)" -ForegroundColor Green
} catch {
    Write-Host "  ❌ Nacos数据库初始化失败: $_" -ForegroundColor Red
}

# 5.2 初始化业务数据库
if (Test-Path $mergedSqlPath) {
    Write-Host "  - 初始化业务数据库..." -ForegroundColor Gray
    try {
        Get-Content $mergedSqlPath | docker exec -i ioedream-mysql mysql -uroot -proot 2>&1 | Out-Null
        
        # 验证表数量
        $tableCount = docker exec ioedream-mysql mysql -uroot -proot ioedream -e "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema='ioedream';" 2>&1 | Select-String "count" -NotMatch | Select-Object -Last 1
        Write-Host "  ✅ 业务数据库初始化成功 (表数量: $tableCount)" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ 业务数据库初始化失败: $_" -ForegroundColor Red
    }
}

# ==================== 步骤6: 启动所有服务 ====================
Write-Host "`n[6/6] 启动所有服务..." -ForegroundColor Cyan

try {
    Write-Host "  - 启动Nacos..." -ForegroundColor Gray
    docker-compose -f docker-compose-all.yml up -d nacos
    
    Write-Host "  - 等待Nacos就绪（最多120秒）..." -ForegroundColor Gray
    $maxWait = 120
    $waited = 0
    while ($waited -lt $maxWait) {
        try {
            $result = docker logs ioedream-nacos 2>&1 | Select-String "Nacos started successfully" -Quiet
            if ($result) {
                Write-Host "  ✅ Nacos已就绪" -ForegroundColor Green
                break
            }
        } catch {}
        
        Start-Sleep -Seconds 5
        $waited += 5
        Write-Host "    等待中... ($waited/$maxWait 秒)" -ForegroundColor Gray -NoNewline
        Write-Host "`r" -NoNewline
    }
    
    # 启动所有微服务
    Write-Host "`n  - 启动微服务..." -ForegroundColor Gray
    docker-compose -f docker-compose-all.yml up -d
    
    Write-Host "  ✅ 所有服务已启动" -ForegroundColor Green
} catch {
    Write-Host "  ❌ 启动服务失败: $_" -ForegroundColor Red
}

# ==================== 显示服务状态 ====================
Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  服务状态检查" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

docker-compose -f docker-compose-all.yml ps

# ==================== 提供下一步操作建议 ====================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  下一步操作建议" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ 数据库修复完成！" -ForegroundColor Green
Write-Host ""
Write-Host "📋 查看服务状态:" -ForegroundColor Yellow
Write-Host "   docker-compose -f docker-compose-all.yml ps" -ForegroundColor Gray
Write-Host ""
Write-Host "📋 查看Nacos日志:" -ForegroundColor Yellow
Write-Host "   docker logs ioedream-nacos -f" -ForegroundColor Gray
Write-Host ""
Write-Host "📋 查看微服务日志:" -ForegroundColor Yellow
Write-Host "   docker logs ioedream-gateway-service -f" -ForegroundColor Gray
Write-Host "   docker logs ioedream-common-service -f" -ForegroundColor Gray
Write-Host ""
Write-Host "🌐 访问Nacos控制台:" -ForegroundColor Yellow
Write-Host "   http://localhost:8848/nacos" -ForegroundColor Gray
Write-Host "   用户名: nacos" -ForegroundColor Gray
Write-Host "   密码: nacos" -ForegroundColor Gray
Write-Host ""
Write-Host "🌐 访问API网关:" -ForegroundColor Yellow
Write-Host "   http://localhost:8080" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
