# IOE-DREAM 企业级Import语句修复脚本 (PowerShell版本)
# 修复所有不完整的junit import语句

param(
    [switch]$DryRun = $false,
    [string]$TargetPath = "."
)

Write-Host "🚀 开始企业级Import语句修复..." -ForegroundColor Green

# 查找所有需要修复的文件
$problemFiles = Get-ChildItem -Path $TargetPath -Recurse -Filter "*.java" |
    Select-String -Pattern "import static org\.ju" |
    Select-Object -Unique Path

Write-Host "📊 发现 $($problemFiles.Count) 个需要修复的文件" -ForegroundColor Yellow

if ($DryRun) {
    Write-Host "🔍 DRY RUN模式 - 仅显示将要修复的文件:" -ForegroundColor Cyan
    $problemFiles | ForEach-Object { Write-Host "  📄 $($_.Path)" }
    return
}

$fixedCount = 0

# 修复每个文件
foreach ($file in $problemFiles) {
    $content = Get-Content -Path $file.Path -Raw
    $originalContent = $content
    $changed = $false

    # 修复策略1: 修复截断的junit import
    if ($content -match '(?m)^\s*import static org\.ju\s*$') {
        $content = $content -replace '(?m)^\s*import static org\.ju\s*$', 'import static org.junit.jupiter.api.Assertions.*;'
        $changed = $true
        Write-Host "🔧 修复截断的junit import: $($file.Path)" -ForegroundColor Yellow
    }

    # 修复策略2: 修复不完整的Assertions导入
    if ($content -match '(?m)^\s*import static org\.junit\.jupiter\.api\.Assertions\s*$') {
        $content = $content -replace '(?m)^\s*import static org\.junit\.jupiter\.api\.Assertions\s*$', 'import static org.junit.jupiter.api.Assertions.*;'
        $changed = $true
        Write-Host "🔧 修复不完整的Assertions导入: $($file.Path)" -ForegroundColor Yellow
    }

    # 修复策略3: 删除重复的Assertions导入
    $content = $content -replace '(?m)import static org\.junit\.jupiter\.api\.Assertions\.\*;\s*\nimport static org\.junit\.jupiter\.api\.Assertions\.\*;', 'import static org.junit.jupiter.api.Assertions.*;'

    if ($changed -or $content -ne $originalContent) {
        Set-Content -Path $file.Path -Value $content -NoNewline
        $fixedCount++
        Write-Host "✅ 已修复: $($file.Path)" -ForegroundColor Green
    }
}

# 统计修复结果
$remainingFiles = Get-ChildItem -Path $TargetPath -Recurse -Filter "*.java" |
    Select-String -Pattern "import static org\.ju" |
    Select-Object -Unique Path

Write-Host "📈 修复统计:" -ForegroundColor Cyan
Write-Host "  📊 修复前问题文件数: $($problemFiles.Count)" -ForegroundColor White
Write-Host "  ✅ 成功修复文件数: $fixedCount" -ForegroundColor Green
Write-Host "  📉 仍有问题文件数: $($remainingFiles.Count)" -ForegroundColor Yellow

if ($remainingFiles.Count -eq 0) {
    Write-Host "🎉 所有问题文件已修复!" -ForegroundColor Green
} else {
    Write-Host "⚠️  仍有 $($remainingFiles.Count) 个文件需要手动检查:" -ForegroundColor Yellow
    $remainingFiles | ForEach-Object { Write-Host "  📄 $($_.Path)" }
}

Write-Host "📋 修复完成时间: $(Get-Date)" -ForegroundColor White
Write-Host "🏁 企业级Import语句修复脚本执行完成" -ForegroundColor Green