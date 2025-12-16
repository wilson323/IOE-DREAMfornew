# =====================================================
# Run Tests with Coverage Report
# Version: v1.0.0
# Description: Run all tests and generate JaCoCo coverage report
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipExecCheck
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Running Tests with Coverage" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Switch to project root directory
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

# Switch to microservices directory
Set-Location "microservices"

# Build Maven command
$mavenArgs = @("clean", "test", "-Dskip.exec.check=true")

if ($SkipExecCheck) {
    Write-Host "[INFO] Skipping exec plugin check..." -ForegroundColor Yellow
}

Write-Host "[1/2] Running tests..." -ForegroundColor Yellow
Write-Host "Command: mvn $($mavenArgs -join ' ')" -ForegroundColor Gray
Write-Host ""

try {
    & mvn $mavenArgs

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "[2/2] Generating coverage report..." -ForegroundColor Yellow
        & mvn jacoco:report

        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "================================================" -ForegroundColor Green
            Write-Host "Coverage Report Generated Successfully" -ForegroundColor Green
            Write-Host "================================================" -ForegroundColor Green
            Write-Host ""
            Write-Host "Coverage reports are available at:" -ForegroundColor White
            Write-Host "  - Parent: microservices/target/site/jacoco/index.html" -ForegroundColor Gray
            Write-Host "  - Each service: microservices/{service}/target/site/jacoco/index.html" -ForegroundColor Gray
            Write-Host ""
        } else {
            Write-Host "[ERROR] Failed to generate coverage report" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "[ERROR] Tests failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "[ERROR] Error running tests: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

