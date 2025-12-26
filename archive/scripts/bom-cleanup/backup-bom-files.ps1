# BOM文件备份脚本
# 在清理BOM字符之前备份所有受影响的文件

Write-Host "开始备份BOM文件..." -ForegroundColor Green

$backupRoot = "bom-backup"
$bomReportFile = "bom-files-report.txt"
$backupLog = "backup-integrity-report.txt"

# 创建备份根目录
if (!(Test-Path $backupRoot)) {
    New-Item -ItemType Directory -Path $backupRoot | Out-Null
    Write-Host "创建备份目录: $backupRoot" -ForegroundColor Cyan
}

$backupTime = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
$backupDir = Join-Path $backupRoot "backup_$backupTime"
New-Item -ItemType Directory -Path $backupDir | Out-Null

Write-Host "备份目录: $backupDir" -ForegroundColor Cyan

# 读取BOM文件清单
if (!(Test-Path $bomReportFile)) {
    Write-Host "❌ 错误: 找不到BOM文件报告 $bomReportFile" -ForegroundColor Red
    exit 1
}

$reportContent = Get-Content $bomReportFile -Raw
$backupStats = @{
    TotalFiles = 0
    SuccessCount = 0
    ErrorCount = 0
    ErrorFiles = @()
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

$backupStats.TotalFiles = $bomFiles.Count
Write-Host "发现 $($backupStats.TotalFiles) 个BOM文件需要备份" -ForegroundColor Yellow

# 备份每个文件
foreach ($sourceFile in $bomFiles) {
    try {
        # 计算相对路径
        $relativePath = $sourceFile.Replace("D:\IOE-DREAM\", "")
        $backupFilePath = Join-Path $backupDir $relativePath

        # 创建备份目录结构
        $backupDirPath = Split-Path $backupFilePath -Parent
        if (!(Test-Path $backupDirPath)) {
            New-Item -ItemType Directory -Path $backupDirPath -Force | Out-Null
        }

        # 复制文件
        Copy-Item -Path $sourceFile -Destination $backupFilePath -Force

        # 验证备份完整性
        $sourceHash = (Get-FileHash $sourceFile -Algorithm SHA256).Hash
        $backupHash = (Get-FileHash $backupFilePath -Algorithm SHA256).Hash

        if ($sourceHash -eq $backupHash) {
            Write-Host "✅ 备份成功: $relativePath" -ForegroundColor Green
            $backupStats.SuccessCount++
        } else {
            Write-Host "❌ 备份验证失败: $relativePath" -ForegroundColor Red
            $backupStats.ErrorCount++
            $backupStats.ErrorFiles += $relativePath
        }
    }
    catch {
        Write-Host "❌ 备份失败: $sourceFile, 错误: $($_.Exception.Message)" -ForegroundColor Red
        $backupStats.ErrorCount++
        $backupStats.ErrorFiles += $sourceFile
    }
}

# 生成备份完整性报告
$reportContent = @"
BOM文件备份完整性报告
==================

备份时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
备份目录: $backupDir
源文件总数: $($backupStats.TotalFiles)
成功备份: $($backupStats.SuccessCount)
备份失败: $($backupStats.ErrorCount)
成功率: $([math]::Round(($backupStats.SuccessCount / $backupStats.TotalFiles) * 100, 2))%

"@

if ($backupStats.ErrorFiles.Count -gt 0) {
    $reportContent += "`n失败文件列表:`n------------`n"
    foreach ($errorFile in $backupStats.ErrorFiles) {
        $reportContent += "$errorFile`n"
    }
}

if ($backupStats.ErrorCount -eq 0) {
    $reportContent += "`n✅ 所有文件备份成功，完整性验证通过！"
} else {
    $reportContent += "`n⚠️ 存在备份失败的文件，请检查上述错误信息。"
}

# 保存备份报告
$reportContent | Out-File -FilePath $backupLog -Encoding UTF8

Write-Host "`n备份操作完成！" -ForegroundColor Green
Write-Host "总文件数: $($backupStats.TotalFiles)" -ForegroundColor Cyan
Write-Host "成功备份: $($backupStats.SuccessCount)" -ForegroundColor Green
Write-Host "备份失败: $($backupStats.ErrorCount)" -ForegroundColor Red
Write-Host "成功率: $([math]::Round(($backupStats.SuccessCount / $backupStats.TotalFiles) * 100, 2))%" -ForegroundColor Cyan
Write-Host "备份目录: $backupDir" -ForegroundColor Cyan
Write-Host "备份报告: $backupLog" -ForegroundColor Cyan

# 输出模块统计
Write-Host "`n按模块备份统计:" -ForegroundColor Yellow
$moduleStats = $bomFiles | Group-Object { $_.Split('\')[1] }
foreach ($stat in $moduleStats) {
    $successInModule = $stat.Count
    Write-Host "  $($stat.Name): $successInModule 个文件" -ForegroundColor White
}

if ($backupStats.ErrorCount -eq 0) {
    Write-Host "`n🎉 备份完成！可以安全进行BOM清理操作。" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n⚠️ 备份过程中存在错误，请修复后重试。" -ForegroundColor Yellow
    exit 1
}