# ============================================================
# IOE-DREAM 临时文件清理脚本
#
# 功能：识别并归档根目录临时文件
# ============================================================

param(
    [switch]$DryRun = $false,
    [switch]$Detailed = $false,
    [string]$ArchiveDir = "documentation/project/archive/temp-files",
    [string[]]$TempFilePatterns = @("*.txt", "*-report.md", "*-report.json", "*-report.csv", "*.log"),
    [int]$MaxAgeDays = 30
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$archivePath = Join-Path $projectRoot $ArchiveDir

# 临时文件识别规则（排除关键文件）
$excludePatterns = @(
    "README*.md",
    "CHANGELOG*.md",
    "LICENSE*.txt",
    ".gitignore",
    ".gitattributes",
    "*.gitkeep"
)

$tempFiles = @()
$archivedFiles = @()
$deletedFiles = @()

if (-not $DryRun) {
    Write-Host "===== 临时文件清理 =====" -ForegroundColor Cyan
} else {
    Write-Host "===== 临时文件清理（预览模式） =====" -ForegroundColor Yellow
}
Write-Host ""

# 识别临时文件
Write-Host "[1/3] 识别临时文件..." -ForegroundColor Yellow

foreach ($pattern in $TempFilePatterns) {
    $files = Get-ChildItem -Path $projectRoot -Filter $pattern -File -ErrorAction SilentlyContinue
    
    foreach ($file in $files) {
        # 排除特定文件
        $shouldExclude = $false
        foreach ($exclude in $excludePatterns) {
            if ($file.Name -like $exclude) {
                $shouldExclude = $true
                break
            }
        }
        
        if (-not $shouldExclude) {
            # 检查文件年龄
            $ageDays = (Get-Date) - $file.LastWriteTime | Select-Object -ExpandProperty Days
            
            $tempFiles += @{
                File = $file.Name
                Path = $file.FullName
                Size = $file.Length
                LastWriteTime = $file.LastWriteTime
                AgeDays = $ageDays
                ShouldArchive = $ageDays -ge $MaxAgeDays
            }
        }
    }
}

Write-Host "  发现临时文件: $($tempFiles.Count) 个" -ForegroundColor $(if ($tempFiles.Count -eq 0) { "Green" } else { "Yellow" })
if ($Detailed -and $tempFiles.Count -gt 0) {
    $tempFiles | ForEach-Object {
        Write-Host "    - $($_.File) ($([Math]::Round($_.Size/1KB, 2)) KB, $($_.AgeDays) 天前)" -ForegroundColor Gray
    }
}
Write-Host ""

# 创建归档目录
if ($tempFiles.Count -gt 0 -and -not $DryRun) {
    if (-not (Test-Path $archivePath)) {
        New-Item -ItemType Directory -Path $archivePath -Force | Out-Null
        Write-Host "[2/3] 创建归档目录: $ArchiveDir" -ForegroundColor Green
    }
}

# 归档或删除文件
Write-Host "[3/3] 处理临时文件..." -ForegroundColor Yellow

foreach ($fileInfo in $tempFiles) {
    if ($DryRun) {
        if ($fileInfo.ShouldArchive) {
            Write-Host "  [将归档] $($fileInfo.File)" -ForegroundColor Yellow
        } else {
            Write-Host "  [将保留] $($fileInfo.File) (未超过$MaxAgeDays天)" -ForegroundColor Gray
        }
    } else {
        if ($fileInfo.ShouldArchive) {
            try {
                # 移动到归档目录，添加日期前缀
                $datePrefix = $fileInfo.LastWriteTime.ToString("yyyyMMdd")
                $archiveFileName = "${datePrefix}_$($fileInfo.File)"
                $archiveFilePath = Join-Path $archivePath $archiveFileName
                
                Move-Item -Path $fileInfo.Path -Destination $archiveFilePath -Force
                $archivedFiles += $fileInfo.File
                
                if ($Detailed) {
                    Write-Host "  [已归档] $($fileInfo.File) -> $archiveFileName" -ForegroundColor Green
                }
            } catch {
                Write-Warning "归档文件失败: $($fileInfo.File) - $_"
            }
        }
    }
}

# 生成报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$report = @{
    CheckType = "TempFileCleanup"
    Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    DryRun = $DryRun
    TotalFiles = $tempFiles.Count
    ArchivedFiles = if ($DryRun) { ($tempFiles | Where-Object { $_.ShouldArchive }).Count } else { $archivedFiles.Count }
    RetainedFiles = ($tempFiles | Where-Object { -not $_.ShouldArchive }).Count
    Files = $tempFiles
}

if (-not $DryRun) {
    $jsonPath = Join-Path $archivePath "cleanup-report_$timestamp.json"
    $report | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8
}

# 输出摘要
if (-not $DryRun) {
    Write-Host ""
    Write-Host "===== 清理完成 =====" -ForegroundColor Cyan
    Write-Host "  总文件数: $($tempFiles.Count)" -ForegroundColor Gray
    Write-Host "  已归档: $($archivedFiles.Count)" -ForegroundColor Green
    Write-Host "  保留: $(($tempFiles | Where-Object { -not $_.ShouldArchive }).Count)" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "===== 预览完成 =====" -ForegroundColor Cyan
    Write-Host "  将归档: $(($tempFiles | Where-Object { $_.ShouldArchive }).Count) 个文件" -ForegroundColor Yellow
    Write-Host "  将保留: $(($tempFiles | Where-Object { -not $_.ShouldArchive }).Count) 个文件" -ForegroundColor Gray
    Write-Host ""
    Write-Host "运行时不使用 -DryRun 参数以执行实际清理" -ForegroundColor Yellow
    Write-Host ""
}

