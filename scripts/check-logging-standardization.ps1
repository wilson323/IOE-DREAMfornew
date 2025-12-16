# =====================================================
# Logging Standardization Check Script
# Version: v1.0.0
# Description: Check System.out.println usage in project
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "",

    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport
)

$ErrorActionPreference = "Stop"

function Write-ColorOutput($ForegroundColor) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    if ($args) {
        Write-Output $args
    }
    $host.UI.RawUI.ForegroundColor = $fc
}

$basePath = if ($ServiceName) {
    "microservices\$ServiceName"
} else {
    "microservices"
}

if (-not (Test-Path $basePath)) {
    Write-ColorOutput Red "[ERROR] Directory not found: $basePath"
    exit 1
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Logging Standardization Check" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[INFO] Searching for System.out.println usage..." -ForegroundColor Yellow

$searchPattern = [regex]::new("System\.out\.(print|println|printf)")
$errPattern = [regex]::new("System\.err\.(print|println|printf)")

$issues = @()
$fileCount = 0

Get-ChildItem -Path $basePath -Recurse -Include "*.java" -File | ForEach-Object {
    $file = $_
    $fileCount++

    try {
        $content = Get-Content $file.FullName -Raw -ErrorAction Stop

        $lineNumber = 1
        $lines = $content -split "`n"
        foreach ($line in $lines) {
            if ($searchPattern.IsMatch($line)) {
                $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")
                $issues += [PSCustomObject]@{
                    File = $relativePath
                    Line = $lineNumber
                    Content = $line.Trim()
                    Type = "System.out"
                }
            }
            if ($errPattern.IsMatch($line)) {
                $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")
                $issues += [PSCustomObject]@{
                    File = $relativePath
                    Line = $lineNumber
                    Content = $line.Trim()
                    Type = "System.err"
                }
            }
            $lineNumber++
        }
    } catch {
        Write-ColorOutput Red "[ERROR] Failed to read file: $($file.FullName), Error: $($_.Exception.Message)"
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Check Results" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Files scanned: $fileCount" -ForegroundColor White
Write-Host "Issues found: $($issues.Count)" -ForegroundColor $(if ($issues.Count -eq 0) { "Green" } else { "Yellow" })
Write-Host ""

if ($issues.Count -eq 0) {
    Write-ColorOutput Green "[OK] No System.out.println found, logging is standardized"
    Write-Host ""
    Write-Host "Recommendations:" -ForegroundColor Cyan
    Write-Host "  - Ensure all logs use log.error/log.warn/log.info/log.debug" -ForegroundColor White
    Write-Host "  - Ensure logs include context (userId, orderId, etc.)" -ForegroundColor White
    Write-Host "  - Ensure exception logs include full stack trace" -ForegroundColor White
    exit 0
}

Write-Host "Issues found:" -ForegroundColor Yellow
Write-Host ""

$groupedIssues = $issues | Group-Object -Property File

foreach ($group in $groupedIssues) {
    Write-ColorOutput Yellow "File: $($group.Name)"
    foreach ($issue in $group.Group) {
        Write-Host "  Line $($issue.Line): $($issue.Content)" -ForegroundColor Red
    }
    Write-Host ""
}

if ($GenerateReport) {
    $reportPath = "documentation\technical\LOGGING_STANDARDIZATION_REPORT.md"
    $reportDir = Split-Path $reportPath -Parent
    if (-not (Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $outCount = ($issues | Where-Object { $_.Type -eq "System.out" } | Measure-Object).Count
    $errCount = ($issues | Where-Object { $_.Type -eq "System.err" } | Measure-Object).Count
    $lastUpdate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    $reportLines = @(
        "# Logging Standardization Check Report",
        "",
        "**Generated**: $lastUpdate",
        "**Scope**: $basePath",
        "**Files Scanned**: $fileCount",
        "**Issues Found**: $($issues.Count)",
        "",
        "## Summary",
        "",
        "### Statistics",
        "",
        "- **System.out usage**: $outCount",
        "- **System.err usage**: $errCount",
        "",
        "## Detailed Issues",
        ""
    )

    foreach ($group in $groupedIssues) {
        $reportLines += "### $($group.Name)"
        $reportLines += ""
        foreach ($issue in $group.Group) {
            $reportLines += "- **Line $($issue.Line)**: ``$($issue.Content)``"
        }
        $reportLines += ""
    }

    $reportLines += "---"
    $reportLines += "**Last Updated**: $lastUpdate"

    $reportContent = $reportLines -join "`n"
    [System.IO.File]::WriteAllText($reportPath, $reportContent, [System.Text.Encoding]::UTF8)
    Write-ColorOutput Green "[OK] Report generated: $reportPath"
}

Write-Host ""
Write-ColorOutput Yellow "[WARN] Found $($issues.Count) issues that need to be fixed"
Write-Host "Recommendation: Replace System.out.println with log.error/log.warn/log.info/log.debug" -ForegroundColor Cyan

exit 1
