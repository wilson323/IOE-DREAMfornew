# =====================================================
# IOE-DREAM Simple Startup Script - No More Crashes!
# Version: v2.0.0 - Crash-Free Edition
# Description: Start IOE-DREAM microservices safely
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipInfra,

    [Parameter(Mandatory=$false)]
    [switch]$SkipBuild
)

# Set error handling to not exit on errors
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM Simple Startup Script v2.0" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Get project root - fix path detection
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if ($ScriptDir) {
    $ProjectRoot = Split-Path -Parent $ScriptDir
} else {
    $ProjectRoot = (Get-Location).Path
}

# Additional check to ensure we're in the right directory
if (-not (Test-Path (Join-Path $ProjectRoot "microservices"))) {
    # If microservices not found, try current directory
    if (Test-Path (Join-Path (Get-Location).Path "microservices")) {
        $ProjectRoot = (Get-Location).Path
    } else {
        Write-Host "[WARNING] Cannot find microservices directory. Please run from IOE-DREAM project root." -ForegroundColor Yellow
        Write-Host "Current detected project root: $ProjectRoot" -ForegroundColor Yellow
    }
}

Write-Host "Project Root: $ProjectRoot" -ForegroundColor Gray
Write-Host ""

# Load environment variables from .env file
function Load-EnvFile {
    param([string]$EnvFile)

    $envVars = @{}

    if (Test-Path $EnvFile) {
        Write-Host "Loading environment from: $EnvFile" -ForegroundColor Gray
        Get-Content $EnvFile | ForEach-Object {
            if ($_ -match '^([^#=]+)=(.*)$') {
                $key = $matches[1].Trim()
                $value = $matches[2].Trim()
                $envVars[$key] = $value
            }
        }
    } else {
        Write-Host "[WARNING] .env file not found at: $EnvFile" -ForegroundColor Yellow
    }

    return $envVars
}

# Load environment variables
$envFile = Join-Path $ProjectRoot ".env"
$envVars = Load-EnvFile -EnvFile $envFile

# Define services with enablement flags from .env
$Services = @(
    @{ Name = "ioedream-gateway-service"; Port = $envVars["GATEWAY_SERVICE_PORT"]; Path = "microservices\ioedream-gateway-service"; Enabled = $envVars["START_GATEWAY_SERVICE"] -eq "true" },
    @{ Name = "ioedream-common-service"; Port = $envVars["COMMON_SERVICE_PORT"]; Path = "microservices\ioedream-common-service"; Enabled = $envVars["START_COMMON_SERVICE"] -eq "true" },
    @{ Name = "ioedream-device-comm-service"; Port = $envVars["DEVICE_COMM_SERVICE_PORT"]; Path = "microservices\ioedream-device-comm-service"; Enabled = $envVars["START_DEVICE_COMM_SERVICE"] -eq "true" },
    @{ Name = "ioedream-oa-service"; Port = $envVars["OA_SERVICE_PORT"]; Path = "microservices\ioedream-oa-service"; Enabled = $envVars["START_OA_SERVICE"] -eq "true" },
    @{ Name = "ioedream-access-service"; Port = $envVars["ACCESS_SERVICE_PORT"]; Path = "microservices\ioedream-access-service"; Enabled = $envVars["START_ACCESS_SERVICE"] -eq "true" },
    @{ Name = "ioedream-attendance-service"; Port = $envVars["ATTENDANCE_SERVICE_PORT"]; Path = "microservices\ioedream-attendance-service"; Enabled = $envVars["START_ATTENDANCE_SERVICE"] -eq "true" },
    @{ Name = "ioedream-video-service"; Port = $envVars["VIDEO_SERVICE_PORT"]; Path = "microservices\ioedream-video-service"; Enabled = $envVars["START_VIDEO_SERVICE"] -eq "true" },
    @{ Name = "ioedream-consume-service"; Port = $envVars["CONSUME_SERVICE_PORT"]; Path = "microservices\ioedream-consume-service"; Enabled = $envVars["START_CONSUME_SERVICE"] -eq "true" },
    @{ Name = "ioedream-visitor-service"; Port = $envVars["VISITOR_SERVICE_PORT"]; Path = "microservices\ioedream-visitor-service"; Enabled = $envVars["START_VISITOR_SERVICE"] -eq "true" }
)

