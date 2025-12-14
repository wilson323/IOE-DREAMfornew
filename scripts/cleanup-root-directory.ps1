# =====================================================
# IOE-DREAM 根目录清理脚本
# 版本: v1.0.0
# 描述: 清理根目录下的冗余文档和临时文件，整合到对应目录
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$DryRun = $false,

    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport = $true
)

$ErrorActionPreference = "Stop"

# 项目根目录
$RootPath = Get-Location
$ArchivePath = "$RootPath\documentation\archive\root-reports"
$ScriptsPath = "$RootPath\scripts"

# 确保归档目录存在
if (-not (Test-Path $ArchivePath)) {
    Write-Host "[INFO] 创建归档目录: $ArchivePath" -ForegroundColor Cyan
    if (-not $DryRun) {
        New-Item -ItemType Directory -Path $ArchivePath -Force | Out-Null
    }
}

# 统计信息
$stats = @{
    ReportsMoved = 0
    ScriptsMoved = 0
    TempFilesDeleted = 0
    Skipped = 0
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 根目录清理脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($DryRun) {
    Write-Host "[WARN] 干运行模式 - 不会实际移动或删除文件" -ForegroundColor Yellow
    Write-Host ""
}

# 定义需要归档的报告文件模式
$ReportPatterns = @(
    "*_REPORT.md",
    "*_GUIDE.md",
    "*_FIX*.md",
    "*_ANALYSIS*.md",
    "*_SUMMARY*.md",
    "*_COMPLETE*.md",
    "*_FINAL*.md",
    "*_EXECUTION*.md",
    "*_STATUS*.md",
    "*_VERIFICATION*.md",
    "*_OPTIMIZATION*.md",
    "*_IMPLEMENTATION*.md",
    "*_PROGRESS*.md",
    "*_TECHNICAL_DEBT*.md",
    "*_CLEANUP*.md",
    "*_CONSOLIDATION*.md"
)

# 定义需要移动到scripts目录的脚本文件
$ScriptFiles = @(
    "build-local-ps1.ps1",
    "start.ps1",
    "fix-vue-encoding*.py"
)

# 定义需要删除的临时文件
$TempFilePatterns = @(
    "*.txt",
    "*.log",
    "*.html",
    "*.css",
    "*.js",
    "nul",
    "error.txt",
    "common-service-logs.txt",
    "views_tree.txt",
    "MANUAL_COMMANDS.txt"
)

# 定义需要保留的核心文件
$KeepFiles = @(
    "README.md",
    "CLAUDE.md",
    ".cursorrules",
    ".gitignore",
    "docker-compose*.yml",
    "Dockerfile*",
    "pom.xml"
)

Write-Host "[1] 处理报告文件..." -ForegroundColor Yellow

Get-ChildItem -Path $RootPath -Filter "*.md" -File | Where-Object {
    $fileName = $_.Name
    $shouldArchive = $false

    # 检查是否匹配报告模式
    foreach ($pattern in $ReportPatterns) {
        if ($fileName -like $pattern) {
            $shouldArchive = $true
            break
        }
    }

    # 排除保留文件
    if ($fileName -in $KeepFiles) {
        $shouldArchive = $false
    }

    $shouldArchive
} | ForEach-Object {
    $file = $_
    $targetPath = Join-Path $ArchivePath $file.Name

    Write-Host "  [归档] $($file.Name) -> $ArchivePath" -ForegroundColor Green

    if (-not $DryRun) {
        Move-Item -Path $file.FullName -Destination $targetPath -Force
        $stats.ReportsMoved++
    } else {
        $stats.ReportsMoved++
    }
}

Write-Host ""
Write-Host "[2] 处理脚本文件..." -ForegroundColor Yellow

Get-ChildItem -Path $RootPath -Filter "*.ps1" -File | Where-Object {
    $_.Name -notlike "cleanup-*.ps1"  # 排除清理脚本本身
} | ForEach-Object {
    $file = $_
    $targetPath = Join-Path $ScriptsPath $file.Name

    Write-Host "  [移动] $($file.Name) -> $ScriptsPath" -ForegroundColor Green

    if (-not $DryRun) {
        if (Test-Path $targetPath) {
            Write-Host "    [警告] 目标文件已存在，跳过: $targetPath" -ForegroundColor Yellow
            $stats.Skipped++
        } else {
            Move-Item -Path $file.FullName -Destination $targetPath -Force
            $stats.ScriptsMoved++
        }
    } else {
        $stats.ScriptsMoved++
    }
}

Get-ChildItem -Path $RootPath -Filter "*.py" -File | ForEach-Object {
    $file = $_
    $targetPath = Join-Path $ScriptsPath $file.Name

    Write-Host "  [移动] $($file.Name) -> $ScriptsPath" -ForegroundColor Green

    if (-not $DryRun) {
        if (Test-Path $targetPath) {
            Write-Host "    [警告] 目标文件已存在，跳过: $targetPath" -ForegroundColor Yellow
            $stats.Skipped++
        } else {
            Move-Item -Path $file.FullName -Destination $targetPath -Force
            $stats.ScriptsMoved++
        }
    } else {
        $stats.ScriptsMoved++
    }
}

Write-Host ""
Write-Host "[3] 处理临时文件..." -ForegroundColor Yellow

Get-ChildItem -Path $RootPath -File | Where-Object {
    $ext = $_.Extension.ToLower()
    $name = $_.Name

    ($ext -in @('.txt', '.log', '.html', '.css', '.js')) -or
    ($name -in @('nul', 'error.txt', 'common-service-logs.txt', 'views_tree.txt', 'MANUAL_COMMANDS.txt'))
} | ForEach-Object {
    $file = $_

    Write-Host "  [删除] $($file.Name)" -ForegroundColor Red

    if (-not $DryRun) {
        Remove-Item -Path $file.FullName -Force
        $stats.TempFilesDeleted++
    } else {
        $stats.TempFilesDeleted++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "清理统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "报告文件归档: $($stats.ReportsMoved) 个" -ForegroundColor Green
Write-Host "脚本文件移动: $($stats.ScriptsMoved) 个" -ForegroundColor Green
Write-Host "临时文件删除: $($stats.TempFilesDeleted) 个" -ForegroundColor Green
Write-Host "跳过文件: $($stats.Skipped) 个" -ForegroundColor Yellow
Write-Host ""

if ($GenerateReport -and -not $DryRun) {
    $reportPath = "$RootPath\documentation\project\ROOT_DIRECTORY_CLEANUP_REPORT.md"
    $reportContent = @"
# 根目录清理报告

> **清理时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
> **执行模式**: $(if ($DryRun) { "干运行" } else { "实际执行" })

## 清理统计

| 类型 | 数量 |
|------|------|
| 报告文件归档 | $($stats.ReportsMoved) |
| 脚本文件移动 | $($stats.ScriptsMoved) |
| 临时文件删除 | $($stats.TempFilesDeleted) |
| 跳过文件 | $($stats.Skipped) |

## 归档位置

- 报告文件: `documentation/archive/root-reports/`
- 脚本文件: `scripts/`

## 清理规则

### 报告文件模式
- `*_REPORT.md`
- `*_GUIDE.md`
- `*_FIX*.md`
- `*_ANALYSIS*.md`
- `*_SUMMARY*.md`
- `*_COMPLETE*.md`
- `*_FINAL*.md`
- 等等...

### 保留的核心文件
- `README.md`
- `CLAUDE.md`
- `.cursorrules`
- `.gitignore`
- `docker-compose*.yml`
- `Dockerfile*`
- `pom.xml`

"@

    $reportContent | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host "[INFO] 清理报告已生成: $reportPath" -ForegroundColor Cyan
}

Write-Host "[OK] 清理完成！" -ForegroundColor Green

