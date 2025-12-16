# =====================================================
# Test Coverage Analysis Script
# Version: v1.0.0
# Description: Analyze existing test cases and identify missing tests
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport,

    [Parameter(Mandatory=$false)]
    [string]$Module = ""
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Test Coverage Analysis" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Switch to project root directory
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

# Switch to microservices directory
Set-Location "microservices"

Write-Host "[1/4] Analyzing existing test cases..." -ForegroundColor Yellow

# Count test cases
$testFiles = Get-ChildItem -Path "." -Filter "*Test.java" -Recurse |
    Where-Object { $_.FullName -notlike "*target*" -and $_.FullName -notlike "*test*" }

$serviceTests = @()
$managerTests = @()
$controllerTests = @()
$daoTests = @()

foreach ($file in $testFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($file.FullName -match "service.*Test\.java") {
        $serviceTests += $file
    } elseif ($file.FullName -match "manager.*Test\.java") {
        $managerTests += $file
    } elseif ($file.FullName -match "controller.*Test\.java") {
        $controllerTests += $file
    } elseif ($file.FullName -match "dao.*Test\.java") {
        $daoTests += $file
    }
}

Write-Host "[2/4] Counting ServiceImpl files..." -ForegroundColor Yellow

# Count ServiceImpl files
$serviceImplFiles = Get-ChildItem -Path "." -Filter "*ServiceImpl.java" -Recurse |
    Where-Object { $_.FullName -notlike "*test*" -and $_.FullName -notlike "*Test*" -and $_.FullName -notlike "*target*" }

Write-Host "[3/4] Counting Manager files..." -ForegroundColor Yellow

# Count Manager files
$managerFiles = Get-ChildItem -Path "." -Filter "*Manager*.java" -Recurse |
    Where-Object { $_.FullName -notlike "*test*" -and $_.FullName -notlike "*Test*" -and $_.FullName -notlike "*target*" -and $_.FullName -like "*Manager.java" }

Write-Host "[4/4] Counting Controller and DAO files..." -ForegroundColor Yellow

# Count Controller files
$controllerFiles = Get-ChildItem -Path "." -Filter "*Controller.java" -Recurse |
    Where-Object { $_.FullName -notlike "*test*" -and $_.FullName -notlike "*Test*" -and $_.FullName -notlike "*target*" }

# Count DAO files
$daoFiles = Get-ChildItem -Path "." -Filter "*Dao.java" -Recurse |
    Where-Object { $_.FullName -notlike "*test*" -and $_.FullName -notlike "*Test*" -and $_.FullName -notlike "*target*" }

# Output statistics
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Test Coverage Statistics" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Service Layer Statistics:" -ForegroundColor White
Write-Host "  ServiceImpl files: $($serviceImplFiles.Count)" -ForegroundColor Gray
Write-Host "  Existing test files: $($serviceTests.Count)" -ForegroundColor $(if ($serviceTests.Count -gt 0) { "Green" } else { "Yellow" })
$serviceCoverage = if ($serviceImplFiles.Count -gt 0) {
    [math]::Round(($serviceTests.Count / $serviceImplFiles.Count) * 100, 2)
} else {
    0
}
Write-Host "  Test coverage: $serviceCoverage%" -ForegroundColor $(if ($serviceCoverage -ge 80) { "Green" } elseif ($serviceCoverage -ge 50) { "Yellow" } else { "Red" })
Write-Host ""

Write-Host "Manager Layer Statistics:" -ForegroundColor White
Write-Host "  Manager files: $($managerFiles.Count)" -ForegroundColor Gray
Write-Host "  Existing test files: $($managerTests.Count)" -ForegroundColor $(if ($managerTests.Count -gt 0) { "Green" } else { "Yellow" })
$managerCoverage = if ($managerFiles.Count -gt 0) {
    [math]::Round(($managerTests.Count / $managerFiles.Count) * 100, 2)
} else {
    0
}
Write-Host "  Test coverage: $managerCoverage%" -ForegroundColor $(if ($managerCoverage -ge 75) { "Green" } elseif ($managerCoverage -ge 50) { "Yellow" } else { "Red" })
Write-Host ""

Write-Host "Controller Layer Statistics:" -ForegroundColor White
Write-Host "  Controller files: $($controllerFiles.Count)" -ForegroundColor Gray
Write-Host "  Existing test files: $($controllerTests.Count)" -ForegroundColor $(if ($controllerTests.Count -gt 0) { "Green" } else { "Yellow" })
$controllerCoverage = if ($controllerFiles.Count -gt 0) {
    [math]::Round(($controllerTests.Count / $controllerFiles.Count) * 100, 2)
} else {
    0
}
Write-Host "  Test coverage: $controllerCoverage%" -ForegroundColor $(if ($controllerCoverage -ge 60) { "Green" } elseif ($controllerCoverage -ge 30) { "Yellow" } else { "Red" })
Write-Host ""

