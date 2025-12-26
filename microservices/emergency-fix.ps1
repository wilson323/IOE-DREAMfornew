# IOE-DREAM 紧急批量修复脚本
# 直接处理所有import语句问题

Write-Host "🚨 执行紧急批量修复..." -ForegroundColor Red

# 获取所有需要修复的文件
$files = Get-ChildItem -Path . -Recurse -Filter "*.java" | Select-String -Pattern "import static org\.ju" | Select-Object -Unique Path

Write-Host "📊 发现 $($files.Count) 个需要修复的文件" -ForegroundColor Yellow

$fixedCount = 0
foreach ($file in $files) {
    Write-Host "🔧 修复: $($file.Path)" -ForegroundColor Cyan

    # 直接读取文件内容
    $content = Get-Content -Path $file.Path -Raw

    # 立即修复: 替换所有不完整的import语句
    $content = $content -replace '(?m)^\s*import static org\.ju\s*$', 'import static org.junit.jupiter.api.Assertions.*;'
    $content = $content -replace '(?m)^\s*import static org\.junit\.jupiter\.api\.Assertions\s*$', 'import static org.junit.jupiter.api.Assertions.*;'

    # 保存文件
    Set-Content -Path $file.Path -Value $content -NoNewline -Encoding UTF8
    $fixedCount++
}

Write-Host "✅ 紧急修复完成! 修复了 $fixedCount 个文件" -ForegroundColor Green