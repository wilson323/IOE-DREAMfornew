# -*- coding: utf-8-with-bom -*-
<#
.SYNOPSIS
    IOE-DREAM Smart Campus Platform - Anti-Flash Exit Startup Script

.DESCRIPTION
    Enterprise-grade startup script with the following features:
    - Encoding Standardization: Auto-detect and fix encoding issues, compatible with PowerShell 5.1 and 7.x
    - Multi-Layer Exception Protection: 5-layer protection mechanism, ensuring script never crashes
    - Version Compatibility: Auto-adapt to different PowerShell versions
    - User Interaction: Complete user interaction and wait mechanism

.PARAMETER StatusOnly
    Check service status only, do not start services

.EXAMPLE
    .\start.ps1
    .\start.ps1 -StatusOnly

.NOTES
    Version: v5.1.0 - Interactive Menu Edition
    Encoding: UTF-8 with BOM (Required)
    Compatibility: PowerShell 5.1+ and PowerShell Core 7.0+
    Author: IOE-DREAM Architecture Committee

    Features:
    - Interactive menu system for easy service management
    - Multi-layer exception protection
    - Automatic encoding detection and compatibility
    - Service status monitoring
#>

# Parameter definition (must be at the top of script)
param([switch]$StatusOnly, [switch]$NoMenu)

