# Rebuild All Services After DataId Fix
# This script rebuilds all services after fixing the dataId configuration issue

param(
    [switch]$SkipTests = $true,
    [switch]$RebuildImages = $false
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Rebuild After DataId Fix" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stop Docker services
Write-Host "[1/4] Stopping Docker services..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down
Write-Host "  [OK] Docker services stopped" -ForegroundColor Green
Write-Host ""

# Step 2: Maven build
Write-Host "[2/4] Building Maven projects..." -ForegroundColor Yellow
Write-Host "  Building from: microservices directory" -ForegroundColor Gray

Push-Location microservices
try {
    # Build common first (required dependency)
    Write-Host "  Building microservices-common..." -ForegroundColor Gray
    mvn clean install -pl microservices-common -am -DskipTests:$SkipTests
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [ERROR] microservices-common build failed" -ForegroundColor Red
        exit 1
    }
    Write-Host "  [OK] microservices-common built successfully" -ForegroundColor Green
    
    # Build all services
    Write-Host "  Building all microservices..." -ForegroundColor Gray
    mvn clean install -DskipTests:$SkipTests
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "  [ERROR] Maven build failed" -ForegroundColor Red
        exit 1
    }
    Write-Host "  [OK] All microservices built successfully" -ForegroundColor Green
} finally {
    Pop-Location
}
Write-Host ""

# Step 3: Rebuild Docker images (optional)
if ($RebuildImages) {
    Write-Host "[3/4] Rebuilding Docker images..." -ForegroundColor Yellow
    docker-compose -f docker-compose-all.yml build --no-cache
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] Docker images rebuilt successfully" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Docker image build failed" -ForegroundColor Red
    }
    Write-Host ""
} else {
    Write-Host "[3/4] Skipping Docker image rebuild (use -RebuildImages to rebuild)" -ForegroundColor Gray
    Write-Host ""
}

# Step 4: Start services
Write-Host "[4/4] Starting Docker services..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml up -d
Write-Host "  [OK] Services started" -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Rebuild Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Wait 30 seconds for services to start" -ForegroundColor White
Write-Host "  2. Check logs: docker-compose -f docker-compose-all.yml logs | findstr dataId" -ForegroundColor White
Write-Host "  3. Check Nacos: http://localhost:8848/nacos" -ForegroundColor White
Write-Host ""
