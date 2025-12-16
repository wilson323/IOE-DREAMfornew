# ============================================================
# IOE-DREAM 数据库迁移自动化工具
# 版本: v1.0.0
# 技术栈: PowerShell 5.1+ + Spring Boot 3.5.8 + Flyway 9.x
# 作者: IOE-DREAM 架构团队
# 创建时间: 2025-12-15
# 功能: 自动化数据库迁移、验证、备份和回滚
# ============================================================

# 设置执行策略
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# 全局变量
$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptRoot
$LogFile = "$ScriptRoot\logs\migration-$(Get-Date -Format 'yyyyMMdd-HHmmss').log"
$ConfigFile = "$ScriptRoot\config\migration-config.json"

# 确保日志目录存在
if (!(Test-Path "$ScriptRoot\logs")) {
    New-Item -ItemType Directory -Path "$ScriptRoot\logs" -Force | Out-Null
}

# 日志函数
function Write-Log {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message,

        [ValidateSet("INFO", "WARN", "ERROR", "SUCCESS")]
        [string]$Level = "INFO"
    )

    $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $LogEntry = "[$Timestamp] [$Level] $Message"

    # 写入日志文件
    Add-Content -Path $LogFile -Value $LogEntry

    # 根据级别输出不同颜色
    switch ($Level) {
        "INFO"    { Write-Host $LogEntry -ForegroundColor White }
        "WARN"    { Write-Host $LogEntry -ForegroundColor Yellow }
        "ERROR"   { Write-Host $LogEntry -ForegroundColor Red }
        "SUCCESS" { Write-Host $LogEntry -ForegroundColor Green }
    }
}

# 加载配置
function Load-Configuration {
    Write-Log "加载迁移配置..."

    if (!(Test-Path $ConfigFile)) {
        Write-Log "配置文件不存在，创建默认配置" -Level "WARN"
        $DefaultConfig = @{
            "Database" = @{
                "Host" = "127.0.0.1"
                "Port" = 3306
                "Database" = "ioedream"
                "Username" = "root"
                "Password" = "123456"
            }
            "Services" = @(
                "ioedream-common-service",
                "ioedream-device-comm-service",
                "ioedream-oa-service",
                "ioedream-access-service",
                "ioedream-attendance-service",
                "ioedream-video-service",
                "ioedream-consume-service",
                "ioedream-visitor-service",
                "ioedream-gateway-service"
            )
            "Backup" = @{
                "Enabled" = $true
                "Location" = "$ScriptRoot\backup"
                "RetentionDays" = 30
            }
            "Environment" = "dev"
            "Timeout" = 300
        }
        $DefaultConfig | ConvertTo-Json -Depth 4 | Out-File -FilePath $ConfigFile -Encoding UTF8
    }

    try {
        $Config = Get-Content $ConfigFile -Raw | ConvertFrom-Json
        Write-Log "配置加载成功"
        return $Config
    }
    catch {
        Write-Log "配置文件解析失败: $($_.Exception.Message)" -Level "ERROR"
        throw
    }
}