# Simple port test function
function Test-Port {
    param([int]$Port)
    try {
        $tcpClient = New-Object System.Net.Sockets.TcpClient
        $asyncResult = $tcpClient.BeginConnect("127.0.0.1", $Port, $null, $null)
        $wait = $asyncResult.AsyncWaitHandle.WaitOne(2000, $false)
        if ($wait) {
            $tcpClient.EndConnect($asyncResult)
            $tcpClient.Close()
            return $true
        } else {
            $tcpClient.Close()
            return $false
        }
    } catch {
        return $false
    }
}

# Step 1: Check infrastructure
if (-not $SkipInfra) {
    Write-Host "[Step 1] Checking infrastructure services..." -ForegroundColor Cyan

    $infraStatus = @{}

    # Check MySQL
    if (Test-Port -Port 3306) {
        Write-Host "  ✓ MySQL (3306) - Running" -ForegroundColor Green
        $infraStatus["MySQL"] = $true
    } else {
        Write-Host "  ✗ MySQL (3306) - Not running" -ForegroundColor Red
        $infraStatus["MySQL"] = $false
    }

    # Check Redis
    if (Test-Port -Port 6379) {
        Write-Host "  ✓ Redis (6379) - Running" -ForegroundColor Green
        $infraStatus["Redis"] = $true
    } else {
        Write-Host "  ✗ Redis (6379) - Not running" -ForegroundColor Red
        $infraStatus["Redis"] = $false
    }

    # Check Nacos
    if (Test-Port -Port 8848) {
        Write-Host "  ✓ Nacos (8848) - Running" -ForegroundColor Green
        $infraStatus["Nacos"] = $true
    } else {
        Write-Host "  ✗ Nacos (8848) - Not running" -ForegroundColor Red
        $infraStatus["Nacos"] = $false
    }

    # If any infra service is down, offer to start Docker
    $infraDown = @($infraStatus.Values | Where-Object { $_ -eq $false }).Count
    if ($infraDown -gt 0) {
        Write-Host ""
        Write-Host "[WARNING] $infraDown infrastructure services are not running!" -ForegroundColor Yellow
        Write-Host "Would you like to start them with Docker? (y/n)" -ForegroundColor Yellow
        $choice = Read-Host
        if ($choice -eq "y" -or $choice -eq "Y") {
            try {
                Write-Host "Starting infrastructure with Docker..." -ForegroundColor Gray
                Set-Location $ProjectRoot
                docker-compose -f docker-compose-all.yml up -d mysql redis nacos
                Write-Host "Waiting 60 seconds for services to start..." -ForegroundColor Yellow
                Start-Sleep -Seconds 60
                Write-Host "Infrastructure startup completed!" -ForegroundColor Green
            } catch {
                Write-Host "[ERROR] Failed to start infrastructure: $($_.Exception.Message)" -ForegroundColor Red
                Write-Host "Please start MySQL, Redis, and Nacos manually" -ForegroundColor Yellow
            }
        }
    }
    Write-Host ""
}