# ============================================================================
# Layer 1: Encoding Standardization and Version Compatibility Check
# ============================================================================
function Initialize-EncodingEnvironment {
    <#
    .SYNOPSIS
        Initialize encoding environment to ensure correct character display
    #>
    try {
        # Detect PowerShell version
        $psVersion = $PSVersionTable.PSVersion.Major
        $isPowerShell7 = $psVersion -ge 7

        # PowerShell 7.x encoding settings
        if ($isPowerShell7) {
            $PSDefaultParameterValues['*:Encoding'] = 'utf8'
            [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
            [Console]::InputEncoding = [System.Text.Encoding]::UTF8
        } else {
            # Windows PowerShell 5.1 encoding settings
            $OutputEncoding = New-Object System.Text.UTF8Encoding $false
            [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
            try {
                chcp 65001 | Out-Null
            } catch {
                # Fallback: Continue execution if chcp fails
            }
        }

        return @{
            Success = $true
            PowerShellVersion = $psVersion
            IsPowerShell7 = $isPowerShell7
        }
    } catch {
        # Fallback: Continue execution even if encoding setup fails
        return @{
            Success = $false
            PowerShellVersion = $PSVersionTable.PSVersion.Major
            IsPowerShell7 = $false
            Error = $_.Exception.Message
        }
    }
}

# ============================================================================
# Layer 2: Script Encoding Detection and Fix
# ============================================================================
function Test-ScriptEncoding {
    <#
    .SYNOPSIS
        Detect script file encoding format
    #>
    param([string]$ScriptPath)

    if (-not (Test-Path $ScriptPath)) {
        return @{
            HasBOM = $false
            VersionCompatible = $false
            NeedsFix = $false
            Error = "File not found"
        }
    }

    try {
        $bytes = [System.IO.File]::ReadAllBytes($ScriptPath)
        $hasBom = $bytes.Length -ge 3 -and
                   $bytes[0] -eq 0xEF -and
                   $bytes[1] -eq 0xBB -and
                   $bytes[2] -eq 0xBF

        $psVersion = $PSVersionTable.PSVersion.Major
        $isPowerShell7 = $psVersion -ge 7

        # PowerShell 5.1 requires BOM, PowerShell 7.x supports BOM-less
        $versionCompatible = if ($isPowerShell7) { $true } else { $hasBom }
        $needsFix = ($psVersion -lt 7) -and (-not $hasBom)

        return @{
            HasBOM = $hasBom
            VersionCompatible = $versionCompatible
            NeedsFix = $needsFix
            PowerShellVersion = $psVersion
            IsPowerShell7 = $isPowerShell7
        }
    } catch {
        return @{
            HasBOM = $false
            VersionCompatible = $false
            NeedsFix = $false
            Error = $_.Exception.Message
        }
    }
}

# ============================================================================
# Layer 3: Safe Log Function (Multi-Layer Protection)
# ============================================================================
function Write-SafeLog {
    <#
    .SYNOPSIS
        Safe log output function with multi-layer exception protection
    #>
    param(
        [string]$Message,
        [string]$Color = "White",
        [switch]$NoNewline
    )

    # Layer 1: Try Write-Host
    try {
        if ($NoNewline) {
            Write-Host $Message -ForegroundColor $Color -NoNewline -ErrorAction Stop
        } else {
            Write-Host $Message -ForegroundColor $Color -ErrorAction Stop
        }
        return
    } catch [System.Management.Automation.PSInvalidOperationException] {
        # Layer 2: PowerShell-specific exception, try fallback
        try {
            Write-Output $Message
            return
        } catch {
            # Continue to next layer
        }
    } catch [System.IO.IOException] {
        # Layer 3: IO exception, try fallback
        try {
            [Console]::WriteLine($Message)
            return
        } catch {
            # Continue to next layer
        }
    } catch {
        # Layer 4: Generic exception, try basic output
        try {
            [Console]::Write($Message)
            if (-not $NoNewline) {
                [Console]::WriteLine()
            }
            return
        } catch {
            # Layer 5: Complete failure, silent handling (ensure script doesn't exit)
        }
    }
}

# ============================================================================
# Layer 4: ASCII Art and Animation
# ============================================================================
function Show-IOEArt {
    <#
    .SYNOPSIS
        Display IOE ASCII art with animated colors (compact version)
    #>
    param([int]$AnimationStep = 0)

    # IOE ASCII Art (compact and beautiful)
    $ioeArt = @"

    ██╗ ██████╗ ███████╗    ██████╗ ██████╗ ███████╗ █████╗ ███╗   ███╗
    ██║██╔═══██╗██╔════╝    ██╔══██╗██╔══██╗██╔════╝██╔══██╗████╗ ████║
    ██║██║   ██║█████╗      ██║  ██║██████╔╝█████╗  ███████║██╔████╔██║
    ██║██║   ██║██╔══╝      ██║  ██║██╔══██╗██╔══╝  ██╔══██║██║╚██╔╝██║
    ██║╚██████╔╝███████╗    ██████╔╝██████╔╝███████╗██║  ██║██║ ╚═╝ ██║
    ╚═╝ ╚═════╝ ╚══════╝    ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝
"@

    # Color animation: cycle through colors
    $colors = @("Cyan", "Magenta", "Yellow", "Green", "Blue", "White")
    $colorIndex = $AnimationStep % $colors.Count
    $currentColor = $colors[$colorIndex]

    # Display ASCII art with color
    $lines = $ioeArt -split "`n"
    foreach ($line in $lines) {
        if ($line.Trim() -ne "") {
            Write-SafeLog $line $currentColor
        } else {
            Write-SafeLog $line "White"
        }
    }

    return $colorIndex
}

function Show-IOEArtAnimated {
    <#
    .SYNOPSIS
        Display IOE ASCII art with smooth color animation (rainbow effect)
    #>
    param([int]$Duration = 2)

    # Enhanced IOE ASCII Art
    $ioeArt = @"

    ██╗ ██████╗ ███████╗    ██████╗ ██████╗ ███████╗ █████╗ ███╗   ███╗
    ██║██╔═══██╗██╔════╝    ██╔══██╗██╔══██╗██╔════╝██╔══██╗████╗ ████║
    ██║██║   ██║█████╗      ██║  ██║██████╔╝█████╗  ███████║██╔████╔██║
    ██║██║   ██║██╔══╝      ██║  ██║██╔══██╗██╔══╝  ██╔══██║██║╚██╔╝██║
    ██║╚██████╔╝███████╗    ██████╔╝██████╔╝███████╗██║  ██║██║ ╚═╝ ██║
    ╚═╝ ╚═════╝ ╚══════╝    ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝
"@

    # Rainbow color sequence
    $rainbowColors = @("Cyan", "Magenta", "Yellow", "Green", "Blue", "White", "Red")

    # Display with rainbow animation
    $lines = $ioeArt -split "`n"
    $frameCount = $Duration * 10  # 10 frames per second

    for ($frame = 0; $frame -lt $frameCount; $frame++) {
        Clear-Host
        $colorIndex = $frame % $rainbowColors.Count
        $currentColor = $rainbowColors[$colorIndex]

        foreach ($line in $lines) {
            if ($line.Trim() -ne "") {
                Write-SafeLog $line $currentColor
            } else {
                Write-SafeLog $line "White"
            }
        }

        Start-Sleep -Milliseconds 100
    }
}

function Show-IOEArtWithBlink {
    <#
    .SYNOPSIS
        Display IOE ASCII art with blinking effect (quick attention grabber)
    #>
    param([int]$Blinks = 2)

    $ioeArt = @"

    ██╗ ██████╗ ███████╗    ██████╗ ██████╗ ███████╗ █████╗ ███╗   ███╗
    ██║██╔═══██╗██╔════╝    ██╔══██╗██╔══██╗██╔════╝██╔══██╗████╗ ████║
    ██║██║   ██║█████╗      ██║  ██║██████╔╝█████╗  ███████║██╔████╔██║
    ██║██║   ██║██╔══╝      ██║  ██║██╔══██╗██╔══╝  ██╔══██║██║╚██╔╝██║
    ██║╚██████╔╝███████╗    ██████╔╝██████╔╝███████╗██║  ██║██║ ╚═╝ ██║
    ╚═╝ ╚═════╝ ╚══════╝    ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝
"@

    for ($i = 0; $i -lt $Blinks; $i++) {
        Clear-Host
        # Alternate between Cyan and Magenta for eye-catching effect
        $color = if ($i % 2 -eq 0) { "Cyan" } else { "Magenta" }
        $lines = $ioeArt -split "`n"
        foreach ($line in $lines) {
            if ($line.Trim() -ne "") {
                Write-SafeLog $line $color
            } else {
                Write-SafeLog $line "White"
            }
        }
        # Quick blink for attention
        Start-Sleep -Milliseconds 200
    }
}

function Show-IOEArtStatic {
    <#
    .SYNOPSIS
        Display IOE ASCII art with static beautiful colors
    #>

    # Beautiful IOE ASCII Art (optimized for console)
    $ioeArt = @"

    ██╗ ██████╗ ███████╗    ██████╗ ██████╗ ███████╗ █████╗ ███╗   ███╗
    ██║██╔═══██╗██╔════╝    ██╔══██╗██╔══██╗██╔════╝██╔══██╗████╗ ████║
    ██║██║   ██║█████╗      ██║  ██║██████╔╝█████╗  ███████║██╔████╔██║
    ██║██║   ██║██╔══╝      ██║  ██║██╔══██╗██╔══╝  ██╔══██║██║╚██╔╝██║
    ██║╚██████╔╝███████╗    ██████╔╝██████╔╝███████╗██║  ██║██║ ╚═╝ ██║
    ╚═╝ ╚═════╝ ╚══════╝    ╚═════╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝

    ════════════════════════════════════════════════════════════════════
     Smart Campus Platform - Interactive Startup Script v5.1.0
    ════════════════════════════════════════════════════════════════════
"@

    # Display with gradient colors (rainbow effect)
    $lines = $ioeArt -split "`n"
    $colors = @("Cyan", "Cyan", "Cyan", "Cyan", "Cyan", "Cyan", "Magenta", "Magenta", "Yellow", "Yellow", "Green", "Green")
    $lineIndex = 0

    foreach ($line in $lines) {
        if ($line.Trim() -ne "") {
            $color = if ($lineIndex -lt $colors.Count) { $colors[$lineIndex] } else { "White" }
            Write-SafeLog $line $color
            $lineIndex++
            # Small delay for smooth animation effect
            Start-Sleep -Milliseconds 20
        } else {
            Write-SafeLog $line "White"
        }
    }
}

# ============================================================================
# Layer 5: Interactive Menu System
# ============================================================================
function Show-InteractiveMenu {
    <#
    .SYNOPSIS
        Display interactive menu and get user selection
    #>
    param(
        [string]$Title = "Main Menu",
        [switch]$FirstRun,
        [string]$ScriptPath = "",
        [string]$ProjectRoot = ""
    )

    try {
        if ($FirstRun) {
            # First run: show ASCII art with animation and full header
            Clear-Host
            # Show blinking effect first (quick attention grabber)
            Show-IOEArtWithBlink -Blinks 2
            # Then show static version
            Show-IOEArtStatic
            Write-SafeLog "" "White"

            # Display environment information
            Write-SafeLog "[Environment Information]" "Cyan"
            Write-SafeLog "  PowerShell Version: $($PSVersionTable.PSVersion)" "Gray"
            Write-SafeLog "  Encoding: $([Console]::OutputEncoding.EncodingName)" "Gray"
            if ($ScriptPath) {
                Write-SafeLog "  Script Path: $ScriptPath" "Gray"
            }
            if ($ProjectRoot) {
                Write-SafeLog "  Project Root: $ProjectRoot" "Gray"
            }
            Write-SafeLog "" "White"
        } else {
            # Subsequent runs: clear screen and show menu only
            Clear-Host
            # Show compact IOE art on subsequent runs
            Show-IOEArt -AnimationStep (Get-Random -Minimum 0 -Maximum 6)
            Write-SafeLog "" "White"
        }

        Write-SafeLog "[$Title]" "Cyan"
        Write-SafeLog "" "White"
        Write-SafeLog "Please select an option:" "Yellow"
        Write-SafeLog "" "White"
        Write-SafeLog "=== Build (Compile) ===" "Magenta"
        Write-SafeLog "  1. Build Backend (Maven)" "White"
        Write-SafeLog "  2. Build Frontend (npm)" "White"
        Write-SafeLog "  3. Build Mobile (uni-app)" "White"
        Write-SafeLog "" "White"
        Write-SafeLog "=== Start Services ===" "Magenta"
        Write-SafeLog "  4. Start Backend Services" "White"
        Write-SafeLog "  5. Start Frontend Service" "White"
        Write-SafeLog "  6. Start Mobile Service" "White"
        Write-SafeLog "  7. Start Frontend + Backend" "White"
        Write-SafeLog "  8. Start All Services" "White"
        Write-SafeLog "" "White"
        Write-SafeLog "=== Docker Deployment ===" "Magenta"
        Write-SafeLog "  9. Docker Deploy (Build & Start)" "White"
        Write-SafeLog "" "White"
        Write-SafeLog "=== Management ===" "Magenta"
        Write-SafeLog "  S. Check Service Status" "White"
        Write-SafeLog "  T. Stop All Services" "White"
        Write-SafeLog "  R. Restart All Services" "White"
        Write-SafeLog "  U. View Access URLs" "White"
        Write-SafeLog "  0. Exit" "White"
        Write-SafeLog "" "White"
        Write-SafeLog "Enter your choice: " "Cyan" -NoNewline

        try {
            $choice = Read-Host
            return $choice
        } catch {
            return "0"
        }
    } catch {
        Write-SafeLog "[ERROR] Failed to display menu: $($_.Exception.Message)" "Red"
        return "0"
    }
}

function Wait-ForUserInput {
    <#
    .SYNOPSIS
        Wait for user to press any key
    #>
    param([string]$Message = "Press any key to continue...")

    Write-SafeLog "" "White"
    Write-SafeLog $Message "Cyan" -NoNewline

    try {
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    } catch {
        try {
            $null = [Console]::ReadKey($true)
        } catch {
            Start-Sleep -Seconds 3
        }
    }
    Write-SafeLog "" "White"
}

# ============================================================================
# Layer 5: Service Status Check Function
# ============================================================================
function Test-ServiceStatus {
    <#
    .SYNOPSIS
        Check service port status
    #>
    param(
        [int]$Port,
        [string]$ServiceName
    )

    try {
        # Layer 1: Use Test-NetConnection (Recommended)
        try {
            $connection = Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue -InformationLevel Quiet -ErrorAction Stop
            return @{
                IsRunning = $connection
                Method = "Test-NetConnection"
            }
        } catch [System.Management.Automation.CommandNotFoundException] {
            # Layer 2: Test-NetConnection unavailable, use TcpClient
            try {
                $tcpClient = New-Object System.Net.Sockets.TcpClient
                $result = $tcpClient.BeginConnect("localhost", $Port, $null, $null)
                $wait = $result.AsyncWaitHandle.WaitOne(1000, $false)
                if ($wait) {
                    $tcpClient.EndConnect($result)
                    $tcpClient.Close()
                    return @{
                        IsRunning = $true
                        Method = "TcpClient"
                    }
                } else {
                    $tcpClient.Close()
                    return @{
                        IsRunning = $false
                        Method = "TcpClient"
                    }
                }
            } catch {
                # Layer 3: TcpClient failed, return unknown status
                return @{
                    IsRunning = $false
                    Method = "Unknown"
                    Error = $_.Exception.Message
                }
            }
        } catch {
            # Layer 4: Other exceptions, return unknown status
            return @{
                IsRunning = $false
                Method = "Unknown"
                Error = $_.Exception.Message
            }
        }
    } catch {
        # Layer 5: Complete failure, return unknown status
        return @{
            IsRunning = $false
            Method = "Failed"
            Error = $_.Exception.Message
        }
    }
}

# ============================================================================
# Layer 6: Service Management Functions
# ============================================================================
function Get-ServiceList {
    <#
    .SYNOPSIS
        Get service configuration list
    #>
    return @(
        @{ Port = 8080; Name = "Gateway"; Description = "API Gateway"; Type = "Backend" },
        @{ Port = 8088; Name = "Common"; Description = "Common Business Service"; Type = "Backend" },
        @{ Port = 8087; Name = "Device"; Description = "Device Communication Service"; Type = "Backend" },
        @{ Port = 8089; Name = "OA"; Description = "OA Office Service"; Type = "Backend" },
        @{ Port = 8090; Name = "Access"; Description = "Access Control Service"; Type = "Backend" },
        @{ Port = 8091; Name = "Attendance"; Description = "Attendance Service"; Type = "Backend" },
        @{ Port = 8092; Name = "Video"; Description = "Video Service"; Type = "Backend" },
        @{ Port = 8094; Name = "Consume"; Description = "Consume Service"; Type = "Backend" },
        @{ Port = 8095; Name = "Visitor"; Description = "Visitor Service"; Type = "Backend" },
        @{ Port = 3000; Name = "Web Admin"; Description = "Web Admin Portal"; Type = "Frontend" },
        @{ Port = 8081; Name = "Mobile H5"; Description = "Mobile H5 App"; Type = "Frontend" }
    )
}

function Show-ServiceStatus {
    <#
    .SYNOPSIS
        Display service status
    #>
    param([switch]$ShowDetails)

    Write-SafeLog "[Service Status Check]" "Cyan"
    Write-SafeLog "Checking service status..." "Yellow"
    Write-SafeLog "" "White"

    $services = Get-ServiceList
    $runningCount = 0
    $totalCount = $services.Count

    foreach ($service in $services) {
        $status = Test-ServiceStatus -Port $service.Port -ServiceName $service.Name

        if ($status.IsRunning) {
            Write-SafeLog "  [OK] $($service.Name) (Port $($service.Port)) - Running" "Green"
            $runningCount++
        } else {
            $statusText = if ($status.Error) { "Check Error" } else { "Stopped" }
            Write-SafeLog "  [XX] $($service.Name) (Port $($service.Port)) - $statusText" "Red"
        }
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[Statistics] $runningCount/$totalCount services running" "Yellow"
    Write-SafeLog "" "White"

    if ($runningCount -eq $totalCount) {
        Write-SafeLog "[SUCCESS] All services are running normally!" "Green"
    } elseif ($runningCount -gt 0) {
        Write-SafeLog "[WARNING] Some services are running, please check stopped services" "Yellow"
    } else {
        Write-SafeLog "[ERROR] All services are stopped" "Red"
    }
}

function Show-AccessURLs {
    <#
    .SYNOPSIS
        Display access URLs
    #>
    Write-SafeLog "" "White"
    Write-SafeLog "[Access URLs]" "Cyan"
    Write-SafeLog "  Web Admin Portal: http://localhost:3000" "White"
    Write-SafeLog "  Mobile H5 App: http://localhost:8081" "White"
    Write-SafeLog "  API Gateway: http://localhost:8080" "White"
    Write-SafeLog "" "White"
}

function Start-AllServices {
    <#
    .SYNOPSIS
        Start all services using Docker Compose
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Start All Services]" "Cyan"
    Write-SafeLog "Starting all services..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check if start-all-services.ps1 exists (preferred for development)
    $startAllScript = Join-Path $ProjectRoot "scripts\start-all-services.ps1"
    if (Test-Path $startAllScript) {
        Write-SafeLog "[INFO] Using development startup script (mvn spring-boot:run)" "Cyan"
        Write-SafeLog "This will start services directly without Docker" "Gray"
        Write-SafeLog "" "White"
        
        try {
            # Execute the start-all-services script
            & powershell -ExecutionPolicy Bypass -File $startAllScript -SkipBuild -WaitForReady
            $exitCode = $LASTEXITCODE
            Write-SafeLog "" "White"
            if ($exitCode -eq 0) {
                Write-SafeLog "[SUCCESS] All services started successfully" "Green"
            } else {
                Write-SafeLog "[WARNING] Some services may have issues (exit code: $exitCode)" "Yellow"
            }
        } catch {
            Write-SafeLog "[ERROR] Failed to start services: $($_.Exception.Message)" "Red"
        }
        return
    }

    # Fallback to Docker Compose method
    Write-SafeLog "[INFO] Using Docker Compose startup method" "Cyan"
    Write-SafeLog "" "White"

    # Check if Docker is available
    try {
        $dockerVersion = docker --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker not found"
        }
        Write-SafeLog "Docker detected: $dockerVersion" "Gray"
    } catch {
        Write-SafeLog "[ERROR] Docker is not installed or not running" "Red"
        Write-SafeLog "Please install Docker Desktop or start Docker service" "Yellow"
        return
    }

    # Check if docker-compose file exists
    $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
    if (-not (Test-Path $composeFile)) {
        Write-SafeLog "[ERROR] docker-compose-all.yml not found" "Red"
        Write-SafeLog "Expected location: $composeFile" "Yellow"
        return
    }

    Write-SafeLog "Using compose file: docker-compose-all.yml" "Gray"
    Write-SafeLog "" "White"

    # Check if JAR files exist before starting backend services
    Write-SafeLog "[Pre-check] Verifying JAR files..." "Cyan"
    $backendServices = @(
        @{ Name = "gateway-service"; Path = "microservices\ioedream-gateway-service\target\ioedream-gateway-service-1.0.0.jar" },
        @{ Name = "common-service"; Path = "microservices\ioedream-common-service\target\ioedream-common-service-1.0.0.jar" },
        @{ Name = "device-comm-service"; Path = "microservices\ioedream-device-comm-service\target\ioedream-device-comm-service-1.0.0.jar" },
        @{ Name = "oa-service"; Path = "microservices\ioedream-oa-service\target\ioedream-oa-service-1.0.0.jar" },
        @{ Name = "access-service"; Path = "microservices\ioedream-access-service\target\ioedream-access-service-1.0.0.jar" },
        @{ Name = "attendance-service"; Path = "microservices\ioedream-attendance-service\target\ioedream-attendance-service-1.0.0.jar" },
        @{ Name = "video-service"; Path = "microservices\ioedream-video-service\target\ioedream-video-service-1.0.0.jar" },
        @{ Name = "consume-service"; Path = "microservices\ioedream-consume-service\target\ioedream-consume-service-1.0.0.jar" },
        @{ Name = "visitor-service"; Path = "microservices\ioedream-visitor-service\target\ioedream-visitor-service-1.0.0.jar" }
    )

    $missingJars = @()
    foreach ($svc in $backendServices) {
        $jarPath = Join-Path $ProjectRoot $svc.Path
        if (-not (Test-Path $jarPath)) {
            $missingJars += $svc
            Write-SafeLog "  [MISSING] $($svc.Name): $jarPath" "Yellow"
        } else {
            Write-SafeLog "  [OK] $($svc.Name)" "Green"
        }
    }

    if ($missingJars.Count -gt 0) {
        Write-SafeLog "" "White"
        Write-SafeLog "[ERROR] Missing JAR files detected!" "Red"
        Write-SafeLog "The following services need to be built first:" "Yellow"
        foreach ($svc in $missingJars) {
            Write-SafeLog "  - $($svc.Name)" "Yellow"
        }
        Write-SafeLog "" "White"
        Write-SafeLog "Please select one of the following options:" "Cyan"
        Write-SafeLog "  1. Build Backend (Maven) - Select option 1 from the menu" "White"
        Write-SafeLog "  2. Docker Deploy (Build & Start) - Select option 9 from the menu" "White"
        Write-SafeLog "" "White"
        Write-SafeLog "Or run manually:" "Gray"
        Write-SafeLog "  cd microservices\microservices-common && mvn clean install -DskipTests" "Gray"
        Write-SafeLog "  cd ..\.. && mvn clean package -DskipTests" "Gray"
        Write-SafeLog "" "White"
        return
    }

    Write-SafeLog "  [SUCCESS] All JAR files verified" "Green"
    Write-SafeLog "" "White"

    # Start infrastructure services first
    Write-SafeLog "[Step 1] Starting infrastructure services (MySQL, Redis, Nacos)..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d mysql redis nacos" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile up -d mysql redis nacos
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Infrastructure services started successfully" "Green"
            Write-SafeLog "  Waiting for services to be ready (30 seconds)..." "Yellow"
            Start-Sleep -Seconds 30
        } else {
            Write-SafeLog "  [WARNING] Some infrastructure services may have issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to start infrastructure: $($_.Exception.Message)" "Red"
        return
    }

    # Start backend services
    Write-SafeLog "" "White"
    Write-SafeLog "[Step 2] Starting backend microservices..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d [backend services]" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile up -d gateway-service common-service device-comm-service oa-service access-service attendance-service video-service consume-service visitor-service
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Backend services started successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Some backend services may have issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to start backend services: $($_.Exception.Message)" "Red"
    }

    # Start frontend services (if available in docker-compose)
    Write-SafeLog "" "White"
    Write-SafeLog "[Step 3] Checking frontend services..." "Cyan"

    # Check if frontend service exists in docker-compose file by parsing YAML
    $composeContent = Get-Content $composeFile -Raw
    # Extract service names from docker-compose file
    $serviceNames = [regex]::Matches($composeContent, '^\s+([a-z0-9-]+):\s*$', [System.Text.RegularExpressions.RegexOptions]::Multiline) | ForEach-Object { $_.Groups[1].Value }
    $hasFrontendService = $false
    $frontendServiceName = $null
    
    foreach ($name in $serviceNames) {
        if ($name -match 'web-admin|frontend|smart-admin|admin') {
            $hasFrontendService = $true
            $frontendServiceName = $name
            break
        }
    }

    if ($hasFrontendService -and $frontendServiceName) {
        Write-SafeLog "Executing: docker-compose up -d $frontendServiceName" "Gray"
        Write-SafeLog "" "White"
        try {
            # Real-time output
            & docker-compose -f $composeFile up -d $frontendServiceName
            $exitCode = $LASTEXITCODE
            Write-SafeLog "" "White"
            if ($exitCode -eq 0) {
                Write-SafeLog "  [SUCCESS] Frontend services started successfully" "Green"
            } else {
                Write-SafeLog "  [WARNING] Frontend services may have issues (exit code: $exitCode)" "Yellow"
            }
        } catch {
            Write-SafeLog "" "White"
            Write-SafeLog "  [WARNING] Frontend services not available or already running" "Yellow"
        }
    } else {
        Write-SafeLog "  [SKIP] Frontend service not defined in docker-compose-all.yml" "Yellow"
        Write-SafeLog "  Frontend can be started separately using option 5 or 6" "Gray"
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] All services startup initiated" "Green"
    Write-SafeLog "Please wait 1-2 minutes for all services to be fully ready" "Yellow"
    Write-SafeLog "" "White"
    Write-SafeLog "To check service status, select option S from the menu" "Gray"
    Write-SafeLog "To view logs: docker-compose -f docker-compose-all.yml logs -f" "Gray"
}

