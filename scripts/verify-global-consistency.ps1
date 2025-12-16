# =====================================================
# 全局一致性验证脚本
# 版本: v1.0.0
# 描述: 验证全局一致性优化是否全部完成
# 创建时间: 2025-12-14
# =====================================================

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 全局一致性验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$totalIssues = 0
$passedChecks = 0
$failedChecks = 0

# 1. 检查架构违规
Write-Host "[检查] 架构边界违规（Feign使用）..." -ForegroundColor Cyan
$feignServices = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "@EnableFeignClients" |
    Where-Object { $_.Path -notmatch "Test\.java$" -and $_.Line -notmatch "//|/\*" } |
    Select-Object -ExpandProperty Path -Unique

if ($feignServices) {
    Write-Host "[违规] 发现Feign使用: $($feignServices.Count)个文件" -ForegroundColor Red
    $feignServices | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
    $failedChecks++
    $totalIssues++
} else {
    Write-Host "[通过] 无Feign违规" -ForegroundColor Green
    $passedChecks++
}

# 2. 检查@Autowired违规
Write-Host "[检查] @Autowired违规..." -ForegroundColor Cyan
$autowiredFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*.java" |
    Select-String -Pattern "^\s*@Autowired\b" |
    Where-Object { $_.Path -notmatch "Test\.java$" -and $_.Line -notmatch "//|/\*" } |
    Select-Object -ExpandProperty Path -Unique

if ($autowiredFiles) {
    Write-Host "[违规] 发现@Autowired: $($autowiredFiles.Count)个文件" -ForegroundColor Red
    $failedChecks++
    $totalIssues++
} else {
    Write-Host "[通过] 无@Autowired违规" -ForegroundColor Green
    $passedChecks++
}

# 3. 检查配置安全（明文密码默认值）
Write-Host "[检查] 配置安全（明文密码默认值）..." -ForegroundColor Cyan
$plainPasswords = Get-ChildItem -Path "microservices" -Recurse -Include "*.yml","*.yaml" |
    Select-String -Pattern 'password:\s*\$\{MYSQL_PASSWORD:123456\}|password:\s*\$\{REDIS_PASSWORD:redis123\}|password:\s*\$\{NACOS_PASSWORD:nacos\}' |
    Where-Object { $_.Path -match "application\.yml$" -and $_.Path -notmatch "test|template" } |
    Select-Object -ExpandProperty Path -Unique

if ($plainPasswords) {
    Write-Host "[违规] 发现疑似明文密码默认值: $($plainPasswords.Count)个文件" -ForegroundColor Red
    $plainPasswords | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
    $failedChecks++
    $totalIssues++
} else {
    Write-Host "[通过] 无明文密码默认值" -ForegroundColor Green
    $passedChecks++
}

# 4. 检查RESTful规范（POST查询接口）
Write-Host "[检查] RESTful规范（POST查询接口）..." -ForegroundColor Cyan
$postQueryPattern = '@PostMapping\("/get|@PostMapping\("/query|@PostMapping\("/list'
$postQuery = Get-ChildItem -Path "microservices" -Recurse -Filter "*Controller.java" |
    Select-String -Pattern $postQueryPattern |
    Where-Object { $_.Line -notmatch "//|/\*" } |
    Select-Object -ExpandProperty Path -Unique

if ($postQuery) {
    Write-Host "[违规] 发现POST查询接口: $($postQuery.Count)个文件" -ForegroundColor Red
    $postQuery | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
    $failedChecks++
    $totalIssues++
} else {
    Write-Host "[通过] 无POST查询接口违规" -ForegroundColor Green
    $passedChecks++
}

# 5. 检查分布式追踪配置
Write-Host "[检查] 分布式追踪配置..." -ForegroundColor Cyan
$servicesWithoutTracing = @()
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
    $configFile = "microservices\$service\src\main\resources\application.yml"
    if (Test-Path $configFile) {
        $hasTracing = Select-String -Path $configFile -Pattern "tracing:\s*$|tracing:\s*enabled:\s*true" -Quiet
        if (-not $hasTracing) {
            $servicesWithoutTracing += $service
        }
    }
}

if ($servicesWithoutTracing.Count -gt 0) {
    Write-Host "[违规] 缺少tracing配置的服务: $($servicesWithoutTracing.Count)个" -ForegroundColor Red
    $servicesWithoutTracing | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
    $failedChecks++
    $totalIssues++
} else {
    Write-Host "[通过] 所有服务已配置tracing" -ForegroundColor Green
    $passedChecks++
}

# 输出总结
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证总结" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "通过检查: $passedChecks" -ForegroundColor Green
Write-Host "失败检查: $failedChecks" -ForegroundColor $(if ($failedChecks -eq 0) { "Green" } else { "Red" })
Write-Host "总问题数: $totalIssues" -ForegroundColor $(if ($totalIssues -eq 0) { "Green" } else { "Red" })

if ($totalIssues -eq 0) {
    Write-Host ""
    Write-Host "[成功] 全局一致性验证全部通过！" -ForegroundColor Green
    exit 0
} else {
    Write-Host ""
    Write-Host "[失败] 发现 $totalIssues 个问题，请修复后重新验证" -ForegroundColor Red
    exit 1
}