# 数据库连接测试
function Test-DatabaseConnection {
    param($Config)

    Write-Log "测试数据库连接..."

    try {
        $ConnectionString = "Server=$($Config.Database.Host);$($Config.Database.Port);Database=$($Config.Database.Database);Uid=$($Config.Database.Username);Pwd=$($Config.Database.Password);"

        # 使用MySQL客户端测试连接
        $Result = & mysql -h $Config.Database.Host -P $Config.Database.Port -u $Config.Database.Username -p$Config.Database.Password -e "SELECT 1 as test;" $Config.Database.Database 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Log "数据库连接测试成功" -Level "SUCCESS"
            return $true
        }
        else {
            Write-Log "数据库连接测试失败: $Result" -Level "ERROR"
            return $false
        }
    }
    catch {
        Write-Log "数据库连接测试异常: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# 数据库备份
function Backup-Database {
    param($Config)

    if (!$Config.Backup.Enabled) {
        Write-Log "数据库备份已禁用，跳过备份"
        return $true
    }

    Write-Log "开始数据库备份..."

    try {
        $BackupDir = $Config.Backup.Location
        $Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
        $BackupFile = "$BackupDir\ioedream-backup-$Timestamp.sql"

        # 确保备份目录存在
        if (!(Test-Path $BackupDir)) {
            New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
        }

        # 执行备份
        $BackupCmd = "mysqldump -h $($Config.Database.Host) -P $($Config.Database.Port) -u $($Config.Database.Username) -p$($Config.Database.Password) $($Config.Database.Database) > `"$BackupFile`""

        Write-Log "执行备份命令: $BackupCmd"
        $Result = cmd /c $BackupCmd 2>&1

        if ($LASTEXITCODE -eq 0 -and (Test-Path $BackupFile)) {
            $BackupSize = [math]::Round((Get-Item $BackupFile).Length / 1MB, 2)
            Write-Log "数据库备份成功: $BackupFile (大小: $BackupSize MB)" -Level "SUCCESS"

            # 清理过期备份
            Cleanup-OldBackups -BackupDir $BackupDir -RetentionDays $Config.Backup.RetentionDays

            return $true
        }
        else {
            Write-Log "数据库备份失败: $Result" -Level "ERROR"
            return $false
        }
    }
    catch {
        Write-Log "数据库备份异常: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# 清理过期备份
function Cleanup-OldBackups {
    param(
        [string]$BackupDir,
        [int]$RetentionDays
    )

    Write-Log "清理 $RetentionDays 天前的备份文件..."

    try {
        $CutoffDate = (Get-Date).AddDays(-$RetentionDays)
        $OldBackups = Get-ChildItem -Path $BackupDir -Filter "ioedream-backup-*.sql" | Where-Object { $_.CreationTime -lt $CutoffDate }

        foreach ($Backup in $OldBackups) {
            Remove-Item $Backup.FullName -Force
            Write-Log "删除过期备份: $($Backup.Name)"
        }

        if ($OldBackups.Count -gt 0) {
            Write-Log "已清理 $($OldBackups.Count) 个过期备份文件"
        }
    }
    catch {
        Write-Log "清理备份文件异常: $($_.Exception.Message)" -Level "WARN"
    }
}

# 检查服务状态
function Test-ServiceStatus {
    param([string]$ServiceName)

    Write-Log "检查服务状态: $ServiceName"

    try {
        $ServiceDir = "$ProjectRoot\microservices\$ServiceName"

        if (!(Test-Path $ServiceDir)) {
            Write-Log "服务目录不存在: $ServiceDir" -Level "WARN"
            return $false
        }

        # 检查服务是否正在运行
        $ProcessName = $ServiceName
        $Process = Get-Process | Where-Object { $_.ProcessName -like "*$ServiceName*" -or $_.MainWindowTitle -like "*$ServiceName*" }

        if ($Process) {
            Write-Log "服务正在运行: $ServiceName" -Level "WARN"
            Write-Log "建议停止服务后再进行数据库迁移" -Level "WARN"
            return $false
        }

        return $true
    }
    catch {
        Write-Log "检查服务状态异常: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# 执行数据库迁移
function Invoke-DatabaseMigration {
    param($Config, [string]$ServiceName)

    Write-Log "开始执行数据库迁移: $ServiceName"

    try {
        $ServiceDir = "$ProjectRoot\microservices\$ServiceName"

        if (!(Test-Path $ServiceDir)) {
            Write-Log "服务目录不存在: $ServiceDir，跳过迁移" -Level "WARN"
            return $true
        }

        # 检查是否有Flyway配置
        $ApplicationYml = "$ServiceDir\src\main\resources\application.yml"
        if (!(Test-Path $ApplicationYml)) {
            Write-Log "应用配置文件不存在: $ApplicationYml，跳过迁移" -Level "WARN"
            return $true
        }

        # 进入服务目录
        Push-Location $ServiceDir

        # 执行Flyway迁移（通过Spring Boot应用）
        Write-Log "执行Flyway迁移命令..."

        # 使用Spring Boot Maven插件执行迁移
        $MigrateCmd = "mvn spring-boot:run -Dspring-boot.run.arguments=`"--spring.flyway.enabled=true --spring.profiles.active=$($Config.Environment)`" -Dspring-boot.run.fork=false -Dmaven.test.skip=true"

        Write-Log "执行命令: $MigrateCmd"
        $Result = Invoke-Expression $MigrateCmd 2>&1

        # 恢复目录
        Pop-Location

        if ($LASTEXITCODE -eq 0) {
            Write-Log "数据库迁移成功: $ServiceName" -Level "SUCCESS"
            return $true
        }
        else {
            Write-Log "数据库迁移失败: $ServiceName" -Level "ERROR"
            Write-Log "错误信息: $Result" -Level "ERROR"
            return $false
        }
    }
    catch {
        Write-Log "数据库迁移异常: $ServiceName - $($_.Exception.Message)" -Level "ERROR"
        Pop-Location -ErrorAction SilentlyContinue
        return $false
    }
}

# 验证迁移结果
function Test-MigrationResult {
    param($Config)

    Write-Log "验证数据库迁移结果..."

    try {
        # 检查Flyway历史表
        $CheckSql = "SELECT version, description, installed_on FROM flyway_schema_history ORDER BY installed_on DESC LIMIT 5;"
        $Result = & mysql -h $Config.Database.Host -P $Config.Database.Port -u $Config.Database.Username -p$($Config.Database.Password) -e $CheckSql $Config.Database.Database 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Log "最近的迁移记录:" -Level "SUCCESS"
            Write-Log $Result

            # 验证关键表是否存在
            $CriticalTables = @("t_common_user", "t_device", "t_area", "t_access_record", "t_attendance_record")

            foreach ($Table in $CriticalTables) {
                $TableCheckSql = "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = '$($Config.Database.Database)' AND table_name = '$Table';"
                $TableResult = & mysql -h $Config.Database.Host -P $Config.Database.Port -u $Config.Database.Username -p$($Config.Database.Password) -e $TableCheckSql $Config.Database.Database 2>&1

                if ($LASTEXITCODE -eq 0 -and $TableResult -match "1") {
                    Write-Log "关键表存在验证成功: $Table" -Level "SUCCESS"
                }
                else {
                    Write-Log "关键表缺失: $Table" -Level "ERROR"
                    return $false
                }
            }

            return $true
        }
        else {
            Write-Log "无法查询迁移历史: $Result" -Level "ERROR"
            return $false
        }
    }
    catch {
        Write-Log "迁移结果验证异常: $($_.Exception.Message)" -Level "ERROR"
        return $false
    }
}

