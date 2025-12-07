# Update Git Remote Repository Configuration
# Target: https://github.com/wilson323/IOE-DREAMfornew.git

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Update Git Remote Repository Configuration" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Change to project root directory
Set-Location D:\IOE-DREAM

# Show current remote configuration
Write-Host "Current remote configuration:" -ForegroundColor Yellow
git remote -v
Write-Host ""

# Update origin to new repository
Write-Host "Updating origin remote URL..." -ForegroundColor Green
git remote set-url origin https://github.com/wilson323/IOE-DREAMfornew.git

# Remove new-origin if exists (no longer needed)
Write-Host "Removing new-origin remote (if exists)..." -ForegroundColor Green
git remote remove new-origin 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "new-origin remote does not exist, skipping..." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Updated remote configuration:" -ForegroundColor Yellow
git remote -v
Write-Host ""

# Test connection
Write-Host "Testing connection to remote repository..." -ForegroundColor Green
$testResult = git ls-remote origin 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "Connection successful!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Remote repository updated successfully:" -ForegroundColor Cyan
    Write-Host "  Repository: https://github.com/wilson323/IOE-DREAMfornew.git" -ForegroundColor White
    Write-Host "  Remote name: origin" -ForegroundColor White
    Write-Host ""
    Write-Host "You can now use:" -ForegroundColor Yellow
    Write-Host "  git push origin main" -ForegroundColor White
    Write-Host "  git pull origin main" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Yellow
    Write-Host "Connection test failed" -ForegroundColor Yellow
    Write-Host "==========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "This might be due to:" -ForegroundColor Yellow
    Write-Host "  1. Network connectivity issues" -ForegroundColor White
    Write-Host "  2. DNS resolution problems" -ForegroundColor White
    Write-Host "  3. Authentication required" -ForegroundColor White
    Write-Host "  4. Repository does not exist or no access" -ForegroundColor White
    Write-Host ""
    Write-Host "The configuration has been updated, but you may need to:" -ForegroundColor Yellow
    Write-Host "  - Check your network connection" -ForegroundColor White
    Write-Host "  - Configure authentication (SSH key or Personal Access Token)" -ForegroundColor White
    Write-Host "  - Verify repository access permissions" -ForegroundColor White
}

Write-Host ""
Read-Host "Press Enter to exit"
