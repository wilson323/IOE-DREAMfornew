# =====================================================
# IOE-DREAM 数据库初始化验证脚本（增强版）
# 功能: 验证数据库初始化完整性，支持P2级优化功能
# 版本: v2.0.0
# 创建时间: 2025-12-10
# =====================================================

param(
    [switch]$Reinitialize,
    [switch]$ShowDetails,
    [switch]$CheckVersion,
    [switch]$CheckEnvironment,
    [string]$Database = 'ioedream',
    [string]$Host = 'localhost',
    [int]$Port = 3306,
    [string]$Username = 'root',
    [string]$Password = '123456'
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
        [string]$Query,
        [switch]$NoHeader
    )
    
    $mysqlPath = "mysql"
    if (Get-Command mysql -ErrorAction SilentlyContinue) {
        $mysqlPath = "mysql"
    } elseif (Test-Path "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe") {
        $mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    } else {
        Write-ColorOutput "  错误: 未找到mysql命令，请确保MySQL客户端已安装" "Red"
        return $null
    }
    
    $queryFile = [System.IO.Path]::GetTempFileName()
    $Query | Out-File -FilePath $queryFile -Encoding UTF8
    
    try {
        $env:MYSQL_PWD = $Password
        $result = & $mysqlPath -h$Host -P$Port -u$Username $Database -N -e $Query 2>&1
        $exitCode = $LASTEXITCODE
        
        if ($exitCode -eq 0) {
            return $result
        } else {
            Write-ColorOutput "  MySQL错误: $result" "Red"
            return $null
        }
    } finally {
        Remove-Item $queryFile -ErrorAction SilentlyContinue
        Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
    }
}

# 检查Docker状态
function Test-DockerStatus {
    Write-ColorOutput "`n[1/8] 检查Docker状态..." "Cyan"
    
    $dockerRunning = docker info 2>$null
    if (-not $dockerRunning) {
        Write-ColorOutput "  ✗ Docker未运行!" "Red"
        return $false
    }
    Write-ColorOutput "  ✓ Docker运行正常" "Green"
    return $true
}

# 检查MySQL容器状态
function Test-MySQLContainer {
    Write-ColorOutput "`n[2/8] 检查MySQL容器状态..." "Cyan"
    
    $mysqlStatus = docker inspect --format='{{.State.Status}}' ioedream-mysql 2>$null
    if ($mysqlStatus -ne "running") {
        Write-ColorOutput "  ✗ MySQL容器未运行 (状态: $mysqlStatus)" "Red"
        Write-ColorOutput "  请先启动MySQL: docker-compose -f docker-compose-all.yml up -d mysql" "Yellow"
        return $false
    }
    Write-ColorOutput "  ✓ MySQL容器运行中" "Green"
    
    $mysqlHealth = docker inspect --format='{{.State.Health.Status}}' ioedream-mysql 2>$null
    if ($mysqlHealth -eq "healthy") {
        Write-ColorOutput "  ✓ MySQL健康状态: $mysqlHealth" "Green"
    } else {
        Write-ColorOutput "  ⚠ MySQL健康状态: $mysqlHealth" "Yellow"
    }
    
    return $true
}

# 检查SQL初始化文件
function Test-SQLFiles {
    Write-ColorOutput "`n[3/8] 检查SQL初始化文件..." "Cyan"
    
    $initDir = "deployment\mysql\init"
    if (-not (Test-Path $initDir)) {
        Write-ColorOutput "  ✗ 初始化脚本目录不存在: $initDir" "Red"
        return $false
    }
    
    $requiredFiles = @(
        "00-version-check.sql",
        "01-ioedream-schema.sql",
        "02-ioedream-data.sql",
        "02-ioedream-data-dev.sql",
        "02-ioedream-data-test.sql",
        "02-ioedream-data-prod.sql",
        "03-optimize-indexes.sql",
        "nacos-schema.sql"
    )
    
    $missingFiles = @()
    foreach ($file in $requiredFiles) {
        $filePath = Join-Path $initDir $file
        if (Test-Path $filePath) {
            Write-ColorOutput "  ✓ $file" "Green"
        } else {
            Write-ColorOutput "  ⚠ $file (可选)" "Yellow"
        }
    }
    
    return $true
}

# 检查数据库连接
function Test-DatabaseConnection {
    Write-ColorOutput "`n[4/8] 检查数据库连接..." "Cyan"
    
    $result = Invoke-MySQLQuery -Query "SELECT 1;" -NoHeader
    if ($result) {
        Write-ColorOutput "  ✓ 数据库连接成功" "Green"
        return $true
    } else {
        Write-ColorOutput "  ✗ 数据库连接失败" "Red"
        return $false
    }
}

