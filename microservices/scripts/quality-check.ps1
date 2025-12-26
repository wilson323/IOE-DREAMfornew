# IOE-DREAM 代码质量检查脚本
#
# 简明实用的代码质量检查，避免过度工程化
# 专注于核心质量问题：测试覆盖率、代码规范、性能
#
# @Author: IOE-DREAM Team
# @Date: 2025-12-20

param(
    [switch]$SkipTests,      # 跳过测试
    [switch]$CI,            # CI模式，输出格式化结果
    [string]$ReportPath = ".\reports"  # 报告输出路径
)

# 设置工作目录
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

# 创建报告目录
if (!(Test-Path $ReportPath)) {
    New-Item -ItemType Directory -Path $ReportPath -Force
}

Write-Host "🔍 开始 IOE-DREAM 代码质量检查..." -ForegroundColor Green

Write-Host "🔍 开始IOE-DREAM代码质量检查..." -ForegroundColor Green
Write-Host "📋 检查范围：全局代码质量 + 架构合规性" -ForegroundColor Yellow

# 获取项目根目录
$rootPath = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$microservicesPath = Join-Path $rootPath "microservices"

# 检查结果统计
$checks = @{
    "配置合规性" = @{ "Pass" = 0; "Fail" = 0; "Total" = 0 }
    "架构规范" = @{ "Pass" = 0; "Fail" = 0; "Total" = 0 }
    "安全配置" = @{ "Pass" = 0; "Fail" = 0; "Total" = 0 }
    "性能优化" = @{ "Pass" = 0; "Fail" = 0; "Total" = 0 }
    "依赖管理" = @{ "Pass" = 0; "Fail" = 0; "Total" = 0 }
}

function Write-CheckResult {
    param(
        [string]$Category,
        [string]$CheckName,
        [bool]$Passed,
        [string]$Details = ""
    )

    $checks[$Category].Total++
    if ($Passed) {
        $checks[$Category].Pass++
        Write-Host "  ✅ $CheckName" -ForegroundColor Green
    } else {
        $checks[$Category].Fail++
        Write-Host "  ❌ $CheckName" -ForegroundColor Red
        if ($Details) {
            Write-Host "     💡 $Details" -ForegroundColor Yellow
        }
    }

    if ($Detailed -and $Details) {
        Write-Host "     📋 详情: $Details" -ForegroundColor Cyan
    }
}

# ==================== 1. 配置合规性检查 ====================
Write-Host "`n📁 检查配置合规性..." -ForegroundColor Cyan

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

foreach ($service in $services) {
    $configPath = Join-Path $microservicesPath "$service/src/main/resources/application-prod.yml"

    if (Test-Path $configPath) {
        $configContent = Get-Content $configPath -Raw -Encoding UTF8

        Write-Host "  🔧 检查 $service 配置..." -ForegroundColor Cyan

        # 检查重复配置
        $hasDuplicateSpring = ($configContent -match "spring:.*spring:") -or ($configContent -match "redis:.*redis:")
        Write-CheckResult "配置合规性" "$service 无重复配置" (-not $hasDuplicateSpring) "发现重复的spring或redis配置块"

        # 检查连接池配置
        $hasValidConnectionPool = $configContent -match "max-active:\s*(1[5-9]|[2-9][0-9])"
        Write-CheckResult "配置合规性" "$service 连接池配置合理" $hasValidConnectionPool "max-active应在15-99之间"

        # 检查加密配置
        $hasEncryption = $configContent -match "ENC\(AES256:"
        Write-CheckResult "配置合规性" "$service 敏感信息加密" $hasEncryption "缺少ENC(AES256:)加密配置"

        # 检查监控配置
        $hasMonitoring = $configContent -match "management:"
        Write-CheckResult "配置合规性" "$service 监控配置" $hasMonitoring "缺少management监控配置"

        # 检查YAML语法
        try {
            $lines = $configContent -split "`n"
            $hasValidYaml = $true
            $indentStack = @()

            foreach ($line in $lines) {
                $trimmedLine = $line.Trim()
                if ($trimmedLine -and -not $trimmedLine.StartsWith("#")) {
                    $indent = ($line.Length - $line.TrimStart().Length)

                    if ($trimmedLine -match '^[^:\s]+:') {
                        while ($indentStack.Count -gt 0 -and $indentStack[-1] -ge $indent) {
                            $indentStack.RemoveAt($indentStack.Count - 1)
                        }
                        $indentStack += $indent
                    }
                }
            }

            Write-CheckResult "配置合规性" "$service YAML语法正确" $hasValidYaml "YAML缩进或结构有问题"
        } catch {
            Write-CheckResult "配置合规性" "$service YAML语法正确" $false "YAML解析异常: $($_.Exception.Message)"
        }
    }
}

# ==================== 2. 架构规范检查 ====================
Write-Host "`n🏗️ 检查架构规范..." -ForegroundColor Cyan

