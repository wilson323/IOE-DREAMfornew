# =====================================================
# IOE-DREAM 微服务启动并验证脚本
# 版本: v1.0.0
# 描述: 启动所有微服务并执行完整动态验证
# 创建时间: 2025-12-14
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild,

    [Parameter(Mandatory=$false)]
    [int]$WaitSeconds = 180,

    [Parameter(Mandatory=$false)]
    [switch]$SkipVerification
)

$ErrorActionPreference = "Stop"

# =====================================================
# 环境变量配置（修复内存不足问题）
# =====================================================

# 设置Maven编译内存（解决编译时内存不足问题）
# 增加Maven编译器的堆内存，避免多个服务同时编译时内存耗尽
if (-not $env:MAVEN_OPTS) {
    $env:MAVEN_OPTS = "-Xmx2048m -Xms1024m -XX:MaxMetaspaceSize=512m"
} else {
    # 如果已设置，确保包含足够的内存配置
    if ($env:MAVEN_OPTS -notmatch "-Xmx") {
        $env:MAVEN_OPTS = $env:MAVEN_OPTS + " -Xmx2048m"
    }
    if ($env:MAVEN_OPTS -notmatch "-Xms") {
        $env:MAVEN_OPTS = $env:MAVEN_OPTS + " -Xms1024m"
    }
}

Write-Output "[INFO] MAVEN_OPTS: $env:MAVEN_OPTS"
Write-Output ""

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

