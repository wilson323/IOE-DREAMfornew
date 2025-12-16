# Exception Handling Standardization Monitor
# Version: v1.0.0
# Description: Monitor exception handling standardization status
# Created: 2025-12-12

param(
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$checkScript = Join-Path $scriptDir "check-exception-handling-standardization.ps1"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Exception Handling Standardization Monitor" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path $checkScript)) {
    Write-Host "[ERROR] Check script not found: $checkScript" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Running exception handling check..." -ForegroundColor Yellow
Write-Host ""

try {
    if ($GenerateReport) {
        $result = & $checkScript -CheckExceptionPatterns -GenerateReport 2>&1
    } else {
        $result = & $checkScript -CheckExceptionPatterns 2>&1
    }
    $result | ForEach-Object { Write-Host $_ }
    
    $hasIssues = $result | Select-String -Pattern "(Exception pattern issues: [1-9]|RuntimeException issues: [1-9]|WARN|ERROR)" -CaseSensitive:$false
    
    if ($hasIssues) {
        Write-Host ""
        Write-Host "[WARN] Found exception handling issues, please fix them" -ForegroundColor Yellow
        exit 1
    } else {
        Write-Host ""
        Write-Host "[OK] Exception handling check passed" -ForegroundColor Green
        exit 0
    }
} catch {
    Write-Host "[ERROR] Check script execution failed" -ForegroundColor Red
    exit 1
}

