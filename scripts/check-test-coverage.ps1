# ============================================================
# IOE-DREAM 测试覆盖率检查脚本
#
# 功能：执行Maven测试并收集覆盖率，与目标覆盖率对比
# ============================================================

param(
    [switch]$Detailed = $false,
    [string]$OutputDir = "reports",
    [switch]$CI = $false,
    [int]$ServiceThreshold = 80,
    [int]$ManagerThreshold = 75,
    [int]$DaoThreshold = 70,
    [int]$ControllerThreshold = 60,
    [int]$UtilThreshold = 90
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"
$reportsDir = Join-Path $projectRoot $OutputDir

if (-not (Test-Path $reportsDir)) {
    New-Item -ItemType Directory -Path $reportsDir -Force | Out-Null
}

$exitCode = 0

if (-not $CI) {
    Write-Host "===== 测试覆盖率检查 =====" -ForegroundColor Cyan
    Write-Host ""
}

# 检查JaCoCo是否配置
$parentPomPath = Join-Path $microservicesDir "pom.xml"
if (-not (Test-Path $parentPomPath)) {
    Write-Error "父POM文件不存在: $parentPomPath"
    exit 1
}

$parentPom = [xml](Get-Content $parentPomPath -Encoding UTF8)
$hasJacoco = $parentPom.project.build.plugins.plugin | Where-Object { $_.artifactId -eq "jacoco-maven-plugin" }

if (-not $hasJacoco) {
    Write-Warning "JaCoCo插件未在父POM中配置，将添加配置建议"
}

# 执行Maven测试（如果JaCoCo已配置）
$coverageData = @{
    CheckType       = "TestCoverage"
    Timestamp       = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    OverallCoverage = 0
    ModuleCoverage  = @{}
    Thresholds      = @{
        Service    = $ServiceThreshold
        Manager    = $ManagerThreshold
        Dao        = $DaoThreshold
        Controller = $ControllerThreshold
        Util       = $UtilThreshold
    }
    Violations      = @()
}

if ($hasJacoco) {
    if (-not $CI) {
        Write-Host "[1/3] 执行Maven测试并收集覆盖率..." -ForegroundColor Yellow
    }

    try {
        # 在父目录执行Maven测试
        Push-Location $microservicesDir

        # 执行测试并生成覆盖率报告
        $mavenOutput = & mvn clean test jacoco:report 2>&1
        $mavenExitCode = $LASTEXITCODE

        if ($mavenExitCode -ne 0) {
            Write-Warning "Maven测试执行失败，跳过覆盖率检查"
            $coverageData.Error = "Maven测试执行失败"
        }
        else {
            if (-not $CI) {
                Write-Host "  Maven测试执行完成" -ForegroundColor Green
            }

            # 查找覆盖率报告
            $jacocoReports = Get-ChildItem -Path $microservicesDir -Filter "index.html" -Recurse |
            Where-Object { $_.FullName -like "*\target\site\jacoco\index.html" }

            if (-not $CI) {
                Write-Host "[2/3] 解析覆盖率报告..." -ForegroundColor Yellow
            }

            # 解析覆盖率数据（简化版，实际应解析XML报告）
            foreach ($report in $jacocoReports) {
                $modulePath = Split-Path (Split-Path (Split-Path $report.DirectoryName -Parent) -Parent) -Leaf

                # 尝试读取XML报告获取精确数据
                $xmlReportPath = Join-Path $report.DirectoryName "jacoco.xml"
                if (Test-Path $xmlReportPath) {
                    $xmlReport = [xml](Get-Content $xmlReportPath -Encoding UTF8)
                    $counter = $xmlReport.report.counter | Where-Object { $_.type -eq "INSTRUCTION" }
                    if ($counter) {
                        $missed = [double]$counter.missed
                        $covered = [double]$counter.covered
                        $total = $missed + $covered
                        $coveragePercent = if ($total -gt 0) { [Math]::Round(($covered / $total) * 100, 2) } else { 0 }

                        $coverageData.ModuleCoverage[$modulePath] = @{
                            Coverage = $coveragePercent
                            Covered  = $covered
                            Missed   = $missed
                            Total    = $total
                        }
                    }
                }
            }
        }

        Pop-Location
    }
    catch {
        Write-Error "执行测试覆盖率检查失败: $_"
        $coverageData.Error = $_.ToString()
        $exitCode = 1
    }
}
else {
    $coverageData.Error = "JaCoCo插件未配置"
    $coverageData.Recommendation = "请在父POM中配置JaCoCo插件"
}

# 计算总体覆盖率
if ($coverageData.ModuleCoverage.Count -gt 0) {
    $totalCovered = ($coverageData.ModuleCoverage.Values | Measure-Object -Property Covered -Sum).Sum
    $totalMissed = ($coverageData.ModuleCoverage.Values | Measure-Object -Property Missed -Sum).Sum
    $total = $totalCovered + $totalMissed
    $coverageData.OverallCoverage = if ($total -gt 0) { [Math]::Round(($totalCovered / $total) * 100, 2) } else { 0 }
}

# 检查覆盖率阈值（简化检查，实际应区分Service/Manager/DAO等）
$minThreshold = [Math]::Min($ServiceThreshold, [Math]::Min($ManagerThreshold, $DaoThreshold))
if ($coverageData.OverallCoverage -lt $minThreshold) {
    $coverageData.Violations += @{
        Type     = "OverallCoverage"
        Current  = $coverageData.OverallCoverage
        Required = $minThreshold
        Message  = "总体覆盖率 $($coverageData.OverallCoverage)% 低于最低要求 $minThreshold%"
    }
    $exitCode = 1
}

# 生成报告
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$jsonPath = Join-Path $reportsDir "test-coverage_$timestamp.json"
$coverageData | ConvertTo-Json -Depth 10 | Out-File -FilePath $jsonPath -Encoding UTF8

# 生成Markdown报告
$mdReport = @"
# IOE-DREAM 测试覆盖率报告

**生成时间**: $($coverageData.Timestamp)
**总体覆盖率**: $($coverageData.OverallCoverage)%

## 覆盖率阈值

| 层级 | 最低要求 |
|------|----------|
| Service层 | ≥$ServiceThreshold% |
| Manager层 | ≥$ManagerThreshold% |
| DAO层 | ≥$DaoThreshold% |
| Controller层 | ≥$ControllerThreshold% |
| 工具类 | ≥$UtilThreshold% |

## 模块覆盖率

$(if ($coverageData.ModuleCoverage.Count -gt 0) {
    "| 模块 | 覆盖率 | 覆盖/总数 | 状态 |`n|------|--------|-----------|------|`n"
    foreach ($module in $coverageData.ModuleCoverage.GetEnumerator() | Sort-Object Name) {
        $status = if ($module.Value.Coverage -ge $minThreshold) { "✅ 通过" } else { "❌ 未达标" }
        "| $($module.Key) | $($module.Value.Coverage)% | $([Math]::Round($module.Value.Covered))/$([Math]::Round($module.Value.Total)) | $status |`n"
    }
} else {
    "⚠️ **暂无覆盖率数据**`n`n"
    if ($coverageData.Error) {
        "错误: $($coverageData.Error)`n`n"
    }
    if ($coverageData.Recommendation) {
        "建议: $($coverageData.Recommendation)`n"
    }
})

## 违规项

$(if ($coverageData.Violations.Count -gt 0) {
    foreach ($violation in $coverageData.Violations) {
        "❌ **$($violation.Type)**: $($violation.Message)`n"
    }
} else {
    "✅ **无违规项**`n"
})

---

**报告文件**: `test-coverage_$timestamp.json`

"@

$mdPath = Join-Path $reportsDir "test-coverage_$timestamp.md"
$mdReport | Out-File -FilePath $mdPath -Encoding UTF8

if (-not $CI) {
    Write-Host ""
    Write-Host "===== 检查完成 =====" -ForegroundColor Cyan
    Write-Host "总体覆盖率: $($coverageData.OverallCoverage)%" -ForegroundColor $(if ($coverageData.OverallCoverage -ge $minThreshold) { "Green" } else { "Red" })
    Write-Host "违规数: $($coverageData.Violations.Count)" -ForegroundColor $(if ($coverageData.Violations.Count -eq 0) { "Green" } else { "Red" })
    Write-Host ""
    Write-Host "详细报告:" -ForegroundColor Cyan
    Write-Host "  JSON: test-coverage_$timestamp.json" -ForegroundColor Gray
    Write-Host "  Markdown: test-coverage_$timestamp.md" -ForegroundColor Gray
    Write-Host ""
}

exit $exitCode

