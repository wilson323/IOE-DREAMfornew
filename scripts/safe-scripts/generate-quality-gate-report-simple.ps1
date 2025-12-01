# IOE-DREAM Quality Gate Report Generator (PowerShell)
# Generate unified verification report and proof file

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

# Initialize report
$report = @{
    reportDate = (Get-Date -Format "yyyy-MM-ddTHH:mm:ssZ")
    projectName = "IOE-DREAM"
    status = "PASSED"
    summary = @{
        totalChecks = 0
        passedChecks = 0
        failedChecks = 0
        complianceRate = "0%"
    }
    compilation = @{ status = "UNKNOWN" }
    codeQuality = @{
        status = "UNKNOWN"
        javaxViolations = 0
        autowiredViolations = 0
        architectureViolations = 0
    }
    repowikiCompliance = @{
        dependencyInjection = @{ status = "UNKNOWN" }
        packageNaming = @{ status = "UNKNOWN" }
        architecture = @{ status = "UNKNOWN" }
    }
}

Write-Host "`n=== IOE-DREAM Quality Gate Report Generation ===" -ForegroundColor Cyan
Write-Host "Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n" -ForegroundColor Cyan

# Step 1: Check compilation environment
Write-Host "Step 1: Checking compilation environment..." -ForegroundColor Blue
try {
    # Check Java
    $javaOutput = java -version 2>&1 | Out-String
    if ($javaOutput -match "version") {
        $javaVersion = ($javaOutput | Select-String "version.*\d+\.\d+").Matches[0].Value
        $report.compilation.javaVersion = $javaVersion
        Write-Host "  [PASSED] Java version: $javaVersion" -ForegroundColor Green
    } else {
        throw "Java version check failed"
    }

    # Check Maven
    $mavenOutput = mvn -version 2>&1 | Out-String
    if ($mavenOutput -match "Apache Maven") {
        $mavenVersion = ($mavenOutput | Select-String "Apache Maven \d+\.\d+\.\d+").Matches[0].Value
        $report.compilation.mavenVersion = $mavenVersion
        Write-Host "  [PASSED] Maven version: $mavenVersion" -ForegroundColor Green
    } else {
        throw "Maven version check failed"
    }

    $report.compilation.status = "PASSED"
    $report.summary.passedChecks++
} catch {
    $report.compilation.status = "FAILED"
    $report.compilation.details = $_.Exception.Message
    $report.summary.failedChecks++
    Write-Host "  [FAILED] Environment check failed: $_" -ForegroundColor Red
}
$report.summary.totalChecks++

# Step 2: Code quality scan
Write-Host "`nStep 2: Scanning code quality..." -ForegroundColor Blue
try {
    $javaFiles = Get-ChildItem -Path "smart-admin-api-java17-springboot3" -Recurse -Filter "*.java" -ErrorAction SilentlyContinue

    # Check javax packages
    $javaxFiles = $javaFiles | Where-Object {
        $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
        if ($content) {
            $content -match "import javax\.(servlet|validation|annotation|persistence|xml\.bind)" -and
            $content -notmatch "import javax\.(crypto|net|security|naming)"
        }
    }
    $report.codeQuality.javaxViolations = $javaxFiles.Count
    if ($javaxFiles.Count -eq 0) {
        $report.repowikiCompliance.packageNaming.status = "PASSED"
        Write-Host "  [PASSED] javax package check (0 violations)" -ForegroundColor Green
    } else {
        $report.repowikiCompliance.packageNaming.status = "FAILED"
        Write-Host "  [FAILED] Found $($javaxFiles.Count) javax violations" -ForegroundColor Red
    }

    # Check @Autowired
    $autowiredFiles = $javaFiles | Where-Object {
        $content = Get-Content $_.FullName -Raw -ErrorAction SilentlyContinue
        $content -and $content -match "@Autowired"
    }
    $report.codeQuality.autowiredViolations = $autowiredFiles.Count
    if ($autowiredFiles.Count -eq 0) {
        $report.repowikiCompliance.dependencyInjection.status = "PASSED"
        Write-Host "  [PASSED] @Autowired check (0 violations)" -ForegroundColor Green
    } else {
        $report.repowikiCompliance.dependencyInjection.status = "FAILED"
        Write-Host "  [FAILED] Found $($autowiredFiles.Count) @Autowired violations" -ForegroundColor Red
    }

    # Check architecture violations
    $controllerFiles = $javaFiles | Where-Object { $_.Name -match "Controller\.java$" }
    $archViolations = 0
    foreach ($file in $controllerFiles) {
        $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
        if ($content -and ($content -match "@Resource.*Dao|@Autowired.*Dao")) {
            $archViolations++
        }
    }
    $report.codeQuality.architectureViolations = $archViolations
    if ($archViolations -eq 0) {
        $report.repowikiCompliance.architecture.status = "PASSED"
        Write-Host "  [PASSED] Architecture check (0 violations)" -ForegroundColor Green
    } else {
        $report.repowikiCompliance.architecture.status = "FAILED"
        Write-Host "  [FAILED] Found $archViolations architecture violations" -ForegroundColor Red
    }

    if ($report.codeQuality.javaxViolations -eq 0 -and
        $report.codeQuality.autowiredViolations -eq 0 -and
        $report.codeQuality.architectureViolations -eq 0) {
        $report.codeQuality.status = "PASSED"
        $report.summary.passedChecks++
    } else {
        $report.codeQuality.status = "FAILED"
        $report.summary.failedChecks++
    }
    $report.summary.totalChecks++

} catch {
    Write-Host "  [FAILED] Code quality scan failed: $_" -ForegroundColor Red
    $report.codeQuality.status = "FAILED"
    $report.summary.failedChecks++
    $report.summary.totalChecks++
}

