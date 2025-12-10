# 批量修复Sleuth依赖问题
# 方案：直接移除子模块中的Sleuth依赖声明，因为这些依赖可能不需要

$ErrorActionPreference = "Stop"

$services = @(
    "ioedream-config-service",
    "ioedream-gateway-service",
    "ioedream-auth-service",
    "ioedream-identity-service",
    "ioedream-audit-service",
    "ioedream-device-service",
    "ioedream-system-service",
    "ioedream-enterprise-service",
    "ioedream-access-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-video-service",
    "ioedream-notification-service",
    "ioedream-infrastructure-service",
    "ioedream-monitor-service",
    "ioedream-report-service",
    "ioedream-scheduler-service"
)

Write-Host "===== Fix Sleuth Dependencies =====" -ForegroundColor Cyan
Write-Host "Strategy: Comment out Sleuth dependencies (can be re-enabled later)`n" -ForegroundColor Yellow

$fixedCount = 0

foreach ($serviceName in $services) {
    $pomPath = "D:\IOE-DREAM\microservices\$serviceName\pom.xml"

    if (-not (Test-Path $pomPath)) {
        Write-Host "Skip: $serviceName (pom.xml not found)" -ForegroundColor DarkGray
        continue
    }

    try {
        Write-Host "Processing: $serviceName" -ForegroundColor Gray

        $content = Get-Content $pomPath -Raw -Encoding UTF8
        $originalContent = $content

        # 注释掉Sleuth依赖
        $content = $content -replace '(?ms)(\s*<dependency>\s*<groupId>org\.springframework\.cloud</groupId>\s*<artifactId>spring-cloud-starter-sleuth</artifactId>\s*</dependency>)', '<!-- $1 -->'
        $content = $content -replace '(?ms)(\s*<dependency>\s*<groupId>org\.springframework\.cloud</groupId>\s*<artifactId>spring-cloud-sleuth-zipkin</artifactId>\s*</dependency>)', '<!-- $1 -->'
        $content = $content -replace '(?ms)(\s*<dependency>\s*<groupId>io\.micrometer</groupId>\s*<artifactId>micrometer-tracing-bridge-brave</artifactId>\s*</dependency>)', '<!-- $1 -->'
        $content = $content -replace '(?ms)(\s*<dependency>\s*<groupId>io\.zipkin\.reporter2</groupId>\s*<artifactId>zipkin-reporter-brave</artifactId>\s*</dependency>)', '<!-- $1 -->'

        if ($originalContent -ne $content) {
            Set-Content -Path $pomPath -Value $content -Encoding UTF8 -NoNewline
            Write-Host "  Fixed - Commented out Sleuth dependencies" -ForegroundColor Green
            $fixedCount++
        } else {
            Write-Host "  No Sleuth dependencies found" -ForegroundColor DarkGray
        }

    } catch {
        Write-Host "  Failed: $_" -ForegroundColor Red
    }
}

Write-Host "`n===== Fix Complete =====" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount services" -ForegroundColor Green
Write-Host "`nNote: Sleuth dependencies are commented out." -ForegroundColor Yellow
Write-Host "To re-enable distributed tracing, uncomment and add proper configuration." -ForegroundColor Yellow