# Logs & PID record
$LogDir = Join-Path $ProjectRoot "logs\start-and-verify"
$PidFile = Join-Path $LogDir "service-pids.json"
if (-not (Test-Path $LogDir)) {
    New-Item -ItemType Directory -Path $LogDir | Out-Null
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

function Start-Microservice {
    param(
        [hashtable]$Service
    )

    $servicePath = Join-Path $ProjectRoot $Service.Path

    if (-not (Test-Path $servicePath)) {
        $errMsg = "Service not found: $($Service.Name)"
        Write-ColorOutput Red $errMsg
        return $false
    }

    # Check if port is already in use
    if (Test-PortInUse -Port $Service.Port) {
        $warnMsg = "Port $($Service.Port) already in use, service may already be running"
        Write-ColorOutput Yellow $warnMsg
        return $true
    }

    $infoMsg = "Starting $($Service.Name) (Port: $($Service.Port))..."
    Write-ColorOutput Cyan $infoMsg

    try {
        $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
        $outFile = Join-Path $LogDir "$($Service.Name)-$timestamp.out.log"
        $errFile = Join-Path $LogDir "$($Service.Name)-$timestamp.err.log"

        # Start service in background (no interactive window), redirect logs
        # 环境变量MAVEN_OPTS已在脚本开头设置，子进程会自动继承
        # 使用cmd.exe包装确保环境变量正确传递（兼容性更好）
        $mavenCmd = "mvn spring-boot:run"
        $process = Start-Process `
            -FilePath "cmd.exe" `
            -ArgumentList "/c", "set MAVEN_OPTS=$env:MAVEN_OPTS && cd /d `"$servicePath`" && $mavenCmd" `
            -NoNewWindow `
            -RedirectStandardOutput $outFile `
            -RedirectStandardError $errFile `
            -PassThru

        # Record PID for later stop
        $pidRecord = @{
            name = $Service.Name
            port = $Service.Port
            pid = $process.Id
            startedAt = (Get-Date).ToString("s")
            stdout = $outFile
            stderr = $errFile
        }
        $existing = @()
        if (Test-Path $PidFile) {
            try {
                $existing = Get-Content $PidFile -Raw | ConvertFrom-Json
            } catch {
                $existing = @()
            }
        }
        $existing = @($existing) + @($pidRecord)
        $existing | ConvertTo-Json -Depth 5 | Set-Content -Path $PidFile -Encoding UTF8

        $okMsg = "Started $($Service.Name) (PID: $($process.Id)) - logs: $outFile"
        Write-ColorOutput Green $okMsg
        return $true

    } catch {
        $errMsg = "Failed to start $($Service.Name): $($_.Exception.Message)"
        Write-ColorOutput Red $errMsg
        return $false
    }
}

function Wait-ForServices {
    param([int]$MaxWaitSeconds = 300)

    Write-ColorOutput Cyan "`nWaiting for services to start (max $MaxWaitSeconds seconds)..."
    Write-Output ""

    $startTime = Get-Date
    $allRunning = $false
    $checkInterval = 5

    while (-not $allRunning) {
        $elapsed = (Get-Date) - $startTime
        if ($elapsed.TotalSeconds -gt $MaxWaitSeconds) {
            Write-ColorOutput Yellow "Timeout waiting for services to start"
            break
        }

        $runningCount = 0
        foreach ($svc in $Microservices) {
            if (Test-PortInUse -Port $svc.Port) {
                $runningCount++
            }
        }

        $progressMsg = "Progress: $runningCount/$($Microservices.Count) services running (elapsed: $([math]::Round($elapsed.TotalSeconds))s)"
        Write-Output $progressMsg

        if ($runningCount -eq $Microservices.Count) {
            $allRunning = $true
            $successMsg = "All services are running!"
            Write-ColorOutput Green $successMsg
            break
        }

        Start-Sleep -Seconds $checkInterval
    }

    Write-Output ""
    return $allRunning
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  IOE-DREAM Microservices Startup and Verification"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "Start time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output ""

# 1. 检查基础设施
Write-ColorOutput Cyan "`n[Step 1] Checking infrastructure..."
$infraOk = $true

if (-not (Test-PortInUse -Port 3306)) {
    $errMsg = "[ERROR] MySQL (3306) is not running"
    Write-ColorOutput Red $errMsg
    $infraOk = $false
} else {
    $okMsg = "[OK] MySQL (3306) - Running"
    Write-ColorOutput Green $okMsg
}

if (-not (Test-PortInUse -Port 6379)) {
    $errMsg = "[ERROR] Redis (6379) is not running"
    Write-ColorOutput Red $errMsg
    $infraOk = $false
} else {
    $okMsg = "[OK] Redis (6379) - Running"
    Write-ColorOutput Green $okMsg
}

if (-not (Test-PortInUse -Port 8848)) {
    $errMsg = "[ERROR] Nacos (8848) is not running"
    Write-ColorOutput Red $errMsg
    $infraOk = $false
} else {
    $okMsg = "[OK] Nacos (8848) - Running"
    Write-ColorOutput Green $okMsg
}

if (-not $infraOk) {
    $errMsg = "`n[ERROR] Infrastructure services are not running. Please start them first:"
    Write-ColorOutput Red $errMsg
    Write-Output "  docker-compose -f docker-compose-all.yml up -d mysql redis nacos"
    Write-Output ""
    exit 1
}

Write-Output ""

# 2. 构建 microservices-common（如果需要）
if (-not $SkipBuild) {
    Write-ColorOutput Cyan "`n[Step 2] Building microservices-common..."
    $commonPath = Join-Path $ProjectRoot "microservices\microservices-common"

    if (Test-Path $commonPath) {
        try {
            Push-Location $commonPath
            $infoMsg = "Running: mvn clean install -DskipTests (MAVEN_OPTS: $env:MAVEN_OPTS)"
            Write-Output $infoMsg

            # 确保构建时使用正确的环境变量
            # 使用cmd.exe包装确保环境变量正确传递
            $buildCmd = "mvn clean install -DskipTests"
            $process = Start-Process `
                -FilePath "cmd.exe" `
                -ArgumentList "/c", "set MAVEN_OPTS=$env:MAVEN_OPTS && $buildCmd" `
                -NoNewWindow `
                -Wait `
                -PassThru

            if ($process.ExitCode -eq 0) {
                $okMsg = "[OK] microservices-common built successfully"
                Write-ColorOutput Green $okMsg
            } else {
                $errMsg = "[ERROR] Build failed with exit code: $($process.ExitCode)"
                Write-ColorOutput Red $errMsg
                Pop-Location
                exit 1
            }
            Pop-Location
        } catch {
            $errMsg = "[ERROR] Build exception: $($_.Exception.Message)"
            Write-ColorOutput Red $errMsg
            Pop-Location
            exit 1
        }
    } else {
        $warnMsg = "[WARN] microservices-common not found, skipping build"
        Write-ColorOutput Yellow $warnMsg
    }
    Write-Output ""
}

# 3. 启动所有微服务
Write-ColorOutput Cyan "`n[Step 3] Starting microservices..."
Write-Output ""

$startedCount = 0
foreach ($svc in $Microservices) {
    if (Start-Microservice -Service $svc) {
        $startedCount++
    }
    # 增加启动间隔到10秒，避免多个服务同时编译导致内存竞争
    # 给Maven编译足够时间完成，减少内存峰值
    $waitMsg = "Waiting 10 seconds before starting next service (to avoid memory competition)..."
    Write-ColorOutput Gray $waitMsg
    Start-Sleep -Seconds 10
}

Write-Output ""
$infoMsg = "Started $startedCount/$($Microservices.Count) services"
Write-ColorOutput Cyan $infoMsg
Write-Output ""

# 4. 等待服务启动
Write-ColorOutput Cyan "`n[Step 4] Waiting for services to be ready..."
$allReady = Wait-ForServices -MaxWaitSeconds $WaitSeconds

if (-not $allReady) {
    $warnMsg = "[WARN] Not all services are ready, but continuing with verification..."
    Write-ColorOutput Yellow $warnMsg
    Write-Output ""
}

# 5. 执行动态验证
if (-not $SkipVerification) {
    Write-ColorOutput Cyan "`n[Step 5] Executing dynamic verification..."
    Write-Output ""

    $verifyScript = Join-Path $ProjectRoot "scripts\verify-dynamic-validation.ps1"
    if (Test-Path $verifyScript) {
        try {
            & $verifyScript
            $exitCode = $LASTEXITCODE
            Write-Output ""
            if ($exitCode -eq 0) {
                $successMsg = "[SUCCESS] All verifications passed!"
                Write-ColorOutput Green $successMsg
            } else {
                $warnMsg = "[WARN] Some verifications failed. Check the output above."
                Write-ColorOutput Yellow $warnMsg
            }
        } catch {
            $errMsg = "[ERROR] Verification script failed: $($_.Exception.Message)"
            Write-ColorOutput Red $errMsg
        }
    } else {
        $errMsg = "[ERROR] Verification script not found: $verifyScript"
        Write-ColorOutput Red $errMsg
    }
} else {
    $infoMsg = "[SKIP] Verification skipped"
    Write-ColorOutput Yellow $infoMsg
}

Write-Output ""
Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  Startup and Verification Complete"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "End time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output ""
Write-Output "Service URLs:"
foreach ($svc in $Microservices) {
    Write-Output "  - $($svc.Name): http://localhost:$($svc.Port)"
}
Write-Output ""
Write-Output "To stop services (recommended):"
Write-Output "  Get-Content `"$PidFile`" -Raw | ConvertFrom-Json | ForEach-Object { Stop-Process -Id `$_.pid -Force }"
Write-Output ""
