# Fix ioedream-attendance-service build path issue
# Ensure microservices-common module is properly built and installed to local Maven repository

# Force UTF-8 encoding - must be executed at the very beginning
$PSDefaultParameterValues['*:Encoding'] = 'utf8'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Fix ioedream-attendance-service build path" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Step 1: Build microservices-common module
Write-Host "`n[Step 1] Building microservices-common module..." -ForegroundColor Yellow
Set-Location "D:\IOE-DREAM\microservices\microservices-common"
mvn clean install -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nERROR: microservices-common module build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "`n[OK] microservices-common module built successfully" -ForegroundColor Green

# Step 2: Refresh ioedream-attendance-service dependencies
Write-Host "`n[Step 2] Refreshing ioedream-attendance-service dependencies..." -ForegroundColor Yellow
Set-Location "D:\IOE-DREAM\microservices\ioedream-attendance-service"
mvn clean compile -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "`nERROR: ioedream-attendance-service compilation failed!" -ForegroundColor Red
    exit 1
}

Write-Host "`n[OK] ioedream-attendance-service compiled successfully" -ForegroundColor Green

# Step 3: Verify EmployeeEntity class exists
Write-Host "`n[Step 3] Verifying EmployeeEntity class..." -ForegroundColor Yellow
$employeeEntityPath = "D:\IOE-DREAM\microservices\microservices-common\src\main\java\net\lab1024\sa\common\hr\entity\EmployeeEntity.java"
if (Test-Path $employeeEntityPath) {
    Write-Host "[OK] EmployeeEntity class file exists: $employeeEntityPath" -ForegroundColor Green
} else {
    Write-Host "[ERROR] EmployeeEntity class file does not exist!" -ForegroundColor Red
    exit 1
}

Write-Host "`n=========================================" -ForegroundColor Cyan
Write-Host "Build path fix completed!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "`nNote: If IDE still shows errors, please:" -ForegroundColor Yellow
Write-Host "1. Right-click project -> Maven -> Reload Project" -ForegroundColor White
Write-Host "2. Or: File -> Invalidate Caches / Restart" -ForegroundColor White
Write-Host "=========================================" -ForegroundColor Cyan
