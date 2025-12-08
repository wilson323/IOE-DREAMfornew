# ============================================================
# 移除Nacos Config依赖脚本（方案2）
# 
# 功能: 从所有微服务pom.xml中移除spring-cloud-starter-alibaba-nacos-config依赖
# 用途: 更彻底的解决方案，完全移除配置中心相关代码
# 
# @Author: IOE-DREAM Team
# @Date: 2025-12-08
# ============================================================

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "移除Nacos Config依赖脚本（方案2）" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否在正确的目录
if (-not (Test-Path "microservices\pom.xml")) {
    Write-Host "错误: 请在项目根目录执行此脚本" -ForegroundColor Red
    exit 1
}

# 需要修改的服务列表
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

Write-Host "开始移除Nacos Config依赖..." -ForegroundColor Yellow
Write-Host ""

$successCount = 0
$failCount = 0

foreach ($service in $services) {
    $pomPath = "microservices\$service\pom.xml"
    
    if (-not (Test-Path $pomPath)) {
        Write-Host "  ⚠️  跳过: $service (pom.xml不存在)" -ForegroundColor Yellow
        $failCount++
        continue
    }
    
    Write-Host "  处理: $service" -ForegroundColor Cyan
    
    try {
        # 读取pom.xml内容
        $content = Get-Content $pomPath -Raw -Encoding UTF8
        
        # 检查是否包含nacos-config依赖
        if ($content -match 'spring-cloud-starter-alibaba-nacos-config') {
            # 注释掉nacos-config依赖
            $content = $content -replace '(<!-- Spring Cloud Nacos Config \(配置中心\) -->\s*<dependency>\s*<groupId>com\.alibaba\.cloud</groupId>\s*<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>\s*</dependency>)', '<!-- Spring Cloud Nacos Config (配置中心) - 已禁用，仅使用服务发现 --><!--$1-->'
            
            # 写回文件
            Set-Content -Path $pomPath -Value $content -Encoding UTF8 -NoNewline
            
            Write-Host "    ✅ 已注释nacos-config依赖" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "    ℹ️  未找到nacos-config依赖（可能已处理）" -ForegroundColor Gray
        }
    } catch {
        Write-Host "    ❌ 处理失败: $_" -ForegroundColor Red
        $failCount++
    }
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "处理完成" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "成功: $successCount" -ForegroundColor Green
Write-Host "失败: $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host ""

if ($successCount -gt 0) {
    Write-Host "下一步操作:" -ForegroundColor Yellow
    Write-Host "1. 重新构建所有微服务: cd microservices && mvn clean install -DskipTests" -ForegroundColor Cyan
    Write-Host "2. 重新构建Docker镜像: docker-compose -f docker-compose-all.yml build" -ForegroundColor Cyan
    Write-Host "3. 启动服务: docker-compose -f docker-compose-all.yml up -d" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "或者执行完整修复脚本:" -ForegroundColor Yellow
    Write-Host "  .\scripts\fix-nacos-config-disable.ps1" -ForegroundColor Cyan
}
