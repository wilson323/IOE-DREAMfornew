# =====================================================
# IOE-DREAM 数据库初始化脚本 (PowerShell)
# 版本: 1.0.0
# 说明: 自动化数据库初始化脚本
# =====================================================

# 设置错误处理
$ErrorActionPreference = "Stop"

# 配置变量
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$DbInitDir = Split-Path -Parent $ScriptDir
$MysqlHost = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { "localhost" }
$MysqlPort = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { 3306 }
$MysqlUser = if ($env:MYSQL_USER) { $env:MYSQL_USER } else { "root" }
$MysqlPassword = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { "123456" }
$MysqlCharset = if ($env:MYSQL_CHARSET) { $env:MYSQL_CHARSET } else { "utf8mb4" }

# 日志配置
$LogFile = Join-Path $DbInitDir "init.log"
$BackupDir = Join-Path $DbInitDir "backup"

# 日志函数
function Write-LogInfo {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [INFO] $Message"
    Write-Host $logEntry -ForegroundColor Green
    Add-Content -Path $LogFile -Value $logEntry -Encoding UTF8
}

function Write-LogWarn {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [WARN] $Message"
    Write-Host $logEntry -ForegroundColor Yellow
    Add-Content -Path $LogFile -Value $logEntry -Encoding UTF8
}

function Write-LogError {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [ERROR] $Message"
    Write-Host $logEntry -ForegroundColor Red
    Add-Content -Path $LogFile -Value $logEntry -Encoding UTF8
}

function Write-LogStep {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logEntry = "[$timestamp] [STEP] $Message"
    Write-Host $logEntry -ForegroundColor Cyan
    Add-Content -Path $LogFile -Value $logEntry -Encoding UTF8
}

# 检查MySQL连接
function Test-MySQLConnection {
    Write-LogStep "检查MySQL连接..."

    try {
        # 尝试连接MySQL并执行简单查询
        $result = & mysql -h"$MysqlHost" -P"$MysqlPort" -u"$MysqlUser" -p"$MysqlPassword" -e "SELECT 1;" 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-LogInfo "MySQL连接成功"
            return $true
        }
    } catch {
        Write-LogError "无法连接到MySQL服务器"
        Write-LogError "请检查连接配置: Host=$MysqlHost, Port=$MysqlPort, User=$MysqlUser"
        return $false
    }

    Write-LogError "无法连接到MySQL服务器"
    Write-LogError "请检查连接配置: Host=$MysqlHost, Port=$MysqlPort, User=$MysqlUser"
    return $false
}

