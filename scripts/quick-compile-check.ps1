# 快速编译检查脚本
# 目的: 快速检查所有微服务的编译状态

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "快速编译检查" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

# 微服务列表
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-database-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-biometric-service"
)

$successCount = 0
$failedCount = 0
$totalServices = $services.Count

Write-Host "开始检查 $totalServices 个服务的编译状态..." -ForegroundColor White

foreach ($serviceName in $services) {
    $servicePath = "D:/IOE-DREAM/microservices/$serviceName"

    Write-Host -NoNewline "检查 $serviceName ... " -ForegroundColor Gray

    if (-not (Test-Path $servicePath)) {
        Write-Host "❌ 目录不存在" -ForegroundColor Red
        $failedCount++
        continue
    }

    try {
        # 设置工作目录并编译
        Push-Location $servicePath
        $result = mvn clean compile -q 2>&1
        $exitCode = $LASTEXITCODE
        Pop-Location

        if ($exitCode -eq 0 -and $result -match "BUILD SUCCESS") {
            Write-Host "✅ 成功" -ForegroundColor Green
            $successCount++
        }
        else {
            # 计算错误数量
            $errorLines = $result -split "`n" | Where-Object { $_ -match "ERROR" }
            $errorCount = $errorLines.Count
            Write-Host "❌ 失败 ($errorCount 错误)" -ForegroundColor Red
            $failedCount++
        }
    }
    catch {
        Write-Host "💥 异常 $($_.Exception.Message)" -ForegroundColor Red
        $failedCount++
        Pop-Location -ErrorAction SilentlyContinue
    }
}

Write-Host "`n====================================" -ForegroundColor Cyan
Write-Host "编译检查完成" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

Write-Host "服务总数: $totalServices" -ForegroundColor White
Write-Host "成功: $successCount" -ForegroundColor Green
Write-Host "失败: $failedCount" -ForegroundColor Red

if ($failedCount -eq 0) {
    Write-Host "🎉 所有服务编译成功！" -ForegroundColor Green
}
else {
    Write-Host "⚠️  有 $failedCount 个服务编译失败" -ForegroundColor Yellow
}

exit 0