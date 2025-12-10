# =====================================================
# IOE-DREAM 数据库版本管理工具
# 版本: v1.0.0
# 描述: 统一的数据库版本管理脚本，支持版本查询、升级、回滚
# 创建时间: 2025-12-10
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("status", "upgrade", "rollback", "history", "validate")]
    [string]$Action = "status",

    [Parameter(Mandatory=$false)]
    [string]$Version = "",

    [Parameter(Mandatory=$false)]
    [string]$Database = "ioedream",

    [Parameter(Mandatory=$false)]
    [string]$Host = "localhost",

    [Parameter(Mandatory=$false)]
    [int]$Port = 3306,

    [Parameter(Mandatory=$false)]
    [string]$Username = "root",

    [Parameter(Mandatory=$false)]
    [string]$Password = ""
)

# =====================================================
# 配置
# =====================================================

$ErrorActionPreference = "Stop"

# MySQL连接参数
$MYSQL_CMD = "mysql"
$MYSQL_ARGS = @(
    "-h", $Host,
    "-P", $Port.ToString(),
    "-u", $Username
)

if ($Password) {
    $MYSQL_ARGS += "-p$Password"
}

# =====================================================
# 辅助函数
# =====================================================

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Execute-MySQLQuery {
    param(
        [string]$Query,
        [string]$Database = ""
    )

    $dbArg = if ($Database) { $Database } else { "" }

    try {
        $result = & $MYSQL_CMD $MYSQL_ARGS $dbArg -e $Query 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput Red "MySQL执行错误: $result"
            return $null
        }
        return $result
    } catch {
        Write-ColorOutput Red "执行MySQL查询失败: $_"
        return $null
    }
}

function Get-CurrentVersion {
    $query = @"
SELECT
    COALESCE(MAX(version), 'V0.0.0') as current_version,
    COUNT(*) as executed_migrations,
    MAX(end_time) as last_migration_time
FROM t_migration_history
WHERE status = 'SUCCESS';
"@

    $result = Execute-MySQLQuery -Query $query -Database $Database
    return $result
}

function Get-MigrationHistory {
    param(
        [int]$Limit = 20
    )

    $query = @"
SELECT
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    TIMESTAMPDIFF(SECOND, start_time, end_time) as duration_seconds
FROM t_migration_history
ORDER BY end_time DESC
LIMIT $Limit;
"@

    $result = Execute-MySQLQuery -Query $query -Database $Database
    return $result
}

function Validate-VersionFormat {
    param([string]$Version)

    if ($Version -match '^V\d+\.\d+\.\d+(-[A-Z]+)?$') {
        return $true
    }
    return $false
}

function Compare-Versions {
    param(
        [string]$Version1,
        [string]$Version2
    )

    # 提取版本号部分（去掉V和可能的后缀）
    $v1 = ($Version1 -replace '^V', '') -replace '-.*$', ''
    $v2 = ($Version2 -replace '^V', '') -replace '-.*$', ''

    $parts1 = $v1.Split('.')
    $parts2 = $v2.Split('.')

    for ($i = 0; $i -lt [Math]::Max($parts1.Length, $parts2.Length); $i++) {
        $p1 = if ($i -lt $parts1.Length) { [int]$parts1[$i] } else { 0 }
        $p2 = if ($i -lt $parts2.Length) { [int]$parts2[$i] } else { 0 }

        if ($p1 -lt $p2) { return -1 }
        if ($p1 -gt $p2) { return 1 }
    }

    return 0
}

# =====================================================
# 主功能函数
# =====================================================

