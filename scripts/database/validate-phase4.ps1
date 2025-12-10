# =====================================================
# IOE-DREAM 阶段4：验证所有服务
# 版本: v1.0.0
# 描述: 综合验证所有微服务数据库配置、初始化流程和版本历史
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
    [string]$Password = "",

    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport
)

$ErrorActionPreference = "Stop"

# =====================================================
# 配置
# =====================================================

$Services = @(
    @{ Name = "ioedream-common-service"; Port = 8088; Type = "核心" },
    @{ Name = "ioedream-device-comm-service"; Port = 8087; Type = "核心" },
    @{ Name = "ioedream-oa-service"; Port = 8089; Type = "核心" },
    @{ Name = "ioedream-access-service"; Port = 8090; Type = "业务" },
    @{ Name = "ioedream-attendance-service"; Port = 8091; Type = "业务" },
    @{ Name = "ioedream-consume-service"; Port = 8094; Type = "业务" },
    @{ Name = "ioedream-visitor-service"; Port = 8095; Type = "业务" },
    @{ Name = "ioedream-video-service"; Port = 8092; Type = "业务" },
    @{ Name = "ioedream-gateway-service"; Port = 8080; Type = "基础设施" }
)

$ExpectedDatabase = "ioedream"
$ConfigFiles = @(
    "application.yml",
    "application-docker.yml",
    "bootstrap.yml"
)

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

function Test-DatabaseConfig {
    param(
        [string]$ServiceName,
        [string]$ConfigFile
    )

    $configPath = "microservices\$ServiceName\src\main\resources\$ConfigFile"

    if (-not (Test-Path $configPath)) {
        return @{ Found = $false; Database = ""; Status = "文件不存在" }
    }

    $content = Get-Content $configPath -Raw -ErrorAction SilentlyContinue
    if (-not $content) {
        return @{ Found = $false; Database = ""; Status = "文件读取失败" }
    }

    # 检查数据库URL
    if ($content -match 'jdbc:mysql://[^/]+/([^?]+)') {
        $dbName = $matches[1]

        # 检查是否是环境变量
        if ($dbName -match '\$\{.*DATABASE.*\}') {
            # 检查环境变量默认值
            if ($content -match '\$\{.*DATABASE.*:([^}]+)\}') {
                $defaultDb = $matches[1]
                if ($defaultDb -eq $ExpectedDatabase) {
                    return @{ Found = $true; Database = $defaultDb; Status = "✅ 正确（环境变量默认值）" }
                }
            }
            return @{ Found = $true; Database = $dbName; Status = "⚠️  环境变量（需验证）" }
        }

        if ($dbName -eq $ExpectedDatabase) {
            return @{ Found = $true; Database = $dbName; Status = "✅ 正确" }
        } else {
            return @{ Found = $true; Database = $dbName; Status = "❌ 不一致" }
        }
    }

    # 检查Nacos配置引用
    if ($content -match 'common-database\.yaml') {
        return @{ Found = $true; Database = "Nacos配置"; Status = "✅ 使用Nacos配置" }
    }

    return @{ Found = $false; Database = ""; Status = "未找到数据库配置" }
}

function Get-MigrationHistory {
    $query = @"
SELECT
    version,
    description,
    script_name,
    status,
    start_time,
    end_time,
    create_time
FROM t_migration_history
ORDER BY create_time DESC
LIMIT 20;
"@

    try {
        $result = & $MYSQL_CMD $MYSQL_ARGS $Database -e $query 2>&1
        if ($LASTEXITCODE -eq 0) {
            return $result
        }
        return $null
    } catch {
        return $null
    }
}