function Start-BackendServices {
    <#
    .SYNOPSIS
        Start backend services using Docker Compose (used by Start-FrontendAndBackend)
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Start Backend Services]" "Cyan"
    Write-SafeLog "Starting backend services..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Docker
    try {
        $null = docker --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker not found"
        }
    } catch {
        Write-SafeLog "[ERROR] Docker is not installed or not running" "Red"
        return
    }

    # Check compose file
    $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
    if (-not (Test-Path $composeFile)) {
        Write-SafeLog "[ERROR] docker-compose-all.yml not found" "Red"
        return
    }

    # Start infrastructure if not running
    Write-SafeLog "[Step 1] Ensuring infrastructure services are running..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d mysql redis nacos rabbitmq" "Gray"
    Write-SafeLog "" "White"
    & docker-compose -f $composeFile up -d mysql redis nacos rabbitmq
    Write-SafeLog "" "White"
    Start-Sleep -Seconds 10

    # Start backend services
    Write-SafeLog "[Step 2] Starting backend microservices..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d [backend services]" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile up -d gateway-service common-service device-comm-service oa-service access-service attendance-service video-service consume-service visitor-service
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Backend services started successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Some services may have issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to start backend services: $($_.Exception.Message)" "Red"
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] Backend services startup initiated" "Green"
}

