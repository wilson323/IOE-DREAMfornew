# ============================================================
# 消费模块数据库迁移执行脚本（PowerShell版本）
# ============================================================
# 功能：自动执行数据库迁移并验证结果
# 执行方式: .\execute-migration.ps1
# ============================================================

# 错误时停止
$ErrorActionPreference = "Stop"

# 配置变量
$MYSQL_HOST = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "127.0.0.1" }
$MYSQL_PORT = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { "3306" }
$MYSQL_USER = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }
$MYSQL_DATABASE = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { "ioedream" }

# 日志函数
function Log-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Log-Warn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Log-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# ============================================================
# 步骤1：检查MySQL连接
# ============================================================
function Test-MySQLConnection {
    Log-Info "步骤1：检查MySQL连接..."

    try {
        # 检查MySQL命令是否可用
        $null = & mysql --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "MySQL客户端未安装"
        }

        # 测试连接
        $result = & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -e "SELECT VERSION();" 2>&1 | Out-String
        if ($LASTEXITCODE -ne 0) {
            throw "无法连接到MySQL服务器"
        }

        $version = $result -split '\n' | Select-Object -Last 1
        Log-Info "MySQL版本: $version"

        # 检查版本是否≥8.0
        $versionMajor = [int]($version -split '\.')[0]
        if ($versionMajor -lt 8) {
            throw "MySQL版本必须≥8.0，当前版本: $version"
        }

        Log-Info "✅ MySQL连接检查通过"
        return $true
    }
    catch {
        Log-Error $_.Exception.Message
        return $false
    }
}

# ============================================================
# 步骤2：检查数据库是否存在
# ============================================================
function Test-DatabaseExists {
    Log-Info "步骤2：检查数据库是否存在..."

    try {
        $result = & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -e "SHOW DATABASES LIKE '$MYSQL_DATABASE';" 2>&1 | Out-String

        if (-not ($result -match $MYSQL_DATABASE)) {
            throw "数据库 $MYSQL_DATABASE 不存在"
        }

        Log-Info "✅ 数据库 $MYSQL_DATABASE 存在"
        return $true
    }
    catch {
        Log-Error $_.Exception.Message
        Log-Info "请先创建数据库: CREATE DATABASE $MYSQL_DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        return $false
    }
}

# ============================================================
# 步骤3：检查旧表是否存在
# ============================================================
function Test-OldTables {
    Log-Info "步骤3：检查旧表是否存在..."

    try {
        $query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MYSQL_DATABASE' AND table_name LIKE 't_consume%';"
        $result = & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e $query 2>&1 | Out-String
        $count = [int]($result -split '\n' | Select-Object -Last 1)

        if ($count -eq 0) {
            Log-Warn "未找到旧表（t_consume_*），跳过数据迁移验证"
        }
        else {
            Log-Info "找到 $count 个旧表"
        }

        return $true
    }
    catch {
        Log-Error $_.Exception.Message
        return $false
    }
}

# ============================================================
# 步骤4：提示用户备份数据
# ============================================================
function Prompt-Backup {
    Log-Warn "⚠️  生产环境必须先备份数据！"
    Write-Host ""
    Write-Host "备份命令示例："
    Write-Host "  mysqldump -h$MYSQL_HOST -u$MYSQL_USER -p $MYSQL_DATABASE > backup_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
    Write-Host ""

    $backup = Read-Host "是否已完成数据备份？(y/n)"
    if ($backup -ne "y" -and $backup -ne "Y") {
        Log-Error "请先完成数据备份后再执行迁移！"
        exit 1
    }
}

# ============================================================
# 步骤5：启动消费服务执行Flyway迁移
# ============================================================
function Start-ConsumeService {
    Log-Info "步骤5：启动消费服务执行Flyway迁移..."

    try {
        $serviceDir = "D:\IOE-DREAM\microservices\ioedream-consume-service"

        if (-not (Test-Path "$serviceDir\pom.xml")) {
            throw "未找到pom.xml文件"
        }

        Log-Info "启动消费服务（Flyway将自动执行迁移）..."

        # 切换到服务目录
        Push-Location $serviceDir

        # 启动服务（后台）
        $logFile = "$env:TEMP\consume-service.log"
        Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run", "-Dspring-boot.run.profiles=docker" -RedirectStandardOutput $logFile -RedirectStandardError $logFile -WindowStyle Hidden

        Log-Info "服务已启动，日志文件: $logFile"

        # 等待Flyway迁移完成（最多等待60秒）
        Log-Info "等待Flyway迁移完成..."
        Start-Sleep -Seconds 10

        $maxAttempts = 12
        for ($i = 1; $i -le $maxAttempts; $i++) {
            $logContent = Get-Content $logFile -Raw -ErrorAction SilentlyContinue

            if ($logContent -match "Successfully applied.*migrations") {
                Log-Info "✅ Flyway迁移成功！"
                break
            }

            if ($logContent -match "Migration.*failed") {
                throw "Flyway迁移失败！请查看日志: $logFile"
            }

            Start-Sleep -Seconds 5
            Log-Info "等待中... ($i/$maxAttempts)"
        }

        Pop-Location
        Log-Info "Flyway迁移执行完成"

        return $true
    }
    catch {
        Log-Error $_.Exception.Message
        Pop-Location
        return $false
    }
}

