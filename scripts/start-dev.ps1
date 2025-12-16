<#
.SYNOPSIS
    IOE-DREAM 开发环境启动脚本
.DESCRIPTION
    开发环境启动前后端和移动端服务（不使用Docker）
.PARAMETER BackendOnly
    仅启动后端服务
.PARAMETER FrontendOnly
    仅启动前端服务
.PARAMETER MobileOnly
    仅启动移动端服务
.PARAMETER SkipBuild
    跳过后端构建
.EXAMPLE
    .\start-dev.ps1
    .\start-dev.ps1 -BackendOnly
    .\start-dev.ps1 -FrontendOnly
    .\start-dev.ps1 -SkipBuild
.NOTES
    Version: v1.0.1
    Author: IOE-DREAM Team
#>

param(
    [switch]$BackendOnly,
    [switch]$FrontendOnly,
    [switch]$MobileOnly,
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# =====================================================
# Global Variables
# =====================================================
$script:ProjectRoot = Split-Path -Parent $PSScriptRoot
if (-not $script:ProjectRoot -or $script:ProjectRoot -eq "") {
    $script:ProjectRoot = (Get-Location).Path
}

# Microservices configuration (startup order)
$script:Microservices = @(
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

# Frontend configuration
$script:FrontendConfig = @{
    Name = "smart-admin-web-javascript"
    Path = "smart-admin-web-javascript"
    Port = 3000
    DevCommand = "npm run dev"
}

# Mobile configuration
$script:MobileConfig = @{
    Name = "smart-app"
    Path = "smart-app"
    Port = 8081
    DevCommand = "npm run dev:h5"
}

# =====================================================
# Utility Functions
# =====================================================
function Write-Log {
    param(
        [string]$Message,
        [string]$Level = "INFO"
    )

    $color = switch ($Level) {
        "INFO" { "White" }
        "OK" { "Green" }
        "WARN" { "Yellow" }
        "ERROR" { "Red" }
        "TITLE" { "Cyan" }
        "GRAY" { "Gray" }
        default { "White" }
    }

    Write-Host $Message -ForegroundColor $color
}

# =====================================================
# Infrastructure Check
# =====================================================
function Test-Infrastructure {
    Write-Log "========================================" "TITLE"
    Write-Log "  Infrastructure Check" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""

    $allGood = $true

    # Check MySQL (port 3306)
    if (Test-PortInUse -Port 3306) {
        Write-Log "  [OK] MySQL (Port 3306) - Running" "OK"
    } else {
        Write-Log "  [XX] MySQL (Port 3306) - Not running" "ERROR"
        $allGood = $false
    }

    # Check Redis (port 6379)
    if (Test-PortInUse -Port 6379) {
        Write-Log "  [OK] Redis (Port 6379) - Running" "OK"
    } else {
        Write-Log "  [XX] Redis (Port 6379) - Not running" "ERROR"
        $allGood = $false
    }

    # Check Nacos (port 8848)
    if (Test-PortInUse -Port 8848) {
        Write-Log "  [OK] Nacos (Port 8848) - Running" "OK"
    } else {
        Write-Log "  [XX] Nacos (Port 8848) - Not running" "ERROR"
        $allGood = $false
    }

    Write-Log ""

    if (-not $allGood) {
        Write-Log "[ERROR] Infrastructure services not running!" "ERROR"
        Write-Log ""
        Write-Log "Please start infrastructure first:" "WARN"
        Write-Log "  Option 1: Use Docker Compose" "INFO"
        Write-Log "    docker-compose -f docker-compose-all.yml up -d mysql redis nacos" "GRAY"
        Write-Log ""
        Write-Log "  Option 2: Start manually" "INFO"
        Write-Log "    - MySQL: Port 3306" "GRAY"
        Write-Log "    - Redis: Port 6379" "GRAY"
        Write-Log "    - Nacos: Port 8848 (http://localhost:8848/nacos)" "GRAY"
        Write-Log ""
        return $false
    }

    Write-Log "[OK] All infrastructure services running" "OK"
    Write-Log ""
    return $true
}

function Test-PortInUse {
    param([int]$Port)

    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $Port, $null, $null)
        $wait = $asyncResult.AsyncWaitHandle.WaitOne(500, $false)

        if ($wait) {
            try {
                $tcpClient.EndConnect($asyncResult)
                $tcpClient.Close()
                return $true
            } catch {
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

function Test-CommandAvailable {
    param([string]$Command)

    $oldPreference = $ErrorActionPreference
    $ErrorActionPreference = "SilentlyContinue"
    try {
        if (Get-Command $Command) {
            return $true
        }
    } catch {
        return $false
    } finally {
        $ErrorActionPreference = $oldPreference
    }
    return $false
}

# =====================================================
# Environment Check
# =====================================================
function Test-DevEnvironment {
    Write-Log "========================================" "TITLE"
    Write-Log "  Environment Check" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""

    $allGood = $true

    # Check Java
    if (Test-CommandAvailable "java") {
        try {
            $javaOutput = & java -version 2>&1
            $javaVer = ($javaOutput | Out-String).Trim().Split("`n")[0]
            Write-Log "  [OK] Java: $javaVer" "OK"
        } catch {
            Write-Log "  [OK] Java: Found" "OK"
        }
    } else {
        Write-Log "  [ERROR] Java not found" "ERROR"
        $allGood = $false
    }

    # Check Maven
    if (Test-CommandAvailable "mvn") {
        try {
            $mvnOutput = & mvn --version 2>&1
            $mvnVer = ($mvnOutput | Out-String).Trim().Split("`n")[0]
            Write-Log "  [OK] Maven: $mvnVer" "OK"
        } catch {
            Write-Log "  [OK] Maven: Found" "OK"
        }
    } else {
        Write-Log "  [ERROR] Maven not found" "ERROR"
        $allGood = $false
    }

    # Check Node.js
    if (Test-CommandAvailable "node") {
        try {
            $nodeVer = (& node --version 2>&1 | Out-String).Trim()
            Write-Log "  [OK] Node.js: $nodeVer" "OK"
        } catch {
            Write-Log "  [OK] Node.js: Found" "OK"
        }
    } else {
        Write-Log "  [ERROR] Node.js not found" "ERROR"
        $allGood = $false
    }

    # Check npm
    if (Test-CommandAvailable "npm") {
        try {
            $npmVer = (& npm --version 2>&1 | Out-String).Trim()
            Write-Log "  [OK] npm: $npmVer" "OK"
        } catch {
            Write-Log "  [OK] npm: Found" "OK"
        }
    } else {
        Write-Log "  [ERROR] npm not found" "ERROR"
        $allGood = $false
    }

    Write-Log ""

    if (-not $allGood) {
        Write-Log "[ERROR] Environment check failed. Please install required tools." "ERROR"
        return $false
    }

    Write-Log "[OK] Environment check passed" "OK"
    Write-Log ""
    return $true
}

# =====================================================
# Build Backend
# =====================================================
function Build-BackendCommon {
    if ($SkipBuild) {
        Write-Log "[SKIP] Build skipped (-SkipBuild flag)" "WARN"
        return $true
    }

    Write-Log "========================================" "TITLE"
    Write-Log "  Building microservices-common" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""

    $commonPath = Join-Path $script:ProjectRoot "microservices\microservices-common"

    if (-not (Test-Path $commonPath)) {
        Write-Log "[ERROR] microservices-common not found: $commonPath" "ERROR"
        return $false
    }

    try {
        Push-Location $commonPath
        Write-Log "  Running: mvn clean install -DskipTests" "GRAY"
        Write-Log ""

        $process = Start-Process -FilePath "mvn" -ArgumentList "clean", "install", "-DskipTests" -NoNewWindow -Wait -PassThru

        if ($process.ExitCode -eq 0) {
            Write-Log ""
            Write-Log "[OK] microservices-common built successfully" "OK"
            Pop-Location
            return $true
        } else {
            Write-Log ""
            Write-Log "[ERROR] Build failed with exit code: $($process.ExitCode)" "ERROR"
            Pop-Location
            return $false
        }
    } catch {
        Write-Log "[ERROR] Build exception: $($_.Exception.Message)" "ERROR"
        Pop-Location
        return $false
    }
}

# =====================================================
# Start Backend Services
# =====================================================
function Start-BackendMicroservices {
    Write-Log "========================================" "TITLE"
    Write-Log "  Starting Backend Services" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""

    foreach ($svc in $script:Microservices) {
        $svcPath = Join-Path $script:ProjectRoot $svc.Path

        if (-not (Test-Path $svcPath)) {
            Write-Log "  [WARN] Service not found: $($svc.Name)" "WARN"
            continue
        }

        # Check if port is already in use
        if (Test-PortInUse -Port $svc.Port) {
            Write-Log "  [WARN] Port $($svc.Port) already in use, skipping $($svc.Name)" "WARN"
            continue
        }

        Write-Log "  [START] $($svc.Name) (Port: $($svc.Port))" "INFO"

        try {
            # Start service in new window
            $startInfo = New-Object System.Diagnostics.ProcessStartInfo
            $startInfo.FileName = "cmd.exe"
            $startInfo.Arguments = "/k cd /d `"$svcPath`" && mvn spring-boot:run"
            $startInfo.UseShellExecute = $true
            $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal

            $process = [System.Diagnostics.Process]::Start($startInfo)

            Write-Log "  [OK] $($svc.Name) started (PID: $($process.Id))" "OK"

            # Wait a bit before starting next service
            Start-Sleep -Seconds 5

        } catch {
            Write-Log "  [ERROR] Failed to start $($svc.Name): $($_.Exception.Message)" "ERROR"
        }
    }

    Write-Log ""
    Write-Log "[INFO] Backend services startup initiated" "INFO"
    Write-Log "[INFO] Wait 2-5 minutes for all services to be ready" "WARN"
    Write-Log ""
}

# =====================================================
# Start Frontend Service
# =====================================================
function Start-FrontendDev {
    Write-Log "========================================" "TITLE"
    Write-Log "  Starting Frontend Service" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""

    $frontendPath = Join-Path $script:ProjectRoot $script:FrontendConfig.Path

    if (-not (Test-Path $frontendPath)) {
        Write-Log "[ERROR] Frontend not found: $frontendPath" "ERROR"
        return
    }

    # Check if port is already in use
    if (Test-PortInUse -Port $script:FrontendConfig.Port) {
        Write-Log "[WARN] Port $($script:FrontendConfig.Port) already in use" "WARN"
        return
    }

    # Check and install dependencies
    $nodeModules = Join-Path $frontendPath "node_modules"
    if (-not (Test-Path $nodeModules)) {
        Write-Log "  [INSTALL] Installing npm dependencies..." "INFO"
        try {
            Push-Location $frontendPath
            $process = Start-Process -FilePath "npm" -ArgumentList "install" -NoNewWindow -Wait -PassThru
            if ($process.ExitCode -ne 0) {
                Write-Log "  [ERROR] npm install failed" "ERROR"
                Pop-Location
                return
            }
            Pop-Location
        } catch {
            Write-Log "  [ERROR] npm install exception: $($_.Exception.Message)" "ERROR"
            Pop-Location
            return
        }
    }

    Write-Log "  [START] $($script:FrontendConfig.Name) (Port: $($script:FrontendConfig.Port))" "INFO"

    try {
        # Start frontend in new window
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = "cmd.exe"
        $startInfo.Arguments = "/k cd /d `"$frontendPath`" && npm run dev"
        $startInfo.UseShellExecute = $true
        $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal

        $process = [System.Diagnostics.Process]::Start($startInfo)

        Write-Log "  [OK] Frontend started (PID: $($process.Id))" "OK"
        Write-Log "  [URL] http://localhost:$($script:FrontendConfig.Port)" "GRAY"

    } catch {
        Write-Log "  [ERROR] Failed to start frontend: $($_.Exception.Message)" "ERROR"
    }

    Write-Log ""
}

# =====================================================
# Start Mobile Service
# =====================================================
function Start-MobileDev {
    Write-Log "========================================" "TITLE"
    Write-Log "  Starting Mobile H5 Service" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""

    $mobilePath = Join-Path $script:ProjectRoot $script:MobileConfig.Path

    if (-not (Test-Path $mobilePath)) {
        Write-Log "[ERROR] Mobile app not found: $mobilePath" "ERROR"
        return
    }

    # Check if port is already in use
    if (Test-PortInUse -Port $script:MobileConfig.Port) {
        Write-Log "[WARN] Port $($script:MobileConfig.Port) already in use" "WARN"
        return
    }

    # Check and install dependencies
    $nodeModules = Join-Path $mobilePath "node_modules"
    if (-not (Test-Path $nodeModules)) {
        Write-Log "  [INSTALL] Installing npm dependencies..." "INFO"
        try {
            Push-Location $mobilePath
            $process = Start-Process -FilePath "npm" -ArgumentList "install" -NoNewWindow -Wait -PassThru
            if ($process.ExitCode -ne 0) {
                Write-Log "  [ERROR] npm install failed" "ERROR"
                Pop-Location
                return
            }
            Pop-Location
        } catch {
            Write-Log "  [ERROR] npm install exception: $($_.Exception.Message)" "ERROR"
            Pop-Location
            return
        }
    }

    Write-Log "  [START] $($script:MobileConfig.Name) (Port: $($script:MobileConfig.Port))" "INFO"

    try {
        # Start mobile in new window
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = "cmd.exe"
        $startInfo.Arguments = "/k cd /d `"$mobilePath`" && npm run dev:h5"
        $startInfo.UseShellExecute = $true
        $startInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal

        $process = [System.Diagnostics.Process]::Start($startInfo)

        Write-Log "  [OK] Mobile H5 started (PID: $($process.Id))" "OK"
        Write-Log "  [URL] http://localhost:$($script:MobileConfig.Port)" "GRAY"

    } catch {
        Write-Log "  [ERROR] Failed to start mobile: $($_.Exception.Message)" "ERROR"
    }

    Write-Log ""
}

# =====================================================
# Show Service Status
# =====================================================
function Show-Status {
    Write-Log "========================================" "TITLE"
    Write-Log "  Service Status" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""

    Write-Log "  Backend Services:" "INFO"
    foreach ($svc in $script:Microservices) {
        if (Test-PortInUse -Port $svc.Port) {
            Write-Log "    [OK] $($svc.Name) - Running (Port: $($svc.Port))" "OK"
        } else {
            Write-Log "    [XX] $($svc.Name) - Not running (Port: $($svc.Port))" "ERROR"
        }
    }

    Write-Log ""
    Write-Log "  Frontend Services:" "INFO"

    if (Test-PortInUse -Port $script:FrontendConfig.Port) {
        Write-Log "    [OK] $($script:FrontendConfig.Name) - Running (Port: $($script:FrontendConfig.Port))" "OK"
    } else {
        Write-Log "    [XX] $($script:FrontendConfig.Name) - Not running (Port: $($script:FrontendConfig.Port))" "ERROR"
    }

    if (Test-PortInUse -Port $script:MobileConfig.Port) {
        Write-Log "    [OK] $($script:MobileConfig.Name) - Running (Port: $($script:MobileConfig.Port))" "OK"
    } else {
        Write-Log "    [XX] $($script:MobileConfig.Name) - Not running (Port: $($script:MobileConfig.Port))" "ERROR"
    }

    Write-Log ""
}

# =====================================================
# Show Access URLs
# =====================================================
function Show-URLs {
    Write-Log "========================================" "TITLE"
    Write-Log "  Access URLs" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""
    Write-Log "  Frontend Admin:  http://localhost:$($script:FrontendConfig.Port)" "INFO"
    Write-Log "  Mobile H5:       http://localhost:$($script:MobileConfig.Port)" "INFO"
    Write-Log "  API Gateway:     http://localhost:8080" "INFO"
    Write-Log ""
}

# =====================================================
# Main Entry
# =====================================================
function Main {
    Write-Log ""
    Write-Log "========================================" "TITLE"
    Write-Log "  IOE-DREAM Dev Environment Startup" "TITLE"
    Write-Log "========================================" "TITLE"
    Write-Log ""
    Write-Log "  Project: $script:ProjectRoot" "GRAY"
    Write-Log ""

    # Environment check
    if (-not (Test-DevEnvironment)) {
        exit 1
    }

    # Infrastructure check (only for backend)
    if (-not $FrontendOnly -and -not $MobileOnly) {
        if (-not (Test-Infrastructure)) {
            Write-Host "[WARN] Continue without infrastructure? (y/N): " -ForegroundColor Yellow -NoNewline
            $continue = Read-Host
            if ($continue -ne "y" -and $continue -ne "Y") {
                Write-Log "[INFO] Exiting. Please start infrastructure first." "INFO"
                exit 0
            }
            Write-Log ""
        }
    }

    # Build backend common module
    if (-not $FrontendOnly -and -not $MobileOnly) {
        if (-not (Build-BackendCommon)) {
            Write-Log "[ERROR] Backend build failed, exiting..." "ERROR"
            exit 1
        }
    }

    # Start services based on parameters
    if (-not $FrontendOnly -and -not $MobileOnly) {
        Start-BackendMicroservices
    }

    if (-not $BackendOnly -and -not $MobileOnly) {
        Start-FrontendDev
    }

    if (-not $BackendOnly -and -not $FrontendOnly) {
        Start-MobileDev
    }

    # Wait a bit and show status
    Write-Log "[INFO] Waiting for services to start..." "INFO"
    Start-Sleep -Seconds 10

    Show-Status
    Show-URLs

    Write-Log "========================================" "TITLE"
    Write-Log "  Startup Complete!" "OK"
    Write-Log "========================================" "TITLE"
    Write-Log ""
    Write-Log "[TIP] Each service runs in its own CMD window." "WARN"
    Write-Log "[TIP] Close the CMD windows to stop services." "WARN"
    Write-Log ""
}

# Run
try {
    Main
} catch {
    Write-Log "[ERROR] Script failed: $($_.Exception.Message)" "ERROR"
    Write-Log "  Line: $($_.InvocationInfo.ScriptLineNumber)" "ERROR"
    exit 1
}