# 创建备份目录和备份
function New-DatabaseBackup {
    Write-LogStep "创建数据库备份..."

    if (-not (Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
        Write-LogInfo "创建备份目录: $BackupDir"
    }

    # 获取现有的ioedream数据库
    try {
        $databases = & mysql -h"$MysqlHost" -P"$MysqlPort" -u"$MysqlUser" -p"$MysqlPassword" -e "SHOW DATABASES LIKE 'ioedream_%';" 2>$null | Where-Object { $_ -notlike "Database*" -and $_ -ne "" }

        foreach ($db in $databases) {
            if ($db.Trim() -ne "") {
                $backupFile = Join-Path $BackupDir "${db}_$(Get-Date -Format 'yyyyMMdd_HHmmss').sql"
                Write-LogInfo "备份数据库: $db -> $backupFile"

                try {
                    & mysqldump -h"$MysqlHost" -P"$MysqlPort" -u"$MysqlUser" -p"$MysqlPassword" `
                        --single-transaction --routines --triggers --events `
                        --default-character-set="$MysqlCharset" "$db" | Out-File -FilePath $backupFile -Encoding UTF8

                    if ($LASTEXITCODE -eq 0) {
                        Write-LogInfo "数据库 $db 备份成功"
                    } else {
                        Write-LogWarn "数据库 $db 备份失败，继续执行初始化"
                    }
                } catch {
                    Write-LogWarn "数据库 $db 备份异常，继续执行初始化: $($_.Exception.Message)"
                }
            }
        }
    } catch {
        Write-LogWarn "获取数据库列表失败，可能没有现有的ioedream数据库: $($_.Exception.Message)"
    }
}

# 执行SQL脚本
function Invoke-SqlScript {
    param(
        [string]$ScriptFile,
        [string]$Description
    )

    if (-not (Test-Path $ScriptFile)) {
        Write-LogError "SQL脚本文件不存在: $ScriptFile"
        return $false
    }

    Write-LogStep "执行: $Description"
    Write-LogInfo "脚本文件: $ScriptFile"

    # 记录执行开始时间
    $startTime = Get-Date

    try {
        # 执行SQL脚本
        $output = & mysql -h"$MysqlHost" -P"$MysqlPort" -u"$MysqlUser" -p"$MysqlPassword" `
            --default-character-set="$MysqlCharset" < $ScriptFile 2>&1

        if ($LASTEXITCODE -eq 0) {
            $endTime = Get-Date
            $duration = ($endTime - $startTime).TotalSeconds
            Write-LogInfo "$Description 执行成功 (耗时: $($duration.ToString('F2'))s)"

            # 记录输出到日志
            if ($output) {
                Add-Content -Path $LogFile -Value "$Description 输出:" -Encoding UTF8
                Add-Content -Path $LogFile -Value $output -Encoding UTF8
            }

            return $true
        } else {
            Write-LogError "$Description 执行失败"
            Write-LogError "错误输出: $output"
            return $false
        }
    } catch {
        Write-LogError "$Description 执行异常: $($_.Exception.Message)"
        return $false
    }
}

# 验证初始化结果
function Test-Initialization {
    Write-LogStep "验证初始化结果..."

    $successCount = 0
    $totalCount = 8
    $databases = @("ioedream_database", "ioedream_common_db", "ioedream_access_db",
                   "ioedream_attendance_db", "ioedream_consume_db", "ioedream_visitor_db",
                   "ioedream_video_db", "ioedream_device_db")

    foreach ($db in $databases) {
        try {
            $result = & mysql -h"$MysqlHost" -P"$MysqlPort" -u"$MysqlUser" -p"$MysqlPassword" `
                -e "USE $db; SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$db';" 2>$null

            if ($LASTEXITCODE -eq 0) {
                $tableCount = ($result | Select-Object -Skip 1).Trim()
                Write-LogInfo "✓ 数据库 $db 初始化成功 ($tableCount 个表)"
                $successCount++
            } else {
                Write-LogError "✗ 数据库 $db 初始化失败或无法访问"
            }
        } catch {
            Write-LogError "✗ 数据库 $db 验证异常: $($_.Exception.Message)"
        }
    }

    Write-LogInfo "初始化验证完成: $successCount/$totalCount 个数据库初始化成功"

    if ($successCount -eq $totalCount) {
        Write-LogInfo "🎉 所有数据库初始化成功！"
        return $true
    } else {
        Write-LogError "部分数据库初始化失败，请检查日志"
        return $false
    }
}

# 显示初始化摘要
function Show-InitializationSummary {
    Write-LogStep "生成初始化摘要..."

    Write-Host "================================================" -ForegroundColor White
    Write-Host "IOE-DREAM 数据库初始化摘要" -ForegroundColor White
    Write-Host "================================================" -ForegroundColor White
    Write-Host "初始化时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor White
    Write-Host "MySQL服务器: $MysqlHost`:$MysqlPort" -ForegroundColor White
    Write-Host "字符集: $MysqlCharset" -ForegroundColor White
    Write-Host "" -ForegroundColor White
    Write-Host "数据库清单:" -ForegroundColor White

    $databases = @("ioedream_database", "ioedream_common_db", "ioedream_access_db",
                   "ioedream_attendance_db", "ioedream_consume_db", "ioedream_visitor_db",
                   "ioedream_video_db", "ioedream_device_db")

    foreach ($db in $databases) {
        try {
            $result = & mysql -h"$MysqlHost" -P"$MysqlPort" -u"$MysqlUser" -p"$MysqlPassword" `
                -e "USE $db; SELECT COUNT(*) as table_count FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$db';" 2>$null

            if ($LASTEXITCODE -eq 0) {
                $tableCount = ($result | Select-Object -Skip 1).Trim()

                if ($db -eq "ioedream_common_db") {
                    try {
                        $userResult = & mysql -h"$MysqlHost" -P"$MysqlPort" -u"$MysqlUser" -p"$MysqlPassword" `
                            -e "USE $db; SELECT COUNT(*) FROM t_user;" 2>$null
                        $userCount = if ($LASTEXITCODE -eq 0) { ($userResult | Select-Object -Skip 1).Trim() } else { "0" }
                        Write-Host "  ✓ $db`: $tableCount 个表, $userCount 个用户" -ForegroundColor Green
                    } catch {
                        Write-Host "  ✓ $db`: $tableCount 个表" -ForegroundColor Green
                    }
                } else {
                    Write-Host "  ✓ $db`: $tableCount 个表" -ForegroundColor Green
                }
            } else {
                Write-Host "  ✗ $db`: 初始化失败" -ForegroundColor Red
            }
        } catch {
            Write-Host "  ✗ $db`: 验证异常" -ForegroundColor Red
        }
    }

    Write-Host "" -ForegroundColor White
    Write-Host "日志文件: $LogFile" -ForegroundColor White
    Write-Host "备份目录: $BackupDir" -ForegroundColor White
    Write-Host "================================================" -ForegroundColor White
}

