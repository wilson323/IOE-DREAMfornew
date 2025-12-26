# PowerShell脚本：修复优化相关的导入路径
# 作者：IOE-DREAM架构团队
# 日期：2025-12-26
# 用途：修复 attendance-engine.rule.model → attendance-engine.model 导入路径

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复优化模块导入路径工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义项目根目录
$projectRoot = "D:\IOE-DREAM\microservices\ioedream-attendance-service\src\main\java"

# 检查目录是否存在
if (-not (Test-Path $projectRoot)) {
    Write-Host "❌ 错误: 目录不存在 - $projectRoot" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 找到目录: $projectRoot" -ForegroundColor Green
Write-Host ""

# 查找所有Java文件
$javaFiles = Get-ChildItem -Path $projectRoot -Filter "*.java" -Recurse -File

Write-Host "📊 找到 $($javaFiles.Count) 个Java文件" -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0
$totalErrors = 0

# 遍历所有Java文件
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $originalContent = $content

    # 修复导入路径: attendance.engine.rule.model → attendance.engine.model
    if ($content -match 'import net\.lab1024\.sa\.attendance\.engine\.rule\.model\.') {
        $content = $content -replace 'import net\.lab1024\.sa\.attendance\.engine\.rule\.model\.', 'import net.lab1024.sa.attendance.engine.model.'
        $totalErrors++
    }

    # 如果内容有变化，写回文件
    if ($content -ne $originalContent) {
        [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
        $fixedCount++
        Write-Host "✅ 修复: $($file.Name.Replace($projectRoot, ''))" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复完成统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📊 发现错误: $totalErrors" -ForegroundColor Yellow
Write-Host "✅ 已修复文件: $fixedCount" -ForegroundColor Green
Write-Host ""

if ($fixedCount -gt 0) {
    Write-Host "✅ 导入路径修复完成!" -ForegroundColor Green
} else {
    Write-Host "ℹ️  没有发现需要修复的导入路径" -ForegroundColor Gray
}

Write-Host ""
