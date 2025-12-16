# ============================================================
# IOE-DREAM 数据库工具函数库
# 版本: v1.0.0
# 作者: IOE-DREAM 架构团队
# 创建时间: 2025-12-15
# 功能: 提供数据库操作的核心工具函数
# ============================================================

# 全局变量
$Global:MySQLPaths = @()
$Global:MySQLExecutable = $null
$Global:MysqldumpExecutable = $null

# 初始化MySQL路径检测
function Initialize-MySQLPaths {
    Write-Host "🔍 检测MySQL安装路径..." -ForegroundColor Cyan

    # 常见的MySQL安装路径
    $PossiblePaths = @(
        "C:\Program Files\MySQL\MySQL Server 8.0\bin",
        "C:\Program Files\MySQL\MySQL Server 8.4\bin",
        "C:\Program Files\MySQL\MySQL Server 5.7\bin",
        "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin",
        "C:\xampp\mysql\bin",
        "C:\wamp64\bin\mysql\mysql8.0.30\bin",
        "C:\mysql\bin"
    )

    # 环境变量PATH中的MySQL
    $env:PATH -split ';' | ForEach-Object {
        if ($_ -match "mysql" -and (Test-Path "$_\mysql.exe")) {
            $PossiblePaths += $_
        }
    }

    # 检测可用的MySQL路径
    foreach ($Path in $PossiblePaths) {
        if (Test-Path "$Path\mysql.exe") {
            $Global:MySQLExecutable = "$Path\mysql.exe"
            Write-Host "✅ 找到MySQL客户端: $Global:MySQLExecutable" -ForegroundColor Green
        }

        if (Test-Path "$Path\mysqldump.exe") {
            $Global:MysqldumpExecutable = "$Path\mysqldump.exe"
            Write-Host "✅ 找到mysqldump工具: $Global:MysqldumpExecutable" -ForegroundColor Green
        }
    }

    if (-not $Global:MySQLExecutable -and -not $Global:MysqldumpExecutable) {
        Write-Host "⚠️ 未找到MySQL命令行工具，将使用替代方案" -ForegroundColor Yellow
        return $false
    }

    return $true
}

