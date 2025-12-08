# Complete Spring Cloud Alibaba Upgrade to 2025.0.0.0 Script
# This script performs a complete upgrade including:
# 1. Clean Maven cache
# 2. Rebuild all JARs with new dependencies
# 3. Rebuild Docker images
# 4. Restart services

param(
    [switch]$SkipTests = $false,
    [switch]$Clean = $true,
    [switch]$RebuildImages = $true
)

$ErrorActionPreference = "Stop"
$script:StartTime = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Complete Spring Cloud Alibaba Upgrade" -ForegroundColor Cyan
Write-Host "  Target Version: 2025.0.0.0" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Verify version configuration
Write-Host "[1/6] Verifying version configuration..." -ForegroundColor Yellow
$pomPath = "microservices\pom.xml"
if (Test-Path $pomPath) {
    $pomContent = Get-Content $pomPath -Raw -Encoding UTF8
    if ($pomContent -match 'spring-cloud-alibaba\.version>2025\.0\.0\.0') {
        Write-Host "  [OK] Parent POM version: 2025.0.0.0" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Parent POM version not updated to 2025.0.0.0" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "  [ERROR] Parent POM file not found" -ForegroundColor Red
    exit 1
}

# Step 2: Clean Maven local cache
if ($Clean) {
    Write-Host "[2/6] Cleaning Maven local cache..." -ForegroundColor Yellow
    $nacosCachePath = "$env:USERPROFILE\.m2\repository\com\alibaba\cloud\spring-cloud-alibaba-dependencies"
    if (Test-Path $nacosCachePath) {
        $oldVersions = Get-ChildItem $nacosCachePath -Directory | Where-Object { $_.Name -ne "2025.0.0.0" }
        foreach ($oldVersion in $oldVersions) {
            Write-Host "  Removing old version: $($oldVersion.Name)" -ForegroundColor Gray
            Remove-Item -Recurse -Force $oldVersion.FullName -ErrorAction SilentlyContinue
        }
        Write-Host "  [OK] Maven cache cleaned" -ForegroundColor Green
    } else {
        Write-Host "  [INFO] No cache found, skipping" -ForegroundColor Gray
    }
} else {
    Write-Host "[2/6] Skipping cache cleanup (use -Clean to clean)" -ForegroundColor Gray
}

# Step 3: Stop Docker services
Write-Host "[3/6] Stopping Docker services..." -ForegroundColor Yellow
try {
    docker-compose -f docker-compose-all.yml down 2>&1 | Out-Null
    Write-Host "  [OK] Docker services stopped" -ForegroundColor Green
} catch {
    Write-Host "  [WARN] Some services may not have been running" -ForegroundColor Yellow
}

# Step 4: Maven build (must build common first)
Write-Host "[4/6] Maven build..." -ForegroundColor Yellow
Push-Location "microservices"
try {
    # Step 4.1: Build microservices-common first (required)
    Write-Host "  Building microservices-common (required first)..." -ForegroundColor Gray
    $mvnArgs = @("clean", "install", "-pl", "microservices-common", "-am")
    if ($SkipTests) {
        $mvnArgs += "-DskipTests"
    }
    
    $buildResult = & mvn $mvnArgs 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "    [OK] microservices-common built successfully" -ForegroundColor Green
    } else {
        Write-Host "    [ERROR] microservices-common build failed" -ForegroundColor Red
        Write-Host $buildResult -ForegroundColor Red
        exit 1
    }
    
    # Step 4.2: Build all services
    Write-Host "  Building all microservices..." -ForegroundColor Gray
    $mvnArgs = @("clean", "install")
    if ($SkipTests) {
        $mvnArgs += "-DskipTests"
    }
    
    $buildResult = & mvn $mvnArgs 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  [OK] All microservices built successfully" -ForegroundColor Green
    } else {
        Write-Host "  [ERROR] Maven build failed" -ForegroundColor Red
        Write-Host $buildResult -ForegroundColor Red
        exit 1
    }
} finally {
    Pop-Location
}

