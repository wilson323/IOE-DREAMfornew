#!/usr/bin/env pwsh

# Test compilation of all microservices
$services = @(
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-oa-service",
    "ioedream-device-comm-service",
    "ioedream-common-service",
    "ioedream-biometric-service",
    "ioedream-database-service"
)

$success = @()
$failed = @()

Write-Host "开始测试所有微服务的编译状态..." -ForegroundColor Cyan
Write-Host ""

foreach ($service in $services) {
    Write-Host "正在编译: $service" -ForegroundColor Yellow

    $result = mvn compile -pl $service -DskipTests -q

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ $service - 编译成功" -ForegroundColor Green
        $success += $service
    } else {
        Write-Host "❌ $service - 编译失败" -ForegroundColor Red
        $failed += $service
    }

    Write-Host ""
}

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "编译结果统计:" -ForegroundColor Cyan
Write-Host "成功: $($success.Count) 个服务" -ForegroundColor Green
Write-Host "失败: $($failed.Count) 个服务" -ForegroundColor Red
Write-Host ""

if ($success.Count -gt 0) {
    Write-Host "编译成功的服务:" -ForegroundColor Green
    $success | ForEach-Object { Write-Host "  ✅ $_" -ForegroundColor Green }
}

if ($failed.Count -gt 0) {
    Write-Host "编译失败的服务:" -ForegroundColor Red
    $failed | ForEach-Object { Write-Host "  ❌ $_" -ForegroundColor Red }
}

Write-Host ""
Write-Host "编译测试完成!" -ForegroundColor Cyan