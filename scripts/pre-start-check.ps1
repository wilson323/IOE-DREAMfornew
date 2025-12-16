# =====================================================
# IOE-DREAM 启动前预检脚本
# 版本: v1.0.0
# 描述: 启动项目前的完整预检，确保所有条件满足
# 创建时间: 2025-12-15
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$FixIssues,

    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

$ErrorActionPreference = "Continue"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

# =====================================================
# 辅助函数
# =====================================================

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Test-PortOpen {
    param([int]$Port, [int]$TimeoutMs = 1000)

    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $Port, $null, $null)
        $wait = $asyncResult.AsyncWaitHandle.WaitOne($TimeoutMs, $false)

        if ($wait) {
            try {
                $tcpClient.EndConnect($asyncResult)
                $tcpClient.Close()
                return $true
            } catch {
                $tcpClient.Close()
                return $false
            }
        } else {
            $tcpClient.Close()
            return $false
        }
    } catch {
        return $false
    }
}

# =====================================================
# 预检项目
# =====================================================

$checkResults = @()

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 启动前预检" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  检查时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"
Write-ColorOutput "  项目路径: $ProjectRoot" "Gray"
Write-ColorOutput ""

# =====================================================
# 1. 环境变量文件检查
# =====================================================
Write-ColorOutput "[1/7] 检查环境变量文件..." "Cyan"

$envFile = Join-Path $ProjectRoot ".env"
if (Test-Path $envFile) {
    Write-ColorOutput "  [OK] .env 文件存在" "Green"
    $checkResults += @{ Name = "环境变量文件"; Status = "PASS" }

    # 检查关键配置
    $envContent = Get-Content $envFile -Raw
    $missingVars = @()

    $requiredVars = @(
        "MYSQL_HOST", "MYSQL_PASSWORD",
        "REDIS_HOST", "REDIS_PASSWORD",
        "NACOS_SERVER_ADDR", "NACOS_AUTH_TOKEN",
        "RABBITMQ_HOST", "RABBITMQ_PASSWORD",
        "JASYPT_PASSWORD"
    )

    foreach ($var in $requiredVars) {
        if ($envContent -notmatch "^$var=") {
            $missingVars += $var
        }
    }

    if ($missingVars.Count -gt 0) {
        Write-ColorOutput "  [警告] 缺少配置: $($missingVars -join ', ')" "Yellow"
        $checkResults += @{ Name = "环境变量完整性"; Status = "WARN"; Details = $missingVars }
    } else {
        Write-ColorOutput "  [OK] 所有必需环境变量已配置" "Green"
        $checkResults += @{ Name = "环境变量完整性"; Status = "PASS" }
    }
} else {
    Write-ColorOutput "  [错误] .env 文件不存在" "Red"
    $checkResults += @{ Name = "环境变量文件"; Status = "FAIL" }
}

# =====================================================
# 2. Java环境检查
# =====================================================
Write-ColorOutput ""
Write-ColorOutput "[2/7] 检查Java环境..." "Cyan"

try {
    $javaVersion = & java -version 2>&1 | Select-String -Pattern "version"
    if ($javaVersion) {
        Write-ColorOutput "  [OK] Java: $javaVersion" "Green"
        $checkResults += @{ Name = "Java环境"; Status = "PASS" }

        # 检查是否Java 17+
        if ($javaVersion -match '"(\d+)') {
            $majorVersion = [int]$matches[1]
            if ($majorVersion -lt 17) {
                Write-ColorOutput "  [警告] 建议使用Java 17+，当前版本: $majorVersion" "Yellow"
            }
        }
    }
} catch {
    Write-ColorOutput "  [错误] Java未安装或不在PATH中" "Red"
    $checkResults += @{ Name = "Java环境"; Status = "FAIL" }
}

# =====================================================
# 3. Maven环境检查
# =====================================================
Write-ColorOutput ""
Write-ColorOutput "[3/7] 检查Maven环境..." "Cyan"

try {
    $mvnVersion = & mvn -version 2>&1 | Select-String -Pattern "Apache Maven"
    if ($mvnVersion) {
        Write-ColorOutput "  [OK] Maven: $mvnVersion" "Green"
        $checkResults += @{ Name = "Maven环境"; Status = "PASS" }
    }
} catch {
    Write-ColorOutput "  [错误] Maven未安装或不在PATH中" "Red"
    $checkResults += @{ Name = "Maven环境"; Status = "FAIL" }
}

# =====================================================
# 4. Node.js环境检查
# =====================================================
Write-ColorOutput ""
Write-ColorOutput "[4/7] 检查Node.js环境..." "Cyan"

try {
    $nodeVersion = & node --version 2>&1
    if ($nodeVersion -match "v\d+") {
        Write-ColorOutput "  [OK] Node.js: $nodeVersion" "Green"
        $checkResults += @{ Name = "Node.js环境"; Status = "PASS" }

        # 检查npm
        $npmVersion = & npm --version 2>&1
        Write-ColorOutput "  [OK] npm: $npmVersion" "Green"
    }
} catch {
    Write-ColorOutput "  [错误] Node.js未安装或不在PATH中" "Red"
    $checkResults += @{ Name = "Node.js环境"; Status = "FAIL" }
}

