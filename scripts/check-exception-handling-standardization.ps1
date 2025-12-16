# =====================================================
# Exception Handling Standardization Check Script
# Version: v1.2.0
# Description: Check exception handling standardization in project
# Created: 2025-01-30
# Updated: 2025-12-11 - Optimized catch block detection logic
# Updated: 2025-12-11 - Improved catch block scanning and log detection accuracy
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$ServiceName = "",

    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport,

    [Parameter(Mandatory=$false)]
    [switch]$CheckRuntimeExceptionOnly,

    [Parameter(Mandatory=$false)]
    [switch]$CheckExceptionPatterns
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
Write-Host "Exception Handling Standardization Check" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Patterns to check
$runtimeExceptionPattern = [regex]::new("throw\s+new\s+RuntimeException")
$businessExceptionPattern = [regex]::new("throw\s+new\s+BusinessException")
$systemExceptionPattern = [regex]::new("throw\s+new\s+SystemException")
# Improved catch pattern to match various catch block formats
# Match: catch (Exception e), catch (BusinessException e), catch (SystemException e), etc.
$catchExceptionPattern = [regex]::new("catch\s*\(\s*[^)]*Exception[^)]*\s+[a-zA-Z_][a-zA-Z0-9_]*\s*\)")
$catchRuntimeExceptionPattern = [regex]::new("catch\s*\(\s*RuntimeException\s+e\s*\)")
# Improved log pattern to match log statements with various spacing and method calls
$logErrorPattern = [regex]::new("log\.(error|warn|info|debug)\s*\(")
$emptyCatchPattern = [regex]::new("catch\s*\([^)]+\)\s*\{\s*\}")
$swallowedExceptionPattern = [regex]::new("catch\s*\([^)]+\)\s*\{\s*//.*\}")

$runtimeExceptionIssues = @()
$exceptionPatternIssues = @()
$fileCount = 0

Write-Host "[INFO] Scanning Java files for exception handling issues..." -ForegroundColor Yellow

