# =====================================================
# IOE-DREAM 启动所有微服务脚本（带环境变量）
# 版本: v1.0.0
# 描述: 从.env文件加载环境变量后启动所有微服务
# 创建时间: 2025-12-15
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild,

    [Parameter(Mandatory=$false)]
    [switch]$WaitForReady
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

function Start-Microservice {
    param(
        [hashtable]$Service
    )

    $servicePath = Join-Path $ProjectRoot $Service.Path

    if (-not (Test-Path $servicePath)) {
        $errMsg = "[ERROR] Service not found: $($Service.Name)"
        Write-ColorOutput Red $errMsg
        return $false
    }

    # Check if port is already in use
    if (Test-PortInUse -Port $Service.Port) {
        $warnMsg = "[WARN] Port $($Service.Port) already in use, skipping $($Service.Name)"
        Write-ColorOutput Yellow $warnMsg
        return $true
    }

    $infoMsg = "[START] Starting $($Service.Name) (Port: $($Service.Port))..."
    Write-ColorOutput Cyan $infoMsg

    try {
        # 创建临时批处理文件来设置环境变量并启动服务
        $batFile = Join-Path $env:TEMP "start-$($Service.Name)-$(Get-Date -Format 'yyyyMMddHHmmss').bat"
        
        # 构建批处理文件内容
        $batContent = "@echo off`r`n"
        $batContent += "cd /d `"$servicePath`"`r`n`r`n"
        $batContent += "REM 设置环境变量`r`n"
        
        # 添加所有环境变量
        Get-ChildItem Env: | Where-Object { 
            $_.Name -match '^(MYSQL_|REDIS_|NACOS_|SPRING_|GATEWAY_|JAVA_|MAVEN_|LOG_|MANAGEMENT_|DOCKER_|DRUID_|REDIS_POOL_|.*_SERVICE_PORT)' 
        } | ForEach-Object {
            $value = $_.Value
            # 转义特殊字符：双引号、&、|、<、>、^
            $value = $value -replace '"', '""'  # 转义双引号
            $value = $value -replace '&', '^&'   # 转义&符号（命令分隔符）
            $value = $value -replace '\|', '^|'  # 转义|符号（管道符）
            $value = $value -replace '<', '^<'   # 转义<符号
            $value = $value -replace '>', '^>'   # 转义>符号
            $value = $value -replace '\^', '^^'  # 转义^符号（需要双写）
            # 如果值包含空格或特殊字符，用引号包裹
            if ($value -match '[ &|<>^]' -or $value.Contains(' ')) {
                $batContent += "set $($_.Name)=`"$value`"`r`n"
            } else {
                $batContent += "set $($_.Name)=$value`r`n"
            }
        }
        
        $batContent += "`r`nREM 显示关键环境变量（用于调试）`r`n"
        $batContent += "echo [INFO] Starting $($Service.Name) service...`r`n"
        $batContent += "echo [INFO] MYSQL_HOST=%MYSQL_HOST%`r`n"
        $batContent += "echo [INFO] NACOS_SERVER_ADDR=%NACOS_SERVER_ADDR%`r`n"
        $batContent += "echo [INFO] REDIS_HOST=%REDIS_HOST%`r`n"
        $batContent += "echo.`r`n"
        $batContent += "`r`nREM 启动服务`r`n"
        $batContent += "mvn spring-boot:run`r`n"
        $batContent += "pause`r`n"
        
        # 写入批处理文件（使用ASCII编码避免编码问题）
        [System.IO.File]::WriteAllText($batFile, $batContent, [System.Text.Encoding]::ASCII)

        # Start service in new window using batch file
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = "cmd.exe"
        $startInfo.Arguments = "/k `"$batFile`""
        $startInfo.UseShellExecute = $true
        $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal

        $process = [System.Diagnostics.Process]::Start($startInfo)

        $okMsg = "[OK] $($Service.Name) started (PID: $($process.Id))"
        Write-ColorOutput Green $okMsg
        return $true

    } catch {
        $errMsg = "[ERROR] Failed to start $($Service.Name): $($_.Exception.Message)"
        Write-ColorOutput Red $errMsg
        return $false
    }
}

function Wait-ForServices {
    param([int]$MaxWaitSeconds = 600)

    Write-ColorOutput Cyan "`nWaiting for all services to be ready (max $MaxWaitSeconds seconds)..."
    Write-Output ""

    $startTime = Get-Date
    $checkInterval = 10
    $lastCount = 0

    while ($true) {
        $elapsed = (Get-Date) - $startTime
        if ($elapsed.TotalSeconds -gt $MaxWaitSeconds) {
            $timeoutMsg = "[WARN] Timeout waiting for services to start"
            Write-ColorOutput Yellow $timeoutMsg
            break
        }

        $runningCount = 0
        $runningServices = @()
        foreach ($svc in $Microservices) {
            if (Test-PortInUse -Port $svc.Port) {
                $runningCount++
                $runningServices += $svc.Name
            }
        }

        if ($runningCount -ne $lastCount) {
            $progressMsg = "[$([math]::Round($elapsed.TotalSeconds))s] Running: $runningCount/$($Microservices.Count) services"
            Write-Output $progressMsg
            $lastCount = $runningCount
        }

        if ($runningCount -eq $Microservices.Count) {
            $successMsg = "`n[SUCCESS] All $runningCount services are running!"
            Write-ColorOutput Green $successMsg
            Write-Output ""
            return $true
        }

        Start-Sleep -Seconds $checkInterval
    }

    Write-Output ""
    return $false
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  IOE-DREAM Start All Microservices"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "Start time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output "Environment variables loaded from: D:\IOE-DREAM\.env"
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
    $errMsg = "`n[ERROR] Infrastructure services are not running!"
    Write-ColorOutput Red $errMsg
    Write-Output "Please start them first:"
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
            $infoMsg = "Running: mvn clean install -DskipTests"
            Write-Output $infoMsg
            Write-Output ""

            $process = Start-Process -FilePath "mvn" -ArgumentList "clean", "install", "-DskipTests" -NoNewWindow -Wait -PassThru

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
} else {
    $skipMsg = "[SKIP] Build skipped (-SkipBuild flag)"
    Write-ColorOutput Yellow $skipMsg
    Write-Output ""
}

