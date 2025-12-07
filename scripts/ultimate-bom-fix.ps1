# 终极BOM编码修复脚本
# 从字节级别彻底清除所有Java文件的BOM标记

$ErrorActionPreference = "Stop"

# 扫描所有microservices下的Java文件
$javaFiles = Get-ChildItem -Path "D:\IOE-DREAM\microservices" -Filter "*.java" -Recurse |
    Where-Object { $_.DirectoryName -notmatch "target" }

Write-Host "===== Ultimate BOM Fix =====" -ForegroundColor Cyan
Write-Host "Scanning $($javaFiles.Count) Java files...`n" -ForegroundColor Yellow

$fixedCount = 0
$scannedCount = 0

foreach ($file in $javaFiles) {
    $scannedCount++

    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)

        # 检查BOM标记 (0xEF 0xBB 0xBF)
        if ($bytes.Length -ge 3 -and
            $bytes[0] -eq 0xEF -and
            $bytes[1] -eq 0xBB -and
            $bytes[2] -eq 0xBF) {

            # 移除BOM
            $newBytes = $bytes[3..($bytes.Length-1)]
            [System.IO.File]::WriteAllBytes($file.FullName, $newBytes)

            Write-Host "Fixed BOM: $($file.FullName.Replace('D:\IOE-DREAM\microservices\', ''))" -ForegroundColor Green
            $fixedCount++
        }

        if ($scannedCount % 100 -eq 0) {
            Write-Host "Progress: $scannedCount/$($javaFiles.Count) files scanned..." -ForegroundColor Gray
        }

    } catch {
        Write-Host "Error processing $($file.Name): $_" -ForegroundColor Red
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Scanned: $scannedCount files" -ForegroundColor Gray
Write-Host "Fixed: $fixedCount files with BOM" -ForegroundColor Green

if ($fixedCount -gt 0) {
    Write-Host "`nRecommendation: Run Maven compile to verify" -ForegroundColor Yellow
}

