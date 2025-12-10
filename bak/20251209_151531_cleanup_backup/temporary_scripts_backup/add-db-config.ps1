# IOE-DREAM 批量添加数据库配置脚本
# 为所有业务微服务添加数据库和Redis配置

$services = @(
    "ioedream-oa-service",
    "ioedream-attendance-service",
    "ioedream-visitor-service", 
    "ioedream-video-service",
    "ioedream-device-comm-service",
    "ioedream-access-service",
    "ioedream-consume-service"
)

$dbConfig = @"

# ==================== 数据源配置 ====================
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://`${MYSQL_HOST:127.0.0.1}:`${MYSQL_PORT:3306}/`${MYSQL_DATABASE:ioedream}?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: `${MYSQL_USERNAME:root}
    password: `${MYSQL_PASSWORD:root1234}
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false

# ==================== Redis配置 ====================
  data:
    redis:
      host: `${REDIS_HOST:127.0.0.1}
      port: `${REDIS_PORT:6379}
      password: `${REDIS_PASSWORD:redis123}
      database: 0
      timeout: 3000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
"@

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " IOE-DREAM 批量添加数据库配置" -ForegroundColor Cyan  
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($service in $services) {
    $configFile = "microservices\$service\src\main\resources\application.yml"
    
    if (Test-Path $configFile) {
        Write-Host "处理服务: $service" -ForegroundColor Yellow
        
        # 读取配置文件
        $content = Get-Content $configFile -Raw
        
        # 检查是否已存在datasource配置
        if ($content -match "datasource:") {
            Write-Host "  跳过（已存在数据库配置）" -ForegroundColor Gray
            continue
        }
        
        # 在Nacos配置后添加数据库配置
        if ($content -match "(?s)(enabled:\s*true\s*#\s*2025\.0\.0\.0.*?\n)") {
            $content = $content -replace "(?s)(enabled:\s*true\s*#\s*2025\.0\.0\.0.*?\n)", "`$1$dbConfig`n"
            
            # 保存文件
            Set-Content -Path $configFile -Value $content -Encoding UTF8 -NoNewline
            Write-Host "  添加成功 √" -ForegroundColor Green
        } else {
            Write-Host "  警告：无法找到插入位置" -ForegroundColor Red
        }
    } else {
        Write-Host "跳过: $service (配置文件不存在)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " 配置添加完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
