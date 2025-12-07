# Simple startup test script
# Test if the startup script can be parsed correctly

$ErrorActionPreference = "Stop"

Write-Host "Testing startup script syntax..." -ForegroundColor Yellow

try {
    $scriptPath = "D:\IOE-DREAM\scripts\start-all-complete.ps1"
    
    # Test script parsing
    $content = Get-Content $scriptPath -Raw -Encoding UTF8
    $errors = $null
    $null = [System.Management.Automation.PSParser]::Tokenize($content, [ref]$errors)
    
    if ($errors.Count -eq 0) {
        Write-Host "[OK] Script syntax is valid" -ForegroundColor Green
        exit 0
    } else {
        Write-Host "[ERROR] Found $($errors.Count) syntax errors:" -ForegroundColor Red
        $errors | Select-Object -First 5 | ForEach-Object {
            Write-Host "  Line $($_.Token.StartLine): $($_.Message)" -ForegroundColor Red
        }
        exit 1
    }
} catch {
    Write-Host "[ERROR] Failed to parse script: $_" -ForegroundColor Red
    exit 1
}

