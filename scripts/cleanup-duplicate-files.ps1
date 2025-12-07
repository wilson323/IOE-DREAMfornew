# Cleanup duplicate repository files
# Remove old repository versions and keep only DAO versions

$ErrorActionPreference = "Stop"

Write-Host "===== Cleanup Duplicate Files =====" -ForegroundColor Cyan

# Files to delete (repository versions)
$filesToDelete = @(
    "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common\organization\repository\AreaDao.java",
    "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common\config\repository\ConfigDao.java"
)

$deletedCount = 0

foreach ($filePath in $filesToDelete) {
    if (Test-Path $filePath) {
        try {
            $fileName = [System.IO.Path]::GetFileName($filePath)
            Write-Host "Deleting: $fileName" -ForegroundColor Yellow
            Remove-Item $filePath -Force
            Write-Host "  Deleted" -ForegroundColor Green
            $deletedCount++
        } catch {
            Write-Host "  Error: $_" -ForegroundColor Red
        }
    } else {
        Write-Host "File not found: $([System.IO.Path]::GetFileName($filePath))" -ForegroundColor Gray
    }
}

# Delete empty repository directories
$repositoryDirs = @(
    "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common\organization\repository",
    "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common\config\repository"
)

foreach ($dirPath in $repositoryDirs) {
    if (Test-Path $dirPath) {
        $items = Get-ChildItem $dirPath -Force
        if ($items.Count -eq 0) {
            try {
                Write-Host "Deleting empty directory: repository" -ForegroundColor Yellow
                Remove-Item $dirPath -Force
                Write-Host "  Deleted" -ForegroundColor Green
            } catch {
                Write-Host "  Error: $_" -ForegroundColor Red
            }
        }
    }
}

Write-Host "`n===== Cleanup Complete =====" -ForegroundColor Cyan
Write-Host "Deleted: $deletedCount duplicate files" -ForegroundColor Green
Write-Host "`nNext: Run 'mvn clean compile -DskipTests -T 4'" -ForegroundColor Yellow