function Get-DatabaseStatus {
    $queries = @{
        "表数量" = "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = '$Database' AND table_type = 'BASE TABLE';"
        "迁移记录数" = "SELECT COUNT(*) as count FROM t_migration_history WHERE status = 'SUCCESS';"
        "当前版本" = "SELECT COALESCE(MAX(version), 'V0.0.0') as version FROM t_migration_history WHERE status = 'SUCCESS';"
    }

    $status = @{}

    foreach ($key in $queries.Keys) {
        try {
            $result = & $MYSQL_CMD $MYSQL_ARGS $Database -e $queries[$key] 2>&1
            if ($LASTEXITCODE -eq 0) {
                $status[$key] = $result
            } else {
                $status[$key] = "查询失败"
            }
        } catch {
            $status[$key] = "查询异常"
        }
    }

    return $status
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput Cyan "`n=========================================="
Write-ColorOutput Cyan "  IOE-DREAM 阶段4：验证所有服务"
Write-ColorOutput Cyan "=========================================="
Write-Output ""

$report = @{
    Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Services = @()
    DatabaseStatus = $null
    MigrationHistory = $null
    Issues = @()
    Summary = @{
        TotalServices = $Services.Count
        ValidServices = 0
        InvalidServices = 0
        DatabaseValid = $false
        MigrationValid = $false
    }
}

# =====================================================
# 1. 验证所有微服务数据库配置
# =====================================================

Write-ColorOutput Cyan "`n[1/3] 验证所有微服务数据库配置"
Write-Output "=========================================="

$totalServices = $Services.Count
$validServices = 0
$serviceIssues = @()

foreach ($service in $Services) {
    Write-Output "`n检查服务: $($service.Name) (端口: $($service.Port), 类型: $($service.Type))"

    $found = $false
    $serviceConfigIssues = @()
    $serviceReport = @{
        Name = $service.Name
        Port = $service.Port
        Type = $service.Type
        Configs = @()
        Valid = $false
    }

    foreach ($configFile in $ConfigFiles) {
        $result = Test-DatabaseConfig -ServiceName $service.Name -ConfigFile $configFile

        if ($result.Found) {
            Write-Output "  $configFile`: $($result.Status) - $($result.Database)"

            $serviceReport.Configs += @{
                File = $configFile
                Status = $result.Status
                Database = $result.Database
            }

            if ($result.Status -match "✅") {
                $found = $true
            } elseif ($result.Status -match "❌") {
                $serviceConfigIssues += "$configFile`: 数据库名不一致 ($($result.Database))"
            }
        }
    }

    if ($found) {
        $validServices++
        $serviceReport.Valid = $true
        Write-ColorOutput Green "  ✅ 服务配置正确"
    } else {
        $serviceReport.Valid = $false
        Write-ColorOutput Yellow "  ⚠️  服务配置需要验证"
        $serviceIssues += "$($service.Name): $($serviceConfigIssues -join ', ')"
    }

    $report.Services += $serviceReport
}

$report.Summary.ValidServices = $validServices
$report.Summary.InvalidServices = $totalServices - $validServices

Write-Output "`n配置验证结果: $validServices/$totalServices 服务配置正确"

# =====================================================
# 2. 验证数据库初始化流程
# =====================================================

Write-ColorOutput Cyan "`n[2/3] 验证数据库初始化流程"
Write-Output "=========================================="

$dbStatus = Get-DatabaseStatus

Write-Output "`n数据库状态:"
foreach ($key in $dbStatus.Keys) {
    $value = $dbStatus[$key]
    if ($value -match "count\s+(\d+)") {
        $count = $matches[1]
        Write-Output "  $key`: $count"
    } elseif ($value -match "version\s+(V\d+\.\d+\.\d+)") {
        $version = $matches[1]
        Write-Output "  $key`: $version"
    } else {
        Write-Output "  $key`: $value"
    }
}

# 验证数据库表数量
$tableCount = 0
if ($dbStatus["表数量"] -match "count\s+(\d+)") {
    $tableCount = [int]$matches[1]
    if ($tableCount -gt 0) {
        Write-ColorOutput Green "  ✅ 数据库表已创建 ($tableCount 个表)"
        $report.Summary.DatabaseValid = $true
    } else {
        Write-ColorOutput Red "  ❌ 数据库表未创建"
        $report.Issues += "数据库表数量为0"
    }
} else {
    Write-ColorOutput Yellow "  ⚠️  无法查询数据库表数量"
    $report.Issues += "无法查询数据库表数量"
}

# 验证迁移记录
$migrationCount = 0
if ($dbStatus["迁移记录数"] -match "count\s+(\d+)") {
    $migrationCount = [int]$matches[1]
    if ($migrationCount -gt 0) {
        Write-ColorOutput Green "  ✅ 迁移记录存在 ($migrationCount 条记录)"
    } else {
        Write-ColorOutput Yellow "  ⚠️  无迁移记录"
        $report.Issues += "无迁移记录"
    }
}

$report.DatabaseStatus = $dbStatus

# =====================================================
# 3. 验证版本历史记录
# =====================================================

Write-ColorOutput Cyan "`n[3/3] 验证版本历史记录"
Write-Output "=========================================="

$history = Get-MigrationHistory

if ($history) {
    Write-Output "`n迁移历史记录:"
    Write-Output $history

    # 检查版本格式
    $versionPattern = "V\d+\.\d+\.\d+"
    $validVersions = ($history | Select-String -Pattern $versionPattern).Matches.Count

    if ($validVersions -gt 0) {
        Write-ColorOutput Green "  ✅ 版本历史记录格式正确"
        $report.Summary.MigrationValid = $true
    } else {
        Write-ColorOutput Yellow "  ⚠️  版本格式需要验证"
        $report.Issues += "版本历史记录格式可能不正确"
    }

    $report.MigrationHistory = $history
} else {
    Write-ColorOutput Yellow "  ⚠️  无法获取迁移历史记录"
    $report.Issues += "无法获取迁移历史记录"
}

# =====================================================
# 总结报告
# =====================================================

Write-ColorOutput Cyan "`n=========================================="
Write-ColorOutput Cyan "  验证结果总结"
Write-ColorOutput Cyan "=========================================="
Write-Output ""

Write-Output "微服务配置验证: $($report.Summary.ValidServices)/$($report.Summary.TotalServices) 服务配置正确"
Write-Output "数据库初始化: $(if ($report.Summary.DatabaseValid) { '✅ 通过' } else { '❌ 失败' })"
Write-Output "版本历史记录: $(if ($report.Summary.MigrationValid) { '✅ 通过' } else { '⚠️  需要验证' })"
Write-Output ""

if ($report.Issues.Count -eq 0) {
    Write-ColorOutput Green "✅ 所有验证通过！"
} else {
    Write-ColorOutput Yellow "发现 $($report.Issues.Count) 个问题:"
    foreach ($issue in $report.Issues) {
        Write-Output "  - $issue"
    }
}

# =====================================================
# 生成报告
# =====================================================

if ($GenerateReport) {
    $reportPath = "documentation\technical\DATABASE_VALIDATION_REPORT_$(Get-Date -Format 'yyyyMMdd_HHmmss').json"
    $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Output "`n验证报告已保存: $reportPath"
}

Write-Output ""

