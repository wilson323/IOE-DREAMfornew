# ============================================================
# IOE-DREAM 架构合规性统一检查入口脚本
#
# 功能：整合所有架构合规性检查，生成统一报告
# 输出：汇总报告 + 各专项报告
# ============================================================

param(
    [switch]$Detailed = $false,
    [string]$OutputFormat = "json",
    [string]$OutputDir = "reports",
    [switch]$CI = $false,
    [string[]]$Checks = @("repository", "autowired", "jakarta", "hikaricp", "manager")  # 可指定检查项
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$scriptsDir = Join-Path $projectRoot "scripts"
$reportsDir = Join-Path $projectRoot $OutputDir

if (-not (Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir -Force | Out-Null
}

$allViolations = @{}
$totalViolations = 0
$exitCode = 0

if (-not $CI) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "IOE-DREAM 架构合规性统一检查" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

# 执行各项检查
$checkScripts = @{
    "repository" = "check-repository-violations.ps1"
    "autowired"  = "check-autowired-violations.ps1"
    "jakarta"    = "check-jakarta-violations.ps1"
}

foreach ($check in $Checks) {
    if ($checkScripts.ContainsKey($check)) {
        $scriptPath = Join-Path $scriptsDir $checkScripts[$check]

        if (-not (Test-Path $scriptPath)) {
            Write-Warning "检查脚本不存在: $scriptPath"
            continue
        }

        if (-not $CI) {
            Write-Host "[执行] $check 检查..." -ForegroundColor Yellow
        }

        try {
            $scriptParams = @{
                Detailed     = $Detailed
                OutputFormat = "json"
                OutputDir    = $OutputDir
                CI           = $CI
            }

            & $scriptPath @scriptParams
            $scriptExitCode = $LASTEXITCODE

            # 读取生成的报告
            $latestReport = Get-ChildItem -Path $reportsDir -Filter "$check-*-violations_*.json" |
            Sort-Object LastWriteTime -Descending | Select-Object -First 1

            if ($latestReport) {
                $reportData = Get-Content $latestReport.FullName | ConvertFrom-Json
                $allViolations[$check] = @{
                    Total      = $reportData.TotalViolations
                    Violations = $reportData.Violations
                    ReportFile = $latestReport.Name
                }
                $totalViolations += $reportData.TotalViolations
            }

            if ($scriptExitCode -ne 0) {
                $exitCode = 1
            }

        }
        catch {
            Write-Error "执行 $check 检查失败: $_"
            $exitCode = 1
        }
    }
    elseif ($check -eq "hikaricp") {
        # HikariCP检查（使用现有脚本的逻辑）
        if (-not $CI) {
            Write-Host "[执行] HikariCP配置检查..." -ForegroundColor Yellow
        }

        $hikariViolations = Get-ChildItem -Path (Join-Path $projectRoot "microservices") -Recurse -Filter "application*.yml" |
        Select-String -Pattern 'hikari:|HikariDataSource|type:\s*.*hikari' |
        Where-Object {
            $_.Line -notmatch '禁止|禁止使用|#.*hikari|LOG_LEVEL_HIKARI'
        }

        $allViolations["hikaricp"] = @{
            Total      = $hikariViolations.Count
            Violations = $hikariViolations | ForEach-Object {
                @{
                    File           = $_.Path.Replace($projectRoot, "").TrimStart('\', '/')
                    LineNumber     = $_.LineNumber
                    Line           = $_.Line.Trim()
                    Recommendation = "将 HikariCP 配置改为 Druid 连接池"
                }
            }
        }
        $totalViolations += $hikariViolations.Count

        if ($hikariViolations.Count -gt 0) {
            $exitCode = 1
        }
    }
    elseif ($check -eq "manager") {
        # Manager类Spring注解检查
        if (-not $CI) {
            Write-Host "[执行] Manager类注解检查..." -ForegroundColor Yellow
        }

        $managerViolations = @()
        $javaFiles = Get-ChildItem -Path (Join-Path $projectRoot "microservices") -Recurse -Filter "*Manager*.java" |
        Where-Object {
            $_.FullName -notlike "*\target\*" -and
            $_.FullName -match "common"  # Manager类应在common模块中
        }

        foreach ($file in $javaFiles) {
            $content = Get-Content $file.FullName -Raw
            $lines = Get-Content $file.FullName

            # 检查是否有Spring注解（@Component, @Service等）
            if ($content -match '@(Component|Service|Repository|Controller|Autowired|Resource)') {
                $lineNumber = 0
                foreach ($line in $lines) {
                    $lineNumber++
                    if ($line -match '@(Component|Service|Repository|Controller|Autowired)' -and
                        $line -notmatch '禁止|禁止使用|//.*|/\*.*') {
                        $managerViolations += @{
                            File           = $file.FullName.Replace($projectRoot, "").TrimStart('\', '/')
                            LineNumber     = $lineNumber
                            Line           = $line.Trim()
                            Recommendation = "Manager类不应使用Spring注解，应使用构造函数注入"
                        }
                    }
                }
            }
        }

        $allViolations["manager"] = @{
            Total      = $managerViolations.Count
            Violations = $managerViolations
        }
        $totalViolations += $managerViolations.Count

        if ($managerViolations.Count -gt 0) {
            $exitCode = 1
        }
    }
}

# 生成汇总报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$summaryReport = @{
    CheckType       = "ArchitectureComplianceSummary"
    Timestamp       = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    TotalViolations = $totalViolations
    CheckResults    = $allViolations
    Summary         = @{
        RepositoryViolations = if ($allViolations.ContainsKey("repository")) { $allViolations["repository"].Total } else { 0 }
        AutowiredViolations  = if ($allViolations.ContainsKey("autowired")) { $allViolations["autowired"].Total } else { 0 }
        JakartaViolations    = if ($allViolations.ContainsKey("jakarta")) { $allViolations["jakarta"].Total } else { 0 }
        HikariCPViolations   = if ($allViolations.ContainsKey("hikaricp")) { $allViolations["hikaricp"].Total } else { 0 }
        ManagerViolations    = if ($allViolations.ContainsKey("manager")) { $allViolations["manager"].Total } else { 0 }
    }
}

# 生成汇总报告文件
if ($OutputFormat -eq "json" -or $OutputFormat -eq "both") {
    $summaryPath = Join-Path $reportsDir "architecture-compliance-summary_$timestamp.json"
    $summaryReport | ConvertTo-Json -Depth 10 | Out-File -FilePath $summaryPath -Encoding UTF8
}

# 生成Markdown摘要报告
$mdReport = @"
# IOE-DREAM 架构合规性检查报告

**生成时间**: $($summaryReport.Timestamp)
**总违规数**: $totalViolations

## 检查结果摘要

| 检查项 | 违规数 | 状态 |
|--------|--------|------|
| @Repository违规 | $($summaryReport.Summary.RepositoryViolations) | $(if ($summaryReport.Summary.RepositoryViolations -eq 0) { "✅ 通过" } else { "❌ 失败" }) |
| @Autowired违规 | $($summaryReport.Summary.AutowiredViolations) | $(if ($summaryReport.Summary.AutowiredViolations -eq 0) { "✅ 通过" } else { "❌ 失败" }) |
| Jakarta EE迁移 | $($summaryReport.Summary.JakartaViolations) | $(if ($summaryReport.Summary.JakartaViolations -eq 0) { "✅ 通过" } else { "❌ 失败" }) |
| HikariCP配置 | $($summaryReport.Summary.HikariCPViolations) | $(if ($summaryReport.Summary.HikariCPViolations -eq 0) { "✅ 通过" } else { "❌ 失败" }) |
| Manager类注解 | $($summaryReport.Summary.ManagerViolations) | $(if ($summaryReport.Summary.ManagerViolations -eq 0) { "✅ 通过" } else { "❌ 失败" }) |

## 详细报告

"@

foreach ($check in $allViolations.Keys) {
    $data = $allViolations[$check]
    $mdReport += "`n### $check 检查详情`n`n"
    $mdReport += "**违规数**: $($data.Total)`n`n"

    if ($data.Total -gt 0) {
        $mdReport += "| 文件 | 行号 | 代码 | 建议 |`n"
        $mdReport += "|------|------|------|------|`n"

        foreach ($violation in $data.Violations) {
            $file = $violation.File -replace '\|', '\|'
            $line = ($violation.Line -replace '\|', '\|').Substring(0, [Math]::Min(50, $violation.Line.Length))
            $rec = $violation.Recommendation -replace '\|', '\|'
            $mdReport += "| $file | $($violation.LineNumber) | ``$line`` | $rec |`n"
        }
    }
    else {
        $mdReport += "✅ **无违规**`n"
    }
}

$mdReport += "`n---`n`n"
$mdReport += "**报告文件**: `architecture-compliance-summary_$timestamp.json`"

$mdPath = Join-Path $reportsDir "architecture-compliance-summary_$timestamp.md"
$mdReport | Out-File -FilePath $mdPath -Encoding UTF8

# 输出摘要
if (-not $CI) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "检查完成" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "总违规数: $totalViolations" -ForegroundColor $(if ($totalViolations -eq 0) { "Green" } else { "Red" })
    Write-Host ""
    Write-Host "详细报告:" -ForegroundColor Cyan
    Write-Host "  JSON: architecture-compliance-summary_$timestamp.json" -ForegroundColor Gray
    Write-Host "  Markdown: architecture-compliance-summary_$timestamp.md" -ForegroundColor Gray
    Write-Host ""
}

exit $exitCode

