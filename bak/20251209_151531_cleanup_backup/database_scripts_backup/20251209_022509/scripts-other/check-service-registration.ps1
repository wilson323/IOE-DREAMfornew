# IOE-DREAM Service Registration Check Script
# Checks if all microservices are registered to Nacos

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Service Registration Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check container status
Write-Host "[1/5] Checking container status..." -ForegroundColor Yellow
$containers = docker ps -a --format "{{.Names}}\t{{.Status}}" | Where-Object { $_ -match "ioedream|nacos|mysql|redis" }
if ($containers) {
    $containers | ForEach-Object {
        if ($_ -match "Up") {
            Write-Host "  [OK] $_" -ForegroundColor Green
        } else {
            Write-Host "  [WARN] $_" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  [ERROR] No containers found" -ForegroundColor Red
}
Write-Host ""

# Step 2: Check Nacos status
Write-Host "[2/5] Checking Nacos status..." -ForegroundColor Yellow
$nacosLogs = docker logs ioedream-nacos --tail 10 2>&1
if ($nacosLogs -match "started successfully") {
    Write-Host "  [OK] Nacos is running" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Nacos may not be running properly" -ForegroundColor Red
    Write-Host "  Nacos logs:" -ForegroundColor Gray
    $nacosLogs | Select-Object -Last 5 | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
}
Write-Host ""

# Step 3: Check for dataId errors
Write-Host "[3/5] Checking for dataId errors..." -ForegroundColor Yellow
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

$dataIdErrorCount = 0
foreach ($service in $services) {
    $logs = docker logs $service --tail 100 2>&1
    if ($logs -match "dataId must be specified") {
        Write-Host "  [ERROR] $service has dataId error" -ForegroundColor Red
        $dataIdErrorCount++
    } elseif ($logs -match "Started.*Application") {
        Write-Host "  [OK] $service started successfully" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] $service status unknown" -ForegroundColor Yellow
    }
}

if ($dataIdErrorCount -eq 0) {
    Write-Host "  [OK] No dataId errors found" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Found $dataIdErrorCount services with dataId errors" -ForegroundColor Red
}
Write-Host ""

# Step 4: Check Nacos service registration
Write-Host "[4/5] Checking Nacos service registration..." -ForegroundColor Yellow
try {
    # Try to get service list from Nacos API
    $nacosResponse = Invoke-WebRequest -Uri "http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=100" -UseBasicParsing -ErrorAction SilentlyContinue
    if ($nacosResponse.StatusCode -eq 200) {
        $serviceList = $nacosResponse.Content | ConvertFrom-Json
        if ($serviceList.count -gt 0) {
            Write-Host "  [OK] Found $($serviceList.count) services registered in Nacos" -ForegroundColor Green
            $serviceList.doms | ForEach-Object { Write-Host "    - $_" -ForegroundColor Gray }
        } else {
            Write-Host "  [WARN] No services registered in Nacos" -ForegroundColor Yellow
        }
    }
} catch {
    Write-Host "  [ERROR] Cannot connect to Nacos API: $_" -ForegroundColor Red
    Write-Host "  [INFO] Make sure Nacos is running and accessible at http://localhost:8848" -ForegroundColor Gray
}
Write-Host ""

# Step 5: Check network connectivity
Write-Host "[5/5] Checking network connectivity..." -ForegroundColor Yellow
$testService = "ioedream-gateway-service"
$pingResult = docker exec $testService ping -c 2 nacos 2>&1
if ($pingResult -match "2 packets transmitted.*2 received") {
    Write-Host "  [OK] $testService can reach Nacos" -ForegroundColor Green
} else {
    Write-Host "  [WARN] $testService may not be able to reach Nacos" -ForegroundColor Yellow
    Write-Host "  Ping result: $pingResult" -ForegroundColor Gray
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($dataIdErrorCount -eq 0) {
    Write-Host "  [OK] No dataId errors detected" -ForegroundColor Green
    Write-Host "  [INFO] If services are not registered, they may still be starting" -ForegroundColor Yellow
    Write-Host "  [INFO] Wait a few minutes and check again" -ForegroundColor Yellow
} else {
    Write-Host "  [ERROR] Found dataId errors - services need to be rebuilt" -ForegroundColor Red
    Write-Host "  [ACTION] Run: docker-compose -f docker-compose-all.yml build --no-cache" -ForegroundColor Cyan
    Write-Host "  [ACTION] Then: docker-compose -f docker-compose-all.yml up -d" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "  1. Check Nacos console: http://localhost:8848/nacos (nacos/nacos)" -ForegroundColor White
Write-Host "  2. View service logs: docker logs <service-name>" -ForegroundColor White
Write-Host "  3. Restart services if needed: docker-compose -f docker-compose-all.yml restart" -ForegroundColor White
Write-Host ""
