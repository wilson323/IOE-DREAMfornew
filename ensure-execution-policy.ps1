# Ensure PowerShell execution policy allows script execution
# Run this once to configure your system

Write-Host "Configuring PowerShell execution policy..." -ForegroundColor Cyan

# Check current execution policy
$currentPolicy = Get-ExecutionPolicy -Scope CurrentUser
Write-Host "Current execution policy: $currentPolicy" -ForegroundColor Gray

if ($currentPolicy -eq "Restricted") {
    Write-Host "Setting execution policy to RemoteSigned..." -ForegroundColor Yellow
    Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
    Write-Host "Execution policy updated successfully!" -ForegroundColor Green
} else {
    Write-Host "Execution policy is already configured." -ForegroundColor Green
}

Write-Host ""
Write-Host "You can now run PowerShell scripts without issues." -ForegroundColor Cyan
Write-Host "Usage: .\simple-start.ps1" -ForegroundColor Gray