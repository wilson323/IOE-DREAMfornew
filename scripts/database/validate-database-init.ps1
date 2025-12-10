# =====================================================
# IOE-DREAM 数据库初始化流程验证脚本
# 版本: v1.0.0
# 描述: 验证数据库初始化流程的完整性和正确性
# 创建时间: 2025-12-10
# =====================================================

param(
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

$ErrorActionPreference = "Stop"

# =====================================================
# 配置
# =====================================================

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
            return $null
        }
        return $result
    } catch {
        return $null
    }
}

# =====================================================
# 验证函数
# =====================================================

function Test-DatabaseExists {
    $query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$Database';"
    $result = Execute-MySQLQuery -Query $query
    if ($result -and ($result -split "`t" | Select-Object -Last 1) -eq "1") {
        return $true
    }
    return $false
}

function Get-TableCount {
    $query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '$Database' AND TABLE_TYPE = 'BASE TABLE';"
    $result = Execute-MySQLQuery -Query $query -Database $Database
    if ($result) {
        return [int]($result -split "`t" | Select-Object -Last 1)
    }
    return 0
}

function Get-MigrationHistoryCount {
    $query = "SELECT COUNT(*) FROM t_migration_history WHERE status = 'SUCCESS';"
    $result = Execute-MySQLQuery -Query $query -Database $Database
    if ($result) {
        return [int]($result -split "`t" | Select-Object -Last 1)
    }
    return 0
}

function Get-CurrentVersion {
    $query = @"
SELECT
    COALESCE(MAX(version), 'V0.0.0') as current_version
FROM t_migration_history
WHERE status = 'SUCCESS';
"@
    $result = Execute-MySQLQuery -Query $query -Database $Database
    if ($result) {
        $lines = $result -split "`n" | Where-Object { $_ -match "V\d+\.\d+\.\d+" }
        if ($lines) {
            return ($lines[0] -split "`t" | Select-Object -First 1)
        }
    }
    return "V0.0.0"
}

function Test-RequiredTables {
    $requiredTables = @(
        "t_common_user",
        "t_common_department",
        "t_common_area",
        "t_common_device",
        "t_common_dict_type",
        "t_common_dict_data",
        "t_common_role",
        "t_common_permission",
        "t_migration_history"
    )

    $missingTables = @()

    foreach ($table in $requiredTables) {
        $query = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '$Database' AND TABLE_NAME = '$table';"
        $result = Execute-MySQLQuery -Query $query -Database $Database
        if (-not $result -or ($result -split "`t" | Select-Object -Last 1) -eq "0") {
            $missingTables += $table
        }
    }

    return $missingTables
}