# 检查数据库版本（P2级优化）
function Test-DatabaseVersion {
    if (-not $CheckVersion) {
        return $true
    }
    
    Write-ColorOutput "`n[5/8] 检查数据库版本..." "Cyan"
    
    # 检查版本检查函数是否存在
    $versionFunc = Invoke-MySQLQuery -Query "SELECT get_current_version();" -NoHeader
    if ($versionFunc) {
        $version = $versionFunc.Trim()
        Write-ColorOutput "  当前版本: $version" "Green"
    } else {
        Write-ColorOutput "  ⚠ 版本检查函数不存在，可能需要执行00-version-check.sql" "Yellow"
    }
    
    # 检查迁移历史
    $migrationCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM t_migration_history WHERE status = 'SUCCESS';" -NoHeader
    if ($migrationCount) {
        Write-ColorOutput "  已执行迁移: $migrationCount 个" "Green"
    }
    
    return $true
}

# 检查环境配置（P2级优化）
function Test-EnvironmentConfig {
    if (-not $CheckEnvironment) {
        return $true
    }
    
    Write-ColorOutput "`n[6/8] 检查环境配置..." "Cyan"
    
    $env = $env:ENVIRONMENT
    if (-not $env) {
        $env = "dev (默认)"
    }
    Write-ColorOutput "  当前环境: $env" "Green"
    
    # 检查环境特定数据
    $testUserCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM t_common_user WHERE username LIKE 'test_%' OR username LIKE 'qa_%';" -NoHeader
    if ($testUserCount -and [int]$testUserCount -gt 0) {
        if ($env -eq "prod") {
            Write-ColorOutput "  ⚠ 警告: 生产环境包含测试用户 ($testUserCount 个)" "Yellow"
        } else {
            Write-ColorOutput "  ✓ 测试用户数量: $testUserCount" "Green"
        }
    }
    
    return $true
}

# 验证数据库完整性
function Test-DatabaseIntegrity {
    Write-ColorOutput "`n[7/8] 验证数据库完整性..." "Cyan"
    
    $checks = @()
    
    # 检查数据库是否存在
    $dbExists = Invoke-MySQLQuery -Query "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '$Database';" -NoHeader
    if ($dbExists) {
        Write-ColorOutput "  ✓ 数据库 $Database 存在" "Green"
        $checks += @{Name="数据库存在"; Status="✓"}
    } else {
        Write-ColorOutput "  ✗ 数据库 $Database 不存在" "Red"
        $checks += @{Name="数据库存在"; Status="✗"}
        return $false
    }
    
    # 检查表数量
    $tableCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='$Database';" -NoHeader
    if ($tableCount) {
        $count = [int]$tableCount.Trim()
        if ($count -ge 20) {
            Write-ColorOutput "  ✓ 表数量: $count (≥20)" "Green"
            $checks += @{Name="表数量"; Status="✓"; Value=$count}
        } else {
            Write-ColorOutput "  ✗ 表数量不足: $count (<20)" "Red"
            $checks += @{Name="表数量"; Status="✗"; Value=$count}
        }
    }
    
    # 检查字典数据
    $dictTypeCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM $Database.t_common_dict_type;" -NoHeader
    if ($dictTypeCount) {
        $count = [int]$dictTypeCount.Trim()
        if ($count -ge 10) {
            Write-ColorOutput "  ✓ 字典类型数量: $count (≥10)" "Green"
            $checks += @{Name="字典类型"; Status="✓"; Value=$count}
        } else {
            Write-ColorOutput "  ✗ 字典类型数量不足: $count (<10)" "Red"
            $checks += @{Name="字典类型"; Status="✗"; Value=$count}
        }
    }
    
    # 检查用户数据
    $userCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM $Database.t_common_user;" -NoHeader
    if ($userCount) {
        $count = [int]$userCount.Trim()
        if ($count -ge 1) {
            Write-ColorOutput "  ✓ 用户数量: $count (≥1)" "Green"
            $checks += @{Name="用户数量"; Status="✓"; Value=$count}
        } else {
            Write-ColorOutput "  ✗ 用户数量不足: $count (<1)" "Red"
            $checks += @{Name="用户数量"; Status="✗"; Value=$count}
        }
    }
    
    # 检查admin用户
    $adminExists = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM $Database.t_common_user WHERE username = 'admin';" -NoHeader
    if ($adminExists -and [int]$adminExists.Trim() -gt 0) {
        Write-ColorOutput "  ✓ admin用户存在" "Green"
        $checks += @{Name="admin用户"; Status="✓"}
    } else {
        Write-ColorOutput "  ✗ admin用户不存在" "Red"
        $checks += @{Name="admin用户"; Status="✗"}
    }
    
    # 检查索引（P2级优化）
    $indexCount = Invoke-MySQLQuery -Query @"
SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA='$Database' AND INDEX_NAME != 'PRIMARY';
"@ -NoHeader
    if ($indexCount) {
        $count = [int]$indexCount.Trim()
        if ($count -ge 50) {
            Write-ColorOutput "  ✓ 索引数量: $count (≥50)" "Green"
            $checks += @{Name="索引数量"; Status="✓"; Value=$count}
        } else {
            Write-ColorOutput "  ⚠ 索引数量: $count (<50，可能未执行索引优化)" "Yellow"
            $checks += @{Name="索引数量"; Status="⚠"; Value=$count}
        }
    }
    
    # 检查Nacos数据库
    $nacosTableCount = Invoke-MySQLQuery -Query "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='nacos';" -NoHeader
    if ($nacosTableCount) {
        $count = [int]$nacosTableCount.Trim()
        if ($count -ge 10) {
            Write-ColorOutput "  ✓ Nacos表数量: $count (≥10)" "Green"
            $checks += @{Name="Nacos表数量"; Status="✓"; Value=$count}
        } else {
            Write-ColorOutput "  ✗ Nacos表数量不足: $count (<10)" "Red"
            $checks += @{Name="Nacos表数量"; Status="✗"; Value=$count}
        }
    }
    
    if ($ShowDetails) {
        Write-ColorOutput "`n  详细检查结果:" "Cyan"
        foreach ($check in $checks) {
            $status = $check.Status
            $name = $check.Name
            $value = if ($check.Value) { " ($($check.Value))" } else { "" }
            Write-ColorOutput "    $status $name$value" $(if ($status -eq "✓") { "Green" } elseif ($status -eq "✗") { "Red" } else { "Yellow" })
        }
    }
    
    $failedChecks = $checks | Where-Object { $_.Status -eq "✗" }
    return ($failedChecks.Count -eq 0)
}

