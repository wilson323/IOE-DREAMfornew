# =====================================================
# 批量更新所有微服务的默认配置脚本
# 用途: 统一更新所有服务的MySQL、Redis、Nacos默认配置
# 创建时间: 2025-12-14
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "批量更新所有微服务的默认配置" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 获取项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesPath = Join-Path $projectRoot "microservices"

# 需要更新的服务列表
$services = @(
    "ioedream-attendance-service",
    "ioedream-visitor-service",
    "ioedream-consume-service",
    "ioedream-access-service",
    "ioedream-video-service",
    "ioedream-oa-service",
    "ioedream-device-comm-service",
    "ioedream-common-service",
    "ioedream-gateway-service",
    "ioedream-database-service"
)

$updatedCount = 0
$errorCount = 0

foreach ($service in $services) {
    $configPath = Join-Path $microservicesPath "$service\src\main\resources\application.yml"

    if (-not (Test-Path $configPath)) {
        Write-Host "[SKIP] 配置文件不存在: $configPath" -ForegroundColor Yellow
        continue
    }

    try {
        Write-Host "[INFO] 正在更新: $service" -ForegroundColor Yellow

        # 读取配置文件
        $content = Get-Content $configPath -Raw -Encoding UTF8
        $originalContent = $content

        # 1. 更新MySQL密码默认值
        $mysqlPasswordPattern = [regex]::new('password:\s*\$\{MYSQL_PASSWORD:\}')
        if ($mysqlPasswordPattern.IsMatch($content)) {
            $content = $mysqlPasswordPattern.Replace($content, 'password: ${MYSQL_PASSWORD:123456}')
            Write-Host "  [FIX] MySQL密码默认值已更新" -ForegroundColor Gray
        }

        # 2. 更新Redis密码默认值
        $redisPasswordPattern = [regex]::new('password:\s*\$\{REDIS_PASSWORD:\}')
        if ($redisPasswordPattern.IsMatch($content)) {
            $content = $redisPasswordPattern.Replace($content, 'password: ${REDIS_PASSWORD:redis123}')
            Write-Host "  [FIX] Redis密码默认值已更新" -ForegroundColor Gray
        }

        # 3. 更新Nacos密码默认值（discovery和config）
        $nacosPasswordPattern = [regex]::new('password:\s*\$\{NACOS_PASSWORD:\}')
        if ($nacosPasswordPattern.IsMatch($content)) {
            $content = $nacosPasswordPattern.Replace($content, 'password: ${NACOS_PASSWORD:nacos}')
            Write-Host "  [FIX] Nacos密码默认值已更新" -ForegroundColor Gray
        }

        # 5. 更新Redisson配置中的Redis密码（如果存在）
        $redissonPasswordPattern = [regex]::new('password:\s*\$\{REDIS_PASSWORD:\}')
        if ($redissonPasswordPattern.IsMatch($content)) {
            $content = $redissonPasswordPattern.Replace($content, 'password: ${REDIS_PASSWORD:redis123}')
            Write-Host "  [FIX] Redisson Redis密码默认值已更新" -ForegroundColor Gray
        }

        # 如果内容有变化，保存文件
        if ($content -ne $originalContent) {
            $content | Set-Content $configPath -Encoding UTF8 -NoNewline
            Write-Host "[SUCCESS] $service - 配置已更新" -ForegroundColor Green
            $updatedCount++
        } else {
            Write-Host "[SKIP] $service - 配置已是最新" -ForegroundColor Gray
        }

    } catch {
        Write-Host "[ERROR] $service - 更新失败: $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "更新完成统计" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Updated: $updatedCount services" -ForegroundColor Green
Write-Host "  Failed: $errorCount services" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Gray" })
Write-Host "  Skipped: $($services.Count - $updatedCount - $errorCount) services" -ForegroundColor Gray
Write-Host ""

if ($errorCount -eq 0) {
    Write-Host "[SUCCESS] All services default config updated!" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Some services failed, please check errors" -ForegroundColor Yellow
}
