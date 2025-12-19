#!/usr/bin/env pwsh

# Simple compilation test for IOE-DREAM services
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

Write-Host "=== IOE-DREAM 服务编译测试 ===" -ForegroundColor Cyan
Write-Host ""

foreach ($service in $services) {
    Write-Host "测试: $service" -ForegroundColor Yellow

    # Change to service directory and compile
    Push-Location $service

    $result = mvn compile -DskipTests -q 2>&1
    $exitCode = $LASTEXITCODE

    Pop-Location

    if ($exitCode -eq 0) {
        Write-Host "✅ $service - 编译成功" -ForegroundColor Green
        $success += $service
    } else {
        Write-Host "❌ $service - 编译失败" -ForegroundColor Red
        $failed += $service
        # Show first few lines of error
        $errorLines = $result -split "`n" | Select-Object -First 3
        foreach ($line in $errorLines) {
            Write-Host "   $line" -ForegroundColor Red
        }
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