# Spring Cloud Alibaba Upgrade Script to 2025.0.0.0
# Upgrade Date: 2025-12-08
# Target: Ensure global consistency, availability, complete functionality, dependency compatibility

param(
    [switch]$SkipTests = $false,
    [switch]$Clean = $false
)

$ErrorActionPreference = "Stop"
$script:StartTime = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Spring Cloud Alibaba Upgrade Script" -ForegroundColor Cyan
Write-Host "  Target Version: 2025.0.0.0" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Verify version configuration
Write-Host "[1/5] Verifying version configuration..." -ForegroundColor Yellow
$pomPath = "microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw -Encoding UTF8
    if ($pomContent -match 'spring-cloud-alibaba\.version>2025\.0\.0\.0') {
        Write-Host "  [OK] Parent POM version updated to 2025.0.0.0" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Parent POM version not updated, please check configuration" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  [ERROR] Parent POM file not found" -ForegroundColor Red
    exit 1
}

# Step 2: Clean Maven local cache (optional)
if ($Clean) {
    Write-Host "[2/5] Cleaning Maven local cache..." -ForegroundColor Yellow
    $nacosCachePath = "$env:USERPROFILE\.m2\repository\com\alibaba\cloud\spring-cloud-alibaba-dependencies"
    if (Test-Path $nacosCachePath) {
        Remove-Item -Recurse -Force "$nacosCachePath\2022.0.0.0" -ErrorAction SilentlyContinue
        Write-Host "  [OK] Old version cache cleaned" -ForegroundColor Green
    }
} else {
    Write-Host "[2/5] Skipping cache cleanup (use -Clean parameter to clean)" -ForegroundColor Gray
}

# Step 3: Maven build
Write-Host "[3/5] Maven build..." -ForegroundColor Yellow
Push-Location "microservices"
try {
    $mvnArgs = @("clean", "install")
    if ($SkipTests) {
        $mvnArgs += "-DskipTests"
    }
    
    Write-Host "  Executing: mvn $($mvnArgs -join ' ')" -ForegroundColor Gray
    $buildResult = & mvn $mvnArgs 2>&1
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Maven build successful" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Maven build failed" -ForegroundColor Red
        Write-Host $buildResult -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}

# Step 4: Docker image build
Write-Host "[4/5] Docker image build..." -ForegroundColor Yellow
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

$buildSuccessCount = 0
$buildFailCount = 0

foreach ($service in $services) {
    Write-Host "  Building: $service" -ForegroundColor Gray
    $servicePath = "microservices\$service"
    if (Test-Path $servicePath) {
        Push-Location $servicePath
        try {
            $imageName = $service.ToLower()
            $buildResult = docker build -t $imageName:latest . 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Host "    [OK] $service image built successfully" -ForegroundColor Green
                $buildSuccessCount++
            } else {
                Write-Host "    [ERROR] $service image build failed" -ForegroundColor Red
                Write-Host $buildResult -ForegroundColor Red
                $buildFailCount++
            }
        } finally {
            Pop-Location
        }
    } else {
        Write-Host "    [WARN] Service path not found: $servicePath" -ForegroundColor Yellow
        $buildFailCount++
    }
}

Write-Host "  Build Summary: $buildSuccessCount succeeded, $buildFailCount failed" -ForegroundColor $(if ($buildFailCount -eq 0) { "Green" } else { "Yellow" })

# Step 5: Verify configuration consistency
Write-Host "[5/5] Verifying configuration consistency..." -ForegroundColor Yellow
$configFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "application.yml" | Where-Object { $_.FullName -like "*\src\main\resources\application.yml" }
$allConsistent = $true
$consistentCount = 0
$inconsistentCount = 0

foreach ($configFile in $configFiles) {
    $content = Get-Content $configFile.FullName -Raw -Encoding UTF8
    if ($content -match 'optional:nacos:' -and $content -match 'enabled: true') {
        $serviceName = $configFile.Directory.Parent.Parent.Name
        Write-Host "  [OK] $serviceName configuration is correct" -ForegroundColor Green
        $consistentCount++
    } else {
        $serviceName = $configFile.Directory.Parent.Parent.Name
        Write-Host "  [WARN] $serviceName configuration may be inconsistent" -ForegroundColor Yellow
        $allConsistent = $false
        $inconsistentCount++
    }
}

Write-Host "  Configuration Summary: $consistentCount consistent, $inconsistentCount inconsistent" -ForegroundColor $(if ($inconsistentCount -eq 0) { "Green" } else { "Yellow" })

if ($allConsistent) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  Upgrade Completed Successfully!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Cyan
    Write-Host "  1. Start Docker Compose: docker-compose -f docker-compose-all.yml up -d" -ForegroundColor White
    Write-Host "  2. Check service status: docker-compose -f docker-compose-all.yml ps" -ForegroundColor White
    Write-Host "  3. View service logs: docker-compose -f docker-compose-all.yml logs -f [service-name]" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "  [WARN] Some configurations may be inconsistent, please check" -ForegroundColor Yellow
}

$elapsed = (Get-Date) - $script:StartTime
Write-Host ""
$totalSeconds = $elapsed.TotalSeconds.ToString('F2')
Write-Host "Total Time: $totalSeconds seconds" -ForegroundColor Gray