function Start-FrontendServices {
    <#
    .SYNOPSIS
        Start frontend services using Docker Compose (legacy function, kept for compatibility)
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Start-FrontendServiceOnly -ProjectRoot $ProjectRoot
}

function Stop-AllServices {
    <#
    .SYNOPSIS
        Stop all services using Docker Compose
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Stop All Services]" "Cyan"
    Write-SafeLog "Stopping all services..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Docker
    try {
        $null = docker --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker not found"
        }
    } catch {
        Write-SafeLog "[ERROR] Docker is not installed or not running" "Red"
        return
    }

    # Check compose file
    $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
    if (-not (Test-Path $composeFile)) {
        Write-SafeLog "[ERROR] docker-compose-all.yml not found" "Red"
        return
    }

    # Stop all services
    Write-SafeLog "Stopping all services..." "Cyan"
    Write-SafeLog "Executing: docker-compose down" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile down
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] All services stopped successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Some services may not have stopped properly (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to stop services: $($_.Exception.Message)" "Red"
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] All services stopped" "Green"
}

# ============================================================================
# Layer 6: Build Functions
# ============================================================================
function Build-Backend {
    <#
    .SYNOPSIS
        Build backend services using Maven
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Build Backend Services]" "Cyan"
    Write-SafeLog "Building backend services with Maven..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Maven
    try {
        $mavenVersion = mvn --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Maven not found"
        }
        Write-SafeLog "Maven detected" "Gray"
    } catch {
        Write-SafeLog "[ERROR] Maven is not installed or not in PATH" "Red"
        Write-SafeLog "Please install Maven or add it to PATH" "Yellow"
        return
    }

    # Check Java
    try {
        $javaVersion = java -version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Java not found"
        }
        Write-SafeLog "Java detected" "Gray"
    } catch {
        Write-SafeLog "[ERROR] Java is not installed or not in PATH" "Red"
        return
    }

    $microservicesPath = Join-Path $ProjectRoot "microservices"
    if (-not (Test-Path $microservicesPath)) {
        Write-SafeLog "[ERROR] microservices directory not found" "Red"
        return
    }

    Write-SafeLog "[Step 1] Building microservices-common..." "Cyan"
    Write-SafeLog "Executing: mvn clean install -DskipTests" "Gray"
    Write-SafeLog "" "White"
    try {
        Push-Location (Join-Path $microservicesPath "microservices-common")
        # Real-time output
        & mvn clean install -DskipTests
        $exitCode = $LASTEXITCODE
        if ($exitCode -eq 0) {
            Write-SafeLog "" "White"
            Write-SafeLog "  [SUCCESS] microservices-common built successfully" "Green"
        } else {
            Write-SafeLog "" "White"
            Write-SafeLog "  [ERROR] microservices-common build failed (exit code: $exitCode)" "Red"
            Pop-Location
            return
        }
        Pop-Location
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to build microservices-common: $($_.Exception.Message)" "Red"
        Pop-Location
        return
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[Step 2] Building all microservices..." "Cyan"
    Write-SafeLog "Executing: mvn clean package -DskipTests" "Gray"
    Write-SafeLog "" "White"
    try {
        Push-Location $microservicesPath
        # Real-time output
        & mvn clean package -DskipTests
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] All microservices built successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Some services may have build issues (exit code: $exitCode)" "Yellow"
        }
        Pop-Location
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to build microservices: $($_.Exception.Message)" "Red"
        Pop-Location
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] Backend build completed" "Green"
}