function Show-Status {
    Write-ColorOutput Cyan "`n=========================================="
    Write-ColorOutput Cyan "  数据库版本状态"
    Write-ColorOutput Cyan "=========================================="
    Write-Output ""

    Write-Output "数据库: $Database"
    Write-Output "主机: ${Host}:${Port}"
    Write-Output ""

    $versionInfo = Get-CurrentVersion
    if ($versionInfo) {
        Write-ColorOutput Green "当前版本: $($versionInfo -split "`t" | Select-Object -First 1)"
        Write-Output "已执行迁移: $($versionInfo -split "`t" | Select-Object -Skip 1 -First 1)"
        Write-Output "最后迁移时间: $($versionInfo -split "`t" | Select-Object -Skip 2 -First 1)"
    } else {
        Write-ColorOutput Red "无法获取版本信息"
    }

    Write-Output ""
}

function Show-History {
    Write-ColorOutput Cyan "`n=========================================="
    Write-ColorOutput Cyan "  迁移历史记录"
    Write-ColorOutput Cyan "=========================================="
    Write-Output ""

    $history = Get-MigrationHistory -Limit 20
    if ($history) {
        Write-Output $history
    } else {
        Write-ColorOutput Yellow "暂无迁移历史记录"
    }

    Write-Output ""
}

function Validate-Migrations {
    Write-ColorOutput Cyan "`n=========================================="
    Write-ColorOutput Cyan "  验证迁移脚本"
    Write-ColorOutput Cyan "=========================================="
    Write-Output ""

    $scriptDir = "deployment\mysql\init"
    $scripts = Get-ChildItem -Path $scriptDir -Filter "*.sql" | Sort-Object Name

    $issues = @()
    $validCount = 0

    foreach ($script in $scripts) {
        $content = Get-Content $script.FullName -Raw

        # 检查版本号格式
        if ($content -match "version.*['`"]V(\d+)_(\d+)_(\d+)") {
            $issues += "❌ $($script.Name): 使用旧版本格式 V$($matches[1])_$($matches[2])_$($matches[3])，应使用 V$($matches[1]).$($matches[2]).$($matches[3])"
        } elseif ($content -match "version.*['`"]V(\d+)\.(\d+)\.(\d+)") {
            $validCount++
            Write-Output "✅ $($script.Name): 版本格式正确"
        }

        # 检查迁移历史记录格式
        if ($content -match "INSERT INTO t_migration_history") {
            if ($content -notmatch "script_name") {
                $issues += "⚠️  $($script.Name): 缺少script_name字段"
            }
        }

        # 检查USE语句
        if ($script.Name -notmatch "nacos" -and $content -notmatch "USE ioedream") {
            $issues += "⚠️  $($script.Name): 缺少USE ioedream语句"
        }
    }

    Write-Output ""
    Write-Output "验证结果: $validCount/$($scripts.Count) 脚本格式正确"

    if ($issues.Count -gt 0) {
        Write-ColorOutput Yellow "`n发现问题:"
        foreach ($issue in $issues) {
            Write-Output "  $issue"
        }
    } else {
        Write-ColorOutput Green "`n✅ 所有脚本验证通过！"
    }

    Write-Output ""
}

function Validate-ServiceConfigs {
    Write-ColorOutput Cyan "`n=========================================="
    Write-ColorOutput Cyan "  验证微服务数据库配置"
    Write-ColorOutput Cyan "=========================================="
    Write-Output ""

    $services = @(
        "ioedream-common-service",
        "ioedream-access-service",
        "ioedream-attendance-service",
        "ioedream-consume-service",
        "ioedream-visitor-service",
        "ioedream-video-service",
        "ioedream-device-comm-service",
        "ioedream-oa-service",
        "ioedream-gateway-service"
    )

    $issues = @()
    $validCount = 0

    foreach ($service in $services) {
        $configFiles = @(
            "microservices\$service\src\main\resources\application.yml",
            "microservices\$service\src\main\resources\application-docker.yml",
            "microservices\$service\src\main\resources\bootstrap.yml"
        )

        $found = $false
        foreach ($configFile in $configFiles) {
            if (Test-Path $configFile) {
                $content = Get-Content $configFile -Raw

                # 检查数据库名
                if ($content -match 'jdbc:mysql://[^/]+/([^?]+)') {
                    $dbName = $matches[1]
                    if ($dbName -eq "ioedream" -or $dbName -match '\$\{.*DATABASE.*ioedream\}') {
                        $found = $true
                        Write-Output "✅ $service`: 数据库配置正确 ($dbName)"
                        $validCount++
                        break
                    } elseif ($dbName -notmatch "ioedream") {
                        $issues += "⚠️  $service`: 数据库名不一致 ($dbName)"
                    }
                }
            }
        }

        if (-not $found) {
            $issues += "⚠️  $service`: 未找到数据库配置"
        }
    }

    Write-Output ""
    Write-Output "验证结果: $validCount/$($services.Count) 服务配置正确"

    if ($issues.Count -gt 0) {
        Write-ColorOutput Yellow "`n发现问题:"
        foreach ($issue in $issues) {
            Write-Output "  $issue"
        }
    } else {
        Write-ColorOutput Green "`n✅ 所有服务配置验证通过！"
    }

    Write-Output ""
}

# =====================================================
# 版本脚本管理
# =====================================================