Write-Host "DAO Layer Statistics:" -ForegroundColor White
Write-Host "  DAO files: $($daoFiles.Count)" -ForegroundColor Gray
Write-Host "  Existing test files: $($daoTests.Count)" -ForegroundColor $(if ($daoTests.Count -gt 0) { "Green" } else { "Yellow" })
$daoCoverage = if ($daoFiles.Count -gt 0) {
    [math]::Round(($daoTests.Count / $daoFiles.Count) * 100, 2)
} else {
    0
}
Write-Host "  Test coverage: $daoCoverage%" -ForegroundColor $(if ($daoCoverage -ge 70) { "Green" } elseif ($daoCoverage -ge 40) { "Yellow" } else { "Red" })
Write-Host ""

# Identify missing tests
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Missing Test Files" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Service layer missing tests
$serviceImplNames = $serviceImplFiles | ForEach-Object {
    $_.Name -replace "ServiceImpl\.java", "ServiceImplTest.java"
}
$missingServiceTests = $serviceImplNames | Where-Object {
    $testName = $_;
    -not ($serviceTests | Where-Object { $_.Name -eq $testName })
}

if ($missingServiceTests.Count -gt 0) {
    $count = $missingServiceTests.Count
    Write-Host "Service layer missing tests ($count files):" -ForegroundColor Yellow
    $missingServiceTests | Select-Object -First 10 | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor Red
    }
    if ($missingServiceTests.Count -gt 10) {
        $remaining = $missingServiceTests.Count - 10
        Write-Host "  ... and $remaining more" -ForegroundColor Gray
    }
    Write-Host ""
}

# Manager layer missing tests
$managerNames = $managerFiles | ForEach-Object {
    $_.Name -replace "Manager\.java", "ManagerTest.java"
}
$missingManagerTests = $managerNames | Where-Object {
    $testName = $_;
    -not ($managerTests | Where-Object { $_.Name -eq $testName })
}

if ($missingManagerTests.Count -gt 0) {
    $count = $missingManagerTests.Count
    Write-Host "Manager layer missing tests ($count files):" -ForegroundColor Yellow
    $missingManagerTests | Select-Object -First 10 | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor Red
    }
    if ($missingManagerTests.Count -gt 10) {
        $remaining = $missingManagerTests.Count - 10
        Write-Host "  ... and $remaining more" -ForegroundColor Gray
    }
    Write-Host ""
}

# Controller layer missing tests
$controllerNames = $controllerFiles | ForEach-Object {
    $_.Name -replace "Controller\.java", "ControllerTest.java"
}
$missingControllerTests = $controllerNames | Where-Object {
    $testName = $_;
    -not ($controllerTests | Where-Object { $_.Name -eq $testName })
}

if ($missingControllerTests.Count -gt 0) {
    $count = $missingControllerTests.Count
    Write-Host "Controller layer missing tests ($count files):" -ForegroundColor Yellow
    $missingControllerTests | Select-Object -First 10 | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor Red
    }
    if ($missingControllerTests.Count -gt 10) {
        $remaining = $missingControllerTests.Count - 10
        Write-Host "  ... and $remaining more" -ForegroundColor Gray
    }
    Write-Host ""
}

# DAO layer missing tests
$daoNames = $daoFiles | ForEach-Object {
    $_.Name -replace "Dao\.java", "DaoTest.java"
}
$missingDaoTests = $daoNames | Where-Object {
    $testName = $_;
    -not ($daoTests | Where-Object { $_.Name -eq $testName })
}

if ($missingDaoTests.Count -gt 0) {
    $count = $missingDaoTests.Count
    Write-Host "DAO layer missing tests ($count files):" -ForegroundColor Yellow
    $missingDaoTests | Select-Object -First 10 | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor Red
    }
    if ($missingDaoTests.Count -gt 10) {
        $remaining = $missingDaoTests.Count - 10
        Write-Host "  ... and $remaining more" -ForegroundColor Gray
    }
    Write-Host ""
}

# Summary
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Analysis Complete" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor White
Write-Host "  1. Run tests: mvn clean test" -ForegroundColor Gray
Write-Host "  2. Generate coverage report: mvn jacoco:report" -ForegroundColor Gray
Write-Host "  3. View coverage report: target/site/jacoco/index.html" -ForegroundColor Gray
Write-Host "  4. Add missing test cases based on the list above" -ForegroundColor Gray
Write-Host ""
