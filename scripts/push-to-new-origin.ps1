# Push code to new-origin remote repository
# Target: https://github.com/wilson323/IOE-DREAMfornew.git

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Push code to new-origin remote repository" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Change to project root directory
Set-Location D:\IOE-DREAM

# Show current branch
Write-Host "Current branch:" -ForegroundColor Yellow
$currentBranch = git branch --show-current
Write-Host $currentBranch -ForegroundColor White
Write-Host ""

# Show remote repository configuration
Write-Host "Remote repository configuration:" -ForegroundColor Yellow
git remote -v
Write-Host ""

# Show current status
Write-Host "Current Git status:" -ForegroundColor Yellow
git status --short
Write-Host ""

# Push to new-origin
Write-Host "Pushing code to new-origin/$currentBranch ..." -ForegroundColor Green
Write-Host ""

$result = git push new-origin $currentBranch 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "Push successful!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "Remote repository: https://github.com/wilson323/IOE-DREAMfornew.git" -ForegroundColor Cyan
    Write-Host "Branch: $currentBranch" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host "Push failed!" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Red
    Write-Host $result -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Please check the error message above." -ForegroundColor Yellow
    Write-Host "Refer to PUSH_TO_NEW_ORIGIN_GUIDE.md for troubleshooting." -ForegroundColor Yellow
}

Write-Host ""
Read-Host "Press Enter to exit"
