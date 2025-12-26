# 批量包路径修复脚本
# 统一修复所有net.lab1024.sa.common.*包路径到net.lab1024.sa.platform.core.*

Write-Host "开始批量修复包路径..." -ForegroundColor Green

$fixedFiles = 0
$totalFiles = 0
$errorFiles = @()

# 获取所有需要修复的Java文件
$javaFiles = Get-ChildItem -Path "microservices" -Filter "*.java" -Recurse
$totalFiles = $javaFiles.Count

Write-Host "发现 $totalFiles 个Java文件，正在检查包路径..." -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content $file.FullName -Raw
        $originalContent = $content
        $hasChanges = $false

        # 修复ResponseDTO包路径
        if ($content -match 'net\.lab1024\.sa\.common\.dto') {
            $content = $content -replace 'net\.lab1024\.sa\.common\.dto', 'net.lab1024.sa.platform.core.dto'
            $hasChanges = $true
        }

        # 修复异常类包路径
        if ($content -match 'net\.lab1024\.sa\.common\.exception') {
            $content = $content -replace 'net\.lab1024\.sa\.common\.exception', 'net.lab1024.sa.platform.core.exception'
            $hasChanges = $true
        }

        # 修复工具类包路径
        if ($content -match 'net\.lab1024\.sa\.common\.util') {
            $content = $content -replace 'net\.lab1024\.sa\.common\.util', 'net.lab1024.sa.platform.core.util'
            $hasChanges = $true
        }

        # 修复PageResult包路径
        if ($content -match 'net\.lab1024\.sa\.common\.page') {
            $content = $content -replace 'net\.lab1024\.sa\.common\.page', 'net.lab1024.sa.platform.core.page'
            $hasChanges = $true
        }

        # 如果有修改，写回文件
        if ($hasChanges) {
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8
            $fixedFiles++
            Write-Host "✅ 修复: $($file.FullName.Replace("D:\IOE-DREAM\", ""))" -ForegroundColor Cyan
        }
    }
    catch {
        Write-Host "❌ 错误: 处理文件失败 - $($file.FullName), 错误: $($_.Exception.Message)" -ForegroundColor Red
        $errorFiles += $file.FullName
    }
}

Write-Host "`n=== 包路径修复完成 ===" -ForegroundColor Green
Write-Host "总文件数: $totalFiles" -ForegroundColor Cyan
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green
Write-Host "错误文件数: $($errorFiles.Count)" -ForegroundColor Red

if ($errorFiles.Count -gt 0) {
    Write-Host "`n错误文件列表:" -ForegroundColor Red
    foreach ($errorFile in $errorFiles) {
        Write-Host "  - $errorFile" -ForegroundColor DarkRed
    }
}

# 生成修复报告
$reportContent = @"
包路径批量修复报告
================

修复时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
修复目录: microservices
总扫描文件数: $totalFiles
成功修复文件数: $fixedFiles
修复失败文件数: $($errorFiles.Count)
修复成功率: $([math]::Round(($fixedFiles / $totalFiles) * 100, 2))%

修复规则:
- net.lab1024.sa.common.dto → net.lab1024.sa.platform.core.dto
- net.lab1024.sa.common.exception → net.lab1024.sa.platform.core.exception
- net.lab1024.sa.common.util → net.lab1024.sa.platform.core.util
- net.lab1024.sa.common.page → net.lab1024.sa.platform.core.page

"@

if ($errorFiles.Count -gt 0) {
    $reportContent += "`n修复失败文件:`n------------`n"
    foreach ($errorFile in $errorFiles) {
        $reportContent += "$errorFile`n"
    }
}

$reportContent | Out-File -FilePath "package-path-fix-report.txt" -Encoding UTF8

Write-Host "修复报告: package-path-fix-report.txt" -ForegroundColor Cyan

if ($fixedFiles -gt 0) {
    Write-Host "`n🎉 包路径修复成功！建议运行编译验证修复效果。" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ 没有发现需要修复的包路径。" -ForegroundColor Yellow
}