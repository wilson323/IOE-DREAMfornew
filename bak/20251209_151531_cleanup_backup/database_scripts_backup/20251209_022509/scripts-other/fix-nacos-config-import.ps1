# ==========================================
# IOE-DREAM Nacos配置导入修复脚本
# 版本: v1.0.0
# 日期: 2025-01-31
# 说明: 修复Spring Boot 2.4+要求的spring.config.import配置
# ==========================================

param(
    [switch]$Verify,
    [switch]$Fix
)

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Nacos配置导入修复工具" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$microservices = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-visitor-service",
    "ioedream-video-service",
    "ioedream-consume-service"
)

$fixedCount = 0
$missingCount = 0

foreach ($service in $microservices) {
    $configFile = "D:\IOE-DREAM\microservices\$service\src\main\resources\application.yml"
    
    if (-not (Test-Path $configFile)) {
        Write-Host "  ✗ $service - 配置文件不存在" -ForegroundColor Red
        continue
    }
    
    $content = Get-Content $configFile -Raw
    
    if ($content -match "spring:\s*\n\s*config:\s*\n\s*import:\s*nacos:") {
        Write-Host "  ✓ $service - 已配置 spring.config.import" -ForegroundColor Green
        $fixedCount++
    } else {
        Write-Host "  ✗ $service - 缺少 spring.config.import" -ForegroundColor Red
        $missingCount++
        
        if ($Fix) {
            # 查找spring:节点下的profiles配置
            if ($content -match "(spring:\s*\n\s*application:\s*\n\s*name:.*\n\s*profiles:\s*\n\s*active:.*\n)") {
                $replacement = $matches[1] + "  # Spring Boot 2.4+ 要求显式声明配置导入`n  config:`n    import: nacos:`n"
                $newContent = $content -replace [regex]::Escape($matches[1]), $replacement
                Set-Content -Path $configFile -Value $newContent -Encoding UTF8
                Write-Host "    → 已修复" -ForegroundColor Yellow
                $fixedCount++
                $missingCount--
            }
        }
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  修复统计" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  已配置: $fixedCount" -ForegroundColor Green
Write-Host "  缺少配置: $missingCount" -ForegroundColor $(if ($missingCount -eq 0) { "Green" } else { "Red" })
Write-Host ""

if ($missingCount -gt 0 -and -not $Fix) {
    Write-Host "提示: 使用 -Fix 参数自动修复缺少的配置" -ForegroundColor Yellow
    Write-Host "  示例: .\scripts\fix-nacos-config-import.ps1 -Fix" -ForegroundColor Gray
}

Write-Host ""
