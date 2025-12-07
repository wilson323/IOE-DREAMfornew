# 批量更新所有Dockerfile，添加Maven阿里云镜像配置

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "更新所有Dockerfile - 添加Maven镜像配置" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

$mavenMirrorConfig = @"
# 配置Maven使用阿里云镜像加速
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyunmaven</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>阿里云公共仓库</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>'
"@

$updatedCount = 0
$skippedCount = 0

foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    
    if (-not (Test-Path $dockerfilePath)) {
        Write-Host "  ⚠️  $service - Dockerfile不存在" -ForegroundColor Yellow
        $skippedCount++
        continue
    }
    
    $content = Get-Content $dockerfilePath -Raw
    
    # 检查是否已配置Maven镜像
    if ($content -match "aliyunmaven|maven\.aliyun\.com") {
        Write-Host "  ✅ $service - 已配置Maven镜像" -ForegroundColor Green
        $skippedCount++
        continue
    }
    
    # 查找FROM行和WORKDIR行之间的位置
    if ($content -match "(FROM maven:.*?AS builder\s*\n)(.*?)(WORKDIR /build)") {
        $newContent = $content -replace "(FROM maven:.*?AS builder\s*\n)(.*?)(WORKDIR /build)", "`$1`$2`n`n$mavenMirrorConfig`n`n`$3"
        
        Set-Content -Path $dockerfilePath -Value $newContent -Encoding UTF8 -NoNewline
        Write-Host "  ✅ $service - 已添加Maven镜像配置" -ForegroundColor Green
        $updatedCount++
    } else {
        Write-Host "  ⚠️  $service - 无法找到插入位置" -ForegroundColor Yellow
        $skippedCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "更新完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "已更新: $updatedCount 个Dockerfile" -ForegroundColor Green
Write-Host "已跳过: $skippedCount 个Dockerfile" -ForegroundColor Yellow
Write-Host ""

if ($updatedCount -gt 0) {
    Write-Host "下一步: 重新构建Docker镜像" -ForegroundColor Yellow
    Write-Host "  docker-compose -f docker-compose-all.yml build --no-cache" -ForegroundColor White
}
