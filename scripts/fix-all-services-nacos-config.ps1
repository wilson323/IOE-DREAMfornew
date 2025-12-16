# =====================================================
# 批量修复所有服务的Nacos配置脚本
# 用途: 修复所有微服务的Nacos密码配置
# 创建时间: 2025-12-14
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "批量修复所有服务的Nacos配置" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 获取项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesPath = Join-Path $projectRoot "microservices"

# 需要修复的服务列表
$services = @(
    "ioedream-attendance-service",
    "ioedream-visitor-service",
    "ioedream-consume-service",
    "ioedream-access-service",
    "ioedream-video-service",
    "ioedream-oa-service",
    "ioedream-device-comm-service",
    "ioedream-common-service",
    "ioedream-gateway-service"
)

$fixedCount = 0
$errorCount = 0

foreach ($service in $services) {
    $configPath = Join-Path $microservicesPath "$service\src\main\resources\application.yml"

    if (-not (Test-Path $configPath)) {
        Write-Host "[SKIP] 配置文件不存在: $configPath" -ForegroundColor Yellow
        continue
    }

    try {
        Write-Host "[INFO] 正在修复: $service" -ForegroundColor Yellow

        # 读取配置文件
        $content = Get-Content $configPath -Raw -Encoding UTF8

        $originalContent = $content

        # 修复Nacos discovery密码配置
        if ($content -match 'password:\s*\$\{NACOS_PASSWORD:\}') {
            $content = $content -replace 'password:\s*\$\{NACOS_PASSWORD:\}', 'password: ${NACOS_PASSWORD:nacos}'
        }

        # 如果内容有变化，保存文件
        if ($content -ne $originalContent) {
            $content | Set-Content $configPath -Encoding UTF8 -NoNewline
            Write-Host "[SUCCESS] $service - Nacos配置已修复" -ForegroundColor Green
            $fixedCount++
        } else {
            Write-Host "[SKIP] $service - Config already correct" -ForegroundColor Gray
        }

    } catch {
        Write-Host "[ERROR] $service - 修复失败: $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "修复完成统计" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Fixed: $fixedCount services" -ForegroundColor Green
Write-Host "  Failed: $errorCount services" -ForegroundColor $(if ($errorCount -gt 0) { "Red" } else { "Gray" })
Write-Host "  Skipped: $($services.Count - $fixedCount - $errorCount) services" -ForegroundColor Gray
Write-Host ""

if ($errorCount -eq 0) {
    Write-Host "[SUCCESS] All services Nacos config fixed!" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Some services failed, please check errors" -ForegroundColor Yellow
}