function Build-Frontend {
    <#
    .SYNOPSIS
        Build frontend application using npm
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Build Frontend Application]" "Cyan"
    Write-SafeLog "Building frontend with npm..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Node.js and npm
    try {
        $nodeVersion = node --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Node.js not found"
        }
        Write-SafeLog "Node.js detected: $nodeVersion" "Gray"

        $npmVersion = npm --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "npm not found"
        }
        Write-SafeLog "npm detected: $npmVersion" "Gray"
    } catch {
        Write-SafeLog "[ERROR] Node.js or npm is not installed" "Red"
        Write-SafeLog "Please install Node.js from https://nodejs.org" "Yellow"
        return
    }

    $frontendPath = Join-Path $ProjectRoot "smart-admin-web-javascript"
    if (-not (Test-Path $frontendPath)) {
        Write-SafeLog "[ERROR] smart-admin-web-javascript directory not found" "Red"
        return
    }

    Write-SafeLog "Building frontend application..." "Cyan"
    try {
        Push-Location $frontendPath

        # Install dependencies if node_modules doesn't exist
        if (-not (Test-Path "node_modules")) {
            Write-SafeLog "[Step 1] Installing dependencies..." "Cyan"
            Write-SafeLog "Executing: npm install" "Gray"
            Write-SafeLog "" "White"
            # Real-time output
            & npm install
            $exitCode = $LASTEXITCODE
            Write-SafeLog "" "White"
            if ($exitCode -ne 0) {
                Write-SafeLog "  [ERROR] npm install failed (exit code: $exitCode)" "Red"
                Pop-Location
                return
            }
            Write-SafeLog "  [SUCCESS] Dependencies installed successfully" "Green"
        }

        Write-SafeLog "" "White"
        Write-SafeLog "[Step 2] Building production bundle..." "Cyan"
        Write-SafeLog "Executing: npm run build:prod" "Gray"
        Write-SafeLog "" "White"
        # Real-time output
        & npm run build:prod
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Frontend built successfully" "Green"
        } else {
            Write-SafeLog "  [ERROR] Frontend build failed (exit code: $exitCode)" "Red"
        }
        Pop-Location
    } catch {
        Write-SafeLog "  [ERROR] Failed to build frontend: $($_.Exception.Message)" "Red"
        Pop-Location
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] Frontend build completed" "Green"
}

