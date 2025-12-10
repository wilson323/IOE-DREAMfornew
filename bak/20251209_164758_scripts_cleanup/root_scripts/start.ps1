# IOE-DREAM PowerShell Launcher
# Uses PowerShell for better UTF-8 support and ASCII art

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    IOE-DREAM Project Launcher" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Time: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

# ASCII Art Banner
Write-Host " _____ _____ _____ _____ ____    _____    ____    _____    __  __" -ForegroundColor Magenta
Write-Host "|_   _|_   _|_   _|_   _|  _ \  |  __ \  / __ \  |_   _|  \  \ \ \ / /" -ForegroundColor Magenta
Write-Host "  | |   | |   | |   | | | | | |__) || |  | |   | |    \  V  / " -ForegroundColor Magenta
Write-Host "  | |   | |   | | | | | |_| |  _  / | |  | |   | |     >  <  " -ForegroundColor Magenta
Write-Host "  | |  _| |  _| | | | |____| | \ \  | |__|  _| |_  ._  /_\_/.  " -ForegroundColor Magenta
Write-Host "  |_| |_____|_____|_____|____/   \___\ \____| |_____|     |___/   " -ForegroundColor Magenta
Write-Host ""

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "           LAUNCH OPTIONS" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Build Backend Only" -ForegroundColor White
Write-Host "2. Start Frontend Only" -ForegroundColor White
Write-Host "3. Start Backend Only" -ForegroundColor White
Write-Host "4. Start Full Project (Recommended)" -ForegroundColor White
Write-Host "5. Stop All Services" -ForegroundColor White
Write-Host "6. Check Status" -ForegroundColor White
Write-Host ""

$choice = Read-Host "Please select option (1-6)"

switch ($choice) {
    "1" { Build-Backend }
    "2" { Start-Frontend }
    "3" { Start-Backend }
    "4" { Start-FullProject }
    "5" { Stop-Services }
    "6" { Check-Status }
    default {
        Write-Host "Invalid choice, starting full project" -ForegroundColor Yellow
        Start-FullProject
    }
}

function Build-Backend {
    Write-Host "`nBuilding Backend..." -ForegroundColor Yellow
    & .\build.ps1
}

function Start-Frontend {
    Write-Host "`nStarting Frontend..." -ForegroundColor Yellow
    if (-not (Test-Path "smart-admin-web-javascript\package.json")) {
        Write-Host "ERROR: Frontend project not found" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        return
    }

    Set-Location "smart-admin-web-javascript"

    if (-not (Test-Path "node_modules")) {
        Write-Host "Installing frontend dependencies..." -ForegroundColor Blue
        & npm install --registry=https://registry.npmmirror.com
    }

    Write-Host "Starting frontend development server..." -ForegroundColor Blue
    Start-Process "cmd" -ArgumentList "/k", "title Frontend - 3000 && npm run dev"
    Write-Host "Frontend started at: http://localhost:3000" -ForegroundColor Green
    Set-Location ".."
}

function Start-Backend {
    Write-Host "`nStarting Backend..." -ForegroundColor Yellow

    Set-Location "microservices"

    $compiled = Read-Host "Is backend compiled? (Y/N)"
    if ($compiled -ne "Y") {
        Write-Host "Building backend first..." -ForegroundColor Blue
        & ..\build.ps1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Backend build failed" -ForegroundColor Red
            Read-Host "Press Enter to exit"
            return
        }
    }

    Write-Host "Starting microservices..." -ForegroundColor Blue
    Start-Process "cmd" -ArgumentList "/k", "title Backend Services && call start-services.bat"
    Write-Host "Backend services starting..." -ForegroundColor Green
    Set-Location ".."
}