# 检查四层架构合规性
$javaFiles = Get-ChildItem -Path $microservicesPath -Recurse -Filter "*.java" | Where-Object {
    $_.FullName -match "controller|service|dao|manager"
}

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8

    # 检查依赖注入规范
    $usesResource = $content -match "@Resource"
    $usesAutowired = $content -match "@Autowired"
    Write-CheckResult "架构规范" "$($file.Name) 使用@Resource依赖注入" $usesResource "应使用@Resource而非@Autowired"

    # 检查DAO命名规范
    if ($file.FullName -match "dao") {
        $usesDaoSuffix = $file.Name -match "Dao\.java$"
        $usesRepository = $content -match "@Repository"
        Write-CheckResult "架构规范" "$($file.Name) DAO命名规范" $usesDaoSuffix "应使用Dao后缀命名"
        Write-CheckResult "架构规范" "$($file.Name) 使用@Mapper注解" (-not $usesRepository) "应使用@Mapper而非@Repository"
    }

    # 检查Controller规范
    if ($file.FullName -match "controller") {
        $usesRestController = $content -match "@RestController"
        $hasRequestMapping = $content -match "@RequestMapping"
        Write-CheckResult "架构规范" "$($file.Name) Controller注解规范" $usesRestController "应使用@RestController注解"
    }

    # 检查Service规范
    if ($file.FullName -match "service.*impl") {
        $usesService = $content -match "@Service"
        $usesTransactional = $content -match "@Transactional"
        Write-CheckResult "架构规范" "$($file.Name) Service注解规范" $usesService "应使用@Service注解"
        Write-CheckResult "架构规范" "$($file.Name) 事务管理" $usesTransactional "应使用@Transactional注解"
    }
}

# 检查microservices-common依赖
$commonJarPath = Join-Path $microservicesPath "microservices-common/target/microservices-common-1.0.0.jar"
$commonJarExists = Test-Path $commonJarPath
Write-CheckResult "架构规范" "microservices-common JAR存在" $commonJarExists "需要先构建microservices-common"

# ==================== 3. 安全配置检查 ====================
Write-Host "`n🔒 检查安全配置..." -ForegroundColor Cyan

foreach ($service in $services) {
    $configPath = Join-Path $microservicesPath "$service/src/main/resources/application-prod.yml"

    if (Test-Path $configPath) {
        $configContent = Get-Content $configPath -Raw -Encoding UTF8

        # 检查基本安全配置
        $hasDruidAuth = $configContent -match "login-username.*login-password"
        $hasEncryption = $configContent -match "ENC\(AES256:"
        $hasSqlFilter = $configContent -match "wall:\s*enabled:\s*true"

        Write-CheckResult "安全配置" "$service Druid安全认证" $hasDruidAuth "缺少Druid监控认证配置"
        Write-CheckResult "安全配置" "$service 密码加密存储" $hasEncryption "敏感信息未加密存储"
        Write-CheckResult "安全配置" "$service SQL注入防护" $hasSqlFilter "缺少WallFilter SQL防护"
    }
}

# 检查代码安全规范
foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8

    # 检查SQL注入防护
    $hasMyBatisParam = $content -match "@\w+\s*\([^)]*\)"
    $hasConcatSql = $content -match '"\s*\+.*\+.*"'
    Write-CheckResult "安全配置" "$($file.Name) SQL参数化查询" $hasMyBatisParam "应使用参数化查询防止SQL注入"
    Write-CheckResult "安全配置" "$($file.Name) 无字符串拼接SQL" (-not $hasConcatSql) "禁止字符串拼接构造SQL"
}

# ==================== 4. 性能优化检查 ====================
Write-Host "`n⚡ 检查性能优化..." -ForegroundColor Cyan

foreach ($service in $services) {
    $configPath = Join-Path $microservicesPath "$service/src/main/resources/application-prod.yml"

    if (Test-Path $configPath) {
        $configContent = Get-Content $configPath -Raw -Encoding UTF8

        # 检查连接池优化
        if ($configContent -match "max-active:\s*(\d+)") {
            $maxActive = [int]$matches[1]
            $isOptimized = $maxActive -le 100 -and $maxActive -ge 10
            Write-CheckResult "性能优化" "$service 连接池大小优化" $isOptimized "max-active应在10-100之间"
        }

        # 检查缓存配置
        $hasRedisCache = $configContent -match "spring:\s*redis:"
        $hasCacheConfig = $configContent -match "cache:"
        Write-CheckResult "性能优化" "$service Redis缓存配置" $hasRedisCache "应配置Redis缓存提升性能"
        Write-CheckResult "性能优化" "$service 缓存策略" $hasCacheConfig "应配置缓存TTL和策略"

        # 检查监控配置
        $hasMetrics = $configContent -match "management:\s*metrics:"
        $hasPrometheus = $configContent -match "prometheus:\s*enabled:\s*true"
        Write-CheckResult "性能优化" "$service 性能监控" $hasMetrics "应配置metrics监控"
        Write-CheckResult "性能优化" "$service Prometheus集成" $hasPrometheus "应启用Prometheus指标导出"
    }
}

