# IOE-DREAM Quick Test Script
# Test all deployment scripts to ensure they work correctly

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM Deployment Test" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$tests = @()

# Test 1: Check Docker installation
Write-Host "[Test 1] Checking Docker..." -ForegroundColor Yellow
$dockerVersion = docker --version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  PASS: Docker is installed ($dockerVersion)" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: Docker is not installed" -ForegroundColor Red
    $tests += $false
}

# Test 2: Check Docker Compose
Write-Host "`n[Test 2] Checking Docker Compose..." -ForegroundColor Yellow
$composeVersion = docker compose version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  PASS: Docker Compose is installed ($composeVersion)" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: Docker Compose is not installed" -ForegroundColor Red
    $tests += $false
}

# Test 3: Check docker-compose-all.yml exists
Write-Host "`n[Test 3] Checking docker-compose-all.yml..." -ForegroundColor Yellow
if (Test-Path "D:\IOE-DREAM\docker-compose-all.yml") {
    Write-Host "  PASS: docker-compose-all.yml exists" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: docker-compose-all.yml not found" -ForegroundColor Red
    $tests += $false
}

# Test 4: Validate docker-compose-all.yml
Write-Host "`n[Test 4] Validating docker-compose-all.yml..." -ForegroundColor Yellow
$validation = docker compose -f "D:\IOE-DREAM\docker-compose-all.yml" config 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  PASS: docker-compose-all.yml is valid" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: docker-compose-all.yml has errors" -ForegroundColor Red
    $tests += $false
}

# Test 5: Check all Dockerfiles exist
Write-Host "`n[Test 5] Checking Dockerfiles..." -ForegroundColor Yellow
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

$allDockerfilesExist = $true
foreach ($service in $services) {
    $dockerfilePath = "D:\IOE-DREAM\microservices\$service\Dockerfile"
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "  FAIL: Dockerfile not found for $service" -ForegroundColor Red
        $allDockerfilesExist = $false
    }
}

if ($allDockerfilesExist) {
    Write-Host "  PASS: All 9 Dockerfiles exist" -ForegroundColor Green
    $tests += $true
} else {
    $tests += $false
}

# Test 6: Check deploy-docker.ps1 exists
Write-Host "`n[Test 6] Checking deploy-docker.ps1..." -ForegroundColor Yellow
if (Test-Path "D:\IOE-DREAM\scripts\deploy-docker.ps1") {
    Write-Host "  PASS: deploy-docker.ps1 exists" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: deploy-docker.ps1 not found" -ForegroundColor Red
    $tests += $false
}

# Test 7: Check start-all-complete.ps1 exists
Write-Host "`n[Test 7] Checking start-all-complete.ps1..." -ForegroundColor Yellow
if (Test-Path "D:\IOE-DREAM\scripts\start-all-complete.ps1") {
    Write-Host "  PASS: start-all-complete.ps1 exists" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: start-all-complete.ps1 not found" -ForegroundColor Red
    $tests += $false
}

# Test 8: Check DEPLOYMENT.md exists
Write-Host "`n[Test 8] Checking DEPLOYMENT.md..." -ForegroundColor Yellow
if (Test-Path "D:\IOE-DREAM\DEPLOYMENT.md") {
    Write-Host "  PASS: DEPLOYMENT.md exists" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: DEPLOYMENT.md not found" -ForegroundColor Red
    $tests += $false
}

# Test 9: Test deploy-docker.ps1 syntax
Write-Host "`n[Test 9] Testing deploy-docker.ps1 syntax..." -ForegroundColor Yellow
$syntaxCheck = powershell -File "D:\IOE-DREAM\scripts\deploy-docker.ps1" -Action status 2>&1
if ($syntaxCheck -match "Docker Service Status" -or $syntaxCheck -match "Config file not found") {
    Write-Host "  PASS: deploy-docker.ps1 syntax is correct" -ForegroundColor Green
    $tests += $true
} else {
    Write-Host "  FAIL: deploy-docker.ps1 has syntax errors" -ForegroundColor Red
    $tests += $false
}

# Summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$passed = ($tests | Where-Object { $_ -eq $true }).Count
$failed = ($tests | Where-Object { $_ -eq $false }).Count
$total = $tests.Count

Write-Host "`nTotal Tests: $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red

if ($failed -eq 0) {
    Write-Host "`nAll tests passed! Ready for deployment." -ForegroundColor Green
    exit 0
} else {
    Write-Host "`nSome tests failed. Please fix the issues before deployment." -ForegroundColor Yellow
    exit 1
}