Get-ChildItem -Path $basePath -Recurse -Include "*.java" -File | Where-Object {
    $_.FullName -notmatch "\\test\\" -and
    $_.FullName -notmatch "Test\.java$"
} | ForEach-Object {
    $file = $_
    $fileCount++

    try {
        $content = Get-Content $file.FullName -Raw -ErrorAction Stop
        $relativePath = $file.FullName.Replace((Get-Location).Path + "\", "")

        # Check for RuntimeException usage
        if ($runtimeExceptionPattern.IsMatch($content)) {
            $lineNumber = 1
            $lines = $content -split "`n"
            foreach ($line in $lines) {
                if ($runtimeExceptionPattern.IsMatch($line)) {
                    $runtimeExceptionIssues += [PSCustomObject]@{
                        File = $relativePath
                        Line = $lineNumber
                        Content = $line.Trim()
                        Type = "RuntimeException"
                        Priority = "P0"
                    }
                }
                $lineNumber++
            }
        }

        # Check exception handling patterns
        if ($CheckExceptionPatterns -or -not $CheckRuntimeExceptionOnly) {
            $lines = $content -split "`n"

            # Find all catch blocks and check for log statements
            for ($i = 0; $i -lt $lines.Count; $i++) {
                $line = $lines[$i]
                $lineNumber = $i + 1
                $trimmedLine = $line.Trim()

                # Detect catch block - improved pattern to match catch statements
                if ($catchExceptionPattern.IsMatch($trimmedLine)) {
                    $catchBlockStart = $lineNumber
                    $catchBlockEnd = 0
                    $hasLog = $false
                    $braceCount = 0
                    $catchBlockLines = @()

                    # Check if catch block starts with opening brace on same line
                    if ($trimmedLine -match "\{") {
                        $braceCount = 1
                    } elseif ($trimmedLine -match "\{.*\}") {
                        # Single line catch block
                        $braceCount = 0
                    } else {
                        # Opening brace on next line
                        $braceCount = 0
                    }

                    # Check if log is in the same line as catch
                    if ($logErrorPattern.IsMatch($line)) {
                        $hasLog = $true
                    }

                    # Scan forward to find the end of catch block and check for log
                    $maxScanLines = 50  # Limit scan to 50 lines to avoid performance issues
                    for ($j = $i + 1; $j -lt $lines.Count -and $j -lt $i + $maxScanLines; $j++) {
                        $nextLine = $lines[$j]
                        $trimmedNextLine = $nextLine.Trim()

                        # Skip empty lines and comments
                        if ($trimmedNextLine -eq "" -or $trimmedNextLine.StartsWith("//") -or $trimmedNextLine.StartsWith("*")) {
                            continue
                        }

                        # Count braces (simple count, ignoring strings for performance)
                        $openBraces = ($nextLine.ToCharArray() | Where-Object { $_ -eq '{' }).Count
                        $closeBraces = ($nextLine.ToCharArray() | Where-Object { $_ -eq '}' }).Count
                        $braceCount += $openBraces - $closeBraces

                        # Check for log in catch block
                        if ($logErrorPattern.IsMatch($nextLine)) {
                            $hasLog = $true
                            break  # Found log, no need to continue scanning
                        }

                        # Detect end of catch block (when brace count reaches 0 or negative)
                        if ($braceCount -le 0) {
                            $catchBlockEnd = $j + 1
                            break
                        }
                    }

                    # If catch block ended without finding log, report issue
                    # But skip if catch block is empty or only contains comments/whitespace
                    if (-not $hasLog) {
                        # Check if catch block has any meaningful content (not just comments/whitespace)
                        $hasContent = $false
                        $contentLines = 0
                        for ($k = $i; $k -lt [Math]::Min($i + 20, $lines.Count); $k++) {
                            $checkLine = $lines[$k].Trim()
                            if ($checkLine -and -not $checkLine.StartsWith("//") -and -not $checkLine.StartsWith("*") -and -not $checkLine.StartsWith("@")) {
                                $contentLines++
                                if ($contentLines -gt 1) {  # More than just the catch statement
                                    $hasContent = $true
                                    break
                                }
                            }
                        }

                        if ($hasContent) {
                            $exceptionPatternIssues += [PSCustomObject]@{
                                File = $relativePath
                                Line = $catchBlockStart
                                Content = "Try-catch block without log statement"
                                Type = "MissingLog"
                                Priority = "P1"
                            }
                        }
                    }
                }

                # Check for empty catch blocks
                if ($emptyCatchPattern.IsMatch($line) -or $swallowedExceptionPattern.IsMatch($line)) {
                    $exceptionPatternIssues += [PSCustomObject]@{
                        File = $relativePath
                        Line = $lineNumber
                        Content = $line.Trim()
                        Type = "EmptyCatch"
                        Priority = "P0"
                    }
                }
            }
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
Write-Host "RuntimeException issues: $($runtimeExceptionIssues.Count)" -ForegroundColor $(if ($runtimeExceptionIssues.Count -eq 0) { "Green" } else { "Yellow" })
Write-Host "Exception pattern issues: $($exceptionPatternIssues.Count)" -ForegroundColor $(if ($exceptionPatternIssues.Count -eq 0) { "Green" } else { "Yellow" })
Write-Host ""

$totalIssues = $runtimeExceptionIssues.Count + $exceptionPatternIssues.Count

if ($totalIssues -eq 0) {
    Write-ColorOutput Green "[OK] No exception handling issues found"
    exit 0
}

# Group issues by file
$allIssues = $runtimeExceptionIssues + $exceptionPatternIssues
$groupedIssues = $allIssues | Group-Object -Property File

Write-Host "Issues found:" -ForegroundColor Yellow
Write-Host ""

# Display P0 issues first
$p0Issues = $allIssues | Where-Object { $_.Priority -eq "P0" }
$p1Issues = $allIssues | Where-Object { $_.Priority -eq "P1" }

if ($p0Issues.Count -gt 0) {
    Write-ColorOutput Red "P0 Issues (Critical): $($p0Issues.Count)"
    $p0Grouped = $p0Issues | Group-Object -Property File
    foreach ($group in $p0Grouped) {
        Write-ColorOutput Yellow "  File: $($group.Name)"
        foreach ($issue in $group.Group) {
            Write-Host "    Line $($issue.Line) [$($issue.Type)]: $($issue.Content)" -ForegroundColor Red
        }
        Write-Host ""
    }
}

if ($p1Issues.Count -gt 0) {
    Write-ColorOutput Yellow "P1 Issues (Important): $($p1Issues.Count)"
    Write-Host "  (Use -GenerateReport to see details)" -ForegroundColor Gray
    Write-Host ""
}

# Generate report
if ($GenerateReport) {
    $reportPath = "documentation\technical\EXCEPTION_HANDLING_STANDARDIZATION_REPORT.md"
    $reportDir = Split-Path $reportPath -Parent
    if (-not (Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $lastUpdate = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $runtimeExceptionCount = $runtimeExceptionIssues.Count
    $missingLogCount = ($exceptionPatternIssues | Where-Object { $_.Type -eq "MissingLog" }).Count
    $emptyCatchCount = ($exceptionPatternIssues | Where-Object { $_.Type -eq "EmptyCatch" }).Count

    $reportLines = @(
        "# Exception Handling Standardization Check Report",
        "",
        "**Generated**: $lastUpdate",
        "**Scope**: $basePath",
        "**Files Scanned**: $fileCount",
        "**Total Issues**: $totalIssues",
        "",
        "## Summary",
        "",
        "### Statistics",
        "",
        "- **RuntimeException usage**: $runtimeExceptionCount",
        "- **Missing log statements**: $missingLogCount",
        "- **Empty catch blocks**: $emptyCatchCount",
        "",
        "## P0 Issues (Critical)",
        ""
    )

    $p0Grouped = $p0Issues | Group-Object -Property File
    foreach ($group in $p0Grouped) {
        $reportLines += "### $($group.Name)"
        $reportLines += ""
        foreach ($issue in $group.Group) {
            $reportLines += "- **Line $($issue.Line)** [$($issue.Type)]: ``$($issue.Content)``"
        }
        $reportLines += ""
    }

    $reportLines += "## P1 Issues (Important)"
    $reportLines += ""

    $p1Grouped = $p1Issues | Group-Object -Property File
    foreach ($group in $p1Grouped) {
        $reportLines += "### $($group.Name)"
        $reportLines += ""
        foreach ($issue in $group.Group) {
            $reportLines += "- **Line $($issue.Line)** [$($issue.Type)]: ``$($issue.Content)``"
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
Write-ColorOutput Yellow "[WARN] Found $totalIssues issues that need to be fixed"
Write-Host "Recommendation: Follow exception handling standardization guide" -ForegroundColor Cyan

exit 1
