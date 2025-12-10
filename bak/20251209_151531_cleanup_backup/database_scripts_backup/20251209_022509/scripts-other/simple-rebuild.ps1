# 简单重建脚本 - 清理缓存并重建

$ErrorActionPreference = "Stop"

Write-Host "清理Docker构建缓存..." -ForegroundColor Yellow
docker builder prune -af

Write-Host "停止所有容器..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down -v

Write-Host "强制重新构建（不使用缓存，拉取最新基础镜像）..." -ForegroundColor Yellow
Write-Host "这可能需要10-30分钟，请耐心等待..." -ForegroundColor Cyan
Write-Host ""

docker-compose -f docker-compose-all.yml build --no-cache --pull

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ 构建成功！" -ForegroundColor Green
    Write-Host ""
    Write-Host "启动服务..." -ForegroundColor Yellow
    docker-compose -f docker-compose-all.yml up -d
} else {
    Write-Host ""
    Write-Host "❌ 构建失败！" -ForegroundColor Red
    Write-Host ""
    Write-Host "如果仍然看到pom-temp.xml错误，请检查Dockerfile是否使用V5方案:" -ForegroundColor Yellow
    Write-Host "  Get-Content microservices\ioedream-gateway-service\Dockerfile | Select-String -Pattern pom-original" -ForegroundColor White
    exit 1
}