# Step 5: Docker image build
if ($RebuildImages) {
    Write-Host "[5/6] Rebuilding Docker images..." -ForegroundColor Yellow
    Write-Host "  Using docker-compose build (more reliable than individual docker build commands)" -ForegroundColor Gray
    
    # Use docker-compose build which handles build context and Dockerfile paths automatically
    $composeFile = "docker-compose-all.yml"
    if (Test-Path $composeFile) {
        Write-Host "  Building all services from docker-compose-all.yml..." -ForegroundColor Gray
        Write-Host "  This may take 10-20 minutes depending on your system..." -ForegroundColor DarkGray
        
        # Disable buildx to avoid compatibility issues
        $env:DOCKER_BUILDKIT = "0"
        $env:COMPOSE_DOCKER_CLI_BUILD = "0"
        
        try {
            # Build all services defined in docker-compose file
            Write-Host "  Executing: docker-compose -f $composeFile build --no-cache" -ForegroundColor DarkGray
            Write-Host "  Note: This will build all 9 microservices. It may take 15-30 minutes." -ForegroundColor DarkGray
            Write-Host "  You can monitor progress in another terminal with:" -ForegroundColor DarkGray
            Write-Host "    docker ps -a" -ForegroundColor DarkGray
            Write-Host ""
            
            # Execute docker-compose build directly (let it output to console for progress)
            # Remove --pull flag as it's not necessary and may cause issues
            & docker-compose -f $composeFile build --no-cache
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  [OK] All Docker images built successfully" -ForegroundColor Green
                Write-Host "  Verifying images..." -ForegroundColor Gray
                $imageCount = (docker images --format "{{.Repository}}" | Select-String -Pattern "ioedream" | Measure-Object).Count
                Write-Host "  Found $imageCount IOE-DREAM images" -ForegroundColor $(if ($imageCount -ge 9) { "Green" } else { "Yellow" })
            } else {
                Write-Host "  [ERROR] Docker image build failed (exit code: $LASTEXITCODE)" -ForegroundColor Red
                Write-Host "  [INFO] You can try building individual services:" -ForegroundColor Yellow
                Write-Host "    docker-compose -f $composeFile build --no-cache gateway-service" -ForegroundColor Gray
                Write-Host "    docker-compose -f $composeFile build --no-cache common-service" -ForegroundColor Gray
            }
        } catch {
            Write-Host "  [ERROR] Exception during docker-compose build: $_" -ForegroundColor Red
            Write-Host "  Exception details: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "  [INFO] Try running manually:" -ForegroundColor Yellow
            Write-Host "    docker-compose -f $composeFile build --no-cache" -ForegroundColor Gray
        } finally {
            # Clean up environment variables
            if ($env:DOCKER_BUILDKIT) {
                Remove-Item Env:\DOCKER_BUILDKIT
            }
            if ($env:COMPOSE_DOCKER_CLI_BUILD) {
                Remove-Item Env:\COMPOSE_DOCKER_CLI_BUILD
            }
        }
    } else {
        Write-Host "  [ERROR] docker-compose-all.yml not found" -ForegroundColor Red
        Write-Host "  [INFO] Falling back to individual docker build commands..." -ForegroundColor Yellow
        
        # Fallback: individual builds (simplified)
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
            $dockerfilePath = Join-Path $servicePath "Dockerfile"
            
            if (-not (Test-Path $dockerfilePath)) {
                Write-Host "    [WARN] Dockerfile not found: $dockerfilePath" -ForegroundColor Yellow
                $buildFailCount++
                continue
            }
            
            $imageName = $service.ToLower()
            $dockerfileRelative = $dockerfilePath.Replace((Get-Location).Path + "\", "").Replace("\", "/")
            
            $env:DOCKER_BUILDKIT = "0"
            $buildResult = & docker build -f $dockerfileRelative -t ${imageName}:latest . 2>&1
            
            if ($LASTEXITCODE -eq 0) {
                Write-Host "    [OK] $service image built" -ForegroundColor Green
                $buildSuccessCount++
            } else {
                Write-Host "    [ERROR] $service build failed" -ForegroundColor Red
                $buildFailCount++
            }
            
            if ($env:DOCKER_BUILDKIT) { Remove-Item Env:\DOCKER_BUILDKIT }
        }
        
        Write-Host "  Build Summary: $buildSuccessCount succeeded, $buildFailCount failed" -ForegroundColor $(if ($buildFailCount -eq 0) { "Green" } else { "Yellow" })
    }
} else {
    Write-Host "[5/6] Skipping Docker image rebuild (use -RebuildImages to rebuild)" -ForegroundColor Gray
}

# Step 6: Verify configuration consistency
Write-Host "[6/6] Verifying configuration consistency..." -ForegroundColor Yellow
$configFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "application.yml" | Where-Object { $_.FullName -like "*\src\main\resources\application.yml" }
$allConsistent = $true
$consistentCount = 0
$inconsistentCount = 0

foreach ($configFile in $configFiles) {
    $content = Get-Content $configFile.FullName -Raw -Encoding UTF8
    $serviceName = $configFile.Directory.Parent.Parent.Name
    
    $hasOptionalNacos = $content -match 'optional:nacos:'
    $hasEnabledTrue = $content -match 'enabled:\s*true.*升级到2025'
    
    if ($hasOptionalNacos -and $hasEnabledTrue) {
        Write-Host "  [OK] $serviceName configuration is correct" -ForegroundColor Green
        $consistentCount++
    } else {
        Write-Host "  [WARN] $serviceName configuration may be inconsistent" -ForegroundColor Yellow
        $allConsistent = $false
        $inconsistentCount++
    }
}

Write-Host "  Configuration Summary: $consistentCount consistent, $inconsistentCount inconsistent" -ForegroundColor $(if ($inconsistentCount -eq 0) { "Green" } else { "Yellow" })

# Final summary
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Upgrade Preparation Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "  1. Start Docker Compose: docker-compose -f docker-compose-all.yml up -d" -ForegroundColor White
Write-Host "  2. Check service status: docker-compose -f docker-compose-all.yml ps" -ForegroundColor White
Write-Host "  3. View service logs: docker-compose -f docker-compose-all.yml logs -f [service-name]" -ForegroundColor White
Write-Host "  4. Verify no 'dataId must be specified' errors in logs" -ForegroundColor White

if (-not $allConsistent) {
    Write-Host ""
    Write-Host "  [WARN] Some configurations may be inconsistent, please check" -ForegroundColor Yellow
}

$elapsed = (Get-Date) - $script:StartTime
Write-Host ""
$totalSeconds = $elapsed.TotalSeconds.ToString('F2')
Write-Host "Total Time: $totalSeconds seconds" -ForegroundColor Gray