# ============================================================
# 步骤6：验证新表创建
# ============================================================
function Test-NewTables {
    Log-Info "步骤6：验证新表创建..."

    try {
        $query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MYSQL_DATABASE' AND table_name LIKE 'POSID%';"
        $result = & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e $query 2>&1 | Out-String
        $count = [int]($result -split '\n' | Select-Object -Last 1)

        if ($count -lt 11) {
            throw "新表创建失败！期望11个POSID表，实际创建: $count"
        }

        Log-Info "✅ 成功创建 $count 个POSID表"

        # 显示所有新表
        Log-Info "新表列表："
        & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e "SHOW TABLES LIKE 'POSID%';" 2>&1

        return $true
    }
    catch {
        Log-Error $_.Exception.Message
        return $false
    }
}

# ============================================================
# 步骤7：验证数据迁移
# ============================================================
function Test-DataMigration {
    Log-Info "步骤7：验证数据迁移..."

    try {
        # 检查是否有旧表数据
        $query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MYSQL_DATABASE' AND table_name LIKE 't_consume%';"
        $result = & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e $query 2>&1 | Out-String
        $oldTableCount = [int]($result -split '\n' | Select-Object -Last 1)

        if ($oldTableCount -eq 0) {
            Log-Info "无旧表数据，跳过数据迁移验证"
            return $true
        }

        Log-Info "旧表与新表数据量对比："
        $query = @"
SELECT
    '旧表数据量' AS label,
    (SELECT COUNT(*) FROM t_consume_account WHERE deleted_flag = 0) AS account_count,
    (SELECT COUNT(*) FROM t_consume_record WHERE deleted_flag = 0) AS record_count,
    (SELECT COUNT(*) FROM t_consume_account_transaction WHERE deleted_flag = 0) AS transaction_count
UNION ALL
SELECT
    '新表数据量' AS label,
    (SELECT COUNT(*) FROM POSID_ACCOUNT WHERE deleted_flag = 0) AS account_count,
    (SELECT COUNT(*) FROM POSID_CONSUME_RECORD WHERE deleted_flag = 0) AS record_count,
    (SELECT COUNT(*) FROM POSID_TRANSACTION WHERE deleted_flag = 0) AS transaction_count;
"@

        & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e $query 2>&1

        Log-Info "✅ 数据迁移验证完成"
        return $true
    }
    catch {
        Log-Error $_.Exception.Message
        return $false
    }
}

# ============================================================
# 步骤8：验证Flyway迁移历史
# ============================================================
function Test-FlywayHistory {
    Log-Info "步骤8：验证Flyway迁移历史..."

    try {
        $query = "SELECT installed_rank, version, script, success, execution_time FROM flyway_schema_history WHERE script LIKE '%POSID%' ORDER BY installed_rank DESC;"
        & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e $query 2>&1

        $query = "SELECT COUNT(*) FROM flyway_schema_history WHERE script LIKE '%POSID%' AND success = 1;"
        $result = & mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p -D"$MYSQL_DATABASE" -e $query 2>&1 | Out-String
        $successCount = [int]($result -split '\n' | Select-Object -Last 1)

        if ($successCount -lt 2) {
            throw "Flyway迁移历史验证失败！"
        }

        Log-Info "✅ Flyway迁移历史验证通过（$successCount 个脚本执行成功）"
        return $true
    }
    catch {
        Log-Error $_.Exception.Message
        return $false
    }
}

# ============================================================
# 主流程
# ============================================================
function Main {
    Write-Host "==================================" -ForegroundColor Cyan
    Write-Host "消费模块数据库迁移执行脚本" -ForegroundColor Cyan
    Write-Host "==================================" -ForegroundColor Cyan
    Write-Host "MySQL Host: $MYSQL_HOST"
    Write-Host "MySQL Port: $MYSQL_PORT"
    Write-Host "MySQL User: $MYSQL_USER"
    Write-Host "Database: $MYSQL_DATABASE"
    Write-Host "==================================" -ForegroundColor Cyan
    Write-Host ""

    # 执行检查
    if (-not (Test-MySQLConnection)) { exit 1 }
    if (-not (Test-DatabaseExists)) { exit 1 }
    if (-not (Test-OldTables)) { exit 1 }

    # 提示备份
    Prompt-Backup

    # 执行迁移
    if (-not (Start-ConsumeService)) { exit 1 }

    # 验证结果
    if (-not (Test-NewTables)) { exit 1 }
    if (-not (Test-DataMigration)) { exit 1 }
    if (-not (Test-FlywayHistory)) { exit 1 }

    Write-Host ""
    Write-Host "==================================" -ForegroundColor Green
    Write-Host "✅ 数据库迁移执行完成！" -ForegroundColor Green
    Write-Host "==================================" -ForegroundColor Green
    Write-Host "下一步：" -ForegroundColor Cyan
    Write-Host "1. 启动双写验证服务（1-2周）" -ForegroundColor White
    Write-Host "2. 定期执行验证SQL" -ForegroundColor White
    Write-Host "3. 查看迁移执行清单: cat MIGRATION_EXECUTION_CHECKLIST.md" -ForegroundColor White
    Write-Host "==================================" -ForegroundColor Green

    # 停止服务
    Log-Info "停止消费服务..."
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*consume-service*" } | Stop-Process -Force
}

# 执行主流程
Main
