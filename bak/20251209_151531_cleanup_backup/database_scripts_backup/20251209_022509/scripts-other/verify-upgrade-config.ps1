# Quick verification script for Spring Cloud Alibaba upgrade
# This script only verifies configuration, does not build

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Spring Cloud Alibaba Upgrade Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Verify parent POM
Write-Host "[1/3] Verifying parent POM version..." -ForegroundColor Yellow
$pomPath = "microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw -Encoding UTF8
    if ($pomContent -match 'spring-cloud-alibaba\.version>2025\.0\.0\.0') {
        Write-Host "  [OK] Parent POM version: 2025.0.0.0" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Parent POM version not updated" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  [ERROR] Parent POM file not found" -ForegroundColor Red
    exit 1
}

# Step 2: Verify application.yml files
Write-Host "[2/3] Verifying application.yml files..." -ForegroundColor Yellow
$configFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "application.yml" | Where-Object { $_.FullName -like "*\src\main\resources\application.yml" }
$consistentCount = 0
$inconsistentCount = 0

foreach ($configFile in $configFiles) {
    $content = Get-Content $configFile.FullName -Raw -Encoding UTF8
    $serviceName = $configFile.Directory.Parent.Parent.Name
    
    $hasOptionalNacos = $content -match 'optional:nacos:'
    $hasEnabledTrue = $content -match 'enabled: true.*升级到2025'
    
    if ($hasOptionalNacos -and $hasEnabledTrue) {
        Write-Host "  [OK] $serviceName" -ForegroundColor Green
        $consistentCount++
    } else {
        Write-Host "  [WARN] $serviceName (optional:nacos:=$hasOptionalNacos, enabled:true=$hasEnabledTrue)" -ForegroundColor Yellow
        $inconsistentCount++
    }
}

Write-Host "  Summary: $consistentCount consistent, $inconsistentCount inconsistent" -ForegroundColor $(if ($inconsistentCount -eq 0) { "Green" } else { "Yellow" })

# Step 3: Verify Docker Compose
Write-Host "[3/3] Verifying Docker Compose environment variables..." -ForegroundColor Yellow
$dockerComposePath = "docker-compose-all.yml"
if (Test-Path $dockerComposePath) {
    $dockerContent = Get-Content $dockerComposePath -Raw -Encoding UTF8
    $importCount = ([regex]::Matches($dockerContent, "SPRING_CONFIG_IMPORT=optional:nacos:")).Count
    Write-Host "  Found $importCount SPRING_CONFIG_IMPORT entries" -ForegroundColor $(if ($importCount -eq 9) { "Green" } else { "Yellow" })
    
    if ($importCount -eq 9) {
        Write-Host "  [OK] All 9 services have SPRING_CONFIG_IMPORT configured" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] Expected 9, found $importCount" -ForegroundColor Yellow
    }
} else {
    Write-Host "  [ERROR] Docker Compose file not found" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Verification Complete" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
