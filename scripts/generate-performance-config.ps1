# =====================================================
# IOE-DREAM 性能优化配置生成器
# 自动生成不同环境下的性能优化配置
#
# 使用方法:
# .\scripts\generate-performance-config.ps1
# .\scripts\generate-performance-config.ps1 -Environment prod
# .\scripts\generate-performance-config.ps1 -Service ioedream-access-service
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("dev", "test", "prod")]
    [string]$Environment = "prod",

    [Parameter(Mandatory=$false)]
    [ValidateSet("all", "ioedream-gateway-service", "ioedream-common-service", "ioedream-access-service",
                "ioedream-attendance-service", "ioedream-consume-service", "ioedream-visitor-service",
                "ioedream-video-service", "ioedream-oa-service", "ioedream-device-comm-service")]
    [string]$Service = "all",

    [Parameter(Mandatory=$false)]
    [ValidateSet("conservative", "standard", "aggressive")]
    [string]$OptimizationLevel = "standard",

    [Parameter(Mandatory=$false)]
    [string]$OutputPath = "./generated-configs"
)

# 工具函数
function Write-ConfigLog {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "INFO" { "Green" }
        "WARN" { "Yellow" }
        "ERROR" { "Red" }
        "SUCCESS" { "Cyan" }
        default { "White" }
    }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Get-EnvironmentConfig {
    param([string]$Env, [string]$OptLevel)

    $configs = @{
        "dev" = @{
            "conservative" = @{
                Memory = @{ Xms = "512m"; Xmx = "1g"; Xmn = "256m"; MetaspaceSize = "128m" }
                Database = @{ InitialSize = 5; MinIdle = 5; MaxActive = 15; MaxWait = "60000" }
                Redis = @{ MaxActive = 8; MaxIdle = 4; Timeout = "3000" }
                Threads = @{ Core = 10; Max = 30; QueueCapacity = 200 }
                Cache = @{ LocalSize = 300; LocalTtl = "180s"; RedisTtl = "3600" }
            }
            "standard" = @{
                Memory = @{ Xms = "512m"; Xmx = "1g"; Xmn = "256m"; MetaspaceSize = "128m" }
                Database = @{ InitialSize = 5; MinIdle = 5; MaxActive = 20; MaxWait = "30000" }
                Redis = @{ MaxActive = 10; MaxIdle = 5; Timeout = "3000" }
                Threads = @{ Core = 15; Max = 50; QueueCapacity = 500 }
                Cache = @{ LocalSize = 500; LocalTtl = "300s"; RedisTtl = "1800" }
            }
            "aggressive" = @{
                Memory = @{ Xms = "768m"; Xmx = "1.5g"; Xmn = "384m"; MetaspaceSize = "192m" }
                Database = @{ InitialSize = 10; MinIdle = 10; MaxActive = 25; MaxWait = "15000" }
                Redis = @{ MaxActive = 15; MaxIdle = 8; Timeout = "2000" }
                Threads = @{ Core = 20; Max = 80; QueueCapacity = 1000 }
                Cache = @{ LocalSize = 800; LocalTtl = "300s"; RedisTtl = "1800" }
            }
        }
        "test" = @{
            "conservative" = @{
                Memory = @{ Xms = "1g"; Xmx = "2g"; Xmn = "512m"; MetaspaceSize = "256m" }
                Database = @{ InitialSize = 10; MinIdle = 10; MaxActive = 25; MaxWait = "30000" }
                Redis = @{ MaxActive = 12; MaxIdle = 6; Timeout = "3000" }
                Threads = @{ Core = 20; Max = 60; QueueCapacity = 500 }
                Cache = @{ LocalSize = 800; LocalTtl = "300s"; RedisTtl = "1800" }
            }
            "standard" = @{
                Memory = @{ Xms = "2g"; Xmx = "4g"; Xmn = "1g"; MetaspaceSize = "512m" }
                Database = @{ InitialSize = 15; MinIdle = 15; MaxActive = 40; MaxWait = "15000" }
                Redis = @{ MaxActive = 20; MaxIdle = 10; Timeout = "3000" }
                Threads = @{ Core = 30; Max = 100; QueueCapacity = 1000 }
                Cache = @{ LocalSize = 1500; LocalTtl = "300s"; RedisTtl = "1800" }
            }
            "aggressive" = @{
                Memory = @{ Xms = "3g"; Xmx = "6g"; Xmn = "1.5g"; MetaspaceSize = "768m" }
                Database = @{ InitialSize = 20; MinIdle = 20; MaxActive = 60; MaxWait = "10000" }
                Redis = @{ MaxActive = 30; MaxIdle = 15; Timeout = "2000" }
                Threads = @{ Core = 50; Max = 150; QueueCapacity = 2000 }
                Cache = @{ LocalSize = 2000; LocalTtl = "300s"; RedisTtl = "1800" }
            }
        }
        "prod" = @{
            "conservative" = @{
                Memory = @{ Xms = "2g"; Xmx = "4g"; Xmn = "1g"; MetaspaceSize = "512m" }
                Database = @{ InitialSize = 20; MinIdle = 20; MaxActive = 50; MaxWait = "10000" }
                Redis = @{ MaxActive = 25; MaxIdle = 12; Timeout = "5000" }
                Threads = @{ Core = 40; Max = 150; QueueCapacity = 1500 }
                Cache = @{ LocalSize = 2000; LocalTtl = "600s"; RedisTtl = "900" }
            }
            "standard" = @{
                Memory = @{ Xms = "4g"; Xmx = "8g"; Xmn = "2g"; MetaspaceSize = "1g" }
                Database = @{ InitialSize = 30; MinIdle = 30; MaxActive = 80; MaxWait = "5000" }
                Redis = @{ MaxActive = 40; MaxIdle = 20; Timeout = "5000" }
                Threads = @{ Core = 60; Max = 200; QueueCapacity = 2000 }
                Cache = @{ LocalSize = 3000; LocalTtl = "600s"; RedisTtl = "900" }
            }
            "aggressive" = @{
                Memory = @{ Xms = "6g"; Xmx = "12g"; Xmn = "3g"; MetaspaceSize = "1.5g" }
                Database = @{ InitialSize = 50; MinIdle = 50; MaxActive = 120; MaxWait = "3000" }
                Redis = @{ MaxActive = 60; MaxIdle = 30; Timeout = "3000" }
                Threads = @{ Core = 100; Max = 300; QueueCapacity = 3000 }
                Cache = @{ LocalSize = 5000; LocalTtl = "600s"; RedisTtl = "600" }
            }
        }
    }

    return $configs[$Env][$OptLevel]
}

