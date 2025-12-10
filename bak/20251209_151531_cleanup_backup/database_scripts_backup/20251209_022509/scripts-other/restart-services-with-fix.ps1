# 重启服务以应用Spring Config Import环境变量修复

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  应用Spring Config Import修复" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查当前目录
if (-not (Test-Path "docker-compose-all.yml")) {
    Write-Host "❌ 错误: 请在项目根目录执行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "[1] 停止所有服务..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ⚠️  停止服务时出现警告，继续执行..." -ForegroundColor Yellow
} else {
    Write-Host "  ✅ 所有服务已停止" -ForegroundColor Green
}

Write-Host "`n[2] 验证环境变量配置..." -ForegroundColor Yellow
$missingServices = @()
$services = @("gateway-service", "common-service", "device-comm-service", "oa-service", 
              "access-service", "attendance-service", "video-service", "consume-service", "visitor-service")

foreach ($service in $services) {
    $hasConfig = Select-String -Path "docker-compose-all.yml" -Pattern "$service:" -Context 0, 10 | Select-String -Pattern "SPRING_CONFIG_IMPORT"
    if (-not $hasConfig) {
        $missingServices += $service
    }
}

if ($missingServices.Count -gt 0) {
    Write-Host "  ❌ 以下服务缺少SPRING_CONFIG_IMPORT配置:" -ForegroundColor Red
    $missingServices | ForEach-Object { Write-Host "    - $_" -ForegroundColor Red }
    exit 1
} else {
    Write-Host "  ✅ 所有9个微服务都已配置SPRING_CONFIG_IMPORT" -ForegroundColor Green
}

Write-Host "`n[3] 启动所有服务..." -ForegroundColor Yellow
Write-Host "  这可能需要几分钟时间..." -ForegroundColor Gray
docker-compose -f docker-compose-all.yml up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ 服务启动失败" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ 服务启动命令已执行" -ForegroundColor Green

Write-Host "`n[4] 等待服务启动..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

Write-Host "`n[5] 检查服务状态..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml ps

Write-Host "`n[6] 检查关键服务日志（最近20行）..." -ForegroundColor Yellow
$testServices = @("ioedream-attendance-service", "ioedream-common-service", "ioedream-gateway-service")
foreach ($service in $testServices) {
    Write-Host "`n  === $service ===" -ForegroundColor Cyan
    docker logs $service --tail 20 2>&1 | Select-String -Pattern "No spring.config.import|APPLICATION FAILED|Started|started" | Select-Object -First 3
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  修复应用完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "验证步骤：" -ForegroundColor Yellow
Write-Host "  1. 检查服务状态: docker-compose -f docker-compose-all.yml ps" -ForegroundColor Gray
Write-Host "  2. 查看详细日志: docker logs ioedream-attendance-service --tail 50" -ForegroundColor Gray
Write-Host "  3. 确认无错误: 不应该再出现 'No spring.config.import property has been defined'" -ForegroundColor Gray
Write-Host ""
Write-Host "⚠️  注意: 这是临时修复，建议尽快重新构建项目以永久修复" -ForegroundColor Yellow