function Build-Mobile {
    <#
    .SYNOPSIS
        Build mobile application using uni-app
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Build Mobile Application]" "Cyan"
    Write-SafeLog "Building mobile app with uni-app..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Node.js and npm
    try {
        $nodeVersion = node --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Node.js not found"
        }
        Write-SafeLog "Node.js detected: $nodeVersion" "Gray"
    } catch {
        Write-SafeLog "[ERROR] Node.js is not installed" "Red"
        return
    }

    $mobilePath = Join-Path $ProjectRoot "smart-app"
    if (-not (Test-Path $mobilePath)) {
        Write-SafeLog "[ERROR] smart-app directory not found" "Red"
        return
    }

    Write-SafeLog "Building mobile application..." "Cyan"
    try {
        Push-Location $mobilePath

        # Install dependencies if node_modules doesn't exist
        if (-not (Test-Path "node_modules")) {
            Write-SafeLog "[Step 1] Installing dependencies..." "Cyan"
            Write-SafeLog "Executing: npm install" "Gray"
            Write-SafeLog "" "White"
            # Real-time output
            & npm install
            $exitCode = $LASTEXITCODE
            Write-SafeLog "" "White"
            if ($exitCode -ne 0) {
                Write-SafeLog "  [ERROR] npm install failed (exit code: $exitCode)" "Red"
                Pop-Location
                return
            }
            Write-SafeLog "  [SUCCESS] Dependencies installed successfully" "Green"
        }

        Write-SafeLog "" "White"
        Write-SafeLog "[Step 2] Building H5 production bundle..." "Cyan"
        Write-SafeLog "Executing: npm run build:h5" "Gray"
        Write-SafeLog "" "White"
        # Real-time output
        & npm run build:h5
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Mobile H5 built successfully" "Green"
        } else {
            Write-SafeLog "  [ERROR] Mobile build failed (exit code: $exitCode)" "Red"
        }
        Pop-Location
    } catch {
        Write-SafeLog "  [ERROR] Failed to build mobile: $($_.Exception.Message)" "Red"
        Pop-Location
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] Mobile build completed" "Green"
}

# ============================================================================
# Layer 7: Start Functions (Enhanced)
# ============================================================================
function Start-BackendServicesOnly {
    <#
    .SYNOPSIS
        Start backend services only (not using Docker, for development)
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Start Backend Services Only]" "Cyan"
    Write-SafeLog "Starting backend services..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    Write-SafeLog "[INFO] Backend services should be started using Docker Compose" "Yellow"
    Write-SafeLog "Use option 8 (Start All Services) or option 9 (Docker Deploy)" "Gray"
    Write-SafeLog "" "White"
    Write-SafeLog "For development, you can start services manually:" "Gray"
    Write-SafeLog "  cd microservices/ioedream-gateway-service" "Gray"
    Write-SafeLog "  mvn spring-boot:run" "Gray"
}

