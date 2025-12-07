# 验证所有Dockerfile是否使用V5方案

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Dockerfile V5方案验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allFixed = $true
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

foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    if (Test-Path $dockerfilePath) {
        $content = Get-Content $dockerfilePath -Raw
        
        $hasBackup = $content -match "cp pom\.xml pom-original\.xml"
        $hasReplace = $content -match "awk.*pom-original\.xml > pom\.xml"
        $hasInstall = $content -match "mvn install:install-file -Dfile=pom\.xml"
        $hasTemp = $content -match "pom-temp\.xml"
        $hasPython = $content -match "python3"
        
        if ($hasBackup -and $hasReplace -and $hasInstall -and -not $hasTemp -and -not $hasPython) {
            Write-Host "  ✅ $service - V5方案正确" -ForegroundColor Green
        } else {
            Write-Host "  ❌ $service - 需要修复" -ForegroundColor Red
            if (-not $hasBackup) {
                Write-Host "      - 缺少: cp pom.xml pom-original.xml" -ForegroundColor Yellow
            }
            if (-not $hasReplace) {
                Write-Host "      - 缺少: awk ... pom-original.xml > pom.xml" -ForegroundColor Yellow
            }
            if (-not $hasInstall) {
                Write-Host "      - 缺少: mvn install:install-file -Dfile=pom.xml" -ForegroundColor Yellow
            }
            if ($hasTemp) {
                Write-Host "      - 错误: 仍使用pom-temp.xml (V4方案)" -ForegroundColor Red
            }
            if ($hasPython) {
                Write-Host "      - 错误: 仍使用python3 (V3方案)" -ForegroundColor Red
            }
            $allFixed = $false
        }
    } else {
        Write-Host "  ❌ $service - Dockerfile不存在" -ForegroundColor Red
        $allFixed = $false
    }
}

Write-Host ""

if ($allFixed) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 所有Dockerfile使用V5方案" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "如果Docker构建仍然失败，请清理Docker缓存:" -ForegroundColor Yellow
    Write-Host "  docker builder prune -af" -ForegroundColor White
    Write-Host "  docker-compose -f docker-compose-all.yml build --no-cache --pull" -ForegroundColor White
    exit 0
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 部分Dockerfile需要修复" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    exit 1
}
