# IOE-DREAM 项目清理预览脚本
# 用途：快速预览将要清理的文件，不执行任何删除/移动操作

Write-Host @"
========================================
IOE-DREAM 项目清理预览
========================================
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
"@ -ForegroundColor Cyan

# 统计各类文件
function Get-FileStats {
    param(
        [string]$Pattern,
        [string]$Description
    )

    $files = Get-ChildItem -Path $Pattern -ErrorAction SilentlyContinue
    if ($files) {
        $totalSize = ($files | Measure-Object -Property Length -Sum).Sum

        [PSCustomObject]@{
            描述 = $Description
            数量 = $files.Count
            总大小 = "{0:N2} MB" -f ($totalSize / 1MB)
            文件列表 = ($files | Select-Object -First 5 -ExpandProperty Name) -join ", "
        }
    }
}

# Markdown文档统计
Write-Host "`n📄 Markdown文档统计:" -ForegroundColor Yellow
Write-Host ("="*80) -ForegroundColor Gray

$mdStats = @(
    (Get-FileStats "P0_*.md" "P0系列报告")
    (Get-FileStats "P2_*.md" "P2系列报告")
    (Get-FileStats "PHASE_*.md" "Phase系列报告")
    (Get-FileStats "*考勤*.md" "考勤模块报告")
    (Get-FileStats "*SCHEDULE*.md" "Smart Schedule报告")
    (Get-FileStats "*QUERYBUILDER*.md" "QueryBuilder报告")
    (Get-FileStats "*TEST*.md" "测试报告")
    (Get-FileStats "*_实施报告.md" "中文实施报告")
    (Get-FileStats "GLOBAL_*.md" "全局分析报告")
    (Get-FileStats "ENTERPRISE_*.md" "企业级报告")
)

$mdStats | Format-Table -AutoSize

$mdTotal = ($mdStats | Measure-Object -Property 数量 -Sum).Sum
$mdSize = ($mdStats | ForEach-Object {
    if ($_ -is [PSCustomObject]) {
        $sizeStr = $_.总大小 -replace " MB", ""
        [double]$sizeStr
    }
} | Measure-Object -Sum).Sum

Write-Host "📊 Markdown文档总计: $mdTotal 个文件, $mdSize MB" -ForegroundColor Green

# 日志文件统计
Write-Host "`n📋 日志和文本文件统计:" -ForegroundColor Yellow
Write-Host ("="*80) -ForegroundColor Gray

$logStats = @(
    (Get-FileStats "compile*.log" "编译日志")
    (Get-FileStats "compile*.txt" "编译文本")
    (Get-FileStats "*-errors*.txt" "错误记录")
    (Get-FileStats "*-report.txt" "报告文本")
    (Get-FileStats "garbled*.txt" "乱码文件列表")
)

$logStats | Format-Table -AutoSize

$logTotal = ($logStats | Measure-Object -Property 数量 -Sum).Sum
$logSize = ($logStats | ForEach-Object {
    if ($_ -is [PSCustomObject]) {
        $sizeStr = $_.总大小 -replace " MB", ""
        [double]$sizeStr
    }
} | Measure-Object -Sum).Sum

Write-Host "📊 日志文件总计: $logTotal 个文件, $logSize MB" -ForegroundColor Green

# 脚本文件统计
Write-Host "`n🔧 脚本文件统计:" -ForegroundColor Yellow
Write-Host ("="*80) -ForegroundColor Gray

$scriptStats = @(
    (Get-FileStats "*bom*.ps1" "BOM清理脚本")
    (Get-FileStats "*encoding*.ps1" "编码修复脚本")
    (Get-FileStats "*cast*.ps1" "类型转换修复脚本")
    (Get-FileStats "*logging*.sh" "日志修复脚本")
    (Get-FileStats "fix-*.ps1" "PowerShell修复脚本")
    (Get-FileStats "fix-*.py" "Python修复脚本")
)

$scriptStats | Format-Table -AutoSize

$scriptTotal = ($scriptStats | Measure-Object -Property 数量 -Sum).Sum
$scriptSize = ($scriptStats | ForEach-Object {
    if ($_ -is [PSCustomObject]) {
        $sizeStr = $_.总大小 -replace " MB", ""
        [double]$sizeStr
    }
} | Measure-Object -Sum).Sum

Write-Host "📊 脚本文件总计: $scriptTotal 个文件, $scriptSize MB" -ForegroundColor Green

# 总计
$totalFiles = $mdTotal + $logTotal + $scriptTotal
$totalSize = $mdSize + $logSize + $scriptSize

Write-Host "`n" + ("="*80) -ForegroundColor Gray
Write-Host "📈 总计统计:" -ForegroundColor Cyan
Write-Host "  - 文件总数: $totalFiles" -ForegroundColor White
Write-Host "  - 总大小: $totalSize MB" -ForegroundColor White
Write-Host "  - 预估可释放空间: $totalSize MB" -ForegroundColor Green

# 清理建议
Write-Host "`n💡 清理建议:" -ForegroundColor Yellow
Write-Host "  1. 立即执行: .\scripts\cleanup-project-root.ps1 -DryRun" -ForegroundColor White
Write-Host "  2. 交互确认: .\scripts\cleanup-project-root.ps1 -Confirm" -ForegroundColor White
Write-Host "  3. 分步执行: .\scripts\cleanup-project-root.ps1 -Phase 1" -ForegroundColor White
Write-Host "  4. 查看详情: .\PROJECT_CLEANUP_ANALYSIS_REPORT.md" -ForegroundColor White

# 保留文件清单
Write-Host "`n✅ 核心文件（将保留）:" -ForegroundColor Green
$coreFiles = @(
    "CLAUDE.md",
    "README.md",
    "AGENTS.md",
    "PROJECT_STATUS_CURRENT.md",
    "RABBITMQ_QUICK_START.md"
)

$coreFiles | ForEach-Object {
    if (Test-Path $_) {
        Write-Host "  ✓ $_" -ForegroundColor Green
    }
}

Write-Host "`n⚠️  注意事项:" -ForegroundColor Yellow
Write-Host "  1. 清理前请确保已提交所有Git更改" -ForegroundColor White
Write-Host "  2. 建议先使用 -DryRun 参数预览" -ForegroundColor White
Write-Host "  3. 清理后的文件将移动到 archive/ 目录" -ForegroundColor White
Write-Host "  4. 日志文件将被永久删除，请确认不需要" -ForegroundColor White

Write-Host "`n" + ("="*80) -ForegroundColor Gray
Write-Host "预览完成！" -ForegroundColor Green
