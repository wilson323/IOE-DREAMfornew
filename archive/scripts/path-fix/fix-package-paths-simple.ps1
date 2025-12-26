# 简化版包路径修复脚本
Write-Host "开始批量修复包路径..." -ForegroundColor Green

$fixedFiles = 0
$totalFiles = 0

# 获取所有需要修复的Java文件
$javaFiles = Get-ChildItem -Path "microservices" -Filter "*.java" -Recurse
$totalFiles = $javaFiles.Count

Write-Host "发现 $totalFiles 个Java文件，正在检查包路径..." -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content $file.FullName -Raw
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
            $content = $content -replace 'net\.lab1024\.sa\.common\.page', 'net.lab1024.sa.common.domain'
            $hasChanges = $true
        }

        # 如果有修改，写回文件
        if ($hasChanges) {
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8
            $fixedFiles++
            $relativePath = $file.FullName.Replace('D:\IOE-DREAM\', '')
            Write-Host "✅ 修复: $relativePath" -ForegroundColor Cyan
        }
    }
    catch {
        Write-Host "❌ 错误: 处理文件失败 - $($file.FullName)" -ForegroundColor Red
    }
}

Write-Host "`n=== 包路径修复完成 ===" -ForegroundColor Green
Write-Host "总文件数: $totalFiles" -ForegroundColor Cyan
Write-Host "修复文件数: $fixedFiles" -ForegroundColor Green

# 生成修复报告
"包路径批量修复报告
================
修复时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
修复目录: microservices
总扫描文件数: $totalFiles
成功修复文件数: $fixedFiles
修复成功率: $([math]::Round(($fixedFiles / $totalFiles) * 100, 2))%

修复规则:
- net.lab1024.sa.common.dto → net.lab1024.sa.platform.core.dto
- net.lab1024.sa.common.exception → net.lab1024.sa.platform.core.exception
- net.lab1024.sa.common.util → net.lab1024.sa.platform.core.util
- net.lab1024.sa.common.page → net.lab1024.sa.common.domain
" | Out-File -FilePath "package-path-fix-report.txt" -Encoding UTF8

Write-Host "修复报告: package-path-fix-report.txt" -ForegroundColor Cyan

if ($fixedFiles -gt 0) {
    Write-Host "`n🎉 包路径修复成功！建议运行编译验证修复效果。" -ForegroundColor Green
} else {
    Write-Host "`n⚠️ 没有发现需要修复的包路径。" -ForegroundColor Yellow
}