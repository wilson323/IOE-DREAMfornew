# ============================================================
# IOE-DREAM 文档结构检查脚本
#
# 功能：检查文档是否在正确目录，验证文档命名规范和链接有效性
# ============================================================

param(
    [switch]$Detailed = $false,
    [string]$OutputDir = "reports",
    [switch]$CI = $false
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$reportsDir = Join-Path $projectRoot $OutputDir

if (-not (Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir -Force | Out-Null
}

$report = @{
    CheckType = "DocumentStructure"
    Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Issues = @()
    Statistics = @{
        TotalDocs = 0
        CorrectLocation = 0
        IncorrectLocation = 0
        InvalidLinks = 0
    }
}

if (-not $CI) {
    Write-Host "===== 文档结构检查 =====" -ForegroundColor Cyan
    Write-Host ""
}

# 定义正确的文档目录
$correctDocDirs = @(
    "documentation",
    "README.md",
    "CLAUDE.md",
    "openspec"
)

# 定义不应该在根目录的文档模式
$rootDocPatterns = @(
    "*-report*.md",
    "*-report*.txt",
    "*-report*.json",
    "NACOS_*.md",
    "*.log"
)

# 检查根目录文档
Write-Host "[1/3] 检查根目录文档..." -ForegroundColor Yellow

$rootDocs = Get-ChildItem -Path $projectRoot -File -Filter "*.md" | 
    Where-Object { $_.Name -notin @("README.md", "CLAUDE.md") }

foreach ($doc in $rootDocs) {
    $isIssue = $false
    $issue = @{
        File = $doc.Name
        Path = $doc.FullName.Replace($projectRoot, "").TrimStart('\', '/')
        IssueType = ""
        Recommendation = ""
    }
    
    # 检查是否匹配临时文档模式
    foreach ($pattern in $rootDocPatterns) {
        if ($doc.Name -like $pattern) {
            $issue.IssueType = "临时文档应在documentation目录"
            $issue.Recommendation = "移动到 documentation/project/archive/temp-files/ 或 documentation/project/temp/"
            $report.Issues += $issue
            $report.Statistics.IncorrectLocation++
            $isIssue = $true
            break
        }
    }
    
    if (-not $isIssue) {
        $report.Statistics.CorrectLocation++
    }
    
    $report.Statistics.TotalDocs++
}

Write-Host "  根目录文档: $($report.Statistics.TotalDocs) 个" -ForegroundColor Gray
Write-Host "  位置正确: $($report.Statistics.CorrectLocation)" -ForegroundColor Green
Write-Host "  位置错误: $($report.Statistics.IncorrectLocation)" -ForegroundColor $(if ($report.Statistics.IncorrectLocation -eq 0) { "Green" } else { "Red" })
Write-Host ""

# 检查documentation目录结构
Write-Host "[2/3] 检查documentation目录结构..." -ForegroundColor Yellow

$docDir = Join-Path $projectRoot "documentation"
if (Test-Path $docDir) {
    $docFiles = Get-ChildItem -Path $docDir -File -Recurse -Filter "*.md"
    $report.Statistics.TotalDocs += $docFiles.Count
    $report.Statistics.CorrectLocation += $docFiles.Count
    
    Write-Host "  documentation目录文档: $($docFiles.Count) 个" -ForegroundColor Green
} else {
    Write-Host "  documentation目录不存在" -ForegroundColor Yellow
}

Write-Host ""

# 检查文档链接（简化版）
Write-Host "[3/3] 检查文档链接有效性..." -ForegroundColor Yellow

$mdFiles = Get-ChildItem -Path $projectRoot -File -Recurse -Filter "*.md" | 
    Where-Object { $_.FullName -notlike "*\target\*" -and $_.FullName -notlike "*\node_modules\*" }

$invalidLinks = 0
foreach ($file in $mdFiles) {
    $content = Get-Content $file.FullName -Raw
    $links = [regex]::Matches($content, '\[([^\]]+)\]\(([^)]+)\)')
    
    foreach ($match in $links) {
        $linkPath = $match.Groups[2].Value
        
        # 跳过外部链接
        if ($linkPath -match '^https?://') {
            continue
        }
        
        # 处理相对路径
        $fileDir = Split-Path $file.FullName
        $absolutePath = Join-Path $fileDir $linkPath
        $normalizedPath = [System.IO.Path]::GetFullPath($absolutePath)
        
        # 检查锚点链接（包含#）
        if ($linkPath -match '#') {
            $anchorPath = $linkPath -replace '#.*', ''
            if ($anchorPath) {
                $absolutePath = Join-Path $fileDir $anchorPath
                $normalizedPath = [System.IO.Path]::GetFullPath($absolutePath)
            }
        }
        
        if (-not (Test-Path $normalizedPath) -and $linkPath -notmatch '^#') {
            $invalidLinks++
            if ($Detailed) {
                $report.Issues += @{
                    File = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
                    Link = $linkPath
                    IssueType = "无效链接"
                    Recommendation = "修复或删除该链接"
                }
            }
        }
    }
}

$report.Statistics.InvalidLinks = $invalidLinks

if ($invalidLinks -gt 0) {
    Write-Host "  发现无效链接: $invalidLinks 个" -ForegroundColor Red
} else {
    Write-Host "  链接检查通过" -ForegroundColor Green
}
Write-Host ""

# 生成报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$jsonPath = Join-Path $reportsDir "document-structure_$timestamp.json"
$report | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8

# Markdown报告
$mdReport = @"
# 文档结构检查报告

**生成时间**: $($report.Timestamp)

## 统计信息

| 指标 | 数量 |
|------|------|
| 总文档数 | $($report.Statistics.TotalDocs) |
| 位置正确 | $($report.Statistics.CorrectLocation) |
| 位置错误 | $($report.Statistics.IncorrectLocation) |
| 无效链接 | $($report.Statistics.InvalidLinks) |

## 发现的问题

$(if ($report.Issues.Count -gt 0) {
    "| 文件 | 问题类型 | 建议 |`n|------|----------|------|`n"
    foreach ($issue in $report.Issues) {
        "| $($issue.File) | $($issue.IssueType) | $($issue.Recommendation) |`n"
    }
} else {
    "✅ **无问题**`n"
})

---

**报告文件**: `document-structure_$timestamp.json`

"@

$mdPath = Join-Path $reportsDir "document-structure_$timestamp.md"
$mdReport | Out-File -FilePath $mdPath -Encoding UTF8

if (-not $CI) {
    Write-Host "===== 检查完成 =====" -ForegroundColor Cyan
    Write-Host "  问题数: $($report.Issues.Count)" -ForegroundColor $(if ($report.Issues.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
    Write-Host "详细报告:" -ForegroundColor Cyan
    Write-Host "  JSON: document-structure_$timestamp.json" -ForegroundColor Gray
    Write-Host "  Markdown: document-structure_$timestamp.md" -ForegroundColor Gray
    Write-Host ""
}

exit $(if ($report.Issues.Count -eq 0) { 0 } else { 1 })