function Start-FrontendServiceOnly {
    <#
    .SYNOPSIS
        Start frontend service only
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Start Frontend Service]" "Cyan"
    Write-SafeLog "Starting frontend service..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Docker first
    try {
        $null = docker --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
            if (Test-Path $composeFile) {
                Write-SafeLog "Starting frontend service with Docker..." "Cyan"
                Write-SafeLog "Executing: docker-compose up -d web-admin" "Gray"
                Write-SafeLog "" "White"
                & docker-compose -f $composeFile up -d web-admin
                $exitCode = $LASTEXITCODE
                Write-SafeLog "" "White"
                if ($exitCode -eq 0) {
                    Write-SafeLog "  [SUCCESS] Frontend service started successfully" "Green"
                    Write-SafeLog "  Access: http://localhost:3000" "Gray"
                    return
                }
            }
        }
    } catch {
        # Docker not available, try local npm
    }

    # Try local npm start
    $frontendPath = Join-Path $ProjectRoot "smart-admin-web-javascript"
    if (Test-Path $frontendPath) {
        Write-SafeLog "Starting frontend with npm..." "Cyan"
        Write-SafeLog "  Run in new terminal: cd smart-admin-web-javascript && npm run dev" "Yellow"
        Write-SafeLog "  Or use: Start-Process powershell -ArgumentList '-NoExit', '-Command', 'cd $frontendPath; npm run dev'" "Gray"
    } else {
        Write-SafeLog "[ERROR] Frontend directory not found" "Red"
    }
}

function Start-MobileServiceOnly {
    <#
    .SYNOPSIS
        Start mobile service only
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Start Mobile Service]" "Cyan"
    Write-SafeLog "Starting mobile H5 service..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Docker first
    try {
        $null = docker --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
            if (Test-Path $composeFile) {
                Write-SafeLog "Starting mobile service with Docker..." "Cyan"
                Write-SafeLog "Executing: docker-compose up -d mobile-h5" "Gray"
                Write-SafeLog "" "White"
                & docker-compose -f $composeFile up -d mobile-h5
                $exitCode = $LASTEXITCODE
                Write-SafeLog "" "White"
                if ($exitCode -eq 0) {
                    Write-SafeLog "  [SUCCESS] Mobile service started successfully" "Green"
                    Write-SafeLog "  Access: http://localhost:8081" "Gray"
                    return
                }
            }
        }
    } catch {
        # Docker not available, try local npm
    }

    # Try local npm start
    $mobilePath = Join-Path $ProjectRoot "smart-app"
    if (Test-Path $mobilePath) {
        Write-SafeLog "Starting mobile with npm..." "Cyan"
        Write-SafeLog "  Run in new terminal: cd smart-app && npm run dev:h5" "Yellow"
    } else {
        Write-SafeLog "[ERROR] Mobile directory not found" "Red"
    }
}

function Start-FrontendAndBackend {
    <#
    .SYNOPSIS
        Start frontend and backend services
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Start Frontend + Backend]" "Cyan"
    Write-SafeLog "Starting frontend and backend services..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Docker
    try {
        $null = docker --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker not found"
        }
    } catch {
        Write-SafeLog "[ERROR] Docker is not installed or not running" "Red"
        return
    }

    # Check compose file
    $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
    if (-not (Test-Path $composeFile)) {
        Write-SafeLog "[ERROR] docker-compose-all.yml not found" "Red"
        return
    }

    # Start infrastructure first
    Write-SafeLog "[Step 1] Starting infrastructure services..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d mysql redis nacos rabbitmq" "Gray"
    Write-SafeLog "" "White"
    & docker-compose -f $composeFile up -d mysql redis nacos rabbitmq
    Write-SafeLog "" "White"
    Start-Sleep -Seconds 10

    # Start backend services
    Write-SafeLog "[Step 2] Starting backend services..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d [backend services]" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile up -d gateway-service common-service device-comm-service oa-service access-service attendance-service video-service consume-service visitor-service
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Backend services started successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Some backend services may have issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to start backend services: $($_.Exception.Message)" "Red"
    }

    # Start frontend services
    Write-SafeLog "" "White"
    Write-SafeLog "[Step 3] Starting frontend services..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d web-admin" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile up -d web-admin
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Frontend services started successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Frontend services may have issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [WARNING] Frontend services not available or already running" "Yellow"
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] Frontend and backend services startup initiated" "Green"
    Write-SafeLog "Please wait 1-2 minutes for all services to be fully ready" "Yellow"
}

function Deploy-Docker {
    <#
    .SYNOPSIS
        Docker deployment: build images and start services
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Docker Deployment]" "Cyan"
    Write-SafeLog "Building Docker images and starting services..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Docker
    try {
        $dockerVersion = docker --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker not found"
        }
        Write-SafeLog "Docker detected: $dockerVersion" "Gray"
    } catch {
        Write-SafeLog "[ERROR] Docker is not installed or not running" "Red"
        return
    }

    # Check compose file
    $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
    if (-not (Test-Path $composeFile)) {
        Write-SafeLog "[ERROR] docker-compose-all.yml not found" "Red"
        return
    }

    Write-SafeLog "[Step 1] Building Docker images..." "Cyan"
    Write-SafeLog "This may take 10-30 minutes on first build..." "Yellow"
    Write-SafeLog "Executing: docker-compose build" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile build
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] Docker images built successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Some images may have build issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to build images: $($_.Exception.Message)" "Red"
        return
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[Step 2] Starting all services..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile up -d
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] All services started successfully" "Green"
        } else {
            Write-SafeLog "  [WARNING] Some services may have issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to start services: $($_.Exception.Message)" "Red"
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] Docker deployment completed" "Green"
    Write-SafeLog "Please wait 1-2 minutes for all services to be fully ready" "Yellow"
    Write-SafeLog "Check service status with option S" "Gray"
}

function Restart-AllServices {
    <#
    .SYNOPSIS
        Restart all services using Docker Compose
    #>
    param([string]$ProjectRoot = $PSScriptRoot)

    Write-SafeLog "[Restart All Services]" "Cyan"
    Write-SafeLog "Restarting all services..." "Yellow"
    Write-SafeLog "" "White"

    # Get project root if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = $PSScriptRoot
        if (-not $ProjectRoot) {
            $ProjectRoot = (Get-Location).Path
        }
    }

    # Check Docker
    try {
        $null = docker --version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker not found"
        }
    } catch {
        Write-SafeLog "[ERROR] Docker is not installed or not running" "Red"
        return
    }

    # Check compose file
    $composeFile = Join-Path $ProjectRoot "docker-compose-all.yml"
    if (-not (Test-Path $composeFile)) {
        Write-SafeLog "[ERROR] docker-compose-all.yml not found" "Red"
        return
    }

    # Stop services first
    Write-SafeLog "[Step 1] Stopping all services..." "Cyan"
    Write-SafeLog "Executing: docker-compose down" "Gray"
    Write-SafeLog "" "White"
    & docker-compose -f $composeFile down
    Write-SafeLog "" "White"
    Start-Sleep -Seconds 5

    # Start services
    Write-SafeLog "[Step 2] Starting all services..." "Cyan"
    Write-SafeLog "Executing: docker-compose up -d" "Gray"
    Write-SafeLog "" "White"
    try {
        # Real-time output
        & docker-compose -f $composeFile up -d
        $exitCode = $LASTEXITCODE
        Write-SafeLog "" "White"
        if ($exitCode -eq 0) {
            Write-SafeLog "  [SUCCESS] All services restarted successfully" "Green"
            Write-SafeLog "  Please wait 1-2 minutes for all services to be fully ready" "Yellow"
        } else {
            Write-SafeLog "  [WARNING] Some services may have issues (exit code: $exitCode)" "Yellow"
        }
    } catch {
        Write-SafeLog "" "White"
        Write-SafeLog "  [ERROR] Failed to restart services: $($_.Exception.Message)" "Red"
    }

    Write-SafeLog "" "White"
    Write-SafeLog "[SUCCESS] All services restart initiated" "Green"
}

