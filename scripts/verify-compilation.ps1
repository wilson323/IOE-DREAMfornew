# =====================================================
# Compilation Verification Script
# Version: v1.0.0
# Description: Verify project compilation status
# Created: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$FixErrors
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Compilation Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = $PSScriptRoot + "\.."
Set-Location $projectRoot

Write-Host "[INFO] Project root: $projectRoot" -ForegroundColor Green
Write-Host ""

# Step 1: Clean Maven cache
Write-Host "[Step 1] Cleaning Maven cache..." -ForegroundColor Yellow
mvn clean -q 2>&1 | Out-Null
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Maven clean completed" -ForegroundColor Green
} else {
    Write-Host "[WARN] Maven clean warning (can be ignored)" -ForegroundColor Yellow
}
Write-Host ""

# Step 2: Compile common module
Write-Host "[Step 2] Compiling microservices-common..." -ForegroundColor Yellow
Set-Location "$projectRoot\microservices\microservices-common"
mvn clean install -DskipTests -q 2>&1 | Tee-Object -Variable buildOutput
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] microservices-common compiled successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] microservices-common compilation failed" -ForegroundColor Red
    $buildOutput | Select-String -Pattern "ERROR" | Select-Object -First 10
    Set-Location $projectRoot
    exit 1
}
Write-Host ""

# Step 3: Compile gateway service
Write-Host "[Step 3] Compiling ioedream-gateway-service..." -ForegroundColor Yellow
Set-Location "$projectRoot\microservices\ioedream-gateway-service"
mvn clean compile -DskipTests -q 2>&1 | Tee-Object -Variable buildOutput
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] ioedream-gateway-service compiled successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] ioedream-gateway-service compilation failed" -ForegroundColor Red
    $buildOutput | Select-String -Pattern "ERROR" | Select-Object -First 10
}
Write-Host ""

# Step 4: Compile common service
Write-Host "[Step 4] Compiling ioedream-common-service..." -ForegroundColor Yellow
Set-Location "$projectRoot\microservices\ioedream-common-service"
mvn clean compile -DskipTests -q 2>&1 | Tee-Object -Variable buildOutput
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] ioedream-common-service compiled successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] ioedream-common-service compilation failed" -ForegroundColor Red
    $buildOutput | Select-String -Pattern "ERROR" | Select-Object -First 10
}
Write-Host ""

# Step 5: Compile consume service
Write-Host "[Step 5] Compiling ioedream-consume-service..." -ForegroundColor Yellow
Set-Location "$projectRoot\microservices\ioedream-consume-service"
mvn clean compile -DskipTests -q 2>&1 | Tee-Object -Variable buildOutput
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] ioedream-consume-service compiled successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] ioedream-consume-service compilation failed" -ForegroundColor Red
    $buildOutput | Select-String -Pattern "ERROR" | Select-Object -First 10
}
Write-Host ""

# Step 6: Compile OA service
Write-Host "[Step 6] Compiling ioedream-oa-service..." -ForegroundColor Yellow
Set-Location "$projectRoot\microservices\ioedream-oa-service"
mvn clean compile -DskipTests -q 2>&1 | Tee-Object -Variable buildOutput
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] ioedream-oa-service compiled successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] ioedream-oa-service compilation failed" -ForegroundColor Red
    $buildOutput | Select-String -Pattern "ERROR" | Select-Object -First 10
}
Write-Host ""

# Step 7: Compile device comm service
Write-Host "[Step 7] Compiling ioedream-device-comm-service..." -ForegroundColor Yellow
Set-Location "$projectRoot\microservices\ioedream-device-comm-service"
mvn clean compile -DskipTests -q 2>&1 | Tee-Object -Variable buildOutput
if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] ioedream-device-comm-service compiled successfully" -ForegroundColor Green
} else {
    Write-Host "[ERROR] ioedream-device-comm-service compilation failed" -ForegroundColor Red
    $buildOutput | Select-String -Pattern "ERROR" | Select-Object -First 10
}
Write-Host ""

Set-Location $projectRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Compilation Verification Complete" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "[INFO] If compilation errors found, please check:" -ForegroundColor Yellow
Write-Host "  1. Is microservices-common compiled first?" -ForegroundColor Yellow
Write-Host "  2. Is Maven cache cleaned (mvn clean)?" -ForegroundColor Yellow
Write-Host "  3. Are dependency versions compatible?" -ForegroundColor Yellow
Write-Host ""
