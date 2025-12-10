# =====================================================
# IOE-DREAM 数据库快速测试脚本
# 功能: 快速验证P2级优化功能（版本管理、环境隔离、性能优化）
# 版本: v1.0.0
# 创建时间: 2025-12-10
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('version', 'environment', 'performance', 'all')]
    [string]$TestType = 'all',

    [string]$Database = 'ioedream',
    [string]$DbHost = 'localhost',
    [int]$Port = 3306,
    [string]$Username = 'root',
    [System.Security.SecureString]$Password = (ConvertTo-SecureString -String '123456' -AsPlainText -Force)
)

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = 'White'
    )
    Write-Host $Message -ForegroundColor $Color
}

# 连接MySQL并执行SQL
function Invoke-MySQLQuery {
    param(
        [string]$Query
    )

    $mysqlPath = "mysql"
    if (Get-Command mysql -ErrorAction SilentlyContinue) {
        $mysqlPath = "mysql"
    } elseif (Test-Path "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe") {
        $mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    } else {
        Write-ColorOutput "  错误: 未找到mysql命令" "Red"
        return $null
    }

    $queryFile = [System.IO.Path]::GetTempFileName()
    $Query | Out-File -FilePath $queryFile -Encoding UTF8

    try {
        # 将 SecureString 转换为普通字符串（仅用于环境变量）
        $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($Password)
        $plainPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
        $env:MYSQL_PWD = $plainPassword
        $result = & $mysqlPath -h$DbHost -P$Port -u$Username $Database -N -e $Query 2>&1
        $exitCode = $LASTEXITCODE

        if ($exitCode -eq 0) {
            return $result
        } else {
            return $null
        }
    } finally {
        Remove-Item $queryFile -ErrorAction SilentlyContinue
        Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
    }
}

# 测试版本管理功能
function Test-VersionManagement {
    Write-ColorOutput "`n[测试1] 版本管理功能测试" "Cyan"
    Write-ColorOutput "==========================================" "Cyan"

    $tests = @()

    # 测试1.1: 检查版本检查函数
    Write-ColorOutput "`n[1.1] 检查版本检查函数..." "Yellow"
    $versionFunc = Invoke-MySQLQuery -Query "SELECT get_current_version();"
    if ($versionFunc) {
        $version = $versionFunc.Trim()
        Write-ColorOutput "  ✓ 版本检查函数正常，当前版本: $version" "Green"
        $tests += @{Name="版本检查函数"; Status="✓"; Value=$version}
    } else {
        Write-ColorOutput "  ✗ 版本检查函数不存在或执行失败" "Red"
        $tests += @{Name="版本检查函数"; Status="✗"}
    }

    # 测试1.2: 检查迁移历史表
    Write-ColorOutput "`n[1.2] 检查迁移历史表..." "Yellow"
    $tableExists = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$Database' AND TABLE_NAME='t_migration_history';"
    if ($tableExists -and [int]$tableExists.Trim() -gt 0) {
        Write-ColorOutput "  ✓ 迁移历史表存在" "Green"
        $tests += @{Name="迁移历史表"; Status="✓"}
    } else {
        Write-ColorOutput "  ✗ 迁移历史表不存在" "Red"
        $tests += @{Name="迁移历史表"; Status="✗"}
    }

    # 测试1.3: 检查迁移记录
    Write-ColorOutput "`n[1.3] 检查迁移记录..." "Yellow"
    $migrationCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM t_migration_history WHERE status = 'SUCCESS';"
    if ($migrationCount) {
        $count = [int]$migrationCount.Trim()
        Write-ColorOutput "  ✓ 成功迁移记录: $count 条" "Green"
        $tests += @{Name="迁移记录"; Status="✓"; Value="$count 条"}
    } else {
        Write-ColorOutput "  ⚠ 无迁移记录" "Yellow"
        $tests += @{Name="迁移记录"; Status="⚠"}
    }

    # 测试1.4: 检查版本管理脚本
    Write-ColorOutput "`n[1.4] 检查版本管理脚本..." "Yellow"
    $scriptPath = "scripts\database\version-manager.ps1"
    if (Test-Path $scriptPath) {
        Write-ColorOutput "  ✓ 版本管理脚本存在: $scriptPath" "Green"
        $tests += @{Name="版本管理脚本"; Status="✓"}
    } else {
        Write-ColorOutput "  ✗ 版本管理脚本不存在: $scriptPath" "Red"
        $tests += @{Name="版本管理脚本"; Status="✗"}
    }

    # 显示测试结果
    Write-ColorOutput "`n版本管理测试结果:" "Cyan"
    foreach ($test in $tests) {
        $status = $test.Status
        $name = $test.Name
        $value = if ($test.Value) { " ($($test.Value))" } else { "" }
        Write-ColorOutput "  $status $name$value" $(if ($status -eq "✓") { "Green" } elseif ($status -eq "✗") { "Red" } else { "Yellow" })
    }

    $failedTests = $tests | Where-Object { $_.Status -eq "✗" }
    return ($failedTests.Count -eq 0)
}

