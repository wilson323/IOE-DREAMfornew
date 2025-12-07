# IOE-DREAM Architecture Compliance Check Script
# Version: 1.0.0
# Purpose: Check project compliance with CLAUDE.md standards
# Environment: Windows PowerShell

$ErrorActionPreference = "Continue"
$basePath = "D:\IOE-DREAM\microservices"
$violations = @()

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Architecture Compliance Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Check @Autowired usage (forbidden)
Write-Host "[Check 1] Scanning @Autowired usage (forbidden)..." -ForegroundColor Yellow
$autowiredFiles = Get-ChildItem -Path $basePath -Include *.java -Recurse -ErrorAction SilentlyContinue |
    Select-String -Pattern "@Autowired" |
    Select-Object -ExpandProperty Path -Unique

if ($autowiredFiles.Count -gt 0) {
    Write-Host "  FAILED: Found $($autowiredFiles.Count) files using @Autowired" -ForegroundColor Red
    foreach ($file in $autowiredFiles) {
        $violations += "FAILED: @Autowired violation in $file"
        Write-Host "    - $file" -ForegroundColor Red
    }
} else {
    Write-Host "  PASSED: No @Autowired usage found" -ForegroundColor Green
}
Write-Host ""

# 2. Check @Repository usage (forbidden)
Write-Host "[Check 2] Scanning @Repository usage (forbidden)..." -ForegroundColor Yellow
$repositoryFiles = Get-ChildItem -Path $basePath -Include *.java -Recurse -ErrorAction SilentlyContinue |
    Select-String -Pattern "@Repository" |
    Select-Object -ExpandProperty Path -Unique

if ($repositoryFiles.Count -gt 0) {
    Write-Host "  FAILED: Found $($repositoryFiles.Count) files using @Repository" -ForegroundColor Red
    foreach ($file in $repositoryFiles) {
        $violations += "FAILED: @Repository violation in $file"
        Write-Host "    - $file" -ForegroundColor Red
    }
} else {
    Write-Host "  PASSED: No @Repository usage found" -ForegroundColor Green
}
Write-Host ""

# 3. Check Repository suffix (forbidden)
Write-Host "[Check 3] Scanning Repository suffix (forbidden)..." -ForegroundColor Yellow
$repositorySuffixFiles = Get-ChildItem -Path $basePath -Filter "*Repository.java" -Recurse -ErrorAction SilentlyContinue

if ($repositorySuffixFiles.Count -gt 0) {
    Write-Host "  FAILED: Found $($repositorySuffixFiles.Count) files with Repository suffix" -ForegroundColor Red
    foreach ($file in $repositorySuffixFiles) {
        $violations += "FAILED: Repository suffix violation in $($file.FullName)"
        Write-Host "    - $($file.FullName)" -ForegroundColor Red
    }
} else {
    Write-Host "  PASSED: No Repository suffix found" -ForegroundColor Green
}
Write-Host ""

# 4. Check Controller direct DAO access (forbidden)
Write-Host "[Check 4] Scanning Controller direct DAO access (forbidden)..." -ForegroundColor Yellow
$controllerFiles = Get-ChildItem -Path $basePath -Filter "*Controller.java" -Recurse -ErrorAction SilentlyContinue
$controllerDaoFiles = @()

foreach ($file in $controllerFiles) {
    $content = Get-Content $file.FullName -Raw -ErrorAction SilentlyContinue
    if ($content -match '@Resource.*Dao' -or $content -match '@Resource.*Repository') {
        $controllerDaoFiles += $file.FullName
    }
}

if ($controllerDaoFiles.Count -gt 0) {
    Write-Host "  FAILED: Found $($controllerDaoFiles.Count) Controllers directly accessing DAO" -ForegroundColor Red
    foreach ($file in $controllerDaoFiles) {
        $violations += "FAILED: Controller direct DAO access violation in $file"
        Write-Host "    - $file" -ForegroundColor Red
    }
} else {
    Write-Host "  PASSED: No Controller direct DAO access found" -ForegroundColor Green
}
Write-Host ""

