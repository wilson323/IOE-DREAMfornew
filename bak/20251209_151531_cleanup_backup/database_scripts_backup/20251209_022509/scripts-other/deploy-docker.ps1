# IOE-DREAM Docker Deployment Script
# One-click build, deploy and manage Docker containers

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("build", "start", "stop", "restart", "status", "logs", "clean", "full")]
    [string]$Action,
    
    [string]$ServiceName = ""
)

$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\IOE-DREAM"
$ComposeFile = "$ProjectRoot\docker-compose-all.yml"
$EnvFile = "$ProjectRoot\.env.docker"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM Docker Deployment Tool" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

function Test-DockerInstalled {
    $dockerVersion = docker --version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Docker installed: $dockerVersion" -ForegroundColor Green
        return $true
    }
    Write-Host "Docker not installed" -ForegroundColor Red
    return $false
}

function Test-DockerComposeInstalled {
    $composeVersion = docker compose version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Docker Compose installed: $composeVersion" -ForegroundColor Green
        return $true
    }
    Write-Host "Docker Compose not installed" -ForegroundColor Red
    return $false
}

function Build-AllImages {
    Write-Host "`n[Building Docker Images]" -ForegroundColor Cyan
    Set-Location $ProjectRoot
    docker compose -f $ComposeFile build --no-cache
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nBuild completed" -ForegroundColor Green
        docker images | Select-String "ioedream"
        return $true
    }
    Write-Host "`nBuild failed" -ForegroundColor Red
    return $false
}

function Start-AllServices {
    Write-Host "`n[Starting Docker Services]" -ForegroundColor Cyan
    Set-Location $ProjectRoot
    
    if (Test-Path $EnvFile) {
        docker compose -f $ComposeFile --env-file $EnvFile up -d
    } else {
        docker compose -f $ComposeFile up -d
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nAll services started" -ForegroundColor Green
        Write-Host "`nWaiting for services (60s)..." -ForegroundColor Gray
        Start-Sleep -Seconds 60
        Show-ServiceStatus
        
        Write-Host "`nAccess URLs:" -ForegroundColor Yellow
        Write-Host "  Nacos: http://localhost:8848/nacos" -ForegroundColor White
        Write-Host "  Gateway: http://localhost:8080" -ForegroundColor White
        return $true
    }
    Write-Host "`nService start failed" -ForegroundColor Red
    return $false
}

function Stop-AllServices {
    Write-Host "`n[Stopping Docker Services]" -ForegroundColor Cyan
    Set-Location $ProjectRoot
    docker compose -f $ComposeFile down
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`nAll services stopped" -ForegroundColor Green
        return $true
    }
    Write-Host "`nService stop failed" -ForegroundColor Red
    return $false
}

function Restart-AllServices {
    Write-Host "`n[Restarting Docker Services]" -ForegroundColor Cyan
    Stop-AllServices
    Start-Sleep -Seconds 5
    Start-AllServices
}

function Show-ServiceStatus {
    Write-Host "`n[Docker Service Status]" -ForegroundColor Cyan
    Set-Location $ProjectRoot
    docker compose -f $ComposeFile ps
    Write-Host ""
    docker ps --filter "name=ioedream" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
}

function Show-ServiceLogs {
    param([string]$Service)
    
    Set-Location $ProjectRoot
    if ([string]::IsNullOrWhiteSpace($Service)) {
        Write-Host "`n[Viewing All Service Logs]" -ForegroundColor Cyan
        docker compose -f $ComposeFile logs --tail=100 -f
    } else {
        Write-Host "`n[Viewing $Service Logs]" -ForegroundColor Cyan
        docker compose -f $ComposeFile logs --tail=100 -f $Service
    }
}

function Clean-AllResources {
    Write-Host "`n[Cleaning Docker Resources]" -ForegroundColor Cyan
    Write-Host "WARNING: This will remove all IOE-DREAM containers and images" -ForegroundColor Yellow
    $confirm = Read-Host "Continue? (Y/N)"
    
    if ($confirm -ne "Y" -and $confirm -ne "y") {
        Write-Host "Cancelled" -ForegroundColor Gray
        return
    }
    
    Set-Location $ProjectRoot
    docker compose -f $ComposeFile down -v
    
    Write-Host "Cleanup completed" -ForegroundColor Green
}

function Deploy-Full {
    Write-Host "`n[Full Deployment Process]" -ForegroundColor Cyan
    
    if (-not (Build-AllImages)) {
        Write-Host "`nDeployment failed: build error" -ForegroundColor Red
        return
    }
    
    Write-Host "`nWaiting 5 seconds..." -ForegroundColor Gray
    Start-Sleep -Seconds 5
    
    if (-not (Start-AllServices)) {
        Write-Host "`nDeployment failed: startup error" -ForegroundColor Red
        return
    }
    
    Write-Host "`nFull deployment successful!" -ForegroundColor Green
}

# Check environment
if (-not (Test-DockerInstalled)) { exit 1 }
if (-not (Test-DockerComposeInstalled)) { exit 1 }

if (-not (Test-Path $ComposeFile)) {
    Write-Host "Config file not found: $ComposeFile" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Execute action
switch ($Action) {
    "build" { Build-AllImages }
    "start" { Start-AllServices }
    "stop" { Stop-AllServices }
    "restart" { Restart-AllServices }
    "status" { Show-ServiceStatus }
    "logs" { Show-ServiceLogs -Service $ServiceName }
    "clean" { Clean-AllResources }
    "full" { Deploy-Full }
}

Write-Host ""
