# Git Large Files Commit Helper Script
# Usage: .\git-commit-large-changes.ps1 [-Message "commit message"] [-Interactive]

param(
    [string]$Message = "",
    [switch]$Interactive = $false
)

# Set output encoding to UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "=== Git Large Files Commit Helper ===" -ForegroundColor Cyan

# Check if there are staged files
$stagedFiles = git diff --cached --name-only
if (-not $stagedFiles) {
    Write-Host "`n[ERROR] No staged files found" -ForegroundColor Red
    Write-Host "Please use 'git add' to stage files first" -ForegroundColor Yellow
    exit 1
}

# Count files
$fileCount = ($stagedFiles | Measure-Object).Count
Write-Host "`nStaged files count: $fileCount" -ForegroundColor Cyan

# Warn if too many files
if ($fileCount -gt 100) {
    Write-Host "`n[WARNING] Large number of files ($fileCount files)" -ForegroundColor Yellow
    Write-Host "Consider committing in batches for better commit history" -ForegroundColor Yellow
    Write-Host ""
    
    if (-not $Interactive) {
        $response = Read-Host "Continue to commit all files? (y/n)"
        if ($response -ne "y" -and $response -ne "Y") {
            Write-Host "Commit cancelled" -ForegroundColor Yellow
            exit 0
        }
    }
}

# Generate commit message
if (-not $Message) {
    Write-Host "`nGenerating commit message..." -ForegroundColor Cyan
    
    # Analyze file types
    $javaFiles = ($stagedFiles | Where-Object { $_ -like "*.java" }).Count
    $vueFiles = ($stagedFiles | Where-Object { $_ -like "*.vue" }).Count
    $jsFiles = ($stagedFiles | Where-Object { $_ -like "*.js" }).Count
    $mdFiles = ($stagedFiles | Where-Object { $_ -like "*.md" }).Count
    $configFiles = ($stagedFiles | Where-Object { $_ -match "\.(yml|yaml|xml|properties)$" }).Count
    
    # Detect main change type
    $hasCommon = ($stagedFiles | Where-Object { $_ -like "*microservices-common*" }).Count -gt 0
    $hasGitignore = ($stagedFiles | Where-Object { $_ -like "*.gitignore" }).Count -gt 0
    $hasScripts = ($stagedFiles | Where-Object { $_ -like "scripts/*" }).Count -gt 0
    $hasDocs = ($stagedFiles | Where-Object { $_ -like "documentation/*" }).Count -gt 0
    
    # Generate commit message
    $commitType = "feat"
    $scope = "common"
    $subject = "Add microservices-common module and related features"
    
    if ($hasGitignore) {
        $commitType = "chore"
        $scope = "config"
        $subject = "Update .gitignore configuration"
    } elseif ($hasDocs) {
        $commitType = "docs"
        $scope = "documentation"
        $subject = "Update project documentation"
    } elseif ($hasScripts) {
        $commitType = "chore"
        $scope = "scripts"
        $subject = "Add or update script files"
    }
    
    $body = @"
- Java files: $javaFiles
- Vue files: $vueFiles
- JavaScript files: $jsFiles
- Markdown files: $mdFiles
- Config files: $configFiles
- Total: $fileCount files
"@
    
    $Message = "$commitType($scope): $subject`n`n$body"
    
    Write-Host "`nGenerated commit message:" -ForegroundColor Green
    Write-Host $Message -ForegroundColor White
    Write-Host ""
    
    if (-not $Interactive) {
        $confirm = Read-Host "Use this commit message? (y/n/edit)"
        if ($confirm -eq "edit" -or $confirm -eq "e") {
            $Message = Read-Host "Enter commit message"
        } elseif ($confirm -ne "y" -and $confirm -ne "Y") {
            Write-Host "Commit cancelled" -ForegroundColor Yellow
            exit 0
        }
    }
}

# Execute commit
Write-Host "`nExecuting commit..." -ForegroundColor Cyan
try {
    # Use here-string for multi-line commit message
    $commitResult = git commit -m $Message 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[SUCCESS] Commit completed!" -ForegroundColor Green
        Write-Host "`nCommit info:" -ForegroundColor Cyan
        git log -1 --stat --oneline
    } else {
        Write-Host "`n[ERROR] Commit failed" -ForegroundColor Red
        Write-Host $commitResult -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "`n[ERROR] Error during commit: $_" -ForegroundColor Red
    exit 1
}
