# =====================================================
# IOE-DREAM 服务验证和启动脚本
# 版本: v1.0.0
# 描述: 清理项目、验证配置、启动所有微服务
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipCleanup,

    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild,

    [Parameter(Mandatory=$false)]
    [switch]$StartServices
)

$ErrorActionPreference = "Stop"

# =====================================================
# 加载环境变量（从.env文件）
# =====================================================

$loadEnvScript = Join-Path $PSScriptRoot "load-env.ps1"
if (Test-Path $loadEnvScript) {
    & $loadEnvScript -Silent
} else {
    Write-Host "[WARN] load-env.ps1 not found, using default values" -ForegroundColor Yellow
}

# =====================================================
# 配置
# =====================================================

$ProjectRoot = Split-Path -Parent $PSScriptRoot
if (-not $ProjectRoot -or $ProjectRoot -eq "") {
    $ProjectRoot = (Get-Location).Path
}

$Microservices = @(
    @{ Name = "ioedream-gateway-service"; Port = 8080; Path = "microservices\ioedream-gateway-service" },
    @{ Name = "ioedream-common-service"; Port = 8088; Path = "microservices\ioedream-common-service" },
    @{ Name = "ioedream-device-comm-service"; Port = 8087; Path = "microservices\ioedream-device-comm-service" },
    @{ Name = "ioedream-oa-service"; Port = 8089; Path = "microservices\ioedream-oa-service" },
    @{ Name = "ioedream-access-service"; Port = 8090; Path = "microservices\ioedream-access-service" },
    @{ Name = "ioedream-attendance-service"; Port = 8091; Path = "microservices\ioedream-attendance-service" },
    @{ Name = "ioedream-video-service"; Port = 8092; Path = "microservices\ioedream-video-service" },
    @{ Name = "ioedream-consume-service"; Port = 8094; Path = "microservices\ioedream-consume-service" },
    @{ Name = "ioedream-visitor-service"; Port = 8095; Path = "microservices\ioedream-visitor-service" }
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

function Test-PortInUse {
    param([int]$Port)

    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $Port, $null, $null)
        $wait = $asyncResult.AsyncWaitHandle.WaitOne(1000, $false)

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

function Test-ServiceHealth {
    param(
        [string]$ServiceName,
        [int]$Port
    )

    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$Port/actuator/health" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            return $true
        }
        return $false
    } catch {
        return $false
    }
}

function Test-ApplicationYml {
    param([string]$ServicePath)

    $ymlPath = Join-Path $ServicePath "src\main\resources\application.yml"
    return Test-Path $ymlPath
}

function Test-StartupClass {
    param([string]$ServicePath)

    $javaFiles = Get-ChildItem -Path $ServicePath -Filter "*Application.java" -Recurse -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -like "*\src\main\java\*" }

    return ($javaFiles.Count -gt 0)
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  IOE-DREAM Service Verification"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "Start time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output "Project root: $ProjectRoot"
Write-Output ""

# 1. 清理项目（如果需要）
if (-not $SkipCleanup) {
    Write-ColorOutput Cyan "`n[Step 1] Cleaning project (with backup)..."
    Write-Output ""

    $cleanupScript = Join-Path $ProjectRoot "scripts\cleanup-project-with-backup.ps1"
    if (Test-Path $cleanupScript) {
        try {
            & $cleanupScript
            Write-ColorOutput Green "[OK] Project cleanup completed"
        } catch {
            Write-ColorOutput Yellow "[WARN] Cleanup script failed: $($_.Exception.Message)"
        }
    } else {
        Write-ColorOutput Yellow "[WARN] Cleanup script not found, skipping..."
    }
    Write-Output ""
} else {
    Write-ColorOutput Yellow "[SKIP] Cleanup skipped"
    Write-Output ""
}

# 2. 检查基础设施服务
Write-ColorOutput Cyan "`n[Step 2] Checking infrastructure services..."

$infraOk = $true
$infraServices = @(
    @{ Name = "MySQL"; Port = 3306 },
    @{ Name = "Redis"; Port = 6379 },
    @{ Name = "Nacos"; Port = 8848 }
)

foreach ($infra in $infraServices) {
    if (Test-PortInUse -Port $infra.Port) {
        Write-ColorOutput Green "  [OK] $($infra.Name) ($($infra.Port)) - Running"
    } else {
        Write-ColorOutput Red "  [ERROR] $($infra.Name) ($($infra.Port)) - Not running"
        $infraOk = $false
    }
}

