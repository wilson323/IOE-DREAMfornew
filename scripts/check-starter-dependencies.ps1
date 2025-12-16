# =====================================================
# IOE-DREAM 启动器依赖检查脚本
# 版本: v1.0.0
# 描述: 检查所有微服务的Spring Boot/Cloud Starter依赖
# 创建时间: 2025-12-15
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$Verbose,

    [Parameter(Mandatory=$false)]
    [switch]$ShowAll
)

$ErrorActionPreference = "Continue"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

# =====================================================
# 必需的启动器列表
# =====================================================

$RequiredStarters = @(
    @{ Name = "spring-boot-starter"; Description = "核心启动器"; Required = $true },
    @{ Name = "spring-boot-starter-actuator"; Description = "监控端点"; Required = $true },
    @{ Name = "spring-cloud-starter-alibaba-nacos-discovery"; Description = "服务发现"; Required = $true },
    @{ Name = "spring-cloud-starter-alibaba-nacos-config"; Description = "配置中心"; Required = $true }
)

$OptionalStarters = @(
    @{ Name = "spring-boot-starter-web"; Description = "Web MVC"; Category = "Web" },
    @{ Name = "spring-boot-starter-webflux"; Description = "WebFlux"; Category = "Web" },
    @{ Name = "spring-boot-starter-security"; Description = "安全框架"; Category = "Security" },
    @{ Name = "spring-boot-starter-data-redis"; Description = "Redis"; Category = "Data" },
    @{ Name = "spring-boot-starter-validation"; Description = "Bean验证"; Category = "Validation" },
    @{ Name = "spring-boot-starter-aop"; Description = "AOP支持"; Category = "AOP" },
    @{ Name = "spring-boot-starter-mail"; Description = "邮件"; Category = "Mail" },
    @{ Name = "spring-boot-starter-quartz"; Description = "定时任务"; Category = "Scheduling" },
    @{ Name = "spring-boot-starter-websocket"; Description = "WebSocket"; Category = "WebSocket" },
    @{ Name = "spring-boot-starter-test"; Description = "测试"; Category = "Test" },
    @{ Name = "spring-cloud-starter-gateway"; Description = "API网关"; Category = "Gateway" },
    @{ Name = "spring-cloud-starter-alibaba-seata"; Description = "分布式事务"; Category = "Transaction" },
    @{ Name = "mybatis-plus-spring-boot3-starter"; Description = "MyBatis-Plus"; Category = "ORM" },
    @{ Name = "druid-spring-boot-3-starter"; Description = "Druid连接池"; Category = "DataSource" },
    @{ Name = "redisson-spring-boot-starter"; Description = "Redisson分布式锁"; Category = "Cache" },
    @{ Name = "jasypt-spring-boot-starter"; Description = "配置加密"; Category = "Security" },
    @{ Name = "resilience4j-spring-boot3"; Description = "熔断器"; Category = "Resilience" }
)

# =====================================================
# 微服务列表
# =====================================================

$Microservices = @(
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

# =====================================================
# 辅助函数
# =====================================================

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Get-PomStarters {
    param([string]$PomPath)

    if (-not (Test-Path $PomPath)) {
        return @()
    }

    $content = Get-Content $PomPath -Raw
    $starters = @()

    # 匹配所有starter依赖
    $pattern = '<artifactId>([^<]*-starter[^<]*)</artifactId>'
    $matches = [regex]::Matches($content, $pattern)

    foreach ($match in $matches) {
        $starters += $match.Groups[1].Value
    }

    return $starters | Sort-Object -Unique
}

# =====================================================
# 主程序
# =====================================================

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 启动器依赖检查" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

$results = @{}
$issueCount = 0

# 检查每个微服务
foreach ($service in $Microservices) {
    $pomPath = Join-Path $ProjectRoot "microservices\$service\pom.xml"
    $starters = Get-PomStarters -PomPath $pomPath

    $serviceResult = @{
        Name = $service
        Starters = $starters
        MissingRequired = @()
        HasActuator = $false
    }

    # 检查必需的启动器
    foreach ($required in $RequiredStarters) {
        $found = $starters | Where-Object { $_ -eq $required.Name }
        if (-not $found) {
            $serviceResult.MissingRequired += $required.Name
        }
        if ($required.Name -eq "spring-boot-starter-actuator" -and $found) {
            $serviceResult.HasActuator = $true
        }
    }

    $results[$service] = $serviceResult
}

# 输出结果
Write-ColorOutput "微服务启动器检查结果:" "Cyan"
Write-ColorOutput ""

foreach ($service in $Microservices) {
    $result = $results[$service]
    $statusColor = if ($result.MissingRequired.Count -eq 0) { "Green" } else { "Red" }
    $statusIcon = if ($result.MissingRequired.Count -eq 0) { "[OK]" } else { "[XX]" }

    Write-ColorOutput "  $statusIcon $($result.Name)" $statusColor

    if ($Verbose -or $result.MissingRequired.Count -gt 0) {
        if ($result.MissingRequired.Count -gt 0) {
            Write-ColorOutput "      缺失: $($result.MissingRequired -join ', ')" "Yellow"
            $issueCount += $result.MissingRequired.Count
        }
    }

    if ($ShowAll) {
        Write-ColorOutput "      已有: $($result.Starters.Count) 个启动器" "Gray"
        foreach ($starter in $result.Starters) {
            Write-ColorOutput "        - $starter" "Gray"
        }
    }
}

# 检查common模块
Write-ColorOutput ""
Write-ColorOutput "公共模块检查:" "Cyan"

$commonPom = Join-Path $ProjectRoot "microservices\microservices-common\pom.xml"
$commonStarters = Get-PomStarters -PomPath $commonPom

$hasActuator = $commonStarters -contains "spring-boot-starter-actuator"
$statusIcon = if ($hasActuator) { "[OK]" } else { "[XX]" }
$statusColor = if ($hasActuator) { "Green" } else { "Red" }

Write-ColorOutput "  $statusIcon microservices-common" $statusColor
if (-not $hasActuator) {
    Write-ColorOutput "      缺失: spring-boot-starter-actuator" "Yellow"
    $issueCount++
}

if ($ShowAll) {
    Write-ColorOutput "      已有: $($commonStarters.Count) 个启动器" "Gray"
}

# 汇总
Write-ColorOutput ""
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  检查结果汇总" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

if ($issueCount -eq 0) {
    Write-ColorOutput "  [成功] 所有服务的必需启动器已配置完整" "Green"
} else {
    Write-ColorOutput "  [警告] 发现 $issueCount 个启动器配置问题" "Yellow"
    Write-ColorOutput ""
    Write-ColorOutput "  建议修复:" "Yellow"
    Write-ColorOutput "    1. 在microservices-common/pom.xml中添加spring-boot-starter-actuator" "Gray"
    Write-ColorOutput "    2. 确保所有服务都能暴露/actuator/health端点" "Gray"
}

Write-ColorOutput ""
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  检查完成" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""

exit $issueCount
