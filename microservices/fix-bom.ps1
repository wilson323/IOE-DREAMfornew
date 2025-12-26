# IOE-DREAM BOM字符修复脚本
# 修复UTF-8 BOM导致的编译错误

Write-Host "🚨 发现BOM字符问题，开始修复..." -ForegroundColor Red

# 查找所有包含BOM字符的文件
$problemFiles = Get-ChildItem -Path . -Recurse -Filter "*.java" | ForEach-Object {
    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $_
    }
}

Write-Host "📊 发现 $($problemFiles.Count) 个包含BOM字符的文件" -ForegroundColor Yellow

$fixedCount = 0
foreach ($file in $problemFiles) {
    Write-Host "🔧 修复BOM: $($file.Path)" -ForegroundColor Cyan

    # 读取文件内容（去除BOM）
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

    # 检查是否还有BOM
    if ($content.StartsWith("`uFEFF")) {
        $content = $content.Substring(1)
    }

    # 保存为UTF-8无BOM
    [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
    $fixedCount++
}

Write-Host "✅ BOM修复完成! 修复了 $fixedCount 个文件" -ForegroundColor Green