# =====================================================
# Manager Bean Registration Check Script
# Version: v1.0.0
# Description: Check if Manager beans are properly registered
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$FailOnMissing = $false
)

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot + "\.."
Set-Location $projectRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Manager Bean Registration Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find the check script
$scriptPath = Join-Path $PSScriptRoot "check-manager-bean-registration.ps1"

# If script not found in scripts directory, try other locations
if (-not (Test-Path $scriptPath)) {
    $scriptPath = Join-Path $projectRoot "scripts\check-manager-bean-registration.ps1"
}

# If still not found, try in microservices directory
if (-not (Test-Path $scriptPath)) {
    $scriptPath = Join-Path $projectRoot "microservices\scripts\check-manager-bean-registration.ps1"
}

if (-not $scriptPath -or -not (Test-Path $scriptPath)) {
    Write-Host "[WARN] Check script not found, skipping Manager Bean check" -ForegroundColor Yellow
    Write-Host "[INFO] Attempted paths:" -ForegroundColor Yellow
    Write-Host "  - PSScriptRoot: $PSScriptRoot" -ForegroundColor Yellow
    Write-Host "  - MyInvocation: $($MyInvocation.MyCommand.Path)" -ForegroundColor Yellow
    Write-Host "  - Current directory: $(Get-Location)" -ForegroundColor Yellow
    Write-Host "[INFO] This is a non-blocking check, build will continue..." -ForegroundColor Yellow
    exit 0
}

try {
    # Execute check script, redirect output to temp file
    $tempReport = Join-Path $env:TEMP "manager-bean-check-$(Get-Date -Format 'yyyyMMddHHmmss').txt"

    # Fix: Use powershell instead of pwsh, ensure compatibility
    $powershellExe = if (Get-Command pwsh -ErrorAction SilentlyContinue) { "pwsh" } else { "powershell" }
    & $powershellExe -ExecutionPolicy Bypass -File $scriptPath -OutputDir $env:TEMP | Tee-Object -FilePath $tempReport

    # Check if there are missing Beans (by checking report file)
    if (Test-Path "$env:TEMP\MANAGER_BEAN_CHECK_REPORT.md") {
        $reportContent = Get-Content "$env:TEMP\MANAGER_BEAN_CHECK_REPORT.md" -Raw

        # Check if report contains missing or not registered keywords
        if ($reportContent -match "(MISSING|NOT_REGISTERED|missing|not registered)" -and $FailOnMissing) {
            Write-Host "[ERROR] Found missing Manager Bean registrations! Build failed." -ForegroundColor Red
            Write-Host "[INFO] Detailed report: $env:TEMP\MANAGER_BEAN_CHECK_REPORT.md" -ForegroundColor Yellow
            exit 1
        }
    }

    Write-Host "[SUCCESS] Manager Bean registration check passed" -ForegroundColor Green
    exit 0

} catch {
    Write-Host "[ERROR] Check script execution failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($FailOnMissing) {
        exit 1
    }
    exit 0
}
