# =====================================================
# 技术栈一致性检查脚本（仅检查，不修改）
# 版本: v1.0.0
# 描述: 检查文档中的技术栈描述是否与标准规范一致
# 创建时间: 2025-01-30
# 注意: 本脚本仅检查，不修改任何文件
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$DocumentationPath = "documentation",

    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport = $true
)

$ErrorActionPreference = "Stop"

# 技术栈标准版本（从pom.xml确认）
$techStackStandard = @{
    "Spring Boot" = "3.5.8"
    "Java" = "17"
    "Spring Cloud" = "2025.0.0"
    "Spring Cloud Alibaba" = "2025.0.0.0"
    "MyBatis-Plus" = "3.5.15"
    "MySQL" = "8.0.35"
    "Druid" = "1.2.25"
    "Lombok" = "1.18.42"
}

# 错误版本模式（需要检查的）
$errorPatterns = @{
    "Spring Boot" = @(
        "3\.2\.",
        "3\.3\.",
        "3\.4\.",
        "3\.5\.[0-4]"
    )
    "Spring Cloud" = @(
        "2023\.",
        "2022\."
    )
    "Spring Cloud Alibaba" = @(
        "2022\.",
        "2023\."
    )
    "MyBatis-Plus" = @(
        "3\.5\.[0-9]"
    )
    "MySQL" = @(
        "8\.0\.[0-3][0-4]"
    )
    "Druid" = @(
        "1\.2\.[0-2][0-4]"
    )
    "Lombok" = @(
        "1\.18\.[0-3][0-9]"
    )
}

# 统计信息
$stats = @{
    TotalFiles = 0
    FilesWithIssues = 0
    TotalIssues = 0
    FilesWithoutReference = 0
}

# 检查报告
$report = @{
    Issues = @()
    FilesWithoutReference = @()
    Errors = @()
}

function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$ForegroundColor = "White"
    )
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    Write-Output $Message
    $host.UI.RawUI.ForegroundColor = $fc
}

