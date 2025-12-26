# 彻底清理ioedream-oa-service的所有BOM字符
Write-Host "彻底清理ioedream-oa-service的所有BOM字符..." -ForegroundColor Green

$oaServicePath = "D:\IOE-DREAM\microservices\ioedream-oa-service"

if (Test-Path $oaServicePath) {
    $javaFiles = Get-ChildItem -Path $oaServicePath -Recurse -Filter "*.java"
    $bomCount = 0
    $totalFiles = $javaFiles.Count

    Write-Host "找到 $totalFiles 个Java文件"

    foreach ($file in $javaFiles) {
        try {
            $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
            if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
                $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
                [System.IO.File]::WriteAllBytes($file.FullName, $bytesWithoutBom)
                Write-Host "✓ BOM已移除: $($file.FullName.Replace($oaServicePath, 'ioedream-oa-service'))"
                $bomCount++
            }
        } catch {
            Write-Host "❌ 处理失败: $($file.FullName) - $($_.Exception.Message)"
        }
    }

    Write-Host "`nBOM清理完成!" -ForegroundColor Cyan
    Write-Host "总文件数: $totalFiles" -ForegroundColor White
    Write-Host "移除BOM数: $bomCount" -ForegroundColor White

    # 验证编译
    Write-Host "`n验证编译..." -ForegroundColor Green
    cd "D:\IOE-DREAM\microservices"
    $compileResult = mvn clean compile -q 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 所有服务编译成功！" -ForegroundColor Green
    } else {
        Write-Host "❌ 仍有编译错误:" -ForegroundColor Red
        $compileResult | Select-String "ERROR.*COMPILATION ERROR" -Context 0,1 | Select-Object -First 5
    }
} else {
    Write-Host "❌ OA服务目录不存在: $oaServicePath" -ForegroundColor Red
}