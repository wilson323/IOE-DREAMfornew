# BOM字符批量清理脚本
# 清理所有Java文件中的UTF-8 BOM字符

Write-Host "开始批量清理BOM字符..." -ForegroundColor Green

$bomReportFile = "bom-files-report.txt"
$cleanReportFile = "bom-cleanup-results.txt"

# 读取BOM文件清单
if (!(Test-Path $bomReportFile)) {
    Write-Host "❌ 错误: 找不到BOM文件报告 $bomReportFile" -ForegroundColor Red
    exit 1
}

$reportContent = Get-Content $bomReportFile -Raw
$cleanStats = @{
    TotalFiles = 0
    SuccessCount = 0
    ErrorCount = 0
    SkippedCount = 0
    ErrorFiles = @()
    ProcessedFiles = @()
}

# 提取BOM文件路径
$bomFiles = @()
$lines = $reportContent -split "`n"
$inFileList = $false

foreach ($line in $lines) {
    if ($line -match "BOM文件清单:") {
        $inFileList = $true
        continue
    }
    if ($line -match "⚠️ 发现") {
        break
    }
    if ($inFileList -and $line.Trim() -ne "") {
        # 提取文件路径 (去掉大小小和修改时间信息)
        if ($line -match "^([^(]+)") {
            $filePath = $matches[1].Trim()
            if (Test-Path $filePath) {
                $bomFiles += $filePath
            }
        }
    }
}

$cleanStats.TotalFiles = $bomFiles.Count
Write-Host "发现 $($cleanStats.TotalFiles) 个BOM文件需要清理" -ForegroundColor Yellow

# 清理每个文件的BOM字符
foreach ($sourceFile in $bomFiles) {
    try {
        Write-Host "处理: $sourceFile" -ForegroundColor Cyan

        # 读取文件字节
        $bytes = [System.IO.File]::ReadAllBytes($sourceFile)

        # 检查是否有BOM字符
        if ($bytes.Length -ge 3 -and
            $bytes[0] -eq 0xEF -and
            $bytes[1] -eq 0xBB -and
            $bytes[2] -eq 0xBF) {

            # 移除BOM字符
            $cleanBytes = $bytes[3..($bytes.Length-1)]

            # 创建临时文件
            $tempFile = $sourceFile + ".tmp"

            # 写入清理后的内容
            [System.IO.File]::WriteAllBytes($tempFile, $cleanBytes)

            # 验证BOM已移除
            $tempBytes = [System.IO.File]::ReadAllBytes($tempFile)
            $hasBOM = $tempBytes.Length -ge 3 -and
                     $tempBytes[0] -eq 0xEF -and
                     $tempBytes[1] -eq 0xBB -and
                     $tempBytes[2] -eq 0xBF

            if (!$hasBOM) {
                # 备份原文件（再次确保安全）
                $backupFile = $sourceFile + ".original.bak"
                Copy-Item -Path $sourceFile -Destination $backupFile -Force

                # 替换原文件
                Remove-Item -Path $sourceFile -Force
                Move-Item -Path $tempFile -Destination $sourceFile

                Write-Host "✅ 清理成功: $sourceFile" -ForegroundColor Green
                $cleanStats.SuccessCount++
                $cleanStats.ProcessedFiles += @{
                    File = $sourceFile
                    OriginalSize = $bytes.Length
                    CleanedSize = $cleanBytes.Length
                    RemovedBytes = 3
                }
            } else {
                Write-Host "❌ 清理失败，BOM字符仍然存在: $sourceFile" -ForegroundColor Red
                Remove-Item -Path $tempFile -Force -ErrorAction SilentlyContinue
                $cleanStats.ErrorCount++
                $cleanStats.ErrorFiles += $sourceFile
            }
        } else {
            Write-Host "⚠️ 文件不包含BOM字符，跳过: $sourceFile" -ForegroundColor Yellow
            $cleanStats.SkippedCount++
        }
    }
    catch {
        Write-Host "❌ 处理失败: $sourceFile, 错误: $($_.Exception.Message)" -ForegroundColor Red
        $cleanStats.ErrorCount++
        $cleanStats.ErrorFiles += $sourceFile

        # 清理临时文件
        $tempFile = $sourceFile + ".tmp"
        if (Test-Path $tempFile) {
            Remove-Item -Path $tempFile -Force -ErrorAction SilentlyContinue
        }
    }
}