# 5. Check javax package usage (should use jakarta)
Write-Host "[Check 5] Scanning javax package usage (should use jakarta)..." -ForegroundColor Yellow
$javaxFiles = Get-ChildItem -Path $basePath -Include *.java -Recurse -ErrorAction SilentlyContinue |
    Select-String -Pattern "import javax\.(annotation|validation|persistence|servlet|transaction)" |
    Select-Object -ExpandProperty Path -Unique

if ($javaxFiles.Count -gt 0) {
    Write-Host "  WARNING: Found $($javaxFiles.Count) files using javax package (should migrate to jakarta)" -ForegroundColor Yellow
    foreach ($file in $javaxFiles) {
        $violations += "WARNING: javax package usage (should migrate) in $file"
        Write-Host "    - $file" -ForegroundColor Yellow
    }
} else {
    Write-Host "  PASSED: No javax package usage found" -ForegroundColor Green
}
Write-Host ""

# 6. Check System.out.println usage (forbidden)
Write-Host "[Check 6] Scanning System.out.println usage (forbidden)..." -ForegroundColor Yellow
$systemOutFiles = Get-ChildItem -Path $basePath -Include *.java -Recurse -ErrorAction SilentlyContinue |
    Select-String -Pattern "System\.out\.(print|println)" |
    Select-Object -ExpandProperty Path -Unique

if ($systemOutFiles.Count -gt 0) {
    Write-Host "  FAILED: Found $($systemOutFiles.Count) files using System.out.println" -ForegroundColor Red
    foreach ($file in $systemOutFiles) {
        $violations += "FAILED: System.out.println violation in $file"
        Write-Host "    - $file" -ForegroundColor Red
    }
} else {
    Write-Host "  PASSED: No System.out.println usage found" -ForegroundColor Green
}
Write-Host ""

# Summary Report
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Check Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($violations.Count -eq 0) {
    Write-Host "SUCCESS: No architecture violations found!" -ForegroundColor Green
} else {
    Write-Host "FAILED: Found $($violations.Count) architecture violations" -ForegroundColor Red
    Write-Host ""
    Write-Host "Violation Details:" -ForegroundColor Yellow
    foreach ($violation in $violations) {
        Write-Host "  $violation" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Please refer to CLAUDE.md for fixing guidelines" -ForegroundColor Yellow
}

# Save report to file
$reportFile = "D:\IOE-DREAM\ARCHITECTURE_COMPLIANCE_CHECK_REPORT_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
$reportContent = "# Architecture Compliance Check Report`n`n"
$reportContent += "**Check Time**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n"
$reportContent += "**Check Scope**: $basePath`n"
$reportContent += "**Total Violations**: $($violations.Count)`n`n"
$reportContent += "## Violation Details`n`n"

foreach ($violation in $violations) {
    $reportContent += "- $violation`n"
}

$reportContent += "`n## Check Items`n`n"
$reportContent += "1. @Autowired usage: Forbidden, should use @Resource`n"
$reportContent += "2. @Repository usage: Forbidden, should use @Mapper`n"
$reportContent += "3. Repository suffix: Forbidden, should use Dao suffix`n"
$reportContent += "4. Controller direct DAO access: Forbidden, should go through Service layer`n"
$reportContent += "5. javax package usage: Should migrate to jakarta`n"
$reportContent += "6. System.out.println usage: Forbidden, should use logging framework`n`n"
$reportContent += "## Fix Suggestions`n`n"
$reportContent += "Please refer to CLAUDE.md file for architecture standards.`n"

$reportContent | Out-File -FilePath $reportFile -Encoding UTF8
Write-Host ""
Write-Host "Report saved to: $reportFile" -ForegroundColor Cyan