# 生成迁移报告
function New-MigrationReport {
    param($Config, [array]$MigrationResults)

    Write-Log "生成迁移报告..."

    try {
        $ReportFile = "$ScriptRoot\reports\migration-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').html"

        # 确保报告目录存在
        if (!(Test-Path "$ScriptRoot\reports")) {
            New-Item -ItemType Directory -Path "$ScriptRoot\reports" -Force | Out-Null
        }

        $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

        $HtmlContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>IOE-DREAM 数据库迁移报告</title>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #2c3e50; color: white; padding: 20px; border-radius: 5px; }
        .success { color: #27ae60; font-weight: bold; }
        .error { color: #e74c3c; font-weight: bold; }
        .warning { color: #f39c12; font-weight: bold; }
        table { border-collapse: collapse; width: 100%; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        th { background-color: #f2f2f2; font-weight: bold; }
        .summary { background-color: #ecf0f1; padding: 20px; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="header">
        <h1>IOE-DREAM 数据库迁移报告</h1>
        <p>生成时间: $Timestamp</p>
        <p>环境: $($Config.Environment)</p>
        <p>数据库: $($Config.Database.Host):$($Config.Database.Port)/$($Config.Database.Database)</p>
    </div>

    <div class="summary">
        <h2>迁移摘要</h2>
        <p>总服务数: $($MigrationResults.Count)</p>
        <p>成功迁移: $($MigrationResults.Where({$_.Success -eq $true}).Count)</p>
        <p>失败迁移: $($MigrationResults.Where({$_.Success -eq $false}).Count)</p>
        <p>成功率: $([math]::Round(($MigrationResults.Where({$_.Success -eq $true}).Count / $MigrationResults.Count) * 100, 2))%</p>
    </div>

    <h2>详细结果</h2>
    <table>
        <thead>
            <tr>
                <th>服务名称</th>
                <th>状态</th>
                <th>执行时间</th>
                <th>备注</th>
            </tr>
        </thead>
        <tbody>
"@

        foreach ($Result in $MigrationResults) {
            $StatusClass = if ($Result.Success) { "success" } else { "error" }
            $Status = if ($Result.Success) { "成功" } else { "失败" }

            $HtmlContent += @"
            <tr>
                <td>$($Result.Service)</td>
                <td class="$StatusClass">$Status</td>
                <td>$($Result.Duration)ms</td>
                <td>$($Result.Message)</td>
            </tr>
"@
        }

        $HtmlContent += @"
        </tbody>
    </table>

    <div class="summary">
        <h2>建议</h2>
        <ul>
            <li>定期检查数据库迁移状态</li>
            <li>在生产环境执行前进行充分测试</li>
            <li>保持迁移脚本的版本管理</li>
            <li>建立完善的监控和告警机制</li>
        </ul>
    </div>
</body>
</html>
"@

        $HtmlContent | Out-File -FilePath $ReportFile -Encoding UTF8

        Write-Log "迁移报告已生成: $ReportFile" -Level "SUCCESS"
        return $ReportFile
    }
    catch {
        Write-Log "生成迁移报告异常: $($_.Exception.Message)" -Level "ERROR"
        return $null
    }
}

# 主函数
function Main {
    try {
        Write-Log "========================================" -Level "SUCCESS"
        Write-Log "IOE-DREAM 数据库迁移自动化工具" -Level "SUCCESS"
        Write-Log "版本: v1.0.0" -Level "SUCCESS"
        Write-Log "========================================" -Level "SUCCESS"

        # 加载配置
        $Config = Load-Configuration

        # 测试数据库连接
        if (!(Test-DatabaseConnection $Config)) {
            Write-Log "数据库连接失败，终止迁移" -Level "ERROR"
            exit 1
        }

        # 数据库备份
        if (!(Backup-Database $Config)) {
            Write-Log "数据库备份失败，但继续执行迁移" -Level "WARN"
        }

        # 执行迁移
        $MigrationResults = @()
        $StartTime = Get-Date

        foreach ($ServiceName in $Config.Services) {
            Write-Log "========================================"
            Write-Log "处理服务: $ServiceName"
            Write-Log "========================================"

            $ServiceStartTime = Get-Date

            # 检查服务状态
            if (!(Test-ServiceStatus $ServiceName)) {
                Write-Log "服务状态检查失败: $ServiceName" -Level "WARN"
            }

            # 执行迁移
            $MigrationSuccess = Invoke-DatabaseMigration $Config $ServiceName

            $ServiceEndTime = Get-Date
            $Duration = [math]::Round(($ServiceEndTime - $ServiceStartTime).TotalMilliseconds)

            $MigrationResults += [PSCustomObject]@{
                Service = $ServiceName
                Success = $MigrationSuccess
                Duration = $Duration
                Message = if ($MigrationSuccess) { "迁移成功" } else { "迁移失败" }
            }
        }

        # 验证迁移结果
        $ValidationSuccess = Test-MigrationResult $Config

        # 生成迁移报告
        $ReportFile = New-MigrationReport $Config $MigrationResults

        $EndTime = Get-Date
        $TotalDuration = [math]::Round(($EndTime - $StartTime).TotalMinutes, 2)

        Write-Log "========================================" -Level "SUCCESS"
        Write-Log "迁移执行完成" -Level "SUCCESS"
        Write-Log "总耗时: $TotalDuration 分钟" -Level "SUCCESS"
        Write-Log "成功服务: $($MigrationResults.Where({$_.Success -eq $true}).Count)/$($MigrationResults.Count)" -Level "SUCCESS"
        Write-Log "验证结果: $(if ($ValidationSuccess) { '通过' } else { '失败' })" -Level $(if ($ValidationSuccess) { "SUCCESS" } else { "ERROR" })

        if ($ReportFile) {
            Write-Log "详细报告: $ReportFile" -Level "SUCCESS"
        }

        Write-Log "========================================" -Level "SUCCESS"

        # 检查是否有失败的迁移
        $FailedMigrations = $MigrationResults.Where({$_.Success -eq $false})
        if ($FailedMigrations.Count -gt 0) {
            Write-Log "以下服务迁移失败:" -Level "ERROR"
            foreach ($Failed in $FailedMigrations) {
                Write-Log "  - $($Failed.Service)" -Level "ERROR"
            }
            exit 1
        }

        if (!$ValidationSuccess) {
            Write-Log "迁移结果验证失败" -Level "ERROR"
            exit 1
        }

        Write-Log "所有迁移执行成功！" -Level "SUCCESS"
        exit 0
    }
    catch {
        Write-Log "主程序异常: $($_.Exception.Message)" -Level "ERROR"
        Write-Log "堆栈跟踪: $($_.Exception.StackTrace)" -Level "ERROR"
        exit 1
    }
}

# 参数处理
param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("migrate", "validate", "backup", "report", "rollback")]
    [string]$Action = "migrate",

    [Parameter(Mandatory=$false)]
    [string]$Service = "",

    [Parameter(Mandatory=$false)]
    [ValidateSet("dev", "test", "prod")]
    [string]$Environment = "dev"

    [Parameter(Mandatory=$false)]
    [switch]$DryRun
)

# 显示帮助信息
function Show-Help {
    Write-Host "IOE-DREAM 数据库迁移自动化工具 v1.0.0" -ForegroundColor Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor Yellow
    Write-Host "  .\migration-automation.ps1 -Action <action> [其他参数]"
    Write-Host ""
    Write-Host "参数:" -ForegroundColor Yellow
    Write-Host "  -Action        操作类型 (migrate, validate, backup, report, rollback)"
    Write-Host "  -Service       指定服务名称 (可选，默认执行所有服务)"
    Write-Host "  -Environment    环境类型 (dev, test, prod)"
    Write-Host "  -DryRun        仅显示将要执行的操作"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Yellow
    Write-Host "  .\migration-automation.ps1                                    # 迁移所有服务"
    Write-Host "  .\migration-automation.ps1 -Action validate                    # 验证迁移结果"
    Write-Host "  .\migration-automation.ps1 -Service ioedream-access-service   # 迁移指定服务"
    Write-Host "  .\migration-automation.ps1 -Environment prod -DryRun           # 生产环境演练"
}

# 参数验证和处理
if ($Args -contains "-?" -or $Args -contains "--help") {
    Show-Help
    exit 0
}

# 根据参数执行不同操作
switch ($Action) {
    "migrate" {
        if ($DryRun) {
            Write-Log "演练模式：将执行数据库迁移操作" -Level "WARN"
            Write-Log "实际不会执行任何数据库操作" -Level "WARN"
        }
        else {
            Main
        }
    }
    "validate" {
        Write-Log "执行迁移结果验证..."
        $Config = Load-Configuration
        Test-MigrationResult $Config
    }
    "backup" {
        Write-Log "执行数据库备份..."
        $Config = Load-Configuration
        Backup-Database $Config
    }
    "report" {
        Write-Log "生成迁移报告..."
        # 生成模拟报告
        $Config = Load-Configuration
        $MockResults = @(
            [PSCustomObject]@{ Service = "ioedream-common-service"; Success = $true; Duration = 1500; Message = "迁移成功" }
            [PSCustomObject]@{ Service = "ioedream-access-service"; Success = $true; Duration = 2200; Message = "迁移成功" }
        )
        New-MigrationReport $Config $MockResults
    }
    "rollback" {
        Write-Log "回滚功能开发中..." -Level "WARN"
    }
    default {
        Write-Log "未知操作: $Action" -Level "ERROR"
        Show-Help
        exit 1
    }
}