function Generate-JvmConfig {
    param($Config, [string]$ServiceName)

    return @"
# =====================================================
# $ServiceName JVM性能优化配置
# 环境: $Environment | 优化级别: $OptimizationLevel
# 生成时间: $(Get-Date)
# =====================================================

# JVM启动参数
JAVA_OPTS="-server
-Xms$($Config.Memory.Xms)
-Xmx$($Config.Memory.Xmx)
-Xmn$($Config.Memory.Xmn)
-XX:MetaspaceSize=$($Config.Memory.MetaspaceSize)
-XX:MaxMetaspaceSize=$([int]($Config.Memory.MetaspaceSize.Replace('m','')) * 2)m
-XX:CompressedClassSpaceSize=256m

# G1GC配置
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:G1HeapRegionSize=16m
-XX:G1NewSizePercent=30
-XX:G1MaxNewSizePercent=40
-XX:G1MixedGCCountTarget=8
-XX:InitiatingHeapOccupancyPercent=45

# 性能优化
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-XX:+UseCompressedOops
-XX:+UseCompressedClassPointers
-XX:+UnlockExperimentalVMOptions
-XX:+UseZGC

# GC日志
-XX:+PrintGCDetails
-XX:+PrintGCTimeStamps
-XX:+PrintGCDateStamps
-Xloggc:./logs/gc-$ServiceName.log
-XX:+UseGCLogFileRotation
-XX:NumberOfGCLogFiles=10
-XX:GCLogFileSize=10M

# 内存溢出处理
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=./logs/dumps/
-XX:OnOutOfMemoryError="kill -9 %p"

# JIT编译优化
-XX:+TieredCompilation
-XX:CompileThreshold=1000
-XX:ReservedCodeCacheSize=256m
-XX:+InlineSmallCode

# 监控和诊断
-XX:+UnlockDiagnosticVMOptions
-XX:+LogVMOutput
-XX:+PrintFlagsFinal
-XX:+PrintSafepointStatistics
-XX:+PrintVMOptions
"

# 容器环境优化
JAVA_OPTS="$JAVA_OPTS -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:MinRAMPercentage=50.0"

# 网络优化
JAVA_OPTS="$JAVA_OPTS -Djava.net.preferIPv4Stack=true -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# Spring Boot优化
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$Environment -Dspring.jmx.enabled=true"
"@
}

