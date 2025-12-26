# MicroServices Common模块企业级BOM清理脚本
# 系统性解决所有BOM字符导致的编译错误

Write-Host "=== MicroServices Common模块企业级BOM清理脚本 ===" -ForegroundColor Cyan

# 获取所有有BOM字符的Java文件
$bomFiles = Get-ChildItem -Path "microservices-common/src" -Name "*.java" -Recurse | ForEach-Object {
    $filePath = Join-Path "microservices-common/src" $_
    $bytes = [System.IO.File]::ReadAllBytes($filePath)
    if ($bytes.Length -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $filePath
    }
}

Write-Host "发现 $($bomFiles.Count) 个文件存在BOM字符，开始企业级清理..." -ForegroundColor Yellow

$processedCount = 0
$errorCount = 0

foreach ($file in $bomFiles) {
    try {
        Write-Host "修复: $file" -ForegroundColor Green

        # 字节级BOM移除
        $bytes = [System.IO.File]::ReadAllBytes($file)
        if ($bytes.Length -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
            [System.IO.File]::WriteAllBytes($file, $bytesWithoutBom)
            $processedCount++
        }
    } catch {
        Write-Host "错误: 处理 $file 时发生异常 - $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host "`n=== 企业级BOM清理完成 ===" -ForegroundColor Cyan
Write-Host "✅ 成功处理: $processedCount 个文件" -ForegroundColor Green
Write-Host "❌ 处理失败: $errorCount 个文件" -ForegroundColor Red

Write-Host "`n验证清理结果..." -ForegroundColor Yellow

# 验证清理结果
$remainingBomFiles = Get-ChildItem -Path "microservices-common/src" -Name "*.java" -Recurse | ForEach-Object {
    $filePath = Join-Path "microservices-common/src" $_
    $bytes = [System.IO.File]::ReadAllBytes($filePath)
    if ($bytes.Length -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $filePath
    }
}

if ($remainingBomFiles.Count -eq 0) {
    Write-Host "🎉 所有BOM字符已清理完成！" -ForegroundColor Green
} else {
    Write-Host "⚠️ 仍有 $($remainingBomFiles.Count) 个文件存在BOM字符:" -ForegroundColor Yellow
    $remainingBomFiles | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
}

Write-Host "`n准备编译验证..." -ForegroundColor Cyan