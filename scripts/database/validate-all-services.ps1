# =====================================================
# IOE-DREAM 所有服务数据库配置验证脚本
# 版本: v1.0.0
# 描述: 验证所有微服务的数据库配置一致性
# 创建时间: 2025-12-10
# =====================================================

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

    $content = Get-Content $configPath -Raw

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

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput Cyan "`n=========================================="
Write-ColorOutput Cyan "  IOE-DREAM 所有服务数据库配置验证"
Write-ColorOutput Cyan "=========================================="
Write-Output ""

$totalServices = $Services.Count
$validServices = 0
$issues = @()

foreach ($service in $Services) {
    Write-Output "检查服务: $($service.Name) (端口: $($service.Port), 类型: $($service.Type))"

    $found = $false
    $serviceIssues = @()

    foreach ($configFile in $ConfigFiles) {
        $result = Test-DatabaseConfig -ServiceName $service.Name -ConfigFile $configFile

        if ($result.Found) {
            Write-Output "  $configFile`: $($result.Status) - $($result.Database)"

            if ($result.Status -match "✅") {
                $found = $true
            } elseif ($result.Status -match "❌") {
                $serviceIssues += "$configFile`: 数据库名不一致 ($($result.Database))"
            }
        }
    }

    if ($found) {
        $validServices++
        Write-ColorOutput Green "  ✅ 服务配置正确"
    } else {
        Write-ColorOutput Yellow "  ⚠️  服务配置需要验证"
        $issues += "$($service.Name): $($serviceIssues -join ', ')"
    }

    Write-Output ""
}

# =====================================================
# 验证Nacos公共配置
# =====================================================

Write-ColorOutput Cyan "检查Nacos公共数据库配置..."
$nacosConfig = "microservices\common-config\nacos\common-database.yaml"
if (Test-Path $nacosConfig) {
    $nacosContent = Get-Content $nacosConfig -Raw
    if ($nacosContent -match 'jdbc:mysql://[^/]+/([^?]+)') {
        $nacosDb = $matches[1]
        if ($nacosDb -eq $ExpectedDatabase -or $nacosDb -match '\$\{.*ioedream') {
            Write-ColorOutput Green "  ✅ Nacos公共配置正确"
        } else {
            Write-ColorOutput Yellow "  ⚠️  Nacos公共配置数据库名: $nacosDb"
            $issues += "Nacos公共配置: 数据库名不一致 ($nacosDb)"
        }
    }
} else {
    Write-ColorOutput Yellow "  ⚠️  Nacos公共配置文件不存在"
    $issues += "Nacos公共配置: 文件不存在"
}

Write-Output ""

# =====================================================
# 验证Docker Compose配置
# =====================================================

Write-ColorOutput Cyan "检查Docker Compose数据库配置..."
$dockerCompose = "docker-compose-all.yml"
if (Test-Path $dockerCompose) {
    $dockerContent = Get-Content $dockerCompose -Raw
    if ($dockerContent -match "MYSQL_DATABASE.*ioedream" -or $dockerContent -match "DB_NAME.*ioedream") {
        Write-ColorOutput Green "  ✅ Docker Compose配置正确"
    } else {
        Write-ColorOutput Yellow "  ⚠️  Docker Compose配置需要验证"
        $issues += "Docker Compose: 数据库配置需要验证"
    }
} else {
    Write-ColorOutput Yellow "  ⚠️  Docker Compose文件不存在"
}

Write-Output ""

# =====================================================
# 总结
# =====================================================

Write-ColorOutput Cyan "=========================================="
Write-ColorOutput Cyan "  验证结果总结"
Write-ColorOutput Cyan "=========================================="
Write-Output ""

Write-Output "总服务数: $totalServices"
Write-Output "配置正确: $validServices"
Write-Output "需要验证: $($totalServices - $validServices)"
Write-Output ""

if ($issues.Count -eq 0) {
    Write-ColorOutput Green "✅ 所有服务数据库配置验证通过！"
} else {
    Write-ColorOutput Yellow "发现 $($issues.Count) 个问题:"
    foreach ($issue in $issues) {
        Write-Output "  - $issue"
    }
}

Write-Output ""