# 检查MySQL命令是否存在
function Test-MySQLCommand {
    try {
        $null = Get-Command mysql -ErrorAction Stop
        $null = Get-Command mysqldump -ErrorAction Stop
        return $true
    } catch {
        Write-LogError "MySQL命令行工具未安装或不在PATH中"
        Write-LogError "请安装MySQL客户端或将MySQL bin目录添加到PATH环境变量"
        return $false
    }
}

# 主函数
function Main {
    Write-LogInfo "开始 IOE-DREAM 数据库初始化..."
    Write-LogInfo "初始化目录: $DbInitDir"

    # 检查环境
    if (-not (Test-MySQLCommand)) {
        return
    }

    if (-not (Test-MySQLConnection)) {
        return
    }

    # 创建备份
    New-DatabaseBackup

    # 记录初始化开始
    "$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') - 开始数据库初始化" | Out-File -FilePath $LogFile -Encoding UTF8

    # 执行初始化脚本
    $scripts = @(
        @{ File = Join-Path $DbInitDir "sql\01-create-databases.sql"; Description = "创建数据库" },
        @{ File = Join-Path $DbInitDir "sql\02-common-schema.sql"; Description = "创建公共表结构" },
        @{ File = Join-Path $DbInitDir "sql\03-business-schema.sql"; Description = "创建业务表结构" },
        @{ File = Join-Path $DbInitDir "sql\99-flyway-schema.sql"; Description = "创建Flyway表" },
        @{ File = Join-Path $DbInitDir "data\common-data.sql"; Description = "初始化公共数据" },
        @{ File = Join-Path $DbInitDir "data\business-data.sql"; Description = "初始化业务数据" }
    )

    $failedScripts = @()

    foreach ($script in $scripts) {
        if (-not (Invoke-SqlScript -ScriptFile $script.File -Description $script.Description)) {
            $failedScripts += $script.Description
        }

        # 在脚本之间添加短暂延迟
        Start-Sleep -Seconds 1
    }

    # 验证初始化结果
    if (Test-Initialization) {
        Write-LogInfo "数据库初始化成功完成！"
        Show-InitializationSummary
        exit 0
    } else {
        Write-LogError "数据库初始化失败！"

        if ($failedScripts.Count -gt 0) {
            Write-LogError "失败的脚本:"
            foreach ($failedScript in $failedScripts) {
                Write-LogError "  - $failedScript"
            }
        }

        exit 1
    }
}

# 显示帮助信息
function Show-Help {
    Write-Host "IOE-DREAM 数据库初始化脚本 (PowerShell)" -ForegroundColor White
    Write-Host ""
    Write-Host "用法: .\init-database.ps1 [选项]" -ForegroundColor White
    Write-Host ""
    Write-Host "环境变量:" -ForegroundColor White
    Write-Host "  MYSQL_HOST     MySQL服务器地址 (默认: localhost)" -ForegroundColor White
    Write-Host "  MYSQL_PORT     MySQL端口 (默认: 3306)" -ForegroundColor White
    Write-Host "  MYSQL_USER     MySQL用户名 (默认: root)" -ForegroundColor White
    Write-Host "  MYSQL_PASSWORD MySQL密码 (默认: 123456)" -ForegroundColor White
    Write-Host "  MYSQL_CHARSET  字符集 (默认: utf8mb4)" -ForegroundColor White
    Write-Host ""
    Write-Host "示例:" -ForegroundColor White
    Write-Host "  # 使用默认配置" -ForegroundColor White
    Write-Host "  .\init-database.ps1" -ForegroundColor White
    Write-Host ""
    Write-Host "  # 自定义配置" -ForegroundColor White
    Write-Host "  `$env:MYSQL_HOST='192.168.1.100'; `$env:MYSQL_PASSWORD='mypass'; .\init-database.ps1" -ForegroundColor White
}

# 参数处理
switch ($args[0]) {
    "-h" {
        Show-Help
        exit 0
    }
    "--help" {
        Show-Help
        exit 0
    }
    "" {
        Main
    }
    default {
        Write-Host "未知参数: $($args[0])" -ForegroundColor Red
        Write-Host "使用 -h 或 --help 查看帮助信息" -ForegroundColor Yellow
        exit 1
    }
}