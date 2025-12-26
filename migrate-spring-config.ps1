# Spring Boot 配置迁移脚本
# 处理 spring.redis.* → spring.data.redis.* 等配置迁移

Write-Host "🚀 开始 Spring Boot 配置批量迁移..." -ForegroundColor Green

# 查找所有 application.yml 文件
$yamlFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "application*.yml" -File

Write-Host "📁 找到 $($yamlFiles.Count) 个配置文件需要处理" -ForegroundColor Yellow

# 配置映射规则
$configMappings = @{
    # Redis 配置迁移
    "spring.redis.host" = "spring.data.redis.host"
    "spring.redis.port" = "spring.data.redis.port"
    "spring.redis.password" = "spring.data.redis.password"
    "spring.redis.database" = "spring.data.redis.database"
    "spring.redis.timeout" = "spring.data.redis.timeout"
    "spring.redis.lettuce.pool.max-active" = "spring.data.redis.lettuce.pool.max-active"
    "spring.redis.lettuce.pool.max-idle" = "spring.data.redis.lettuce.pool.max-idle"
    "spring.redis.lettuce.pool.min-idle" = "spring.data.redis.lettuce.pool.min-idle"
    "spring.redis.lettuce.pool.max-wait" = "spring.data.redis.lettuce.pool.max-wait"
    "spring.redis.lettuce.cluster.refresh.adaptive" = "spring.data.redis.lettuce.cluster.refresh.adaptive"
    "spring.redis.lettuce.cluster.refresh.period" = "spring.data.redis.lettuce.cluster.refresh.period"

    # Tomcat 配置迁移
    "server.tomcat.max-threads" = "server.tomcat.threads.max"
    "server.tomcat.min-spare-threads" = "server.tomcat.threads.min-spare"
    "server.tomcat.max-connections" = "server.tomcat.max-connections"
    "server.tomcat.accept-count" = "server.tomcat.accept-count"

    # Prometheus 配置迁移
    "management.metrics.export.prometheus.enabled" = "management.prometheus.metrics.export.enabled"
    "management.metrics.export.prometheus.step" = "management.prometheus.metrics.export.step"
    "management.metrics.export.prometheus.descriptions" = "management.prometheus.metrics.export.descriptions"

    # Actuator 端点配置迁移
    "management.endpoint.metrics.enabled" = "management.endpoint.metrics.access"
    "management.endpoint.prometheus.enabled" = "management.endpoint.prometheus.access"
    "management.endpoint.health.enabled" = "management.endpoint.health.access"
    "management.endpoint.info.enabled" = "management.endpoint.info.access"
}

$processedFiles = 0
$updatedConfigs = 0

foreach ($file in $yamlFiles) {
    Write-Host "🔧 处理文件: $($file.FullName)" -ForegroundColor Cyan

    $content = Get-Content -Path $file.FullName -Raw
    $originalContent = $content
    $fileUpdates = 0

    # 执行配置迁移
    foreach ($oldConfig in $configMappings.Keys) {
        $newConfig = $configMappings[$oldConfig]

        # 使用正则表达式进行精确替换
        $pattern = "(\s*)$([regex]::Escape($oldConfig))\s*:\s*"
        $replacement = "`${1}$newConfig`:`r"

        if ($content -match $oldConfig) {
            $content = $content -ireplace $pattern, $replacement
            $fileUpdates++
            $updatedConfigs++
            Write-Host "  ✅ $oldConfig → $newConfig" -ForegroundColor Green
        }
    }

    # 处理带特殊字符的 key（需要引号）
    # 查找包含点号但没有引号的 key
    $content = $content -ireplace '(\s*)([a-zA-Z][a-zA-Z0-9]*\.[a-zA-Z][a-zA-Z0-9]*(\.[a-zA-Z][a-zA-Z0-9]*)+)\s*:', '${1}"${2}":'

    # 只有在内容发生变化时才写入文件
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -Encoding UTF8
        Write-Host "  📝 已更新 $($fileUpdates) 个配置项" -ForegroundColor Yellow
        $processedFiles++
    } else {
        Write-Host "  ⚪ 无需更新" -ForegroundColor Gray
    }

    Write-Host ""
}

Write-Host "🎉 配置迁移完成！" -ForegroundColor Green
Write-Host "📊 处理了 $processedFiles 个文件，更新了 $updatedConfigs 个配置项" -ForegroundColor Cyan
Write-Host ""