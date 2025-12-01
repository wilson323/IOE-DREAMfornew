# Fix BOM marks in attendance module Java files
# 2025-01-XX

Write-Host "Fixing BOM marks in attendance module..." -ForegroundColor Cyan

$baseDir = "D:\IOE-DREAM\smart-admin-api-java17-springboot3"
$attendanceDir = Join-Path $baseDir "sa-admin\src\main\java\net\lab1024\sa\admin\module\attendance"

if (-not (Test-Path $attendanceDir)) {
    Write-Host "Error: Directory not found: $attendanceDir" -ForegroundColor Red
    exit 1
}

$javaFiles = Get-ChildItem -Path $attendanceDir -Filter "*.java" -Recurse
$fixedCount = 0
$errorCount = 0

Write-Host "Found $($javaFiles.Count) Java files, checking BOM marks..." -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    try {
        # Read file as bytes to check for BOM
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        
        # Check for UTF-8 BOM (EF BB BF)
        $hasBom = $false
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $hasBom = $true
        }
        
        if ($hasBom) {
            # Read content without BOM
            $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
            
            # Write back without BOM
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            
            Write-Host "  Fixed: $($file.Name)" -ForegroundColor Green
            $fixedCount++
        }
    }
    catch {
        Write-Host "  Error: $($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "Summary:" -ForegroundColor Cyan
Write-Host "  Fixed: $fixedCount files" -ForegroundColor Green
Write-Host "  Errors: $errorCount files" -ForegroundColor $(if ($errorCount -eq 0) { "Green" } else { "Red" })

if ($errorCount -eq 0) {
    Write-Host ""
    Write-Host "All BOM marks removed successfully!" -ForegroundColor Green
    Write-Host "Run: cd $baseDir; mvn clean compile -DskipTests" -ForegroundColor Cyan
} else {
    Write-Host ""
    Write-Host "Some files failed to fix, please check errors" -ForegroundColor Yellow
    exit 1
}
