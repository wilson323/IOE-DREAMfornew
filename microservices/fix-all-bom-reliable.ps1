# 可靠的BOM清理脚本
Write-Host "=== 可靠的BOM清理脚本 ===" -ForegroundColor Cyan

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path "microservices-common/src" -Name "*.java" -Recurse

$processedCount = 0
$bomCount = 0

foreach ($file in $javaFiles) {
    $fullPath = Join-Path "microservices-common/src" $file

    try {
        # 读取文件字节
        $bytes = [System.IO.File]::ReadAllBytes($fullPath)

        # 检查是否有BOM
        if ($bytes.Length -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            Write-Host "发现BOM: $file" -ForegroundColor Yellow

            # 移除BOM
            $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
            [System.IO.File]::WriteAllBytes($fullPath, $bytesWithoutBom)

            $bomCount++
            Write-Host "✅ 已移除BOM: $file" -ForegroundColor Green
        }

        $processedCount++

    } catch {
        Write-Host "❌ 处理失败: $file - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== BOM清理统计 ===" -ForegroundColor Cyan
Write-Host "✅ 总处理文件: $processedCount" -ForegroundColor Green
Write-Host "🔧 移除BOM文件: $bomCount" -ForegroundColor Yellow

Write-Host "`n现在修复import语法错误..." -ForegroundColor Cyan

# 修复所有可能的import语法错误
$javaFiles = Get-ChildItem -Path "microservices-common/src" -Name "*.java" -Recurse
$fixedCount = 0

foreach ($file in $javaFiles) {
    $fullPath = Join-Path "microservices-common/src" $file

    try {
        $content = Get-Content -Path $fullPath -Raw -Encoding UTF8

        # 修复各种可能的语法错误
        $originalContent = $content
        $content = $content -replace 'iimport', 'import'
        $content = $content -replace 'i\r?\nimport', 'import'
        $content = $content -replace '\r\n\s*i\r?\nimport', '`r`nimport'

        if ($content -ne $originalContent) {
            $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
            [System.IO.File]::WriteAllText($fullPath, $content, $utf8NoBom)
            $fixedCount++
            Write-Host "✅ 修复语法: $file" -ForegroundColor Green
        }

    } catch {
        Write-Host "❌ 修复失败: $file - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== 语法修复统计 ===" -ForegroundColor Cyan
Write-Host "🔧 修复文件数: $fixedCount" -ForegroundColor Yellow

Write-Host "`n企业级修复完成！准备编译验证..." -ForegroundColor Cyan