function Generate-DatabaseConfig {
    param($Config, [string]$ServiceName)

    return @"
# =====================================================
# $ServiceName 数据库性能优化配置
# 环境: $Environment | 优化级别: $OptimizationLevel
# 生成时间: $(Get-Date)
# =====================================================

spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      # 基础连接配置
      initial-size: $($Config.Database.InitialSize)
      min-idle: $($Config.Database.MinIdle)
      max-active: $($Config.Database.MaxActive)
      max-wait: $($Config.Database.MaxWait)

      # 连接有效性检查
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      validation-query-timeout: 3

      # 连接回收配置
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      max-evictable-idle-time-millis: 900000

      # 预编译语句缓存
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      max-open-prepared-statements: 100

      # 性能监控
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        reset-enable: false
        login-username: admin
        login-password: "$($Environment)_druid_admin"

      # Web监控
      web-stat-filter:
        enabled: true
        url-pattern: /*
        exclusions: "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*,/actuator/*"

      # SQL监控
      filter:
        stat:
          enabled: true
          db-type: mysql
          log-slow-sql: true
          slow-sql-millis: 1000
          merge-sql: true

        wall:
          enabled: true
          db-type: mysql
          config:
            multi-statement-allow: false
            delete-allow: true
            update-allow: true
            insert-allow: true
            select-allow: true

  # MyBatis-Plus优化
  mybatis-plus:
    configuration:
      # 二级缓存
      cache-enabled: true
      # 延迟加载
      lazy-loading-enabled: true
      # 超时设置
      default-statement-timeout: 30
      # 执行器类型
      default-executor-type: REUSE
      # 开启驼峰命名
      map-underscore-to-camel-case: true
      # 本地缓存
      local-cache-scope: session
      # JDBC类型获取
      jdbc-type-for-null: null
"@
}

function Generate-RedisConfig {
    param($Config, [string]$ServiceName)

    return @"
# =====================================================
# $ServiceName Redis缓存优化配置
# 环境: $Environment | 优化级别: $OptimizationLevel
# 生成时间: $(Get-Date)
# =====================================================

spring:
  data:
    redis:
      host: `$"{REDIS_HOST:127.0.0.1}"
      port: `$"{REDIS_PORT:6379}"
      password: `$"{REDIS_PASSWORD:}"
      database: 0
      timeout: $($Config.Redis.Timeout)
      lettuce:
        pool:
          max-active: $($Config.Redis.MaxActive)
          max-idle: $($Config.Redis.MaxIdle)
          min-idle: 0
          max-wait: -1ms
          time-between-eviction-runs: 10s
        cluster:
          refresh:
            adaptive: true
            timeout: 1000ms

  # Spring Cache配置
  cache:
    type: redis
    key-prefix: ioedream:$ServiceName
    default-ttl: $($Config.Cache.RedisTtl)
    cache-null-values: false
    use-key-prefix: true

  # Caffeine本地缓存
  caffeine:
    spec: maximumSize=$($Config.Cache.LocalSize),expireAfterWrite=$($Config.Cache.LocalTtl),recordStats
    cache-names: user,dict,menu,permission,config

# 自定义缓存配置
cache:
  # 缓存预热
  warmup:
    enabled: true
    delay: 30s
    thread-pool-size: 4
    timeout: 30s

  # 缓存监控
  monitoring:
    enabled: true
    stats-interval: 60s
    hit-rate-threshold: 0.8
    real-time: true

  # 多级缓存策略
  multi-level:
    l1-local:
      enabled: true
      maximum-size: $([int]($Config.Cache.LocalSize) * 0.7)
      expire-after-write: $($Config.Cache.LocalTtl)

    l2-redis:
      enabled: true
      key-prefix: "L2:$($ServiceName)"
      default-ttl: $($Config.Cache.RedisTtl)

    l3-gateway:
      enabled: true
      ttl: 300
      max-size: 1000
"@
}

function Generate-ThreadConfig {
    param($Config, [string]$ServiceName)

    return @"
# =====================================================
# $ServiceName 线程池优化配置
# 环境: $Environment | 优化级别: $OptimizationLevel
# 生成时间: $(Get-Date)
# =====================================================

# 业务线程池配置
business-thread-pool:
  core-size: $($Config.Threads.Core)
  max-size: $($Config.Threads.Max)
  queue-capacity: $($Config.Threads.QueueCapacity)
  keep-alive-time: 60s
  thread-name-prefix: "business-$($ServiceName)-"
  rejected-policy: CALLER_RUNS

# 异步任务线程池
async-thread-pool:
  core-size: $([math]::Max(10, [int]($Config.Threads.Core) * 0.4))
  max-size: $([math]::Max(20, [int]($Config.Threads.Max) * 0.5))
  queue-capacity: $([math]::Max(100, [int]($Config.Threads.QueueCapacity) * 0.3))
  keep-alive-time: 30s
  thread-name-prefix: "async-$($ServiceName)-"
  rejected-policy: CALLER_RUNS

# 定时任务线程池
scheduled-thread-pool:
  core-size: $([math]::Max(5, [int]($Config.Threads.Core) * 0.2))
  max-size: $([math]::Max(10, [int]($Config.Threads.Max) * 0.3))
  keep-alive-time: 60s
  thread-name-prefix: "scheduled-$($ServiceName)-"

# Tomcat线程池优化
server:
  tomcat:
    threads:
      max: $($Config.Threads.Max)
      min-spare: $([math]::Max(10, [int]($Config.Threads.Core) * 0.5))
    max-connections: $([int]($Config.Threads.Max) * 50)
    accept-count: 100
    connection-timeout: 20000

# Spring异步配置
spring:
  task:
    execution:
      pool:
        core-size: $([math]::Max(8, [int]($Config.Threads.Core) * 0.3))
        max-size: $([math]::Max(16, [int]($Config.Threads.Max) * 0.4))
        queue-capacity: $([math]::Max(50, [int]($Config.Threads.QueueCapacity) * 0.2))
        keep-alive-time: 30s
        thread-name-prefix: "async-exec-$($ServiceName)-"
    scheduling:
      pool:
        core-size: $([math]::Max(5, [int]($Config.Threads.Core) * 0.2))
        max-size: $([math]::Max(10, [int]($Config.Threads.Max) * 0.3))
        keep-alive-time: 60s
        thread-name-prefix: "scheduled-exec-$($ServiceName)-"
"@
}

function Generate-PerformanceConfig {
    param($Config, [string]$ServiceName)

    return @"
# =====================================================
# $ServiceName 综合性能优化配置
# 环境: $Environment | 优化级别: $OptimizationLevel
# 生成时间: $(Get-Date)
# =====================================================

# 性能监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,threaddump,heapdump,env,caches
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5,0.9,0.95,0.99
      sla:
        http.server.requests: 100ms,200ms,500ms,1s,2s,5s

# 日志性能优化
logging:
  level:
    root: INFO
    net.lab1024.sa: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-}] %-5level [%X{application:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-}] %-5level [%X{application:-}] %logger{36} - %msg%n"
  file:
    name: ./logs/$($ServiceName).log
    max-size: 100MB
    max-history: 30
    total-size-cap: 1GB

# HTTP客户端优化
http:
  client:
    connect-timeout: 5000
    read-timeout: 10000
    max-total: $($Config.Threads.Max * 2)
    default-max-per-route: $([math]::Max(10, [int]($Config.Threads.Core)))
    connection-request-timeout: 5000
    socket-timeout: 30000

# Resilience4j配置
resilience4j:
  circuitbreaker:
    configs:
      default:
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        slidingWindowSize: 100
        minimumNumberOfCalls: 10
        permittedNumberOfCallsInHalfOpenState: 5
        automaticTransitionFromOpenToHalfOpenEnabled: true
    instances:
      default:
        baseConfig: default

  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1000
        retryExceptions:
          - java.net.SocketTimeoutException
          - java.io.IOException
    instances:
      default:
        baseConfig: default

  ratelimiter:
    configs:
      default:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 0
    instances:
      default:
        baseConfig: default

# 数据库优化
spring:
  jpa:
    hibernate:
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: false
        show_sql: false
        use_sql_comments: false
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
        connection:
          provider_disables_autocommit: true
"@
}

function Generate-ServiceConfig {
    param($Config, [string]$ServiceName)

    $outputDir = Join-Path $OutputPath $ServiceName $Environment
    if (-not (Test-Path $outputDir)) {
        New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
    }

    # 生成JVM配置
    $jvmConfig = Generate-JvmConfig -Config $Config -ServiceName $ServiceName
    $jvmFile = Join-Path $outputDir "jvm-optimized.conf"
    $jvmConfig | Out-File -FilePath $jvmFile -Encoding UTF8
    Write-ConfigLog "生成JVM配置: $jvmFile" "SUCCESS"

    # 生成数据库配置
    $dbConfig = Generate-DatabaseConfig -Config $Config -ServiceName $ServiceName
    $dbFile = Join-Path $outputDir "database-optimized.yml"
    $dbConfig | Out-File -FilePath $dbFile -Encoding UTF8
    Write-ConfigLog "生成数据库配置: $dbFile" "SUCCESS"

    # 生成Redis配置
    $redisConfig = Generate-RedisConfig -Config $Config -ServiceName $ServiceName
    $redisFile = Join-Path $outputDir "cache-optimized.yml"
    $redisConfig | Out-File -FilePath $redisFile -Encoding UTF8
    Write-ConfigLog "生成缓存配置: $redisFile" "SUCCESS"

    # 生成线程池配置
    $threadConfig = Generate-ThreadConfig -Config $Config -ServiceName $ServiceName
    $threadFile = Join-Path $outputDir "thread-optimized.yml"
    $threadConfig | Out-File -FilePath $threadFile -Encoding UTF8
    Write-ConfigLog "生成线程池配置: $threadFile" "SUCCESS"

    # 生成综合性能配置
    $perfConfig = Generate-PerformanceConfig -Config $Config -ServiceName $ServiceName
    $perfFile = Join-Path $outputDir "performance-optimized.yml"
    $perfConfig | Out-File -FilePath $perfFile -Encoding UTF8
    Write-ConfigLog "生成性能配置: $perfFile" "SUCCESS"

    # 生成Docker Compose配置
    $dockerConfig = Generate-DockerComposeConfig -Config $Config -ServiceName $ServiceName
    $dockerFile = Join-Path $outputDir "docker-compose-optimized.yml"
    $dockerConfig | Out-File -FilePath $dockerFile -Encoding UTF8
    Write-ConfigLog "生成Docker配置: $dockerFile" "SUCCESS"
}

function Generate-DockerComposeConfig {
    param($Config, [string]$ServiceName)

    return @"
# =====================================================
# $ServiceName Docker Compose优化配置
# 环境: $Environment | 优化级别: $OptimizationLevel
# 生成时间: $(Get-Date)
# =====================================================

version: '3.8'

services:
  $ServiceName:
    image: ioedream/$($ServiceName):latest
    container_name: $($ServiceName)-$($Environment)
    environment:
      # JVM配置
      JAVA_OPTS: "-Xms$($Config.Memory.Xms) -Xmx$($Config.Memory.Xmx) -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

      # 数据库配置
      DB_HOST: mysql-$($Environment)
      DB_PORT: 3306
      DB_NAME: ioedream_$($Environment)
      DB_USERNAME: app_user
      DB_PASSWORD: "$($Environment)_secure_password"

      # Redis配置
      REDIS_HOST: redis-$($Environment)
      REDIS_PORT: 6379
      REDIS_PASSWORD: "$($Environment)_redis_password"

      # 应用配置
      SPRING_PROFILES_ACTIVE: $Environment
      LOG_LEVEL: INFO
    ports:
      - "8080:8080"
    volumes:
      - ./logs:/app/logs
      - ./config:/app/config
    deploy:
      resources:
        limits:
          memory: $($Config.Memory.Xmx)
          cpus: '2.0'
        reservations:
          memory: $($Config.Memory.Xms)
          cpus: '1.0'
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
"@
}

function Main {
    Write-ConfigLog "========================================" "INFO"
    Write-ConfigLog "IOE-DREAM 性能优化配置生成器" "INFO"
    Write-ConfigLog "环境: $Environment" "INFO"
    Write-ConfigLog "服务: $Service" "INFO"
    Write-ConfigLog "优化级别: $OptimizationLevel" "INFO"
    Write-ConfigLog "输出路径: $OutputPath" "INFO"
    Write-ConfigLog "========================================" "INFO"

    try {
        # 获取环境配置
        $config = Get-EnvironmentConfig -Env $Environment -OptLevel $OptimizationLevel
        Write-ConfigLog "环境配置加载完成" "SUCCESS"

        # 确定要处理的服务
        $services = if ($Service -eq "all") {
            @("ioedream-gateway-service", "ioedream-common-service", "ioedream-access-service",
              "ioedream-attendance-service", "ioedream-consume-service", "ioedream-visitor-service",
              "ioedream-video-service", "ioedream-oa-service", "ioedream-device-comm-service")
        } else {
            @($Service)
        }

        # 生成配置文件
        foreach ($serviceName in $services) {
            Write-ConfigLog "正在生成 $serviceName 的性能配置..." "INFO"
            Generate-ServiceConfig -Config $config -ServiceName $serviceName
        }

        # 生成部署脚本
        Generate-DeploymentScript -Services $services -Config $config

        Write-ConfigLog "========================================" "SUCCESS"
        Write-ConfigLog "性能优化配置生成完成！" "SUCCESS"
        Write-ConfigLog "生成配置数量: $($services.Count) 个服务" "SUCCESS"
        Write-ConfigLog "配置文件位置: $OutputPath" "SUCCESS"
        Write-ConfigLog "========================================" "SUCCESS"

        Write-ConfigLog "使用方法:" "INFO"
        Write-ConfigLog "1. 将生成的配置文件复制到对应服务的resources目录" "INFO"
        Write-ConfigLog "2. 更新docker-compose.yml中的环境变量" "INFO"
        Write-ConfigLog "3. 重启服务以应用新配置" "INFO"
        Write-ConfigLog "4. 运行性能测试验证优化效果" "INFO"

    } catch {
        Write-ConfigLog "配置生成过程中发生错误: $($_.Exception.Message)" "ERROR"
        exit 1
    }
}

function Generate-DeploymentScript {
    param($Services, $Config)

    $scriptPath = Join-Path $OutputPath "deploy-performance-configs.sh"
    $script = @"
#!/bin/bash
# IOE-DREAM 性能优化配置部署脚本
# 生成时间: $(Get-Date)

set -e

echo "开始部署性能优化配置..."

# 复制配置文件
for service in $($Services -join ' '); do
    echo "配置服务: $service"

    # 复制配置文件到服务目录
    if [ -d "./microservices/$service/src/main/resources" ]; then
        cp -r "./generated-configs/$service/$Environment/"* "./microservices/$service/src/main/resources/"
        echo "已配置: $service"
    else
        echo "警告: 服务目录不存在: $service"
    fi
done

echo "性能优化配置部署完成！"
echo "请重启服务以应用新配置。"

# 生成重启命令
echo ""
echo "重启命令参考:"
for service in $($Services -join ' '); do
    echo "docker-compose restart $service"
done
"@

    $script | Out-File -FilePath $scriptPath -Encoding UTF8
    Write-ConfigLog "生成部署脚本: $scriptPath" "SUCCESS"

    # 设置执行权限（如果是在Linux/Mac上）
    if ($IsLinux -or $IsMacOS) {
        chmod +x $scriptPath
    }
}

# 执行主函数
Main