# 显示验证总结
function Show-VerificationSummary {
    Write-ColorOutput "`n[8/8] 验证总结..." "Cyan"
    
    $allPassed = $true
    
    if (-not (Test-DockerStatus)) { $allPassed = $false }
    if (-not (Test-MySQLContainer)) { $allPassed = $false }
    if (-not (Test-SQLFiles)) { $allPassed = $false }
    if (-not (Test-DatabaseConnection)) { $allPassed = $false }
    if (-not (Test-DatabaseVersion)) { $allPassed = $false }
    if (-not (Test-EnvironmentConfig)) { $allPassed = $false }
    if (-not (Test-DatabaseIntegrity)) { $allPassed = $false }
    
    Write-ColorOutput "`n==========================================" "Cyan"
    if ($allPassed) {
        Write-ColorOutput "  ✓ 数据库初始化验证通过！" "Green"
    } else {
        Write-ColorOutput "  ✗ 数据库初始化验证失败！" "Red"
        Write-ColorOutput "  请检查上述错误信息并修复" "Yellow"
    }
    Write-ColorOutput "==========================================" "Cyan"
    
    return $allPassed
}

# 重新初始化
function Start-Reinitialize {
    Write-ColorOutput "`n[重新初始化] 开始重新初始化数据库..." "Cyan"
    Write-ColorOutput "⚠ 警告: 这将删除现有数据并重新初始化！" "Red"
    
    $confirm = Read-Host "确认重新初始化? (yes/no)"
    if ($confirm -ne 'yes') {
        Write-ColorOutput "  已取消重新初始化" "Yellow"
        return
    }
    
    Write-ColorOutput "  停止现有服务..." "Yellow"
    docker-compose -f docker-compose-all.yml down
    
    Write-ColorOutput "  删除数据库卷..." "Yellow"
    docker volume rm ioedream_mysql_data 2>$null
    
    Write-ColorOutput "  启动服务（自动初始化）..." "Yellow"
    docker-compose -f docker-compose-all.yml up -d
    
    Write-ColorOutput "  等待初始化完成..." "Yellow"
    Start-Sleep -Seconds 30
    
    Write-ColorOutput "  验证初始化结果..." "Yellow"
    Show-VerificationSummary
}

# 主逻辑
Write-ColorOutput "`n==========================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 数据库初始化验证（增强版）" "Cyan"
Write-ColorOutput "==========================================" "Cyan"

if ($Reinitialize) {
    Start-Reinitialize
} else {
    $result = Show-VerificationSummary
    exit $(if ($result) { 0 } else { 1 })
}


