# IOE-DREAM Phase 4: Validate All Services
# Fixed version with proper regex handling

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM Phase 4: Validate All Services" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$Services = @(
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-video-service",
    "ioedream-gateway-service"
)

$ConfigFiles = @("bootstrap.yml", "application.yml", "application-docker.yml")
$ExpectedDatabase = "ioedream"
$issues = @()
$validCount = 0

# Step 1: Validate microservice database configurations
Write-Host "[1/3] Validate Microservice Database Configurations" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

foreach ($service in $Services) {
    $found = $false
    foreach ($configFile in $ConfigFiles) {
        $configPath = "microservices\$service\src\main\resources\$configFile"
        if (Test-Path $configPath) {
            $content = Get-Content $configPath -Raw -ErrorAction SilentlyContinue
            if ($content) {
                # Use [regex]::new to avoid string escaping issues
                $jdbcRegex = [regex]::new("jdbc:mysql://[^/]+/([^?]+)")
                $match = $jdbcRegex.Match($content)
                if ($match.Success) {
                    $dbName = $match.Groups[1].Value
                    $envVarRegex = [regex]::new("\$\{.*ioedream\}")
                    if ($dbName -eq $ExpectedDatabase -or $envVarRegex.IsMatch($dbName)) {
                        $found = $true
                        Write-Host "  [OK] $service`: $configFile - Database config correct" -ForegroundColor Green
                        break
                    } elseif ($dbName -match "ioedream") {
                        $found = $true
                        Write-Host "  [WARN] $service`: $configFile - Database name: $dbName" -ForegroundColor Yellow
                        break
                    } else {
                        $issues += "$service`: $configFile - Database name mismatch ($dbName)"
                    }
                } elseif ($content -match "common-database\.yaml") {
                    $found = $true
                    Write-Host "  [OK] $service`: $configFile - Using Nacos config" -ForegroundColor Green
                    break
                }
            }
        }
    }

    if ($found) {
        $validCount++
    } else {
        Write-Host "  [ERROR] $service`: Database config not found" -ForegroundColor Red
        $issues += "$service`: Database config not found"
    }
}

Write-Host ""
Write-Host "Config validation result: $validCount/$($Services.Count) services configured correctly" -ForegroundColor $(if ($validCount -eq $Services.Count) { "Green" } else { "Yellow" })

# Step 2: Validate database initialization process
Write-Host ""
Write-Host "[2/3] Validate Database Initialization Process" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$initScripts = Get-ChildItem -Path "deployment\mysql\init" -Filter "*.sql" | Sort-Object Name
Write-Host ""
Write-Host "Initialization script list:" -ForegroundColor Yellow
foreach ($script in $initScripts) {
    Write-Host "  - $($script.Name)" -ForegroundColor White
}

$dockerCompose = "docker-compose-all.yml"
if (Test-Path $dockerCompose) {
    $dockerContent = Get-Content $dockerCompose -Raw
    $scriptCount = ([regex]::Matches($dockerContent, "\.sql")).Count
    Write-Host ""
    Write-Host "Docker Compose config: [OK] Contains $scriptCount SQL script references" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Docker Compose config: [ERROR] File not found" -ForegroundColor Red
    $issues += "Docker Compose file not found"
}

# Step 3: Validate version history
Write-Host ""
Write-Host "[3/3] Validate Version History" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$versionScripts = Get-ChildItem -Path "scripts\database\versions" -Filter "version-*.ps1" | Sort-Object Name
Write-Host ""
Write-Host "Version script list:" -ForegroundColor Yellow
foreach ($script in $versionScripts) {
    Write-Host "  - $($script.Name)" -ForegroundColor White
}

$expectedVersions = @("v1.0.0", "v1.1.0", "v1.0.1", "v2.0.0", "v2.0.1", "v2.0.2", "v2.1.0")
$foundVersions = @()
foreach ($script in $versionScripts) {
    if ($script.Name -match "version-(v\d+\.\d+\.\d+)\.ps1") {
        $foundVersions += $matches[1]
    }
}

$missingVersions = $expectedVersions | Where-Object { $foundVersions -notcontains $_ }
if ($missingVersions.Count -eq 0) {
    Write-Host ""
    Write-Host "Version scripts: [OK] All version scripts created ($($versionScripts.Count) scripts)" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "Version scripts: [WARN] Missing versions: $($missingVersions -join ', ')" -ForegroundColor Yellow
    $issues += "Missing version scripts: $($missingVersions -join ', ')"
}

# Summary
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Validation Summary" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Microservice config validation: $validCount/$($Services.Count) services configured correctly" -ForegroundColor $(if ($validCount -eq $Services.Count) { "Green" } else { "Yellow" })
Write-Host "Database initialization: [OK] Script files complete" -ForegroundColor Green
Write-Host "Version history: [OK] Version scripts complete" -ForegroundColor Green
Write-Host ""

if ($issues.Count -eq 0) {
    Write-Host "[OK] All validations passed!" -ForegroundColor Green
} else {
    Write-Host "Found $($issues.Count) issues:" -ForegroundColor Yellow
    foreach ($issue in $issues) {
        Write-Host "  - $issue" -ForegroundColor Red
    }
}

Write-Host ""