# 测试环境隔离功能
function Test-EnvironmentIsolation {
    Write-ColorOutput "`n[测试2] 环境隔离功能测试" "Cyan"
    Write-ColorOutput "==========================================" "Cyan"

    $tests = @()

    # 测试2.1: 检查环境变量
    Write-ColorOutput "`n[2.1] 检查环境变量..." "Yellow"
    $env = $env:ENVIRONMENT
    if ($env) {
        Write-ColorOutput "  ✓ 环境变量ENVIRONMENT: $env" "Green"
        $tests += @{Name="环境变量"; Status="✓"; Value=$env}
    } else {
        Write-ColorOutput "  ⚠ 环境变量ENVIRONMENT未设置（默认: dev）" "Yellow"
        $tests += @{Name="环境变量"; Status="⚠"; Value="未设置"}
    }

    # 测试2.2: 检查环境特定数据脚本
    Write-ColorOutput "`n[2.2] 检查环境特定数据脚本..." "Yellow"
    $envScripts = @(
        "deployment\mysql\init\02-ioedream-data-dev.sql",
        "deployment\mysql\init\02-ioedream-data-test.sql",
        "deployment\mysql\init\02-ioedream-data-prod.sql"
    )

    $allExist = $true
    foreach ($script in $envScripts) {
        if (Test-Path $script) {
            Write-ColorOutput "  ✓ $script" "Green"
        } else {
            Write-ColorOutput "  ✗ $script 不存在" "Red"
            $allExist = $false
        }
    }

    if ($allExist) {
        $tests += @{Name="环境脚本"; Status="✓"}
    } else {
        $tests += @{Name="环境脚本"; Status="✗"}
    }

    # 测试2.3: 检查测试用户（环境隔离验证）
    Write-ColorOutput "`n[2.3] 检查测试用户（环境隔离验证）..." "Yellow"
    $testUserCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM t_common_user WHERE username LIKE 'test_%' OR username LIKE 'qa_%';"
    if ($testUserCount) {
        $count = [int]$testUserCount.Trim()
        if ($count -gt 0) {
            Write-ColorOutput "  ✓ 测试用户数量: $count" "Green"
            $tests += @{Name="测试用户"; Status="✓"; Value="$count 个"}
        } else {
            Write-ColorOutput "  ⚠ 无测试用户（可能在生产环境）" "Yellow"
            $tests += @{Name="测试用户"; Status="⚠"; Value="0 个"}
        }
    }

    # 测试2.4: 检查Docker Compose环境配置
    Write-ColorOutput "`n[2.4] 检查Docker Compose环境配置..." "Yellow"
    $dockerComposeFile = "docker-compose-all.yml"
    if (Test-Path $dockerComposeFile) {
        $content = Get-Content $dockerComposeFile -Raw
        if ($content -match "ENVIRONMENT") {
            Write-ColorOutput "  ✓ Docker Compose包含ENVIRONMENT配置" "Green"
            $tests += @{Name="Docker配置"; Status="✓"}
        } else {
            Write-ColorOutput "  ⚠ Docker Compose未包含ENVIRONMENT配置" "Yellow"
            $tests += @{Name="Docker配置"; Status="⚠"}
        }
    } else {
        Write-ColorOutput "  ✗ Docker Compose文件不存在" "Red"
        $tests += @{Name="Docker配置"; Status="✗"}
    }

    # 显示测试结果
    Write-ColorOutput "`n环境隔离测试结果:" "Cyan"
    foreach ($test in $tests) {
        $status = $test.Status
        $name = $test.Name
        $value = if ($test.Value) { " ($($test.Value))" } else { "" }
        Write-ColorOutput "  $status $name$value" $(if ($status -eq "✓") { "Green" } elseif ($status -eq "✗") { "Red" } else { "Yellow" })
    }

    $failedTests = $tests | Where-Object { $_.Status -eq "✗" }
    return ($failedTests.Count -eq 0)
}