function Check-TechnologyStackInFile {
    param(
        [string]$FilePath
    )

    try {
        $content = Get-Content $FilePath -Raw -Encoding UTF8
        $fileIssues = @()
        $hasIssues = $false

        # 检查是否引用技术栈标准
        $hasReference = $content -match "TECHNOLOGY_STACK_STANDARD"

        # 检查是否包含技术栈描述
        $hasTechStack = $content -match "Spring Boot|技术栈|技术选型"

        # 如果包含技术栈描述但未引用标准，记录问题
        if ($hasTechStack -and -not $hasReference) {
            $relativePath = $FilePath.Replace((Get-Location).Path + "\", "")
            if ($relativePath -notmatch "TECHNOLOGY_STACK_STANDARD|archive|bak|\.git") {
                $report.FilesWithoutReference += @{
                    File = $relativePath
                    Reason = "包含技术栈描述但未引用标准规范"
                }
                $stats.FilesWithoutReference++
            }
        }

        # 检查错误版本模式
        foreach ($tech in $errorPatterns.Keys) {
            $standardVersion = $techStackStandard[$tech]
            foreach ($pattern in $errorPatterns[$tech]) {
                $regex = [regex]::new("$tech\s+$pattern")
                if ($content -match $regex) {
                    $matches = $regex.Matches($content)
                    foreach ($match in $matches) {
                        $fileIssues += @{
                            Technology = $tech
                            FoundVersion = $match.Value
                            StandardVersion = "$tech $standardVersion"
                            Pattern = $pattern
                            Line = $match.Value
                        }
                        $hasIssues = $true
                        $stats.TotalIssues++
                    }
                }
            }
        }

        if ($hasIssues) {
            $relativePath = $FilePath.Replace((Get-Location).Path + "\", "")
            $report.Issues += @{
                File = $relativePath
                Issues = $fileIssues
            }
            $stats.FilesWithIssues++
            return $true
        }

        return $false
    } catch {
        $errorMsg = "Error checking file: $FilePath - $($_.Exception.Message)"
        Write-ColorOutput "[ERROR] $errorMsg" -ForegroundColor Red
        $report.Errors += $errorMsg
        return $false
    }
}

# 主执行流程
Write-ColorOutput "================================================" -ForegroundColor Cyan
Write-ColorOutput "技术栈一致性检查脚本（仅检查，不修改）" -ForegroundColor Cyan
Write-ColorOutput "================================================" -ForegroundColor Cyan
Write-Output ""

# 检查文档目录
if (-not (Test-Path $DocumentationPath)) {
    Write-ColorOutput "[ERROR] 文档目录不存在: $DocumentationPath" -ForegroundColor Red
    exit 1
}

Write-ColorOutput "[INFO] 开始检查文档目录: $DocumentationPath" -ForegroundColor Cyan
Write-Output ""

# 获取所有Markdown文件
$mdFiles = Get-ChildItem -Path $DocumentationPath -Recurse -Filter "*.md" | Where-Object {
    $_.FullName -notmatch "\\node_modules\\" -and
    $_.FullName -notmatch "\\target\\" -and
    $_.FullName -notmatch "\\\.git\\" -and
    $_.FullName -notmatch "\\archive\\" -and
    $_.FullName -notmatch "\\bak\\"
}

$stats.TotalFiles = $mdFiles.Count
Write-ColorOutput "[INFO] 找到 $($stats.TotalFiles) 个Markdown文件" -ForegroundColor Cyan
Write-Output ""

# 处理每个文件
$processed = 0
foreach ($file in $mdFiles) {
    $processed++
    $progress = [math]::Round(($processed / $stats.TotalFiles) * 100, 2)
    Write-Progress -Activity "检查文档" -Status "$processed / $($stats.TotalFiles) ($progress%)" -PercentComplete $progress

    Check-TechnologyStackInFile -FilePath $file.FullName | Out-Null
}

Write-Progress -Activity "检查文档" -Completed

Write-Output ""
Write-ColorOutput "================================================" -ForegroundColor Cyan
Write-ColorOutput "检查完成统计" -ForegroundColor Cyan
Write-ColorOutput "================================================" -ForegroundColor Cyan
Write-Output ""
Write-ColorOutput "总文件数: $($stats.TotalFiles)" -ForegroundColor White
Write-ColorOutput "发现问题文件: $($stats.FilesWithIssues)" -ForegroundColor $(if ($stats.FilesWithIssues -gt 0) { "Yellow" } else { "Green" })
Write-ColorOutput "总问题数: $($stats.TotalIssues)" -ForegroundColor $(if ($stats.TotalIssues -gt 0) { "Yellow" } else { "Green" })
Write-ColorOutput "未引用标准文件数: $($stats.FilesWithoutReference)" -ForegroundColor $(if ($stats.FilesWithoutReference -gt 0) { "Yellow" } else { "Green" })
Write-Output ""

# 生成报告
if ($GenerateReport) {
    $reportPath = Join-Path $DocumentationPath "technical\TECHNOLOGY_STACK_CONSISTENCY_CHECK_REPORT_$(Get-Date -Format 'yyyy-MM-dd_HHmmss').md"

    $reportContent = @"
# 技术栈一致性检查报告

**生成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
**脚本版本**: v1.0.0
**检查模式**: 仅检查，不修改

## 统计信息

| 指标 | 数量 |
|------|------|
| 总文件数 | $($stats.TotalFiles) |
| 发现问题文件 | $($stats.FilesWithIssues) |
| 总问题数 | $($stats.TotalIssues) |
| 未引用标准文件数 | $($stats.FilesWithoutReference) |

## 技术栈标准版本

参考: [技术栈标准规范](./TECHNOLOGY_STACK_STANDARD.md)

| 技术组件 | 标准版本 |
|---------|---------|
| Spring Boot | **3.5.8** |
| Spring Cloud | **2025.0.0** |
| Spring Cloud Alibaba | **2025.0.0.0** |
| Java | **17** |
| MyBatis-Plus | **3.5.15** |
| MySQL | **8.0.35** |
| Druid | **1.2.25** |
| Lombok | **1.18.42** |

## 发现的问题

"@

    if ($report.Issues.Count -gt 0) {
        $reportContent += "`n### 技术栈版本不一致的文件`n`n"
        foreach ($issue in $report.Issues) {
            $reportContent += "`n#### $($issue.File)`n`n"
            foreach ($detail in $issue.Issues) {
                $reportContent += "- **$($detail.Technology)**: 发现 `$($detail.FoundVersion)`，标准为 `$($detail.StandardVersion)``n"
            }
        }
    } else {
        $reportContent += "`n✅ 未发现技术栈版本不一致的问题。`n"
    }

    if ($report.FilesWithoutReference.Count -gt 0) {
        $reportContent += "`n### 未引用技术栈标准的文件`n`n"
        foreach ($file in $report.FilesWithoutReference) {
            $reportContent += "- **$($file.File)**: $($file.Reason)`n"
        }
    } else {
        $reportContent += "`n✅ 所有包含技术栈描述的文件都已引用标准规范。`n"
    }

    if ($report.Errors.Count -gt 0) {
        $reportContent += "`n## 错误信息`n`n"
        foreach ($error in $report.Errors) {
            $reportContent += "- $error`n"
        }
    }

    [System.IO.File]::WriteAllText($reportPath, $reportContent, [System.Text.Encoding]::UTF8)
    Write-ColorOutput "[OK] 检查报告已生成: $reportPath" -ForegroundColor Green
}

Write-Output ""
Write-ColorOutput "[OK] 脚本执行完成！" -ForegroundColor Green
Write-ColorOutput "[INFO] 本脚本仅检查，不修改任何文件" -ForegroundColor Cyan