function Test-RequiredData {
    $checks = @()

    # 检查字典类型
    $query = "SELECT COUNT(*) FROM t_common_dict_type WHERE deleted_flag = 0;"
    $result = Execute-MySQLQuery -Query $query -Database $Database
    $dictTypeCount = if ($result) { [int]($result -split "`t" | Select-Object -Last 1) } else { 0 }
    $checks += @{ Name = "字典类型"; Count = $dictTypeCount; Expected = 10; Status = ($dictTypeCount -ge 10) }

    # 检查用户
    $query = "SELECT COUNT(*) FROM t_common_user WHERE deleted_flag = 0;"
    $result = Execute-MySQLQuery -Query $query -Database $Database
    $userCount = if ($result) { [int]($result -split "`t" | Select-Object -Last 1) } else { 0 }
    $checks += @{ Name = "用户"; Count = $userCount; Expected = 1; Status = ($userCount -ge 1) }

    # 检查角色
    $query = "SELECT COUNT(*) FROM t_common_role WHERE deleted_flag = 0;"
    $result = Execute-MySQLQuery -Query $query -Database $Database
    $roleCount = if ($result) { [int]($result -split "`t" | Select-Object -Last 1) } else { 0 }
    $checks += @{ Name = "角色"; Count = $roleCount; Expected = 3; Status = ($roleCount -ge 3) }

    return $checks
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput Cyan "`n=========================================="
Write-ColorOutput Cyan "  IOE-DREAM 数据库初始化流程验证"
Write-ColorOutput Cyan "=========================================="
Write-Output ""

Write-Output "数据库: $Database"
Write-Output "主机: ${Host}:${Port}"
Write-Output ""

# 1. 验证数据库存在
Write-ColorOutput Cyan "[1/5] 验证数据库存在..."
if (Test-DatabaseExists) {
    Write-ColorOutput Green "  ✅ 数据库存在"
} else {
    Write-ColorOutput Red "  ❌ 数据库不存在"
    exit 1
}
Write-Output ""

# 2. 验证表结构
Write-ColorOutput Cyan "[2/5] 验证表结构..."
$tableCount = Get-TableCount
Write-Output "  表数量: $tableCount"

if ($tableCount -lt 20) {
    Write-ColorOutput Red "  ❌ 表数量不足（期望≥20，实际$tableCount）"
} else {
    Write-ColorOutput Green "  ✅ 表数量正常"
}

$missingTables = Test-RequiredTables
if ($missingTables.Count -gt 0) {
    Write-ColorOutput Yellow "  ⚠️  缺少必需表: $($missingTables -join ', ')"
} else {
    Write-ColorOutput Green "  ✅ 所有必需表存在"
}
Write-Output ""

# 3. 验证基础数据
Write-ColorOutput Cyan "[3/5] 验证基础数据..."
$dataChecks = Test-RequiredData
foreach ($check in $dataChecks) {
    $status = if ($check.Status) { "✅" } else { "❌" }
    Write-Output "  $status $($check.Name): $($check.Count) (期望≥$($check.Expected))"
}
Write-Output ""

# 4. 验证迁移历史
Write-ColorOutput Cyan "[4/5] 验证迁移历史..."
$migrationCount = Get-MigrationHistoryCount
Write-Output "  迁移记录数: $migrationCount"

if ($migrationCount -lt 1) {
    Write-ColorOutput Yellow "  ⚠️  迁移历史为空（可能是首次初始化）"
} else {
    Write-ColorOutput Green "  ✅ 迁移历史存在"
}

$currentVersion = Get-CurrentVersion
Write-Output "  当前版本: $currentVersion"
Write-Output ""

# 5. 验证版本历史记录
Write-ColorOutput Cyan "[5/5] 验证版本历史记录..."
$query = @"
SELECT
    version,
    description,
    script_name,
    status,
    DATE_FORMAT(create_time, '%Y-%m-%d %H:%i:%s') as create_time
FROM t_migration_history
ORDER BY create_time DESC
LIMIT 10;
"@

$history = Execute-MySQLQuery -Query $query -Database $Database
if ($history) {
    Write-Output "  最近10条迁移记录:"
    $lines = $history -split "`n" | Where-Object { $_ -match "V\d+\.\d+\.\d+" }
    foreach ($line in $lines) {
        Write-Output "    $line"
    }
    Write-ColorOutput Green "  ✅ 版本历史记录完整"
} else {
    Write-ColorOutput Yellow "  ⚠️  无法获取版本历史记录"
}
Write-Output ""

# =====================================================
# 总结
# =====================================================

Write-ColorOutput Cyan "=========================================="
Write-ColorOutput Cyan "  验证结果总结"
Write-ColorOutput Cyan "=========================================="
Write-Output ""

$allPassed = $true

if (-not (Test-DatabaseExists)) {
    Write-ColorOutput Red "❌ 数据库不存在"
    $allPassed = $false
}

if ($tableCount -lt 20) {
    Write-ColorOutput Red "❌ 表数量不足"
    $allPassed = $false
}

if ($missingTables.Count -gt 0) {
    Write-ColorOutput Red "❌ 缺少必需表"
    $allPassed = $false
}

foreach ($check in $dataChecks) {
    if (-not $check.Status) {
        Write-ColorOutput Red "❌ $($check.Name)数据不足"
        $allPassed = $false
    }
}

if ($allPassed) {
    Write-ColorOutput Green "✅ 数据库初始化流程验证通过！"
    Write-Output ""
    Write-Output "数据库状态:"
    Write-Output "  - 数据库: $Database"
    Write-Output "  - 表数量: $tableCount"
    Write-Output "  - 迁移记录: $migrationCount"
    Write-Output "  - 当前版本: $currentVersion"
} else {
    Write-ColorOutput Red "❌ 数据库初始化流程验证失败，请检查上述问题"
}

Write-Output ""

