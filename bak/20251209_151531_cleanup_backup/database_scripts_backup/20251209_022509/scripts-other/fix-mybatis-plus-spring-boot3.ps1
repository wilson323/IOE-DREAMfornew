# MyBatis-Plus Spring Boot 3.x 兼容性修复脚本
# 重新构建项目以应用修复

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MyBatis-Plus Spring Boot 3.x 修复" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查当前目录
if (-not (Test-Path "microservices")) {
    Write-Host "❌ 错误: 请在项目根目录执行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host "[1] 验证依赖修复..." -ForegroundColor Yellow
$oldStarter = Select-String -Path "microservices\*\pom.xml" -Pattern "mybatis-plus-boot-starter" -ErrorAction SilentlyContinue
if ($oldStarter) {
    Write-Host "  ❌ 仍有文件使用旧的starter" -ForegroundColor Red
    $oldStarter | ForEach-Object { Write-Host "    $($_.Path):$($_.LineNumber)" -ForegroundColor Red }
    exit 1
} else {
    Write-Host "  ✅ 所有文件已使用 mybatis-plus-spring-boot3-starter" -ForegroundColor Green
}

Write-Host "`n[2] 清理Maven缓存..." -ForegroundColor Yellow
mvn clean -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ⚠️  Maven清理失败，继续执行..." -ForegroundColor Yellow
} else {
    Write-Host "  ✅ Maven清理完成" -ForegroundColor Green
}

Write-Host "`n[3] 重新构建microservices-common..." -ForegroundColor Yellow
Write-Host "  这是关键步骤，必须先完成" -ForegroundColor Gray
Set-Location microservices/microservices-common
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ microservices-common构建失败" -ForegroundColor Red
    Set-Location ../..
    exit 1
}
Write-Host "  ✅ microservices-common构建成功" -ForegroundColor Green
Set-Location ../..

Write-Host "`n[4] 重新构建所有微服务..." -ForegroundColor Yellow
Write-Host "  这可能需要几分钟时间..." -ForegroundColor Gray
mvn clean install -DskipTests -pl microservices/ioedream-gateway-service,microservices/ioedream-common-service,microservices/ioedream-device-comm-service,microservices/ioedream-oa-service,microservices/ioedream-access-service,microservices/ioedream-attendance-service,microservices/ioedream-consume-service,microservices/ioedream-visitor-service,microservices/ioedream-video-service -am
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ 微服务构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ 所有微服务构建成功" -ForegroundColor Green

Write-Host "`n[5] 重新构建Docker镜像..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml build --no-cache
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Docker镜像构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ Docker镜像构建成功" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "  1. 启动服务: docker-compose -f docker-compose-all.yml up -d" -ForegroundColor Gray
Write-Host "  2. 检查日志: docker logs ioedream-attendance-service --tail 50" -ForegroundColor Gray
Write-Host "  3. 验证无错误: 确认不再出现 'Invalid bean definition' 错误" -ForegroundColor Gray