function Load-VersionScripts {
    $versionDir = Join-Path $PSScriptRoot "versions"
    $versionScripts = @{}

    if (Test-Path $versionDir) {
        $scripts = Get-ChildItem -Path $versionDir -Filter "version-*.ps1"
        foreach ($script in $scripts) {
            $version = $script.BaseName -replace "version-", ""
            $version = $version -replace "-", "."
            $versionScripts[$version] = $script.FullName
        }
    }

    return $versionScripts
}

function Invoke-VersionUpgrade {
    param(
        [string]$TargetVersion = ""
    )

    Write-ColorOutput Cyan "`n=========================================="
    Write-ColorOutput Cyan "  执行版本升级"
    Write-ColorOutput Cyan "=========================================="
    Write-Output ""

    # 加载版本脚本
    $versionScripts = Load-VersionScripts

    # 获取当前版本
    $currentVersionResult = Get-CurrentVersion
    $currentVersion = "V0.0.0"
    if ($currentVersionResult) {
        $lines = $currentVersionResult -split "`n"
        foreach ($line in $lines) {
            if ($line -match "V\d+\.\d+\.\d+") {
                $currentVersion = $matches[0]
                break
            }
        }
    }

    Write-Output "当前版本: $currentVersion"
    Write-Output ""

    # 准备配置
    $config = @{
        Args = $MYSQL_ARGS
        Database = $Database
    }

    # 定义版本执行顺序
    $versionOrder = @(
        "v1.0.0",
        "v1.1.0",
        "v1.0.1",
        "v2.0.0",
        "v2.0.1",
        "v2.0.2",
        "v2.1.0"
    )

    $successCount = 0
    $failCount = 0

    foreach ($version in $versionOrder) {
        $versionUpper = $version.ToUpper()

        # 如果指定了目标版本，检查是否需要执行
        if ($TargetVersion -and $versionUpper -gt $TargetVersion) {
            Write-Output "跳过版本 $versionUpper (超过目标版本 $TargetVersion)"
            continue
        }

        # 检查版本是否已执行
        $checkQuery = "SELECT COUNT(*) as count FROM t_migration_history WHERE version = '$versionUpper' AND status = 'SUCCESS';"
        $checkResult = Execute-MySQLQuery -Query $checkQuery -Database $Database

        if ($checkResult -match "count\s+1") {
            Write-Output "✅ 版本 $versionUpper 已执行，跳过"
            continue
        }

        # 加载并执行版本脚本
        if ($versionScripts.ContainsKey($version)) {
            $scriptPath = $versionScripts[$version]
            Write-Output "执行版本脚本: $scriptPath"

            try {
                # 导入模块
                $module = Import-Module $scriptPath -PassThru -Force

                # 调用对应的函数
                $functionName = "Invoke-Version$($version -replace '\.', '')"
                if (Get-Command $functionName -ErrorAction SilentlyContinue) {
                    $result = & $functionName -Config $config
                    if ($result) {
                        $successCount++
                    } else {
                        $failCount++
                        Write-ColorOutput Red "版本 $versionUpper 执行失败"
                        break
                    }
                } else {
                    Write-ColorOutput Yellow "未找到函数: $functionName"
                    $failCount++
                }

                # 移除模块
                Remove-Module $module -ErrorAction SilentlyContinue
            } catch {
                Write-ColorOutput Red "执行版本脚本失败: $_"
                $failCount++
                break
            }
        } else {
            Write-ColorOutput Yellow "未找到版本脚本: $version"
        }
    }

    Write-Output ""
    Write-Output "升级完成: 成功 $successCount, 失败 $failCount"
    Write-Output ""
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput Cyan "`n=========================================="
Write-ColorOutput Cyan "  IOE-DREAM 数据库版本管理工具"
Write-ColorOutput Cyan "=========================================="
Write-Output ""

switch ($Action) {
    "status" {
        Show-Status
    }
    "history" {
        Show-History
    }
    "upgrade" {
        Invoke-VersionUpgrade -TargetVersion $Version
    }
    "validate" {
        Validate-Migrations
        Validate-ServiceConfigs
    }
    default {
        Write-ColorOutput Yellow "未知操作: $Action"
        Write-Output ""
        Write-Output "可用操作:"
        Write-Output "  status   - 显示当前版本状态"
        Write-Output "  history  - 显示迁移历史"
        Write-Output "  upgrade  - 执行版本升级"
        Write-Output "  validate - 验证脚本和服务配置"
        Write-Output ""
    }
}
