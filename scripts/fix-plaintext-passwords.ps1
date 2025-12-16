# =====================================================
# 修复配置中的明文密码默认值
# 版本: v1.0.0
# 描述: 将配置中的明文密码默认值替换为安全配置
# 创建时间: 2025-12-14
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "[修复] 开始修复配置中的明文密码默认值..." -ForegroundColor Cyan

# 需要修复的配置文件列表
$configFiles = @(
    "microservices\ioedream-common-service\src\main\resources\application.yml",
    "microservices\ioedream-access-service\src\main\resources\application.yml",
    "microservices\ioedream-attendance-service\src\main\resources\application.yml",
    "microservices\ioedream-consume-service\src\main\resources\application.yml",
    "microservices\ioedream-visitor-service\src\main\resources\application.yml",
    "microservices\ioedream-video-service\src\main\resources\application.yml",
    "microservices\ioedream-oa-service\src\main\resources\application.yml",
    "microservices\ioedream-device-comm-service\src\main\resources\application.yml",
    "microservices\ioedream-gateway-service\src\main\resources\application.yml"
)

$fixedCount = 0
$errorCount = 0

foreach ($file in $configFiles) {
    $fullPath = Join-Path $PSScriptRoot ".." $file

    if (-not (Test-Path $fullPath)) {
        Write-Host "[跳过] 文件不存在: $file" -ForegroundColor Yellow
        continue
    }

    try {
        $content = Get-Content $fullPath -Raw -Encoding UTF8
        $originalContent = $content

        # 修复MySQL密码默认值
        $content = $content -replace 'password:\s*\$\{MYSQL_PASSWORD:123456\}', 'password: ${MYSQL_PASSWORD:}'
        $content = $content -replace 'password:\s*\$\{MYSQL_PASSWORD:root\}', 'password: ${MYSQL_PASSWORD:}'

        # 修复Redis密码默认值
        $content = $content -replace 'password:\s*\$\{REDIS_PASSWORD:redis123\}', 'password: ${REDIS_PASSWORD:}'
        $content = $content -replace 'password:\s*\$\{REDIS_PASSWORD:123456\}', 'password: ${REDIS_PASSWORD:}'

        # 修复其他密码默认值
        $content = $content -replace 'password:\s*\$\{SWAGGER_PASSWORD:swagger123\}', 'password: ${SWAGGER_PASSWORD:}'
        $content = $content -replace 'password:\s*\$\{DRUID_STAT_PASSWORD:admin\}', 'password: ${DRUID_STAT_PASSWORD:}'

        if ($content -ne $originalContent) {
            # 使用UTF-8 without BOM保存
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($fullPath, $content, $utf8NoBom)
            Write-Host "[修复] $file" -ForegroundColor Green
            $fixedCount++
        } else {
            Write-Host "[通过] $file (无需修复)" -ForegroundColor Gray
        }
    } catch {
        Write-Host "[错误] 修复失败: $file - $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host "`n[完成] 修复完成: 修复$fixedCount个文件, 错误$errorCount个" -ForegroundColor Cyan

if ($errorCount -gt 0) {
    exit 1
}