# 测试性能优化功能
function Test-PerformanceOptimization {
    Write-ColorOutput "`n[测试3] 性能优化功能测试" "Cyan"
    Write-ColorOutput "==========================================" "Cyan"

    $tests = @()

    # 测试3.1: 检查索引优化脚本
    Write-ColorOutput "`n[3.1] 检查索引优化脚本..." "Yellow"
    $indexScript = "deployment\mysql\init\03-optimize-indexes.sql"
    if (Test-Path $indexScript) {
        Write-ColorOutput "  ✓ 索引优化脚本存在: $indexScript" "Green"
        $tests += @{Name="索引优化脚本"; Status="✓"}
    } else {
        Write-ColorOutput "  ✗ 索引优化脚本不存在: $indexScript" "Red"
        $tests += @{Name="索引优化脚本"; Status="✗"}
    }

    # 测试3.2: 检查索引数量
    Write-ColorOutput "`n[3.2] 检查索引数量..." "Yellow"
    $indexCount = Invoke-MySQLQuery -Query @"
SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA='$Database' AND INDEX_NAME != 'PRIMARY';
"@
    if ($indexCount) {
        $count = [int]$indexCount.Trim()
        if ($count -ge 50) {
            Write-ColorOutput "  ✓ 索引数量: $count (≥50，优化良好)" "Green"
            $tests += @{Name="索引数量"; Status="✓"; Value="$count 个"}
        } else {
            Write-ColorOutput "  ⚠ 索引数量: $count (<50，可能需要优化)" "Yellow"
            $tests += @{Name="索引数量"; Status="⚠"; Value="$count 个"}
        }
    }

    # 测试3.3: 检查批量插入优化（检查数据脚本格式）
    Write-ColorOutput "`n[3.3] 检查批量插入优化..." "Yellow"
    $dataScript = "deployment\mysql\init\02-ioedream-data.sql"
    if (Test-Path $dataScript) {
        $content = Get-Content $dataScript -Raw
        # 检查是否使用批量INSERT（VALUES多行）
        if ($content -match "INSERT.*VALUES.*\(.*\),\s*\(.*\)") {
            Write-ColorOutput "  ✓ 数据脚本使用批量插入优化" "Green"
            $tests += @{Name="批量插入"; Status="✓"}
        } else {
            Write-ColorOutput "  ⚠ 数据脚本可能未使用批量插入" "Yellow"
            $tests += @{Name="批量插入"; Status="⚠"}
        }
    } else {
        Write-ColorOutput "  ✗ 数据脚本不存在" "Red"
        $tests += @{Name="批量插入"; Status="✗"}
    }

    # 测试3.4: 检查INSERT IGNORE（幂等性）
    Write-ColorOutput "`n[3.4] 检查INSERT IGNORE（幂等性）..." "Yellow"
    if (Test-Path $dataScript) {
        $content = Get-Content $dataScript -Raw
        if ($content -match "INSERT IGNORE") {
            Write-ColorOutput "  ✓ 数据脚本使用INSERT IGNORE（支持幂等性）" "Green"
            $tests += @{Name="幂等性"; Status="✓"}
        } else {
            Write-ColorOutput "  ⚠ 数据脚本未使用INSERT IGNORE" "Yellow"
            $tests += @{Name="幂等性"; Status="⚠"}
        }
    }

    # 测试3.5: 性能基准测试（可选）
    Write-ColorOutput "`n[3.5] 性能基准测试（查询响应时间）..." "Yellow"
    $startTime = Get-Date
    $testResult = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM t_common_user;"
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalMilliseconds

    if ($null -ne $testResult -and $duration -lt 100) {
        Write-ColorOutput "  ✓ 查询响应时间: $([math]::Round($duration, 2))ms (<100ms，性能良好)" "Green"
        $tests += @{Name="查询性能"; Status="✓"; Value="$([math]::Round($duration, 2))ms"}
    } else {
        Write-ColorOutput "  ⚠ 查询响应时间: $([math]::Round($duration, 2))ms (≥100ms，可能需要优化)" "Yellow"
        $tests += @{Name="查询性能"; Status="⚠"; Value="$([math]::Round($duration, 2))ms"}
    }

    # 显示测试结果
    Write-ColorOutput "`n性能优化测试结果:" "Cyan"
    foreach ($test in $tests) {
        $status = $test.Status
        $name = $test.Name
        $value = if ($test.Value) { " ($($test.Value))" } else { "" }
        Write-ColorOutput "  $status $name$value" $(if ($status -eq "✓") { "Green" } elseif ($status -eq "✗") { "Red" } else { "Yellow" })
    }

    $failedTests = $tests | Where-Object { $_.Status -eq "✗" }
    return ($failedTests.Count -eq 0)
}

# 主逻辑
Write-ColorOutput "`n==========================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 数据库P2级优化功能快速测试" "Cyan"
Write-ColorOutput "==========================================" "Cyan"

$allPassed = $true

switch ($TestType) {
    'version' {
        if (-not (Test-VersionManagement)) { $allPassed = $false }
    }
    'environment' {
        if (-not (Test-EnvironmentIsolation)) { $allPassed = $false }
    }
    'performance' {
        if (-not (Test-PerformanceOptimization)) { $allPassed = $false }
    }
    'all' {
        if (-not (Test-VersionManagement)) { $allPassed = $false }
        if (-not (Test-EnvironmentIsolation)) { $allPassed = $false }
        if (-not (Test-PerformanceOptimization)) { $allPassed = $false }
    }
}

Write-ColorOutput "`n==========================================" "Cyan"
if ($allPassed) {
    Write-ColorOutput "  ✓ 所有测试通过！" "Green"
} else {
    Write-ColorOutput "  ✗ 部分测试失败，请检查上述错误信息" "Red"
}
Write-ColorOutput "==========================================" "Cyan"

exit $(if ($allPassed) { 0 } else { 1 })
