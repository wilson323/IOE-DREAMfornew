# 修复测试文件的BOM问题
$testDir = "microservices\ioedream-consume-service\src\test\java"
$files = Get-ChildItem -Path $testDir -Filter "*Test.java" -Recurse -File
$fixedCount = 0

foreach ($file in $files) {
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        $hasBom = ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF)

        if ($hasBom) {
            # 移除BOM并重新保存
            $content = [System.Text.Encoding]::UTF8.GetString($bytes, 3, $bytes.Length - 3)
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)
            $fixedCount++
            Write-Host "Fixed BOM: $($file.FullName)"
        }
    }
    catch {
        Write-Host "Error processing $($file.FullName): $_" -ForegroundColor Red
    }
}

Write-Host "`nTotal files fixed: $fixedCount" -ForegroundColor Green