# Step 2: Build project (if needed)
if (-not $SkipBuild) {
    Write-Host "[Step 2] Building project..." -ForegroundColor Cyan

    $microservicesDir = Join-Path $ProjectRoot "microservices"

    if (-not (Test-Path $microservicesDir)) {
        Write-Host "[ERROR] Cannot find microservices directory at: $microservicesDir" -ForegroundColor Red
        Write-Host "[WARNING] Skipping build step and continuing with startup..." -ForegroundColor Yellow
    } else {
        try {
            Set-Location $microservicesDir
            Write-Host "Current directory: $(Get-Location)" -ForegroundColor Gray

            # Build in correct order: common-core -> common -> other services
            Write-Host "Building microservices-common-core..." -ForegroundColor Gray
            mvn clean install -pl microservices-common-core -am -DskipTests
            if ($LASTEXITCODE -ne 0) {
                Write-Host "[WARNING] Common core build had issues, continuing..." -ForegroundColor Yellow
            }

            Write-Host "Building microservices-common..." -ForegroundColor Gray
            mvn clean install -pl microservices-common -am -DskipTests
            if ($LASTEXITCODE -ne 0) {
                Write-Host "[WARNING] Common build had issues, continuing..." -ForegroundColor Yellow
            }

            Write-Host "Build step completed!" -ForegroundColor Green
        } catch {
            Write-Host "[WARNING] Build encountered issues: $($_.Exception.Message)" -ForegroundColor Yellow
            Write-Host "Continuing with startup anyway..." -ForegroundColor Yellow
        }
    }
    Write-Host ""
}

# Step 3: Start microservices
Write-Host "[Step 3] Starting microservices..." -ForegroundColor Cyan

# Display which services are enabled/disabled
Write-Host "Service Configuration:" -ForegroundColor Gray
$enabledServices = 0
$disabledServices = 0
foreach ($service in $Services) {
    if ($service.Enabled) {
        Write-Host "  ✓ $($service.Name) (Port: $($service.Port)) - Enabled" -ForegroundColor Green
        $enabledServices++
    } else {
        Write-Host "  ✗ $($service.Name) (Port: $($service.Port)) - Disabled" -ForegroundColor Yellow
        $disabledServices++
    }
}
Write-Host "Enabled: $enabledServices, Disabled: $disabledServices" -ForegroundColor Gray
Write-Host ""

$startedServices = 0
$failedServices = 0

foreach ($service in $Services) {
    # Skip disabled services
    if (-not $service.Enabled) {
        continue
    }
    $servicePath = Join-Path $ProjectRoot $service.Path

    # Check if service directory exists
    if (-not (Test-Path $servicePath)) {
        Write-Host "  ✗ $($service.Name) - Directory not found" -ForegroundColor Red
        $failedServices++
        continue
    }

    # Check if port is already in use
    if (Test-Port -Port $service.Port) {
        Write-Host "  → $($service.Name) - Already running (Port: $($service.Port))" -ForegroundColor Yellow
        continue
    }

    Write-Host "  → Starting $($service.Name) (Port: $($service.Port))..." -ForegroundColor Gray

    try {
        # Create batch file for this service
        $batContent = @"
@echo off
cd /d "$servicePath"
echo Starting $($service.Name)...
mvn spring-boot:run -DskipTests -Dpmd.skip=true
pause
"@

        $batFile = Join-Path $env:TEMP "start-$($service.Name).bat"
        [System.IO.File]::WriteAllText($batFile, $batContent, [System.Text.Encoding]::ASCII)

        # Start service in new window
        $process = Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "`"$batFile`"" -PassThru

        Write-Host "    Started (PID: $($process.Id))" -ForegroundColor Green
        $startedServices++

        # Give service time to start
        Start-Sleep -Seconds 3

    } catch {
        Write-Host "  ✗ Failed to start $($service.Name): $($_.Exception.Message)" -ForegroundColor Red
        $failedServices++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Startup Summary:" -ForegroundColor Cyan
Write-Host "  Started: $startedServices services" -ForegroundColor Green
Write-Host "  Failed: $failedServices services" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Services will take 2-5 minutes to fully start." -ForegroundColor Yellow
Write-Host "You can check individual service status by accessing:" -ForegroundColor Gray
foreach ($service in $Services) {
    Write-Host "  - $($service.Name): http://localhost:$($service.Port)" -ForegroundColor Gray
}
Write-Host ""

Write-Host "Press any key to exit..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")