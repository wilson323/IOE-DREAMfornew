# Cleanup ALL repository folders
# Remove all old repository versions and keep only DAO versions

$ErrorActionPreference = "Stop"

Write-Host "===== Cleanup All Repository Folders =====" -ForegroundColor Cyan

# Find all repository folders in common module
$repositoryDirs = Get-ChildItem -Path "D:\IOE-DREAM\microservices\microservices-common\src\main\java" -Directory -Recurse |
    Where-Object { $_.Name -eq "repository" }

Write-Host "Found $($repositoryDirs.Count) repository directories`n" -ForegroundColor Yellow

$deletedFiles = 0
$deletedDirs = 0

foreach ($dir in $repositoryDirs) {
    Write-Host "Processing: $($dir.FullName)" -ForegroundColor Gray

    # Count files in directory
    $files = Get-ChildItem $dir.FullName -File
    Write-Host "  Contains $($files.Count) files" -ForegroundColor Gray

    # Delete directory and all contents
    try {
        Remove-Item $dir.FullName -Recurse -Force
        Write-Host "  Deleted directory and $($files.Count) files" -ForegroundColor Green
        $deletedFiles += $files.Count
        $deletedDirs++
    } catch {
        Write-Host "  Error: $_" -ForegroundColor Red
    }
}

Write-Host "`n===== Cleanup Complete =====" -ForegroundColor Cyan
Write-Host "Deleted:" -ForegroundColor Green
Write-Host "  Directories: $deletedDirs" -ForegroundColor Green
Write-Host "  Files: $deletedFiles" -ForegroundColor Green
Write-Host "`nNext: Run 'mvn clean compile -DskipTests -T 4'" -ForegroundColor Yellow

