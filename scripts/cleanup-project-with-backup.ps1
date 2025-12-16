# =====================================================
# IOE-DREAM 项目清理脚本（带备份）
# 版本: v1.0.0
# 描述: 清理项目不必要文件，清理前自动备份
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$DryRun,

    [Parameter(Mandatory=$false)]
    [string]$BackupDir = "bak\cleanup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
)

$ErrorActionPreference = "Stop"

# =====================================================
# 配置
# =====================================================

$ProjectRoot = Split-Path -Parent $PSScriptRoot
if (-not $ProjectRoot -or $ProjectRoot -eq "") {
    $ProjectRoot = (Get-Location).Path
}

$BackupPath = Join-Path $ProjectRoot $BackupDir

# =====================================================
# 辅助函数
# =====================================================

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

function Backup-File {
    param(
        [string]$SourcePath,
        [string]$RelativePath
    )

    if (-not (Test-Path $SourcePath)) {
        return $false
    }

    $targetPath = Join-Path $BackupPath $RelativePath
    $targetDir = Split-Path -Parent $targetPath

    try {
        if (-not (Test-Path $targetDir)) {
            New-Item -ItemType Directory -Path $targetDir -Force | Out-Null
        }

        if (Test-Path -Path $SourcePath -PathType Container) {
            Copy-Item -Path $SourcePath -Destination $targetPath -Recurse -Force
        } else {
            Copy-Item -Path $SourcePath -Destination $targetPath -Force
        }

        return $true
    } catch {
        Write-ColorOutput Red "[ERROR] Failed to backup: $SourcePath - $($_.Exception.Message)"
        return $false
    }
}

function Remove-FileSafe {
    param(
        [string]$Path,
        [string]$Description
    )

    if (-not (Test-Path $Path)) {
        return $false
    }

    try {
        if ($DryRun) {
            Write-ColorOutput Yellow "[DRY-RUN] Would remove: $Description ($Path)"
            return $true
        }

        if (Test-Path -Path $Path -PathType Container) {
            Remove-Item -Path $Path -Recurse -Force
        } else {
            Remove-Item -Path $Path -Force
        }

        Write-ColorOutput Green "[OK] Removed: $Description"
        return $true
    } catch {
        Write-ColorOutput Red "[ERROR] Failed to remove: $Path - $($_.Exception.Message)"
        return $false
    }
}

# =====================================================
# 清理项目
# =====================================================

Write-ColorOutput Cyan "========================================"
Write-ColorOutput Cyan "  IOE-DREAM Project Cleanup"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "Start time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output "Project root: $ProjectRoot"
Write-Output "Backup directory: $BackupPath"
if ($DryRun) {
    Write-ColorOutput Yellow "[DRY-RUN MODE] No files will be deleted"
}
Write-Output ""

# 1. 创建备份目录
if (-not $DryRun) {
    Write-ColorOutput Cyan "`n[Step 1] Creating backup directory..."
    try {
        if (-not (Test-Path $BackupPath)) {
            New-Item -ItemType Directory -Path $BackupPath -Force | Out-Null
        }
        Write-ColorOutput Green "[OK] Backup directory created: $BackupPath"
    } catch {
        Write-ColorOutput Red "[ERROR] Failed to create backup directory: $($_.Exception.Message)"
        exit 1
    }
}

# 2. 清理项目根目录的不必要文件
Write-ColorOutput Cyan "`n[Step 2] Cleaning root directory unnecessary files..."

$rootFilesToClean = @(
    @{ Path = "nul"; Description = "Windows reserved name file" },
    @{ Path = "apache-maven-3.9.11"; Description = "Maven directory (should not be in project root)" }
)

foreach ($item in $rootFilesToClean) {
    $fullPath = Join-Path $ProjectRoot $item.Path
    if (Test-Path $fullPath) {
        Write-Output "  Processing: $($item.Description)"
        if (-not $DryRun) {
            Backup-File -SourcePath $fullPath -RelativePath $item.Path | Out-Null
        }
        Remove-FileSafe -Path $fullPath -Description $item.Description
    }
}

# 3. 清理日志文件
Write-ColorOutput Cyan "`n[Step 3] Cleaning log files..."

$logPatterns = @(
    @{ Pattern = "*.log"; Description = "Log files" },
    @{ Pattern = "hs_err_pid*.log"; Description = "JVM crash logs" },
    @{ Pattern = "replay_pid*.log"; Description = "JVM replay logs" }
)

$logFilesCleaned = 0
foreach ($pattern in $logPatterns) {
    $logFiles = Get-ChildItem -Path $ProjectRoot -Filter $pattern.Pattern -Recurse -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -notlike "*node_modules*" -and $_.FullName -notlike "*target*" }

    foreach ($logFile in $logFiles) {
        $relativePath = $logFile.FullName.Substring($ProjectRoot.Length + 1)
        Write-Output "  Found: $relativePath"

        if (-not $DryRun) {
            Backup-File -SourcePath $logFile.FullName -RelativePath $relativePath | Out-Null
        }

        if (Remove-FileSafe -Path $logFile.FullName -Description "$($pattern.Description): $relativePath") {
            $logFilesCleaned++
        }
    }
}

Write-Output "  Total log files cleaned: $logFilesCleaned"

# 4. 清理logs目录中的旧日志（保留最近7天）
Write-ColorOutput Cyan "`n[Step 4] Cleaning old logs in logs directory..."

