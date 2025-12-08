# Verify Spring Cloud Alibaba 2025.0.0.0 Upgrade Configuration
# This script verifies all configurations are correct before building

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Spring Cloud Alibaba 2025.0.0.0" -ForegroundColor Cyan
Write-Host "  Configuration Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allValid = $true

# Step 1: Verify parent POM
Write-Host "[1/4] Verifying parent POM version..." -ForegroundColor Yellow
$pomPath = "microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw -Encoding UTF8
    if ($pomContent -match 'spring-cloud-alibaba\.version>2025\.0\.0\.0') {
        Write-Host "  [OK] Parent POM version: 2025.0.0.0" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Parent POM version not 2025.0.0.0" -ForegroundColor Red
        $allValid = $false
    }
} else {
    Write-Host "  [ERROR] Parent POM file not found" -ForegroundColor Red
    $allValid = $false
}

# Step 2: Verify application.yml files
Write-Host "[2/4] Verifying application.yml files..." -ForegroundColor Yellow
$configFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "application.yml" | Where-Object { $_.FullName -like "*\src\main\resources\application.yml" }
$consistentCount = 0
$inconsistentCount = 0

foreach ($configFile in $configFiles) {
    $content = Get-Content $configFile.FullName -Raw -Encoding UTF8
    $serviceName = $configFile.Directory.Parent.Parent.Name
    
    $hasOptionalNacos = $content -match 'optional:nacos:'
    # 检查config.enabled: true（在nacos.config块中，使用更简单的匹配模式）
    # 匹配模式：config: 后面任意内容，然后找到 enabled: true
    $hasConfigEnabled = $content -match 'config:.*?enabled:\s*true'
    # 检查import-check.enabled: true
    $hasImportCheckEnabled = $content -match 'import-check:.*?enabled:\s*true'
    
    if ($hasOptionalNacos -and $hasConfigEnabled -and $hasImportCheckEnabled) {
        Write-Host "  [OK] $serviceName" -ForegroundColor Green
        $consistentCount++
    } else {
        Write-Host "  [ERROR] $serviceName" -ForegroundColor Red
        Write-Host "    - optional:nacos: = $hasOptionalNacos" -ForegroundColor Gray
        Write-Host "    - config.enabled: true = $hasConfigEnabled" -ForegroundColor Gray
        Write-Host "    - import-check.enabled: true = $hasImportCheckEnabled" -ForegroundColor Gray
        $inconsistentCount++
        $allValid = $false
    }
}

Write-Host "  Summary: $consistentCount consistent, $inconsistentCount inconsistent" -ForegroundColor $(if ($inconsistentCount -eq 0) { "Green" } else { "Red" })

# Step 3: Verify Docker Compose
Write-Host "[3/4] Verifying Docker Compose environment variables..." -ForegroundColor Yellow
$dockerComposePath = "docker-compose-all.yml"
if (Test-Path $dockerComposePath) {
    $dockerContent = Get-Content $dockerComposePath -Raw -Encoding UTF8
    $importCount = ([regex]::Matches($dockerContent, "SPRING_CONFIG_IMPORT=optional:nacos:")).Count
    Write-Host "  Found $importCount SPRING_CONFIG_IMPORT entries" -ForegroundColor $(if ($importCount -eq 9) { "Green" } else { "Red" })
    
    if ($importCount -eq 9) {
        Write-Host "  [OK] All 9 services have SPRING_CONFIG_IMPORT configured" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Expected 9, found $importCount" -ForegroundColor Red
        $allValid = $false
    }
} else {
    Write-Host "  [ERROR] Docker Compose file not found" -ForegroundColor Red
    $allValid = $false
}

# Step 4: Check for old JAR files (warning only)
Write-Host "[4/4] Checking for old JAR files..." -ForegroundColor Yellow
$jarFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.jar" | Where-Object { $_.FullName -like "*\target\*.jar" -and $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" }
if ($jarFiles.Count -gt 0) {
    Write-Host "  [WARN] Found $($jarFiles.Count) JAR files - these need to be rebuilt" -ForegroundColor Yellow
    Write-Host "  Recommendation: Run complete-upgrade-to-2025.ps1 to rebuild" -ForegroundColor Yellow
} else {
    Write-Host "  [INFO] No JAR files found (need to build)" -ForegroundColor Gray
}

# Final summary
Write-Host ""
if ($allValid) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Configuration Verification: PASSED" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next Step: Execute upgrade script" -ForegroundColor Cyan
    Write-Host "  .\scripts\complete-upgrade-to-2025.ps1 -Clean -RebuildImages -SkipTests" -ForegroundColor White
    exit 0
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  Configuration Verification: FAILED" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please fix the errors above before proceeding" -ForegroundColor Yellow
    exit 1
}