function Start-FullProject {
    Write-Host "`nStarting Full Project..." -ForegroundColor Yellow

    # Backend Preparation
    Write-Host "Step 1: Backend Preparation..." -ForegroundColor Blue
    Set-Location "microservices"

    $compiled = Read-Host "Is backend compiled? (Y/N)"
    if ($compiled -ne "Y") {
        Write-Host "Building backend..." -ForegroundColor Blue
        & ..\build.ps1
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Backend build failed" -ForegroundColor Red
            Read-Host "Press Enter to exit"
            return
        }
    }

    # Start Backend Services
    Write-Host "Step 2: Starting Backend Services..." -ForegroundColor Blue
    Start-Process "cmd" -ArgumentList "/k", "title Backend Services && call start-services.bat"

    # Start Frontend
    Write-Host "Step 3: Starting Frontend..." -ForegroundColor Blue
    Set-Location "..\smart-admin-web-javascript"
    if (-not (Test-Path "node_modules")) {
        Write-Host "Installing frontend dependencies..." -ForegroundColor Blue
        & npm install --registry=https://registry.npmmirror.com
    }
    Start-Process "cmd" -ArgumentList "/k", "title Frontend - 3000 && npm run dev"

    # Start Mobile (if exists)
    Write-Host "Step 4: Starting Mobile (if available)..." -ForegroundColor Blue
    Set-Location ".."
    if (Test-Path "smart-app\src\manifest.json") {
        Set-Location "smart-app"
        if (-not (Test-Path "node_modules")) {
            Write-Host "Installing mobile dependencies..." -ForegroundColor Blue
            & npm install --registry=https://registry.npmmirror.com
        }
        Start-Process "cmd" -ArgumentList "/k", "title Mobile - 8081 && npm run dev:h5"
        Set-Location ".."
    }

    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "      FULL PROJECT STARTED!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Service URLs:" -ForegroundColor Yellow
    Write-Host "  - Frontend: http://localhost:3000" -ForegroundColor White
    if (Test-Path "smart-app\src\manifest.json") {
        Write-Host "  - Mobile: http://localhost:8081" -ForegroundColor White
    }
    Write-Host "  - Backend Gateway: http://localhost:8080" -ForegroundColor White
    Write-Host "  - API Docs: http://localhost:8080/doc.html" -ForegroundColor White
    Write-Host ""
    Write-Host "Login: admin / 123456" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Waiting for services to start (30 seconds)..." -ForegroundColor Blue
    Start-Sleep -Seconds 30
}

function Stop-Services {
    Write-Host "`nStopping All Services..." -ForegroundColor Yellow

    Write-Host "Stopping Java processes..." -ForegroundColor Blue
    Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue
    Write-Host "Stopping Node.js processes..." -ForegroundColor Blue
    Stop-Process -Name "node" -Force -ErrorAction SilentlyContinue
    Write-Host "All services stopped" -ForegroundColor Green
}

function Check-Status {
    Write-Host "`nChecking Service Status..." -ForegroundColor Yellow
    Write-Host ""

    Write-Host "Frontend (3000):" -ForegroundColor Cyan
    $frontend = netstat -an | Select-String ":3000"
    if ($frontend) {
        Write-Host "  Status: RUNNING" -ForegroundColor Green
    } else {
        Write-Host "  Status: STOPPED" -ForegroundColor Red
    }

    Write-Host "Backend Gateway (8080):" -ForegroundColor Cyan
    $backend = netstat -an | Select-String ":8080"
    if ($backend) {
        Write-Host "  Status: RUNNING" -ForegroundColor Green
    } else {
        Write-Host "  Status: STOPPED" -ForegroundColor Red
    }

    Write-Host "Mobile (8081):" -ForegroundColor Cyan
    $mobile = netstat -an | Select-String ":8081"
    if ($mobile) {
        Write-Host "  Status: RUNNING" -ForegroundColor Green
    } else {
        Write-Host "  Status: STOPPED" -ForegroundColor Red
    }

    Write-Host ""
    Write-Host "Java Processes:" -ForegroundColor Cyan
    $javaProcesses = Get-Process "java" -ErrorAction SilentlyContinue
    if ($javaProcesses) {
        Write-Host "  Status: RUNNING" -ForegroundColor Green
        $javaProcesses | ForEach-Object { Write-Host "  - $($_.Id) $($_.ProcessName)" -ForegroundColor Gray }
    } else {
        Write-Host "  Status: NONE" -ForegroundColor Red
    }

    Write-Host "Node.js Processes:" -ForegroundColor Cyan
    $nodeProcesses = Get-Process "node" -ErrorAction SilentlyContinue
    if ($nodeProcesses) {
        Write-Host "  Status: RUNNING" -ForegroundColor Green
        $nodeProcesses | ForEach-Object { Write-Host "  - $($_.Id) $($_.ProcessName)" -ForegroundColor Gray }
    } else {
        Write-Host "  Status: NONE" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Gray
Write-Host "         OPERATION COMPLETE" -ForegroundColor Gray
Write-Host "========================================" -ForegroundColor Gray
Write-Host ""
Write-Host "Quick Commands:" -ForegroundColor Yellow
Write-Host "  - Build: .\build.ps1" -ForegroundColor White
Write-Host "  - Start: .\start.ps1" -ForegroundColor White
Write-Host "  - Stop: .\start.ps1 (option 5)" -ForegroundColor White
Write-Host ""

Read-Host "Press Enter to exit"