if (-not $infraOk) {
    Write-ColorOutput Red "`n[ERROR] Infrastructure services are not running!"
    Write-Output "Please start them first:"
    Write-Output "  docker-compose -f docker-compose-all.yml up -d mysql redis nacos"
    Write-Output ""
    exit 1
}
Write-Output ""

# 3. 验证微服务配置
Write-ColorOutput Cyan "`n[Step 3] Verifying microservice configurations..."

$configIssues = @()
foreach ($svc in $Microservices) {
    $servicePath = Join-Path $ProjectRoot $svc.Path

    if (-not (Test-Path $servicePath)) {
        $configIssues += "  [ERROR] $($svc.Name) - Service directory not found"
        continue
    }

    $ymlExists = Test-ApplicationYml -ServicePath $servicePath
    $startupExists = Test-StartupClass -ServicePath $servicePath

    if ($ymlExists -and $startupExists) {
        Write-ColorOutput Green "  [OK] $($svc.Name) - Config and startup class exist"
    } else {
        if (-not $ymlExists) {
            $configIssues += "  [ERROR] $($svc.Name) - application.yml missing"
        }
        if (-not $startupExists) {
            $configIssues += "  [ERROR] $($svc.Name) - Startup class missing"
        }
    }
}

if ($configIssues.Count -gt 0) {
    Write-ColorOutput Red "`n[ERROR] Configuration issues found:"
    foreach ($issue in $configIssues) {
        Write-Output $issue
    }
    Write-Output ""
    exit 1
}
Write-Output ""

# 4. 构建 microservices-common（如果需要）
if (-not $SkipBuild) {
    Write-ColorOutput Cyan "`n[Step 4] Building microservices-common..."
    $commonPath = Join-Path $ProjectRoot "microservices\microservices-common"

    if (Test-Path $commonPath) {
        try {
            Push-Location $commonPath
            Write-Output "  Running: mvn clean install -DskipTests"
            Write-Output ""

            $process = Start-Process -FilePath "mvn" -ArgumentList "clean", "install", "-DskipTests" -NoNewWindow -Wait -PassThru

            if ($process.ExitCode -eq 0) {
                Write-ColorOutput Green "  [OK] microservices-common built successfully"
            } else {
                Write-ColorOutput Red "  [ERROR] Build failed with exit code: $($process.ExitCode)"
                Pop-Location
                exit 1
            }
            Pop-Location
        } catch {
            Write-ColorOutput Red "  [ERROR] Build exception: $($_.Exception.Message)"
            Pop-Location
            exit 1
        }
    } else {
        Write-ColorOutput Yellow "  [WARN] microservices-common not found, skipping build"
    }
    Write-Output ""
} else {
    Write-ColorOutput Yellow "[SKIP] Build skipped"
    Write-Output ""
}

# 5. 启动服务（如果需要）
if ($StartServices) {
    Write-ColorOutput Cyan "`n[Step 5] Starting microservices..."
    Write-Output ""

    $startScript = Join-Path $ProjectRoot "scripts\start-all-services.ps1"
    if (Test-Path $startScript) {
        try {
            & $startScript -WaitForReady
            Write-ColorOutput Green "[OK] Services startup initiated"
        } catch {
            Write-ColorOutput Yellow "[WARN] Start script failed: $($_.Exception.Message)"
        }
    } else {
        Write-ColorOutput Yellow "[WARN] Start script not found"
    }
    Write-Output ""
} else {
    Write-ColorOutput Cyan "`n[Step 5] Service startup skipped (use -StartServices to start)"
    Write-Output ""
}

# 6. 验证服务状态
Write-ColorOutput Cyan "`n[Step 6] Verifying service status..."
Write-Output ""

$runningCount = 0
foreach ($svc in $Microservices) {
    if (Test-PortInUse -Port $svc.Port) {
        $healthOk = Test-ServiceHealth -ServiceName $svc.Name -Port $svc.Port
        if ($healthOk) {
            Write-ColorOutput Green "  [OK] $($svc.Name) - Running and healthy (Port: $($svc.Port))"
        } else {
            Write-ColorOutput Yellow "  [WARN] $($svc.Name) - Running but health check failed (Port: $($svc.Port))"
        }
        $runningCount++
    } else {
        Write-ColorOutput Red "  [XX] $($svc.Name) - Not running (Port: $($svc.Port))"
    }
}

Write-Output ""
Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  Verification Summary"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "Infrastructure: OK"
Write-Output "Configuration: OK"
Write-Output "Services running: $runningCount/$($Microservices.Count)"
Write-Output ""
Write-Output "End time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output ""