# 测试数据库连接（支持多种方式）
function Test-DatabaseConnection {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Host,

        [Parameter(Mandatory=$true)]
        [string]$Port,

        [Parameter(Mandatory=$true)]
        [string]$Username,

        [Parameter(Mandatory=$true)]
        [string]$Password,

        [Parameter(Mandatory=$true)]
        [string]$Database
    )

    Write-Host "🔗 测试数据库连接..." -ForegroundColor Cyan

    # 方式1: 使用MySQL命令行工具
    if ($Global:MySQLExecutable) {
        try {
            $TestQuery = "SELECT 1 as connection_test;"
            $Result = & $Global:MySQLExecutable -h $Host -P $Port -u $Username -p$Password -e $TestQuery $Database 2>&1

            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ 数据库连接成功（MySQL客户端测试）" -ForegroundColor Green
                return $true
            }
        }
        catch {
            Write-Host "⚠️ MySQL客户端测试失败: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    # 方式2: 使用网络连接测试（端口连通性）
    try {
        $TcpClient = New-Object System.Net.Sockets.TcpClient
        $Connect = $TcpClient.BeginConnect($Host, $Port, $null, $null)
        $Wait = $Connect.AsyncWaitHandle.WaitOne(5000, $false)

        if ($Wait) {
            $TcpClient.EndConnect($Connect)
            $TcpClient.Close()
            Write-Host "✅ 数据库端口连接成功: $Host:$Port" -ForegroundColor Green
            return $true
        }
        else {
            Write-Host "❌ 数据库端口连接超时: $Host:$Port" -ForegroundColor Red
            return $false
        }
    }
    catch {
        Write-Host "❌ 数据库网络连接失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }

    Write-Host "❌ 所有连接测试失败" -ForegroundColor Red
    return $false
}

# 数据库备份（支持多种方式）
function Backup-Database {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Host,

        [Parameter(Mandatory=$true)]
        [string]$Port,

        [Parameter(Mandatory=$true)]
        [string]$Username,

        [Parameter(Mandatory=$true)]
        [string]$Password,

        [Parameter(Mandatory=$true)]
        [string]$Database,

        [Parameter(Mandatory=$true)]
        [string]$BackupFile
    )

    Write-Host "💾 开始数据库备份..." -ForegroundColor Cyan
    Write-Host "📁 备份文件: $BackupFile" -ForegroundColor Yellow

    # 确保备份目录存在
    $BackupDir = Split-Path -Parent $BackupFile
    if (!(Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
        Write-Host "📁 创建备份目录: $BackupDir" -ForegroundColor Green
    }

    # 方式1: 使用mysqldump工具
    if ($Global:MysqldumpExecutable) {
        try {
            $DumpArgs = @(
                "-h", $Host,
                "-P", $Port,
                "-u", $Username,
                "-p$Password",
                "--single-transaction",
                "--routines",
                "--triggers",
                "--events",
                "--add-drop-table",
                "--create-options",
                "--default-character-set=utf8mb4",
                $Database
            )

            Write-Host "🔧 使用mysqldump备份..." -ForegroundColor Green
            & $Global:MysqldumpExecutable $DumpArgs | Out-File -FilePath $BackupFile -Encoding UTF8

            if ($LASTEXITCODE -eq 0) {
                $FileSize = (Get-Item $BackupFile).Length / 1MB
                Write-Host "✅ 备份完成! 文件大小: $($FileSize.ToString('F2'))MB" -ForegroundColor Green
                return $true
            }
        }
        catch {
            Write-Host "❌ mysqldump备份失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    }

    # 方式2: 创建备份记录文件（用于演示）
    try {
        Write-Host "📝 创建备份记录文件..." -ForegroundColor Yellow

        $BackupRecord = @"
# ============================================================
# IOE-DREAM 数据库备份记录
# ============================================================
备份时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
备份目标: $Database
数据库服务器: $Host:$Port
用户: $Username
备份文件: $BackupFile
状态: 备份请求已创建（需要手动执行备份）
# ============================================================
# 由于系统未检测到MySQL命令行工具，请手动执行以下命令：
# mysqldump -h $Host -P $Port -u $Username -p$Password --single-transaction --routines --triggers --events $Database > "$BackupFile"
# ============================================================
"@

        $BackupRecord | Out-File -FilePath "$BackupFile.backup-info.txt" -Encoding UTF8
        Write-Host "⚠️ 已创建备份记录文件，请手动执行备份" -ForegroundColor Yellow
        return $false
    }
    catch {
        Write-Host "❌ 创建备份记录失败: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 执行SQL迁移脚本
function Invoke-SqlMigration {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Host,

        [Parameter(Mandatory=$true)]
        [string]$Port,

        [Parameter(Mandatory=$true)]
        [string]$Username,

        [Parameter(Mandatory=$true)]
        [string]$Password,

        [Parameter(Mandatory=$true)]
        [string]$Database,

        [Parameter(Mandatory=$true)]
        [string]$SqlFile
    )

    Write-Host "🚀 执行SQL迁移脚本..." -ForegroundColor Cyan
    Write-Host "📄 脚本文件: $SqlFile" -ForegroundColor Yellow

    if (!(Test-Path $SqlFile)) {
        Write-Host "❌ SQL文件不存在: $SqlFile" -ForegroundColor Red
        return $false
    }

    # 方式1: 使用MySQL客户端执行
    if ($Global:MySQLExecutable) {
        try {
            $Result = & $Global:MySQLExecutable -h $Host -P $Port -u $Username -p$Password $Database < $SqlFile 2>&1

            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ SQL脚本执行成功" -ForegroundColor Green
                return $true
            }
            else {
                Write-Host "❌ SQL脚本执行失败: $Result" -ForegroundColor Red
                return $false
            }
        }
        catch {
            Write-Host "❌ SQL脚本执行异常: $($_.Exception.Message)" -ForegroundColor Red
            return $false
        }
    }
    else {
        Write-Host "⚠️ 未找到MySQL客户端，无法执行SQL脚本" -ForegroundColor Yellow
        Write-Host "💡 请手动执行: mysql -h $Host -P $Port -u $Username -p$Password $Database < `"$SqlFile`"" -ForegroundColor Cyan
        return $false
    }
}

# 获取数据库版本信息
function Get-DatabaseVersion {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Host,

        [Parameter(Mandatory=$true)]
        [string]$Port,

        [Parameter(Mandatory=$true)]
        [string]$Username,

        [Parameter(Mandatory=$true)]
        [string]$Password,

        [string]$Database = "mysql"
    )

    Write-Host "🔍 获取数据库版本信息..." -ForegroundColor Cyan

    if ($Global:MySQLExecutable) {
        try {
            $VersionQuery = "SELECT VERSION() as mysql_version;"
            $Result = & $Global:MySQLExecutable -h $Host -P $Port -u $Username -p$Password -e $VersionQuery $Database 2>&1

            if ($LASTEXITCODE -eq 0) {
                $Version = $Result -split '\n' | Select-Object -Skip 1 | Where-Object { $_.Trim() -ne "" }
                Write-Host "✅ MySQL版本: $($Version.Trim())" -ForegroundColor Green
                return $Version.Trim()
            }
        }
        catch {
            Write-Host "⚠️ 无法获取数据库版本: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }

    return "Unknown"
}

# 验证数据库状态
function Test-DatabaseHealth {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Host,

        [Parameter(Mandatory=$true)]
        [string]$Port,

        [Parameter(Mandatory=$true)]
        [string]$Username,

        [Parameter(Mandatory=$true)]
        [string]$Password,

        [Parameter(Mandatory=$true)]
        [string]$Database
    )

    Write-Host "🏥 数据库健康检查..." -ForegroundColor Cyan

    $HealthStatus = @{
        Connection = $false
        Version = "Unknown"
        Tables = 0
        Size = "Unknown"
        FlywayTable = $false
    }

    # 测试连接
    $HealthStatus.Connection = Test-DatabaseConnection -Host $Host -Port $Port -Username $Username -Password $Password -Database $Database

    if ($HealthStatus.Connection) {
        # 获取版本
        $HealthStatus.Version = Get-DatabaseVersion -Host $Host -Port $Port -Username $Username -Password $Password -Database $Database

        # 检查Flyway表
        if ($Global:MySQLExecutable) {
            try {
                $FlywayCheck = & $Global:MySQLExecutable -h $Host -P $Port -u $Username -p$Password -e "SHOW TABLES LIKE 'flyway_schema_history'" $Database 2>&1
                if ($FlywayCheck -match "flyway_schema_history") {
                    $HealthStatus.FlywayTable = $true
                    Write-Host "✅ Flyway历史表存在" -ForegroundColor Green
                }
                else {
                    Write-Host "⚠️ Flyway历史表不存在" -ForegroundColor Yellow
                }
            }
            catch {
                Write-Host "⚠️ 无法检查Flyway表: $($_.Exception.Message)" -ForegroundColor Yellow
            }
        }
    }

    return $HealthStatus
}

# 自动初始化函数（模块导入时自动执行）
Initialize-MySQLPaths

# 导出函数供其他脚本使用
Export-ModuleMember -Function @(
    'Test-DatabaseConnection',
    'Backup-Database',
    'Invoke-SqlMigration',
    'Get-DatabaseVersion',
    'Test-DatabaseHealth'
)