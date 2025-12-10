# 批量修复所有Dockerfile中的-N参数问题
# 问题: mvn clean install -N 导致JAR包缺少Main-Class清单
# 解决: 移除-N参数,生成完整的Spring Boot可执行JAR

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "修复所有Dockerfile构建参数" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    "ioedream-gateway-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-consume-service",
    "ioedream-visitor-service"
)

$fixedCount = 0
$errorCount = 0

foreach ($service in $services) {
    $dockerfilePath = "microservices\$service\Dockerfile"
    
    if (Test-Path $dockerfilePath) {
        Write-Host "修复 $service..." -ForegroundColor Yellow
        
        try {
            # 读取文件内容
            $content = Get-Content $dockerfilePath -Raw
            
            # 替换 -N 参数
            $newContent = $content -replace 'mvn clean install -N -DskipTests', 'mvn clean install -DskipTests'
            $newContent = $newContent -replace 'mvn clean package -N -DskipTests', 'mvn clean package -DskipTests'
            
            # 写回文件
            $newContent | Set-Content $dockerfilePath -NoNewline
            
            Write-Host "  ✓ 已修复 $service" -ForegroundColor Green
            $fixedCount++
        }
        catch {
            Write-Host "  ✗ 修复失败: $_" -ForegroundColor Red
            $errorCount++
        }
    }
    else {
        Write-Host "  ✗ 文件不存在: $dockerfilePath" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "修复完成" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "成功修复: $fixedCount 个服务" -ForegroundColor Green
if ($errorCount -gt 0) {
    Write-Host "失败: $errorCount 个服务" -ForegroundColor Red
}
Write-Host ""
