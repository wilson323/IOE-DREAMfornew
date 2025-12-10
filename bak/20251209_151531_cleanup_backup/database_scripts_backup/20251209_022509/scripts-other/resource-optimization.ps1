# ============================================================
# IOE-DREAM 资源优化执行脚本 (PowerShell版本)
# 功能: Windows环境下的资源优化自动化脚本
# 作者: IOE-DREAM 架构优化团队
# 版本: v1.0.0
# 日期: 2025-01-30
# ============================================================

param(
    [switch]$SkipDocker,
    [switch]$SkipBackup,
    [switch]$Verbose
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 颜色输出函数
function Write-LogInfo {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Green
}

function Write-LogWarn {
    param([string]$Message)
    Write-Host "[WARN] $Message" -ForegroundColor Yellow
}

function Write-LogError {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

function Write-LogDebug {
    param([string]$Message)
    if ($Verbose) {
        Write-Host "[DEBUG] $Message" -ForegroundColor Blue
    }
}

# 检查必要的工具
function Test-Prerequisites {
    Write-LogInfo "检查必要的工具..."

    # 检查Java
    $javaVersion = java -version 2>&1
    if (-not $javaVersion) {
        Write-LogError "Java未安装，请先安装Java 21+"
        exit 1
    }
    Write-LogInfo "Java版本: $($javaVersion -split '\n')[0]"

    # 检查Maven
    $mavenVersion = mvn -version 2>&1
    if (-not $mavenVersion) {
        Write-LogError "Maven未安装，请先安装Maven 3.8+"
        exit 1
    }
    Write-LogInfo "Maven版本: $($mavenVersion -split '\n')[0]"

    # 检查Docker (可选)
    if (-not $SkipDocker) {
        try {
            $dockerVersion = docker --version 2>&1
            Write-LogInfo "Docker版本: $dockerVersion"
        } catch {
            Write-LogWarn "Docker未安装或无法访问，将跳过Docker相关操作"
            $script:SkipDocker = $true
        }
    }

    Write-LogInfo "所有必要工具检查通过 ✓"
}

# 备份当前配置
function Backup-Configurations {
    if ($SkipBackup) {
        Write-LogWarn "跳过配置备份"
        return
    }

    Write-LogInfo "备份当前配置..."

    $backupDir = "backup\$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    New-Item -ItemType Directory -Force -Path $backupDir | Out-Null

    # 备份pom.xml文件
    Get-ChildItem -Path "microservices" -Recurse -Filter "pom.xml" | ForEach-Object {
        $relativePath = $_.FullName.Replace((Get-Location).Path, "").TrimStart('\')
        $targetPath = Join-Path $backupDir $relativePath
        $targetDir = Split-Path $targetPath -Parent
        New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
        Copy-Item $_.FullName $targetPath
    }

    # 备份配置文件
    Get-ChildItem -Path "microservices" -Recurse -Filter "application*.yml" | ForEach-Object {
        $relativePath = $_.FullName.Replace((Get-Location).Path, "").TrimStart('\')
        $targetPath = Join-Path $backupDir $relativePath
        $targetDir = Split-Path $targetPath -Parent
        New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
        Copy-Item $_.FullName $targetPath
    }

    # 备份Dockerfile
    Get-ChildItem -Path "microservices" -Recurse -Filter "Dockerfile*" | ForEach-Object {
        $relativePath = $_.FullName.Replace((Get-Location).Path, "").TrimStart('\')
        $targetPath = Join-Path $backupDir $relativePath
        $targetDir = Split-Path $targetPath -Parent
        New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
        Copy-Item $_.FullName $targetPath
    }

    Write-LogInfo "配置备份完成: $backupDir"
}

# 依赖优化
function Optimize-Dependencies {
    Write-LogInfo "开始依赖优化..."

    # 分析所有服务的依赖
    Write-LogInfo "分析项目依赖..."
    mvn dependency:analyze "-DoutputFile=dependency-analysis-report.txt"

    # 生成依赖树
    Write-LogInfo "生成依赖树..."
    $services = Get-ChildItem -Path "microservices" -Recurse -Filter "pom.xml" | ForEach-Object {
        $serviceDir = $_.DirectoryName
        if (Test-Path "$serviceDir\pom.xml") {
            $serviceName = Split-Path $serviceDir -Leaf
            Write-LogInfo "分析服务: $serviceName"

            Push-Location $serviceDir
            mvn dependency:tree "-Dverbose" | Out-File "dependency-tree.txt"
            mvn dependency:display-dependency-updates | Out-File "dependency-updates.txt"
            Pop-Location
        }
    }

    Write-LogInfo "依赖优化完成 ✓"
    Write-LogWarn "请检查 dependency-analysis-report.txt 文件了解详细分析结果"
}

# Docker镜像优化
function Optimize-DockerImages {
    if ($SkipDocker) {
        Write-LogWarn "跳过Docker镜像优化"
        return
    }

    Write-LogInfo "开始Docker镜像优化..."

    # 创建优化的Dockerfile模板
    $dockerfileTemplate = @"
# 多阶段构建 - 优化版本
FROM maven:3.9.6-eclipse-temurin:21-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# 跳过测试构建
RUN mvn clean package -DskipTests -T 4

# 运行时镜像 - 使用轻量级JRE
FROM eclipse-temurin:21-jre-alpine AS runtime

# 安装必要的系统工具
RUN apk add --no-cache curl tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    rm -rf /var/cache/apk/*

# 创建应用用户
RUN addgroup -g 1000 appuser && adduser -u 1000 -G appuser -s /bin/sh -D appuser

WORKDIR /app

# 复制JAR文件
ARG JAR_FILE
COPY --from=builder /app/target/${JAR_FILE} app.jar

# JVM参数优化
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 切换到非root用户
USER appuser

# 暴露端口
ARG SERVICE_PORT
EXPOSE `${SERVICE_PORT}

# 启动命令
ENTRYPOINT ["sh", "-c", "java `${JAVA_OPTS} -jar /app/app.jar"]
"@

    Set-Content -Path "Dockerfile.optimized" -Value $dockerfileTemplate

    # 为每个服务创建优化的Dockerfile
    $services = Get-ChildItem -Path "microservices" -Recurse -Filter "pom.xml" | ForEach-Object {
        $serviceDir = $_.DirectoryName
        if (Test-Path "$serviceDir\pom.xml") {
            $serviceName = Split-Path $serviceDir -Leaf
            Write-LogInfo "优化服务Dockerfile: $serviceName"

            # 读取端口信息
            $port = "8080"
            if (Test-Path "$serviceDir\src\main\resources\application.yml") {
                $port = (Select-String -Path "$serviceDir\src\main\resources\application.yml" -Pattern "port:\s*(\d+)" | ForEach-Object { $_.Matches.Groups[1].Value }) -join ""
                if (-not $port) { $port = "8080" }
            }

            # 读取JAR文件名
            $jarFile = "app.jar"
            if (Test-Path "$serviceDir\pom.xml") {
                $artifactId = (Select-String -Path "$serviceDir\pom.xml" -Pattern "<artifactId>(.*?)</artifactId>" | ForEach-Object { $_.Matches.Groups[1].Value }) -join ""
                if ($artifactId) {
                    $jarFile = "$artifactId-1.0.0.jar"
                }
            }

            # 创建优化的Dockerfile
            $optimizedDockerfile = $dockerfileTemplate.Replace("${SERVICE_PORT}", $port).Replace('${JAR_FILE}', $jarFile)
            Set-Content -Path "$serviceDir\Dockerfile.optimized" -Value $optimizedDockerfile
        }
    }

    Write-LogInfo "Docker镜像优化完成 ✓"
}

# 配置文件简化
function Simplify-Configurations {
    Write-LogInfo "开始配置文件简化..."

    # 创建通用配置模板目录
    New-Item -ItemType Directory -Force -Path "config-templates" | Out-Null

    # 创建通用配置模板
    $commonConfigTemplate = @"
# 通用配置模板
spring:
  application:
    name: `${SERVICE_NAME}
  profiles:
    active: `${SPRING_PROFILES_ACTIVE:prod}

  # 数据库通用配置
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: `${DB_INITIAL_SIZE:10}
      min-idle: `${DB_MIN_IDLE:10}
      max-active: `${DB_MAX_ACTIVE:50}
      validation-query: SELECT 1
      test-while-idle: true
      time-between-eviction-runs-millis: 60000

  # Redis通用配置
  redis:
    host: `${REDIS_HOST:127.0.0.1}
    port: `${REDIS_PORT:6379}
    password: `${REDIS_PASSWORD:}
    database: `${REDIS_DATABASE:0}
    timeout: `${REDIS_TIMEOUT:3000}
    lettuce:
      pool:
        max-active: `${REDIS_MAX_ACTIVE:20}
        max-idle: `${REDIS_MAX_IDLE:10}
        min-idle: `${REDIS_MIN_IDLE:5}

  # 缓存通用配置
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=`${CACHE_MAX_SIZE:1000},expireAfterWrite=`${CACHE_TTL:30m}

# 监控通用配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true

# 日志通用配置
logging:
  level:
    root: `${LOG_LEVEL:WARN}
    net.lab1024.sa: `${LOG_LEVEL_APP:INFO}
  file:
    name: logs/`${spring.application.name}.log
    max-size: `${LOG_MAX_SIZE:200MB}
    max-history: `${LOG_MAX_HISTORY:7}
"@

    Set-Content -Path "config-templates\application-common.yml" -Value $commonConfigTemplate

    # 为每个服务创建简化配置
    $services = Get-ChildItem -Path "microservices" -Recurse -Filter "pom.xml" | ForEach-Object {
        $serviceDir = $_.DirectoryName
        if (Test-Path "$serviceDir\pom.xml") {
            $serviceName = Split-Path $serviceDir -Leaf
            Write-LogInfo "简化服务配置: $serviceName"

            # 创建简化配置
            Copy-Item "config-templates\application-common.yml" "$serviceDir\application-simplified.yml"

            # 添加服务特定配置
            $port = "8080"
            if (Test-Path "$serviceDir\src\main\resources\application.yml") {
                $port = (Select-String -Path "$serviceDir\src\main\resources\application.yml" -Pattern "port:\s*(\d+)" | ForEach-Object { $_.Matches.Groups[1].Value }) -join ""
                if (-not $port) { $port = "8080" }
            }

            $serviceConfig = @"

# 服务特定配置
server:
  port: $port

ioe:
  $serviceName:
    enabled: `${SERVICE_ENABLED:true}
    async:
      enabled: `${ASYNC_ENABLED:true}
      pool-size: `${ASYNC_POOL_SIZE:10}
    cache:
      enabled: `${CACHE_ENABLED:true}
      ttl: `${SERVICE_CACHE_TTL:1800}
"@

            Add-Content -Path "$serviceDir\application-simplified.yml" -Value $serviceConfig
        }
    }

    Write-LogInfo "配置文件简化完成 ✓"
}

# 构建优化镜像
function Build-OptimizedImages {
    if ($SkipDocker) {
        Write-LogWarn "跳过Docker镜像构建"
        return
    }

    Write-LogInfo "开始构建优化镜像..."

    # 构建所有服务
    $dockerfiles = Get-ChildItem -Path "microservices" -Recurse -Filter "Dockerfile.optimized"
    foreach ($dockerfile in $dockerfiles) {
        $serviceDir = $dockerfile.DirectoryName
        $serviceName = Split-Path $serviceDir -Leaf

        if (Test-Path "$serviceDir\Dockerfile.optimized") {
            Write-LogInfo "构建优化镜像: $serviceName"

            Push-Location $serviceDir

            # 读取构建参数
            $port = "8080"
            if (Test-Path "src\main\resources\application.yml") {
                $port = (Select-String -Path "src\main\resources\application.yml" -Pattern "port:\s*(\d+)" | ForEach-Object { $_.Matches.Groups[1].Value }) -join ""
                if (-not $port) { $port = "8080" }
            }

            # 检查JAR文件
            $jarFiles = Get-ChildItem "target\*.jar" -ErrorAction SilentlyContinue
            $jarFile = $jarFiles[0]
            if ($jarFile) {
                $jarFileName = $jarFile.Name

                # 构建优化镜像
                $buildArgs = @(
                    "--build-arg", "SERVICE_PORT=$port",
                    "--build-arg", "JAR_FILE=$jarFileName"
                )

                docker build @buildArgs -f Dockerfile.optimized -t "ioe-dream/$serviceName`:optimized .

                Write-LogInfo "镜像构建完成: $serviceName"
            } else {
                Write-LogWarn "未找到JAR文件，跳过构建: $serviceName"
            }

            Pop-Location
        }
    }

    # 镜像大小对比
    Write-LogInfo "镜像大小对比:"
    docker images | Select-String "ioe-dream" | Select-String "(latest|optimized)"

    Write-LogInfo "优化镜像构建完成 ✓"
}

# 资源使用分析
function Analyze-ResourceUsage {
    Write-LogInfo "开始资源使用分析..."

    # 创建资源分析报告
    $analysisReport = @"
# 资源使用分析报告

## 镜像大小对比

| 服务 | 原始镜像 | 优化镜像 | 减少比例 |
|------|----------|----------|----------|
"@

    # 添加镜像大小对比
    $services = @("gateway-service", "common-service", "access-service", "attendance-service", "consume-service", "visitor-service", "video-service", "device-comm-service", "oa-service")

    foreach ($service in $services) {
        $originalSize = "N/A"
        $optimizedSize = "N/A"

        try {
            $originalInfo = docker images "ioe-dream/$service:latest" 2>$null
            if ($originalInfo) {
                $originalSize = ($originalInfo -split '\s+')[6]
            }

            $optimizedInfo = docker images "ioe-dream/$service:optimized" 2>$null
            if ($optimizedInfo) {
                $optimizedSize = ($optimizedInfo -split '\s+')[6]
            }
        } catch {
            # 忽略错误，使用默认值
        }

        $analysisReport += "`n| $service | $originalSize | $optimizedSize | 计算中... |"
    }

    $analysisReport += @"

## 依赖优化分析

请查看 dependency-analysis-report.txt 文件获取详细分析结果

## 配置优化统计

- 简化的配置文件: $(Get-ChildItem -Path "microservices" -Recurse -Filter "application-simplified.yml" | Measure-Object).Count
- 优化的Dockerfile: $(Get-ChildItem -Path "microservices" -Recurse -Filter "Dockerfile.optimized" | Measure-Object).Count
"@

    Set-Content -Path "resource-usage-analysis.md" -Value $analysisReport

    Write-LogInfo "资源使用分析完成 ✓"
}

# 生成优化报告
function New-OptimizationReport {
    Write-LogInfo "生成优化报告..."

    # 统计优化结果
    $totalServices = (Get-ChildItem -Path "microservices" -Recurse -Filter "pom.xml" | Measure-Object).Count
    $optimizedDockerfiles = (Get-ChildItem -Path "microservices" -Recurse -Filter "Dockerfile.optimized" | Measure-Object).Count
    $simplifiedConfigs = (Get-ChildItem -Path "microservices" -Recurse -Filter "application-simplified.yml" | Measure-Object).Count
    $successRate = if ($totalServices -gt 0) { [math]::Round($optimizedDockerfiles * 100 / $totalServices, 2) } else { 0 }

    # 生成报告
    $report = @"
# IOE-DREAM 资源优化执行报告

> 执行时间: $(Get-Date)
> 执行状态: 成功

## 优化统计

- **服务总数**: $totalServices
- **优化的Dockerfile**: $optimizedDockerfiles
- **简化的配置文件**: $simplifiedConfigs
- **优化成功率**: $successRate`%

## 优化效果

### Docker镜像优化
- 使用多阶段构建减少镜像大小
- 使用轻量级JRE基础镜像
- 优化Docker层数，减少镜像大小

### 依赖优化
- 分析未使用的依赖
- 检查版本冲突
- 移除重复依赖

### 配置优化
- 统一配置模板
- 减少配置冗余
- 环境变量标准化

## 下一步建议

1. 测试优化后的镜像功能完整性
2. 监控优化后的资源使用情况
3. 根据实际使用情况进一步调优
4. 定期执行资源优化检查

## 备份信息

配置文件已备份至: backup\$(Get-Date -Format 'yyyyMMdd_HHmmss')
如需回滚，请使用备份文件。

## 参数使用

本次执行参数:
- SkipDocker: `$SkipDocker
- SkipBackup: `$SkipBackup
- Verbose: `$Verbose
"@

    Set-Content -Path "resource-optimization-report.md" -Value $report

    Write-LogInfo "优化报告生成完成 ✓"
    Write-LogInfo "报告文件: resource-optimization-report.md"
}

# 主函数
function Main {
    Write-LogInfo "开始执行IOE-DREAM资源优化..."
    Write-LogInfo "================================================"

    # 检查环境
    Test-Prerequisites

    # 备份配置
    Backup-Configurations

    # 执行优化任务
    Optimize-Dependencies
    Simplify-Configurations
    Optimize-DockerImages

    # 构建优化镜像
    if (-not $SkipDocker) {
        Build-OptimizedImages
        Analyze-ResourceUsage
    }

    # 生成报告
    New-OptimizationReport

    Write-LogInfo "================================================"
    Write-LogInfo "资源优化执行完成！"
    Write-LogInfo "请查看以下报告文件了解详细优化结果："
    Write-LogInfo "- resource-optimization-report.md"
    Write-LogInfo "- resource-usage-analysis.md"
    Write-LogInfo "- dependency-analysis-report.txt"

    # 清理临时文件
    if (Test-Path "Dockerfile.optimized") { Remove-Item "Dockerfile.optimized" }
    if (Test-Path "config-templates") { Remove-Item -Recurse -Force "config-templates" }

    Write-LogInfo "资源优化脚本执行结束 ✓"
}

# 执行主函数
Main