# 生成清理结果报告
$reportContent = @"
BOM字符清理结果报告
==================

清理时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
总文件数: $($cleanStats.TotalFiles)
清理成功: $($cleanStats.SuccessCount)
清理失败: $($cleanStats.ErrorCount)
跳过文件: $($cleanStats.SkippedCount)
成功率: $([math]::Round(($cleanStats.SuccessCount / $cleanStats.TotalFiles) * 100, 2))%

节省空间: $($cleanStats.ProcessedFiles.Count * 3) 字节 (每个文件移除3个BOM字节)

"@

if ($cleanStats.ErrorFiles.Count -gt 0) {
    $reportContent += "`n失败文件列表:`n------------`n"
    foreach ($errorFile in $cleanStats.ErrorFiles) {
        $reportContent += "$errorFile`n"
    }
}

$reportContent += "`n成功清理的文件详情:`n------------------`n"
foreach ($processedFile in $cleanStats.ProcessedFiles) {
    $relativePath = $processedFile.File.Replace("D:\IOE-DREAM\", "")
    $reportContent += "$relativePath`n"
    $reportContent += "  原始大小: $($processedFile.OriginalSize) 字节`n"
    $reportContent += "  清理后大小: $($processedFile.CleanedSize) 字节`n"
    $reportContent += "  移除BOM字节: $($processedFile.RemovedBytes) 字节`n`n"
}

# 保存清理报告
$reportContent | Out-File -FilePath $cleanReportFile -Encoding UTF8

Write-Host "`nBOM字符清理操作完成！" -ForegroundColor Green
Write-Host "总文件数: $($cleanStats.TotalFiles)" -ForegroundColor Cyan
Write-Host "清理成功: $($cleanStats.SuccessCount)" -ForegroundColor Green
Write-Host "清理失败: $($cleanStats.ErrorCount)" -ForegroundColor Red
Write-Host "跳过文件: $($cleanStats.SkippedCount)" -ForegroundColor Yellow
Write-Host "成功率: $([math]::Round(($cleanStats.SuccessCount / $cleanStats.TotalFiles) * 100, 2))%" -ForegroundColor Cyan
Write-Host "节省空间: $($cleanStats.ProcessedFiles.Count * 3) 字节" -ForegroundColor Cyan
Write-Host "清理报告: $cleanReportFile" -ForegroundColor Cyan

# 按模块统计
Write-Host "`n按模块清理统计:" -ForegroundColor Yellow
$moduleStats = @()
foreach ($file in $cleanStats.ProcessedFiles) {
    $module = $file.File.Split('\')[1]
    $moduleStats += $module
}

$moduleGroups = $moduleStats | Group-Object
foreach ($group in $moduleGroups) {
    Write-Host "  $($group.Name): $($group.Count) 个文件" -ForegroundColor White
}

if ($cleanStats.ErrorCount -eq 0 -and $cleanStats.SkippedCount -eq 0) {
    Write-Host "`n🎉 所有BOM字符清理成功！" -ForegroundColor Green
    Write-Host "建议接下来运行验证脚本确认清理结果。" -ForegroundColor Cyan
    exit 0
} elseif ($cleanStats.ErrorCount -eq 0 -and $cleanStats.SkippedCount -gt 0) {
    Write-Host "`n✅ 清理完成，部分文件不包含BOM字符已跳过。" -ForegroundColor Yellow
    exit 0
} else {
    Write-Host "`n⚠️ 清理过程中存在错误，请检查上述失败文件。" -ForegroundColor Yellow
    exit 1
}