# 3. 启动所有微服务
Write-ColorOutput Cyan "`n[Step 3] Starting all microservices..."
Write-Output ""

$startedCount = 0
$skippedCount = 0
$failedCount = 0

foreach ($svc in $Microservices) {
    $result = Start-Microservice -Service $svc
    if ($result) {
        if (Test-PortInUse -Port $svc.Port) {
            $skippedCount++
        } else {
            $startedCount++
        }
    } else {
        $failedCount++
    }
    Start-Sleep -Seconds 5
}

Write-Output ""
$summaryMsg = "Summary: Started=$startedCount, AlreadyRunning=$skippedCount, Failed=$failedCount"
Write-ColorOutput Cyan $summaryMsg
Write-Output ""

# 4. 等待服务启动（如果需要）
if ($WaitForReady) {
    $allReady = Wait-ForServices -MaxWaitSeconds 600

    if ($allReady) {
        Write-ColorOutput Green "`n[SUCCESS] All services are ready!"
    } else {
        Write-ColorOutput Yellow "`n[WARN] Not all services are ready, but startup process completed."
    }
    Write-Output ""
} else {
    $infoMsg = "[INFO] Services are starting in background. Wait 2-5 minutes for all services to be ready."
    Write-ColorOutput Yellow $infoMsg
    Write-Output ""
    $tipMsg = "[TIP] Use -WaitForReady flag to wait for all services to be ready."
    Write-ColorOutput Cyan $tipMsg
    Write-Output ""
}

# 5. 显示服务状态
Write-ColorOutput Cyan "`n[Step 4] Service Status:"
Write-Output ""

foreach ($svc in $Microservices) {
    if (Test-PortInUse -Port $svc.Port) {
        $okMsg = "  [OK] $($svc.Name) - Running (Port: $($svc.Port))"
        Write-ColorOutput Green $okMsg
    } else {
        $warnMsg = "  [XX] $($svc.Name) - Not running (Port: $($svc.Port))"
        Write-ColorOutput Red $warnMsg
    }
}

Write-Output ""

# 6. 显示访问地址
Write-ColorOutput Cyan "`n[Step 5] Service URLs:"
Write-Output ""
foreach ($svc in $Microservices) {
    Write-Output "  - $($svc.Name): http://localhost:$($svc.Port)"
}
Write-Output ""

Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  Startup Complete!"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "End time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output ""

