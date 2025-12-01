# IOE-DREAM One-Click Compile Script - Windows PowerShell
# Purpose: Auto-setup compilation environment and execute Maven compilation
# Author: Claude Code
# Date: 2025-11-19

param(
    [string]$Module = "all",
    [switch]$SkipTests = $true,
    [switch]$Clean = $true,
    [string]$Profile = "dev"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = "D:\IOE-DREAM"
$BackendPath = "$ProjectRoot\smart-admin-api-java17-springboot3"

Write-Host ""
Write-Host "=== IOE-DREAM One-Click Compile Script ===" -ForegroundColor Cyan
Write-Host "Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check environment
Write-Host "Step 1: Check compilation environment..." -ForegroundColor Blue

# Check Java
$javaCheck = Get-Command java -ErrorAction SilentlyContinue
if ($javaCheck) {
    Write-Host "  [PASS] Java installed" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Java not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Check Maven
$mavenCheck = Get-Command mvn -ErrorAction SilentlyContinue
if ($mavenCheck) {
    Write-Host "  [PASS] Maven installed" -ForegroundColor Green
} else {
    Write-Host "  [FAIL] Maven not installed or not in PATH" -ForegroundColor Red
    exit 1
}

# Step 2: Set UTF-8 encoding environment variables
Write-Host ""
Write-Host "Step 2: Set UTF-8 encoding environment variables..." -ForegroundColor Blue
$env:MAVEN_OPTS = "-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.region=CN"
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8"
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Host "  [DONE] UTF-8 encoding environment variables set" -ForegroundColor Green
Write-Host "    MAVEN_OPTS: $env:MAVEN_OPTS" -ForegroundColor Gray
Write-Host "    JAVA_TOOL_OPTIONS: $env:JAVA_TOOL_OPTIONS" -ForegroundColor Gray

# Step 3: Switch to backend project directory
Write-Host ""
Write-Host "Step 3: Switch to backend project directory..." -ForegroundColor Blue
if (-not (Test-Path $BackendPath)) {
    Write-Host "  [FAIL] Backend project directory not found: $BackendPath" -ForegroundColor Red
    exit 1
}
Set-Location $BackendPath
Write-Host "  [DONE] Current directory: $(Get-Location)" -ForegroundColor Green

# Step 4: Build Maven compilation command
Write-Host ""
Write-Host "Step 4: Build compilation command..." -ForegroundColor Blue
$mavenGoals = @()

if ($Clean) {
    $mavenGoals += "clean"
}

$mavenGoals += "compile"

if ($SkipTests) {
    $mavenGoals += "-DskipTests"
}

# Select compilation scope based on module
$mvnArgs = @()
switch ($Module.ToLower()) {
    "base" {
        $mvnArgs += "-pl"
        $mvnArgs += "sa-base"
        $mvnArgs += "-am"
        Write-Host "  [INFO] Compile module: sa-base (with dependencies)" -ForegroundColor Yellow
    }
    "support" {
        $mvnArgs += "-pl"
        $mvnArgs += "sa-support"
        $mvnArgs += "-am"
        Write-Host "  [INFO] Compile module: sa-support (with dependencies)" -ForegroundColor Yellow
    }
    "admin" {
        $mvnArgs += "-pl"
        $mvnArgs += "sa-admin"
        $mvnArgs += "-am"
        Write-Host "  [INFO] Compile module: sa-admin (with dependencies)" -ForegroundColor Yellow
    }
    "all" {
        Write-Host "  [INFO] Compile all modules" -ForegroundColor Yellow
    }
    default {
        Write-Host "  [WARN] Unknown module: $Module, using default all modules" -ForegroundColor Yellow
    }
}

# Add Profile
$mvnArgs += "-P"
$mvnArgs += $Profile
Write-Host "  [INFO] Use Profile: $Profile" -ForegroundColor Yellow

# Build complete command
$mavenCommand = "mvn $($mavenGoals -join ' ') $($mvnArgs -join ' ')"
Write-Host "  [DONE] Compilation command: $mavenCommand" -ForegroundColor Green

# Step 5: Execute compilation
Write-Host ""
Write-Host "Step 5: Execute compilation..." -ForegroundColor Blue
Write-Host "Start time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host ""

$compileStartTime = Get-Date

# Execute compilation (capture all output including errors)
$ErrorActionPreference = "Continue"
$compileOutput = @()
$compileResult = & mvn $mavenGoals $mvnArgs 2>&1
$compileOutput = $compileResult

$compileEndTime = Get-Date
$compileDuration = $compileEndTime - $compileStartTime

# Check compilation result
$outputString = $compileOutput | Out-String
if ($outputString -match "BUILD SUCCESS") {
        Write-Host ""
        Write-Host "=== Compilation SUCCESS ===" -ForegroundColor Green
        Write-Host "Compilation time: $($compileDuration.TotalSeconds.ToString('F2')) seconds" -ForegroundColor Green
        Write-Host "End time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
        
        # Check for warnings
        $warningCount = ($outputString | Select-String -Pattern "WARNING" -AllMatches).Matches.Count
        if ($warningCount -gt 0) {
            Write-Host ""
            Write-Host "Warning: Found $warningCount compilation warnings (can be ignored)" -ForegroundColor Yellow
        }
        
        exit 0
    } elseif ($outputString -match "BUILD FAILURE" -or $outputString -match "\[ERROR\]") {
        Write-Host ""
        Write-Host "=== Compilation FAILED ===" -ForegroundColor Red
        Write-Host "Compilation time: $($compileDuration.TotalSeconds.ToString('F2')) seconds" -ForegroundColor Red
        Write-Host "End time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Error messages:" -ForegroundColor Red
        
        # Extract and display first 20 errors
        $errors = $outputString -split "`n" | Where-Object { $_ -match "\[ERROR\]" } | Select-Object -First 20
        $errors | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        
        exit 1
} else {
    Write-Host ""
    Write-Host "=== Compilation result uncertain ===" -ForegroundColor Yellow
    Write-Host "Compilation time: $($compileDuration.TotalSeconds.ToString('F2')) seconds" -ForegroundColor Yellow
    Write-Host "Please check compilation output manually" -ForegroundColor Yellow
    exit 1
}

