# IOE-DREAM 精确架构合规性检查脚本
# Version: 1.0.0
# Purpose: 只检查实际代码行，排除注释
# Environment: Windows PowerShell

$ErrorActionPreference = "Continue"
$basePath = "D:\IOE-DREAM\microservices"
$realViolations = @()

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Precise Architecture Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 排除目录
$excludeDirs = @("archive", "target", "test", "analytics")

$javaFiles = Get-ChildItem -Path $basePath -Include *.java -Recurse -ErrorAction SilentlyContinue |
    Where-Object {
        $exclude = $false
        foreach ($dir in $excludeDirs) {
            if ($_.FullName -like "*\$dir\*") {
                $exclude = $true
                break
            }
        }
        -not $exclude
    }

# 检查@Autowired（排除注释）
Write-Host "[Check] Scanning @Autowired in actual code (excluding comments)..." -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    $lines = Get-Content $file -ErrorAction SilentlyContinue
    $inComment = $false

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i].Trim()

        # 跳过空行
        if ([string]::IsNullOrWhiteSpace($line)) { continue }

        # 检测多行注释开始/结束
        if ($line -match '/\*') { $inComment = $true }
        if ($line -match '\*/') { $inComment = $false; continue }
        if ($inComment) { continue }

        # 跳过单行注释
        if ($line -match '^\s*//') { continue }
        if ($line -match '^\s*/\*') { continue }
        if ($line -match '\*/\s*$') { continue }

        # 检查@Autowired（实际代码行
        if ($line -match '@Autowired') {
            $realViolations += "REAL: @Autowired in $($file.FullName):$($i+1) - $line"
            Write-Host "  FOUND: $($file.FullName):$($i+1)" -ForegroundColor Red
            Write-Host "    Line: $line" -ForegroundColor Gray
            break
        }
    }
}

Write-Host ""
Write-Host "Total real @Autowired violations: $($realViolations.Count)" -ForegroundColor $(if ($realViolations.Count -eq 0) { "Green" } else { "Red" })

# 检查@Repository（排除注释）
Write-Host ""
Write-Host "[Check] Scanning @Repository in actual code (excluding comments)..." -ForegroundColor Yellow

$repositoryViolations = @()
foreach ($file in $javaFiles) {
    $lines = Get-Content $file -ErrorAction SilentlyContinue
    $inComment = $false

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i].Trim()

        # 跳过空行
        if ([string]::IsNullOrWhiteSpace($line)) { continue }

        # 检测多行注释开始/结束
        if ($line -match '/\*') { $inComment = $true }
        if ($line -match '\*/') { $inComment = $false; continue }
        if ($inComment) { continue }

        # 跳过单行注释
        if ($line -match '^\s*//') { continue }
        if ($line -match '^\s*/\*') { continue }
        if ($line -match '\*/\s*$') { continue }

        # 检查@Repository
        if ($line -match '@Repository') {
            $repositoryViolations += "REAL: @Repository in $($file.FullName):$($i+1) - $line"
            Write-Host "  FOUND: $($file.FullName):$($i+1)" -ForegroundColor Red
            Write-Host "    Line: $line" -ForegroundColor Gray
            break
        }
    }
}

Write-Host ""
Write-Host "Total real @Repository violations: $($repositoryViolations.Count)" -ForegroundColor $(if ($repositoryViolations.Count -eq 0) { "Green" } else { "Red" })

# 输出汇总
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "@Autowired violations: $($realViolations.Count)" -ForegroundColor $(if ($realViolations.Count -eq 0) { "Green" } else { "Red" })
Write-Host "@Repository violations: $($repositoryViolations.Count)" -ForegroundColor $(if ($repositoryViolations.Count -eq 0) { "Green" } else { "Red" })

# 保存报告
$reportFile = "D:\IOE-DREAM\PRECISE_ARCHITECTURE_CHECK_REPORT_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
$reportContent = "# Precise Architecture Check Report`n`n"
$reportContent += "**Check Time**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n"
$reportContent += "**@Autowired Violations**: $($realViolations.Count)`n"
$reportContent += "**@Repository Violations**: $($repositoryViolations.Count)`n`n"

if ($realViolations.Count -gt 0) {
    $reportContent += "## @Autowired Violations`n`n"
    foreach ($violation in $realViolations) {
        $reportContent += "- $violation`n"
    }
    $reportContent += "`n"
}

if ($repositoryViolations.Count -gt 0) {
    $reportContent += "## @Repository Violations`n`n"
    foreach ($violation in $repositoryViolations) {
        $reportContent += "- $violation`n"
    }
}

$reportContent | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host ""
Write-Host "Report saved to: $reportFile" -ForegroundColor Cyan