# ============================================================================
# Layer 7: Main Execution Logic (Multi-Layer Exception Protection)
# ============================================================================

# Initialize encoding environment
$encodingResult = Initialize-EncodingEnvironment
if (-not $encodingResult.Success) {
    Write-SafeLog "WARNING: Encoding environment initialization failed: $($encodingResult.Error)" "Yellow"
}

# Detect script encoding
$scriptPath = $MyInvocation.MyCommand.Path
if ($scriptPath) {
    $encodingCheck = Test-ScriptEncoding -ScriptPath $scriptPath
    if ($encodingCheck.NeedsFix) {
        Write-SafeLog "WARNING: Script encoding issue - PowerShell $($encodingCheck.PowerShellVersion) requires UTF-8 with BOM" "Yellow"
        Write-SafeLog "   Please save the file as UTF-8 with BOM format in your editor" "Yellow"
    }
}

# Error handling configuration
$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# Main execution logic
try {
    # Get project root directory
    $projectRoot = $PSScriptRoot
    if (-not $projectRoot) {
        $projectRoot = (Get-Location).Path
    }

    # Interactive menu mode or direct status check
    if ($StatusOnly) {
        # Direct status check mode (for command line usage)
        Show-ServiceStatus
        Show-AccessURLs
    } elseif ($NoMenu) {
        # No menu mode (for automation)
        Write-SafeLog "[TIP] Use -StatusOnly parameter to check service status" "Yellow"
        Write-SafeLog "  Or run without parameters for interactive menu" "Gray"
    } else {
        # Interactive menu mode
        $continue = $true
        $firstRun = $true
        while ($continue) {
            $choice = Show-InteractiveMenu -Title "Main Menu" -FirstRun:$firstRun -ScriptPath $scriptPath -ProjectRoot $projectRoot
            $firstRun = $false

            switch ($choice.ToUpper()) {
                "1" {
                    Build-Backend -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "2" {
                    Build-Frontend -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "3" {
                    Build-Mobile -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "4" {
                    Start-BackendServicesOnly -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "5" {
                    Start-FrontendServiceOnly -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "6" {
                    Start-MobileServiceOnly -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "7" {
                    Start-FrontendAndBackend -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "8" {
                    Start-AllServices -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "9" {
                    Deploy-Docker -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "S" {
                    Show-ServiceStatus
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "T" {
                    Stop-AllServices -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "R" {
                    Restart-AllServices -ProjectRoot $projectRoot
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "U" {
                    Show-AccessURLs
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
                "0" {
                    Write-SafeLog "" "White"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Write-SafeLog "Thank you for using IOE-DREAM!" "Green"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    $continue = $false
                }
                default {
                    Write-SafeLog "" "White"
                    Write-SafeLog "[ERROR] Invalid choice. Please select a valid option." "Red"
                    Write-SafeLog "═══════════════════════════════════════════════════════════" "Gray"
                    Wait-ForUserInput -Message "Press any key to return to menu..."
                }
            }
        }
    }

} catch [System.Management.Automation.PSInvalidOperationException] {
    # Layer 2: PowerShell-specific exception
    Write-SafeLog "[ERROR] PowerShell operation exception: $($_.Exception.Message)" "Red"
    Write-SafeLog "  Exception Type: $($_.Exception.GetType().FullName)" "Yellow"
} catch [System.IO.IOException] {
    # Layer 3: IO exception
    Write-SafeLog "[ERROR] File system exception: $($_.Exception.Message)" "Red"
    Write-SafeLog "  Exception Type: $($_.Exception.GetType().FullName)" "Yellow"
} catch [System.Net.NetworkInformation.PingException] {
    # Layer 4: Network exception
    Write-SafeLog "[ERROR] Network connection exception: $($_.Exception.Message)" "Red"
    Write-SafeLog "  Exception Type: $($_.Exception.GetType().FullName)" "Yellow"
} catch {
    # Layer 5: Generic exception
    Write-SafeLog "[ERROR] Script execution encountered unexpected error" "Red"
    Write-SafeLog "  Error Message: $($_.Exception.Message)" "Yellow"
    Write-SafeLog "  Error Type: $($_.Exception.GetType().FullName)" "Yellow"
    Write-SafeLog "  Error Location: Line $($_.InvocationInfo.ScriptLineNumber)" "Yellow"
} finally {
    # Only show exit message if not in interactive menu mode
    if ($StatusOnly -or $NoMenu) {
        Write-SafeLog "" "White"
        Write-SafeLog "Press any key to exit..." "Cyan" -NoNewline

        try {
            # Layer 1: Try RawUI.ReadKey
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        } catch [System.Management.Automation.Host.HostException] {
            # Layer 2: RawUI unavailable, try ReadKey
            try {
                $null = [Console]::ReadKey($true)
            } catch {
                # Layer 3: ReadKey failed, wait 5 seconds
                try {
                    Start-Sleep -Seconds 5
                } catch {
                    # Layer 4: Sleep failed, wait 3 seconds
                    try {
                        [System.Threading.Thread]::Sleep(3000)
                    } catch {
                        # Layer 5: Complete failure, silent handling (ensure script doesn't exit)
                    }
                }
            }
        } catch {
            # Layer 5: Other exceptions, wait 3 seconds
            try {
                Start-Sleep -Seconds 3
            } catch {
                # Complete failure, silent handling
            }
        }

        Write-SafeLog "" "White"
        Write-SafeLog "[SUCCESS] Script execution completed" "Green"
    }
}
