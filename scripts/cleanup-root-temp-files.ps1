# IOE-DREAM 根目录临时文件清理脚本
# 自动识别并归档临时报告文件

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  根目录临时文件清理" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 定义归档目录
$archiveDir = "documentation\project\archive\reports"
$rootDir = "D:\IOE-DREAM"

# 创建归档目录
if (-not (Test-Path $archiveDir)) {
    New-Item -ItemType Directory -Path $archiveDir -Force | Out-Null
    Write-Host "✅ 创建归档目录: $archiveDir" -ForegroundColor Green
}

# 定义临时文件模式
$tempFilePatterns = @(
    "MERGE_*.md",
    "FINAL_*.md",
    "COMPLETE_*.md",
    "*_FINAL_*.md",
    "*_COMPLETE_*.md",
    "*_SUMMARY.md",
    "*_REPORT.md",
    "*_ANALYSIS.md",
    "*_FIX.md",
    "*_STATUS.md"
)

# 分类归档
$categories = @{
    "merge-reports" = @("MERGE_*.md")
    "final-reports" = @("FINAL_*.md", "*_FINAL_*.md")
    "complete-reports" = @("COMPLETE_*.md", "*_COMPLETE_*.md")
    "summary-reports" = @("*_SUMMARY.md")
    "analysis-reports" = @("*_ANALYSIS.md", "*_REPORT.md")
    "fix-reports" = @("*_FIX.md")
    "status-reports" = @("*_STATUS.md")
}

$movedFiles = @()
$skippedFiles = @()

Write-Host "[1] 扫描根目录临时文件..." -ForegroundColor Yellow
Push-Location $rootDir

foreach ($category in $categories.Keys) {
    $categoryDir = Join-Path $archiveDir $category
    if (-not (Test-Path $categoryDir)) {
        New-Item -ItemType Directory -Path $categoryDir -Force | Out-Null
    }
    
    foreach ($pattern in $categories[$category]) {
        $files = Get-ChildItem -Path . -Filter $pattern -File -ErrorAction SilentlyContinue
        
        foreach ($file in $files) {
            # 跳过关键文件
            if ($file.Name -eq "CLAUDE.md" -or 
                $file.Name -eq "DEPLOYMENT.md" -or
                $file.Name -eq "README.md") {
                $skippedFiles += $file
                continue
            }
            
            try {
                $destPath = Join-Path $categoryDir $file.Name
                
                # 如果目标文件已存在，添加时间戳
                if (Test-Path $destPath) {
                    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
                    $nameWithoutExt = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
                    $ext = [System.IO.Path]::GetExtension($file.Name)
                    $destPath = Join-Path $categoryDir "${nameWithoutExt}_${timestamp}${ext}"
                }
                
                Move-Item -Path $file.FullName -Destination $destPath -Force
                $movedFiles += @{
                    Source = $file.Name
                    Destination = $destPath
                    Category = $category
                }
                Write-Host "  ✅ 已归档: $($file.Name) -> $category" -ForegroundColor Green
            } catch {
                Write-Host "  ❌ 归档失败: $($file.Name) - $_" -ForegroundColor Red
            }
        }
    }
}

Pop-Location

# 生成清理报告
Write-Host "`n[2] 生成清理报告..." -ForegroundColor Yellow
$reportPath = Join-Path $archiveDir "cleanup-report-$(Get-Date -Format 'yyyyMMdd_HHmmss').md"

$report = "# 根目录临时文件清理报告`n`n"
$report += "**清理时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n"
$report += "**清理范围**: 根目录临时报告文件`n`n"
$report += "## 清理统计`n`n"
$report += "- **已归档文件**: $($movedFiles.Count) 个`n"
$report += "- **跳过文件**: $($skippedFiles.Count) 个`n`n"
$report += "## 已归档文件列表`n`n"

foreach ($file in $movedFiles) {
    $report += "- **$($file.Source)** -> $($file.Category)/`n"
}

if ($skippedFiles.Count -gt 0) {
    $report += "`n## 跳过的文件（关键文件）`n`n"
    foreach ($file in $skippedFiles) {
        $report += "- $($file.Name)`n"
    }
}

$report | Out-File -FilePath $reportPath -Encoding UTF8
Write-Host "  ✅ 清理报告已生成: $reportPath" -ForegroundColor Green

# 总结
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  清理完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "已归档文件: $($movedFiles.Count) 个" -ForegroundColor Green
Write-Host "跳过文件: $($skippedFiles.Count) 个" -ForegroundColor Yellow
Write-Host "归档目录: $archiveDir" -ForegroundColor Cyan
Write-Host ""