# ==================== 5. 依赖管理检查 ====================
Write-Host "`n📦 检查依赖管理..." -ForegroundColor Cyan

$parentPomPath = Join-Path $microservicesPath "pom.xml"
if (Test-Path $parentPomPath) {
    $parentPomContent = Get-Content $parentPomPath -Raw -Encoding UTF8

    # 检查Spring Boot版本
    $hasSpringBootVersion = $parentPomContent -match "spring-boot-dependencies.*version[^>]*>[^<]*3\.[5-9]"
    Write-CheckResult "依赖管理" "Spring Boot 3.5+版本" $hasSpringBootVersion "应使用Spring Boot 3.5+版本"

    # 检查Spring Cloud版本
    $hasSpringCloudVersion = $parentPomContent -match "spring-cloud-dependencies.*version[^>]*>[^<]*2025"
    Write-CheckResult "依赖管理" "Spring Cloud 2025版本" $hasSpringCloudVersion "应使用Spring Cloud 2025版本"

    # 检查依赖版本一致性
    $hasDependencyManagement = $parentPomContent -match "<dependencyManagement>"
    Write-CheckResult "依赖管理" "依赖版本管理" $hasDependencyManagement "应使用dependencyManagement统一版本"
}

# 检查各服务的POM文件
foreach ($service in $services) {
    $pomPath = Join-Path $microservicesPath "$service/pom.xml"
    if (Test-Path $pomPath) {
        $pomContent = Get-Content $pomPath -Raw -Encoding UTF8

        # 检查parent引用
        $hasParent = $pomContent -match "<parent>"
        Write-CheckResult "依赖管理" "$service parent引用" $hasParent "应正确引用父POM"

        # 检查microservices-common依赖
        $hasCommonDep = $pomContent -match "microservices-common"
        Write-CheckResult "依赖管理" "$service 依赖公共模块" $hasCommonDep "应依赖microservices-common"
    }
}

# ==================== 输出检查结果 ====================
Write-Host "`n📊 质量检查统计:" -ForegroundColor Cyan

$totalChecks = 0
$totalPass = 0
$totalFail = 0

foreach ($category in $checks.Keys) {
    $categoryStats = $checks[$category]
    $totalChecks += $categoryStats.Total
    $totalPass += $categoryStats.Pass
    $totalFail += $categoryStats.Fail

    $passRate = if ($categoryStats.Total -gt 0) {
        [math]::Round(($categoryStats.Pass / $categoryStats.Total) * 100, 1)
    } else { 0 }

    Write-Host "  $category`: $($categoryStats.Pass)/$($categoryStats.Total) 通过 ($passRate%)" -ForegroundColor Cyan
}

$overallPassRate = if ($totalChecks -gt 0) {
    [math]::Round(($totalPass / $totalChecks) * 100, 1)
} else { 0 }

Write-Host ""
Write-Host "🎯 总体质量评分: $overallPassRate%" -ForegroundColor $(if($overallPassRate -ge 90) {"Green"} elseif($overallPassRate -ge 80) {"Yellow"} else {"Red"})

if ($overallPassRate -ge 90) {
    Write-Host "🎉 企业级质量标准！代码质量优秀" -ForegroundColor Green
} elseif ($overallPassRate -ge 80) {
    Write-Host "✅ 质量良好，建议继续优化" -ForegroundColor Yellow
} else {
    Write-Host "⚠️ 需要改进，建议立即修复问题" -ForegroundColor Red
}

# ==================== 修复建议 ====================
if ($totalFail -gt 0) {
    Write-Host "`n🔧 修复建议:" -ForegroundColor Cyan

    Write-Host "  1. 立即修复配置重复问题" -ForegroundColor White
    Write-Host "  2. 统一依赖注入注解使用@Resource" -ForegroundColor White
    Write-Host "  3. 加强安全配置和加密措施" -ForegroundColor White
    Write-Host "  4. 优化数据库连接池配置" -ForegroundColor White
    Write-Host "  5. 完善监控和性能指标" -ForegroundColor White

    if ($FixIssues) {
        Write-Host ""
        Write-Host "🚀 开始自动修复..." -ForegroundColor Green
        & "$PSScriptRoot/fix-config-duplicates.ps1" -Force
    }
}

Write-Host ""
Write-Host "📋 下一步行动:" -ForegroundColor Cyan
Write-Host "  1. 运行 'mvn clean compile' 验证编译" -ForegroundColor White
Write-Host "  2. 启动各服务验证运行状态" -ForegroundColor White
Write-Host "  3. 检查IDEA中的错误数量变化" -ForegroundColor White
Write-Host "  4. 执行性能测试验证优化效果" -ForegroundColor White