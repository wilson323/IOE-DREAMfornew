# Clean Unused Files Script
# Created: 2025-11-19
# Purpose: Clean temporary files, log files and backup directories

param(
    [switch]$DryRun = $false,
    [switch]$Verbose = $true
)

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
$ReportFile = Join-Path $ProjectRoot "clean-unused-files-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
$FileListBefore = Join-Path $ProjectRoot "file-list-before-cleanup-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
$DeletedCount = 0
$TotalSize = 0
$Report = @()

function Write-Log {
    param([string]$Message, [string]$Type = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] [$Type] $Message"
    Write-Host $logMessage -ForegroundColor $(if($Type -eq "ERROR"){"Red"}elseif($Type -eq "SUCCESS"){"Green"}elseif($Type -eq "WARN"){"Yellow"}else{"White"})
    $script:Report += $logMessage
}

function Get-FileSize {
    param([string]$Path)
    if (Test-Path $Path -PathType Leaf) {
        return (Get-Item $Path).Length
    } elseif (Test-Path $Path -PathType Container) {
        return (Get-ChildItem $Path -Recurse -File | Measure-Object -Property Length -Sum).Sum
    }
    return 0
}

function Format-FileSize {
    param([long]$Bytes)
    if ($Bytes -gt 1MB) {
        return "{0:N2} MB" -f ($Bytes / 1MB)
    } elseif ($Bytes -gt 1KB) {
        return "{0:N2} KB" -f ($Bytes / 1KB)
    } else {
        return "$Bytes Bytes"
    }
}

function Remove-SafeItem {
    param(
        [string]$Path,
        [string]$Category
    )
    
    if (-not (Test-Path $Path)) {
        Write-Log "File not found: $Path" "WARN"
        return
    }
    
    $size = Get-FileSize -Path $Path
    $sizeStr = Format-FileSize -Bytes $size
    
    if ($DryRun) {
        Write-Log "[DRY RUN] [$Category] $Path ($sizeStr)" "INFO"
    } else {
        try {
            Remove-Item -Path $Path -Recurse -Force
            Write-Log "[DELETED] [$Category] $Path ($sizeStr)" "SUCCESS"
            $script:DeletedCount++
            $script:TotalSize += $size
        } catch {
            Write-Log "Delete failed: $Path - $($_.Exception.Message)" "ERROR"
        }
    }
}

Write-Log "========== Start Cleaning Unused Files ==========" "INFO"
Write-Log "Project Root: $ProjectRoot" "INFO"
Write-Log "Mode: $(if ($DryRun) { 'Dry Run (No Actual Deletion)' } else { 'Actual Deletion' })" "INFO"
Write-Log "" "INFO"

Write-Log "Generating file list before cleanup..." "INFO"
Get-ChildItem -Path $ProjectRoot -Recurse -ErrorAction SilentlyContinue | Select-Object FullName, Length | Out-File -FilePath $FileListBefore -Encoding UTF8
Write-Log "File list saved: $FileListBefore" "INFO"
Write-Log "" "INFO"

Write-Log "========== Category 1: Temporary Files ==========" "INFO"

$Category1Files = @(
    ".last-validation",
    ".todo-lock",
    "garbage_files_20251116_174253.txt",
    "CONSISTENCY_REPORT.md"
)

foreach ($file in $Category1Files) {
    $fullPath = Join-Path $ProjectRoot $file
    Remove-SafeItem -Path $fullPath -Category "临时文件"
}

Write-Log "" "INFO"

Write-Log "========== Category 2: Log Files ==========" "INFO"

$Category2Files = @(
    "compile-errors.log",
    "compile_errors.log",
    "compile_errors.txt",
    "compile_progress.txt",
    "hs_err_pid206592.log",
    "replay_pid206592.log",
    "systematic_encoding_solution_20251116_175304.log",
    "workflow_output.log",
    "spring_boot_startup_test.log",
    "local_startup_test.log",
    "permission_monitor_20251117.log"
)

foreach ($file in $Category2Files) {
    $fullPath = Join-Path $ProjectRoot $file
    Remove-SafeItem -Path $fullPath -Category "日志文件"
}

Write-Log "" "INFO"

Write-Log "========== Category 3: Backup Directories ==========" "INFO"

$ArchiveDir = Join-Path $ProjectRoot "archives\backups"
if (-not (Test-Path $ArchiveDir)) {
    New-Item -Path $ArchiveDir -ItemType Directory -Force | Out-Null
    Write-Log "Created archive directory: $ArchiveDir" "INFO"
}

$Category3Dirs = @(
    "encoding_backup_20251116_152403",
    "frontend_backup_20251117_213449",
    "disabled_encoding_issues",
    "fix_logs"
)

foreach ($dir in $Category3Dirs) {
    $fullPath = Join-Path $ProjectRoot $dir
    if (Test-Path $fullPath) {
        $archiveName = "$dir-$(Get-Date -Format 'yyyyMMdd').zip"
        $archivePath = Join-Path $ArchiveDir $archiveName
        
        if ($DryRun) {
            Write-Log "[DRY RUN ARCHIVE] $fullPath -> $archivePath" "INFO"
        } else {
            try {
                Compress-Archive -Path $fullPath -DestinationPath $archivePath -Force
                Write-Log "[ARCHIVED] $fullPath -> $archivePath" "SUCCESS"
                
                Remove-SafeItem -Path $fullPath -Category "Backup Directory"
            } catch {
                Write-Log "Archive failed: $fullPath - $($_.Exception.Message)" "ERROR"
            }
        }
    }
}

Write-Log "" "INFO"

Write-Log "========== Category 4: Business Files (Need Confirmation) ==========" "INFO"

$Category4Files = @(
    "all_permission_codes.txt",
    "backend_permissions.txt",
    "temp_backend_permissions.txt",
    "permission_alert_20251117_221144.md",
    "permission_fix_report_20251117_220924.md",
    "permission_trend_report_20251117.md",
    "cache-compliance-report.md"
)

Write-Log "Following files need manual confirmation:" "WARN"
foreach ($file in $Category4Files) {
    $fullPath = Join-Path $ProjectRoot $file
    if (Test-Path $fullPath) {
        $size = Get-FileSize -Path $fullPath
        $sizeStr = Format-FileSize -Bytes $size
        Write-Log "  - $file ($sizeStr)" "INFO"
    }
}

Write-Log "" "INFO"

Write-Log "========== Cleanup Completed ==========" "INFO"
Write-Log "Deleted files/directories: $DeletedCount" "INFO"
Write-Log "Disk space freed: $(Format-FileSize -Bytes $TotalSize)" "INFO"
Write-Log "" "INFO"

$Report | Out-File -FilePath $ReportFile -Encoding UTF8
Write-Log "Report saved: $ReportFile" "INFO"

Write-Host "`nCleanup task completed!" -ForegroundColor Green
Write-Host "Report file: $ReportFile" -ForegroundColor Cyan
Write-Host "File list before cleanup: $FileListBefore" -ForegroundColor Cyan