# Calculate compliance rate
$report.summary.complianceRate = [math]::Round(($report.summary.passedChecks / $report.summary.totalChecks) * 100, 2).ToString() + "%"

# Determine overall status
if ($report.summary.failedChecks -gt 0) {
    $report.status = "FAILED"
}

# Generate report file
$reportFile = "quality-gate-report-$timestamp.json"
$reportJson = $report | ConvertTo-Json -Depth 10
$reportJson | Out-File -FilePath $reportFile -Encoding UTF8

Write-Host "`n[SUCCESS] Report generated: $reportFile" -ForegroundColor Green

# Generate proof file
$proofFile = "quality-gate-proof-$timestamp.proof"
$proofContent = @"
IOE-DREAM Quality Gate Verification Proof
========================================
Verification Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
Project Path: $ProjectRoot
Verification Type: Unified Quality Gate Verification

Verification Summary:
- Total Checks: $($report.summary.totalChecks)
- Passed: $($report.summary.passedChecks)
- Failed: $($report.summary.failedChecks)
- Compliance Rate: $($report.summary.complianceRate)

Detailed Status:
- Compilation: $($report.compilation.status)
- Code Quality: $($report.codeQuality.status)

repowiki Compliance:
- Dependency Injection: $($report.repowikiCompliance.dependencyInjection.status)
- Package Naming: $($report.repowikiCompliance.packageNaming.status)
- Architecture: $($report.repowikiCompliance.architecture.status)

Violation Statistics:
- javax violations: $($report.codeQuality.javaxViolations)
- @Autowired violations: $($report.codeQuality.autowiredViolations)
- Architecture violations: $($report.codeQuality.architectureViolations)

Overall Status: $($report.status)

This proof file indicates that quality gate verification has been completed.
Report file: $reportFile

Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
"@

$proofContent | Out-File -FilePath $proofFile -Encoding UTF8

Write-Host "[SUCCESS] Proof generated: $proofFile" -ForegroundColor Green

# Output summary
Write-Host "`n=== Verification Summary ===" -ForegroundColor Cyan
Write-Host "Total Checks: $($report.summary.totalChecks)" -ForegroundColor White
Write-Host "Passed: $($report.summary.passedChecks)" -ForegroundColor Green
Write-Host "Failed: $($report.summary.failedChecks)" -ForegroundColor $(if ($report.summary.failedChecks -gt 0) { "Red" } else { "Green" })
Write-Host "Compliance Rate: $($report.summary.complianceRate)" -ForegroundColor Cyan
Write-Host "Overall Status: $($report.status)" -ForegroundColor $(if ($report.status -eq "PASSED") { "Green" } else { "Red" })
Write-Host "`nReport: $reportFile" -ForegroundColor Cyan
Write-Host "Proof: $proofFile" -ForegroundColor Cyan
Write-Host "`n[SUCCESS] Quality gate report generation completed!`n" -ForegroundColor Green

exit $(if ($report.status -eq "PASSED") { 0 } else { 1 })