$logsDir = Join-Path $ProjectRoot "logs"
if (Test-Path $logsDir) {
    $cutoffDate = (Get-Date).AddDays(-7)
    $oldLogs = Get-ChildItem -Path $logsDir -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { $_.LastWriteTime -lt $cutoffDate }

    $oldLogsCount = 0
    foreach ($logFile in $oldLogs) {
        $relativePath = $logFile.FullName.Substring($ProjectRoot.Length + 1)
        Write-Output "  Found old log: $relativePath (Last modified: $($logFile.LastWriteTime))"

        if (-not $DryRun) {
            Backup-File -SourcePath $logFile.FullName -RelativePath $relativePath | Out-Null
        }

        if (Remove-FileSafe -Path $logFile.FullName -Description "Old log file: $relativePath") {
            $oldLogsCount++
        }
    }

    Write-Output "  Old log files cleaned: $oldLogsCount"
} else {
    Write-Output "  Logs directory not found, skipping..."
}

# 5. 清理微服务目录中的临时文件
Write-ColorOutput Cyan "`n[Step 5] Cleaning temporary files in microservices..."

$microservicesDir = Join-Path $ProjectRoot "microservices"
if (Test-Path $microservicesDir) {
    $tempPatterns = @(
        "*.log",
        "hs_err_pid*.log",
        "replay_pid*.log"
    )

    $tempFilesCleaned = 0
    foreach ($pattern in $tempPatterns) {
        $tempFiles = Get-ChildItem -Path $microservicesDir -Filter $pattern -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -notlike "*target*" -and $_.FullName -notlike "*src*" }

        foreach ($tempFile in $tempFiles) {
            $relativePath = $tempFile.FullName.Substring($ProjectRoot.Length + 1)
            Write-Output "  Found: $relativePath"

            if (-not $DryRun) {
                Backup-File -SourcePath $tempFile.FullName -RelativePath $relativePath | Out-Null
            }

            if (Remove-FileSafe -Path $tempFile.FullName -Description "Temporary file: $relativePath") {
                $tempFilesCleaned++
            }
        }
    }

    Write-Output "  Temporary files cleaned: $tempFilesCleaned"
} else {
    Write-Output "  Microservices directory not found, skipping..."
}

# 6. 清理空目录
Write-ColorOutput Cyan "`n[Step 6] Cleaning empty directories..."

$emptyDirsCleaned = 0
if (-not $DryRun) {
    $emptyDirs = Get-ChildItem -Path $ProjectRoot -Directory -Recurse -ErrorAction SilentlyContinue |
        Where-Object {
            $_.FullName -notlike "*node_modules*" -and
            $_.FullName -notlike "*target*" -and
            $_.FullName -notlike "*\.git*" -and
            (Get-ChildItem -Path $_.FullName -Recurse -ErrorAction SilentlyContinue | Measure-Object).Count -eq 0
        }

    foreach ($emptyDir in $emptyDirs) {
        $relativePath = $emptyDir.FullName.Substring($ProjectRoot.Length + 1)
        Write-Output "  Found empty directory: $relativePath"

        try {
            Remove-Item -Path $emptyDir.FullName -Force -ErrorAction Stop
            Write-ColorOutput Green "[OK] Removed empty directory: $relativePath"
            $emptyDirsCleaned++
        } catch {
            Write-ColorOutput Yellow "[WARN] Failed to remove empty directory: $relativePath - $($_.Exception.Message)"
        }
    }

    Write-Output "  Empty directories cleaned: $emptyDirsCleaned"
} else {
    Write-ColorOutput Yellow "[DRY-RUN] Would clean empty directories"
}

# 7. 生成清理报告
Write-ColorOutput Cyan "`n[Step 7] Generating cleanup report..."

$reportPath = Join-Path $BackupPath "CLEANUP_REPORT.md"
$reportContent = @"
# IOE-DREAM 项目清理报告

**清理时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**项目根目录**: $ProjectRoot
**备份目录**: $BackupPath
**清理模式**: $(if ($DryRun) { "DRY-RUN (模拟)" } else { "实际清理" })

## 清理摘要

- **日志文件清理**: $logFilesCleaned 个文件
- **旧日志清理**: $oldLogsCount 个文件（7天前）
- **临时文件清理**: $tempFilesCleaned 个文件
- **空目录清理**: $emptyDirsCleaned 个目录

## 清理的文件类型

1. Windows保留名称文件（nul）
2. Maven目录（apache-maven-3.9.11）
3. 日志文件（*.log）
4. JVM崩溃日志（hs_err_pid*.log）
5. JVM重放日志（replay_pid*.log）
6. 7天前的旧日志文件
7. 空目录

## 备份说明

所有被清理的文件都已备份到: $BackupPath

如需恢复，请从备份目录复制文件回原位置。

## 注意事项

- 备份目录包含所有被清理的文件
- 建议保留备份至少7天
- 如需恢复文件，请从备份目录复制
"@

if (-not $DryRun) {
    try {
        $reportContent | Out-File -FilePath $reportPath -Encoding UTF8
        Write-ColorOutput Green "[OK] Cleanup report generated: $reportPath"
    } catch {
        Write-ColorOutput Yellow "[WARN] Failed to generate report: $($_.Exception.Message)"
    }
}

# 8. 显示清理结果
Write-ColorOutput Cyan "`n========================================"
Write-ColorOutput Cyan "  Cleanup Summary"
Write-ColorOutput Cyan "========================================"
Write-Output ""
Write-Output "Log files cleaned: $logFilesCleaned"
Write-Output "Old logs cleaned: $oldLogsCount"
Write-Output "Temporary files cleaned: $tempFilesCleaned"
Write-Output "Empty directories cleaned: $emptyDirsCleaned"
Write-Output ""
if (-not $DryRun) {
    Write-ColorOutput Green "[SUCCESS] Cleanup completed!"
    Write-Output "Backup location: $BackupPath"
    Write-Output "Report location: $reportPath"
} else {
    Write-ColorOutput Yellow "[DRY-RUN] No files were actually deleted"
}
Write-Output ""
Write-Output "End time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Output ""

