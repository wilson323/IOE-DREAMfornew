# Test script to verify path detection

Write-Host "=== Path Detection Test ===" -ForegroundColor Cyan

# Test different path detection methods
$Method1 = Split-Path -Parent $PSScriptRoot
$Method2 = Split-Path -Parent $MyInvocation.MyCommand.Path
$Method3 = (Get-Location).Path

Write-Host "PSScriptRoot method: $Method1" -ForegroundColor Gray
Write-Host "MyInvocation method: $Method2" -ForegroundColor Gray
Write-Host "Current location: $Method3" -ForegroundColor Gray

# Check if microservices exists in each location
Write-Host ""
Write-Host "Checking microservices directory:" -ForegroundColor Cyan

if (Test-Path (Join-Path $Method1 "microservices")) {
    Write-Host "  ✓ Found at: $Method1\microservices" -ForegroundColor Green
    $ProjectRoot = $Method1
} elseif (Test-Path (Join-Path $Method2 "microservices")) {
    Write-Host "  ✓ Found at: $Method2\microservices" -ForegroundColor Green
    $ProjectRoot = $Method2
} elseif (Test-Path (Join-Path $Method3 "microservices")) {
    Write-Host "  ✓ Found at: $Method3\microservices" -ForegroundColor Green
    $ProjectRoot = $Method3
} else {
    Write-Host "  ✗ Not found in any standard location" -ForegroundColor Red
    Write-Host "  Please check the script location" -ForegroundColor Yellow
}

if ($ProjectRoot) {
    Write-Host ""
    Write-Host "Using Project Root: $ProjectRoot" -ForegroundColor Cyan

    # List microservices directories
    Write-Host ""
    Write-Host "Available microservices:" -ForegroundColor Cyan
    Get-ChildItem (Join-Path $ProjectRoot "microservices") -Directory | ForEach-Object {
        Write-Host "  - $($_.Name)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")