# =====================================================
# 5. Docker环境检查
# =====================================================
Write-ColorOutput ""
Write-ColorOutput "[5/7] 检查Docker环境..." "Cyan"

try {
    $dockerVersion = & docker --version 2>&1
    if ($dockerVersion -match "Docker version") {
        Write-ColorOutput "  [OK] Docker: $dockerVersion" "Green"
        $checkResults += @{ Name = "Docker环境"; Status = "PASS" }

        # 检查Docker是否运行
        $dockerInfo = & docker info 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-ColorOutput "  [OK] Docker正在运行" "Green"
        } else {
            Write-ColorOutput "  [警告] Docker已安装但未运行" "Yellow"
            $checkResults += @{ Name = "Docker运行状态"; Status = "WARN" }
        }
    }
} catch {
    Write-ColorOutput "  [警告] Docker未安装（仅本地开发模式可用）" "Yellow"
    $checkResults += @{ Name = "Docker环境"; Status = "WARN" }
}

# =====================================================
# 6. 基础设施服务检查
# =====================================================
Write-ColorOutput ""
Write-ColorOutput "[6/7] 检查基础设施服务..." "Cyan"

$infraServices = @(
    @{ Name = "MySQL"; Port = 3306 },
    @{ Name = "Redis"; Port = 6379 },
    @{ Name = "Nacos"; Port = 8848 },
    @{ Name = "RabbitMQ"; Port = 5672 }
)

$infraReady = $true
foreach ($svc in $infraServices) {
    $isRunning = Test-PortOpen -Port $svc.Port
    $status = if ($isRunning) { "[OK]" } else { "[XX]" }
    $color = if ($isRunning) { "Green" } else { "Yellow" }
    Write-ColorOutput "  $status $($svc.Name) (Port: $($svc.Port))" $color

    if (-not $isRunning) {
        $infraReady = $false
    }
}

if ($infraReady) {
    $checkResults += @{ Name = "基础设施服务"; Status = "PASS" }
} else {
    $checkResults += @{ Name = "基础设施服务"; Status = "WARN" }
    Write-ColorOutput ""
    Write-ColorOutput "  [提示] 启动基础设施:" "Yellow"
    Write-ColorOutput "    docker-compose -f docker-compose-all.yml up -d mysql redis nacos rabbitmq" "Gray"
}

# =====================================================
# 7. Maven依赖检查
# =====================================================
Write-ColorOutput ""
Write-ColorOutput "[7/7] 检查Maven本地仓库..." "Cyan"

$m2Repo = Join-Path $env:USERPROFILE ".m2\repository\net\lab1024\sa"
$requiredModules = @(
    "microservices-common-core",
    "microservices-common-security",
    "microservices-common-data",
    "microservices-common-cache",
    "microservices-common-export",
    "microservices-common-workflow"
)

$missingModules = @()
foreach ($module in $requiredModules) {
    $modulePath = Join-Path $m2Repo $module
    if (-not (Test-Path $modulePath)) {
        $missingModules += $module
    }
}

if ($missingModules.Count -eq 0) {
    Write-ColorOutput "  [OK] 所有公共模块已安装" "Green"
    $checkResults += @{ Name = "Maven依赖"; Status = "PASS" }
} else {
    Write-ColorOutput "  [警告] 缺少模块: $($missingModules -join ', ')" "Yellow"
    $checkResults += @{ Name = "Maven依赖"; Status = "WARN" }
    Write-ColorOutput ""
    Write-ColorOutput "  [提示] 安装缺失模块:" "Yellow"
    Write-ColorOutput "    cd $ProjectRoot\microservices\microservices-common" "Gray"
    Write-ColorOutput "    mvn clean install -DskipTests" "Gray"
}

# =====================================================
# 汇总报告
# =====================================================
Write-ColorOutput ""
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  预检结果汇总" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

$passCount = ($checkResults | Where-Object { $_.Status -eq "PASS" }).Count
$warnCount = ($checkResults | Where-Object { $_.Status -eq "WARN" }).Count
$failCount = ($checkResults | Where-Object { $_.Status -eq "FAIL" }).Count

foreach ($result in $checkResults) {
    $statusIcon = switch ($result.Status) {
        "PASS" { "[PASS]" }
        "WARN" { "[WARN]" }
        "FAIL" { "[FAIL]" }
    }
    $statusColor = switch ($result.Status) {
        "PASS" { "Green" }
        "WARN" { "Yellow" }
        "FAIL" { "Red" }
    }
    Write-ColorOutput "  $statusIcon $($result.Name)" $statusColor
}

Write-ColorOutput ""
Write-ColorOutput "  通过: $passCount | 警告: $warnCount | 失败: $failCount" "White"
Write-ColorOutput ""

if ($failCount -gt 0) {
    Write-ColorOutput "  [结果] 预检未通过，请先解决失败项" "Red"
    exit 1
} elseif ($warnCount -gt 0) {
    Write-ColorOutput "  [结果] 预检通过但有警告，建议解决后再启动" "Yellow"
    exit 0
} else {
    Write-ColorOutput "  [结果] 预检全部通过，可以启动项目!" "Green"
    exit 0
}
