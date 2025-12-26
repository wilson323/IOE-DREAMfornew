###############################################################################
# Spring Boot 3.5 + Jakarta规范快速检查脚本 (PowerShell版)
#
# 用途: 快速验证技术栈一致性
# 作者: Spring Boot 3.5 + Jakarta规范守护专家
# 日期: 2025-12-26
###############################################################################

# 错误处理
$ErrorActionPreference = "Continue"

# 计数器
$script:violations = 0
$script:warnings = 0
$script:passes = 0

# 打印函数
function Print-Header {
    param([string]$Message)
    Write-Host "`n=============================================" -ForegroundColor Cyan
    Write-Host "$Message" -ForegroundColor Cyan
    Write-Host "=============================================`n" -ForegroundColor Cyan
}

function Print-Success {
    param([string]$Message)
    Write-Host "✅ $Message" -ForegroundColor Green
    $script:passes++
}

function Print-Error {
    param([string]$Message)
    Write-Host "❌ $Message" -ForegroundColor Red
    $script:violations++
}

function Print-Warning {
    param([string]$Message)
    Write-Host "⚠️  $Message" -ForegroundColor Yellow
    $script:warnings++
}

# 设置项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"

if (-not (Test-Path $microservicesDir)) {
    Write-Host "错误: 找不到microservices目录: $microservicesDir" -ForegroundColor Red
    exit 1
}

Set-Location $microservicesDir

###############################################################################
# 1. 检查Spring Boot版本一致性
###############################################################################
Print-Header "步骤1: 检查Spring Boot版本一致性"

$pomContent = Get-Content "pom.xml" -Raw
if ($pomContent -match '<spring-boot.version>([^<]+)</spring-boot.version>') {
    $bootVersion = $matches[1]
    if ($bootVersion -eq "3.5.8") {
        Print-Success "Spring Boot版本: $bootVersion (符合要求)"
    } else {
        Print-Error "Spring Boot版本: $bootVersion (应为3.5.8)"
    }
} else {
    Print-Error "无法读取Spring Boot版本"
}

###############################################################################
# 2. 检查javax.*违规使用
###############################################################################
Print-Header "步骤2: 检查javax.*违规使用"

$javaxFiles = Get-ChildItem -Path . -Recurse -Filter "*.java" -File |
    Select-String -Pattern "import javax\.(annotation|validation|persistence|servlet|xml\.bind)\." |
    Select-Object -Unique -ExpandProperty Path

if ($javaxFiles.Count -eq 0) {
    Print-Success "javax.*违规使用: 0次 (符合要求)"
} else {
    Print-Error "javax.*违规使用: $($javaxFiles.Count)次文件"
    Write-Host "违规文件列表:" -ForegroundColor Red
    $javaxFiles | Select-Object -First 10 | ForEach-Object {
        Write-Host "  - $_" -ForegroundColor Red
    }
}

###############################################################################
# 3. 检查Jakarta.*正确使用
###############################################################################
Print-Header "步骤3: 检查Jakarta.*正确使用"

$jakartaResourceCount = (Get-ChildItem -Path . -Recurse -Filter "*.java" -File |
    Select-String -Pattern "import jakarta.annotation.Resource" |
    Measure-Object).Count

if ($jakartaResourceCount -gt 0) {
    Print-Success "jakarta.annotation.Resource使用: $jakartaResourceCount次"
} else {
    Print-Warning "jakarta.annotation.Resource使用: 0次 (可能存在未迁移的@Autowired)"
}

###############################################################################
# 4. 检查依赖注入注解规范
###############################################################################
Print-Header "步骤4: 检查依赖注入注解规范"

$autowiredCount = (Get-ChildItem -Path . -Recurse -Filter "*.java" -File |
    Select-String -Pattern "@Autowired" |
    Measure-Object).Count

$resourceCount = (Get-ChildItem -Path . -Recurse -Filter "*.java" -File |
    Select-String -Pattern "@Resource" |
    Measure-Object).Count

$totalInjection = $autowiredCount + $resourceCount

if ($totalInjection -gt 0) {
    $resourcePercentage = [math]::Round(($resourceCount / $totalInjection) * 100, 1)
    Print-Success "依赖注入统计: @Resource $resourceCount次 ($resourcePercentage%), @Autowired $autowiredCount次"

    if ($autowiredCount -gt 0) {
        Print-Warning "发现$autowiredCount处@Autowired使用，建议统一为@Resource"
    }
} else {
    Print-Warning "未发现依赖注入注解使用"
}

###############################################################################
# 5. 检查OpenAPI规范遵循
###############################################################################
Print-Header "步骤5: 检查OpenAPI 3.0规范遵循"

$requiredModeCount = (Get-ChildItem -Path . -Recurse -Filter "*.java" -File |
    Select-String -Pattern "requiredMode" |
    Measure-Object).Count

if ($requiredModeCount -eq 0) {
    Print-Success "OpenAPI 3.1违规(requiredMode): 0次 (符合要求)"
} else {
    Print-Error "OpenAPI 3.1违规(requiredMode): $requiredModeCount次"
}

###############################################################################
# 6. 检查Java版本
###############################################################################
Print-Header "步骤6: 检查Java版本配置"

if ($pomContent -match '<java.version>([^<]+)</java.version>') {
    $javaVersion = $matches[1]
}

if ($pomContent -match '<maven.compiler.source>([^<]+)</maven.compiler.source>') {
    $mavenCompilerSource = $matches[1]
}

if ($javaVersion -eq "17" -and $mavenCompilerSource -eq "17") {
    Print-Success "Java版本配置: $javaVersion (符合要求)"
} else {
    Print-Error "Java版本配置: source=$mavenCompilerSource (应为17)"
}

###############################################################################
# 7. 检查父POM统一性
###############################################################################
Print-Header "步骤7: 检查父POM统一性"

$totalPoms = (Get-ChildItem -Path . -Recurse -Filter "pom.xml" -File | Measure-Object).Count
$unifiedParentPoms = (Get-ChildItem -Path . -Recurse -Filter "pom.xml" -File |
    Select-String -Pattern "ioedream-microservices-parent" |
    Select-Object -Unique -ExpandProperty Path |
    Measure-Object).Count

if ($totalPoms -eq $unifiedParentPoms) {
    Print-Success "父POM统一性: $unifiedParentPoms/$totalPoms (100%)"
} else {
    Print-Error "父POM统一性: $unifiedParentPoms/$totalPoms (应全部使用统一父POM)"
}

###############################################################################
# 8. 检查Maven模块结构
###############################################################################
Print-Header "步骤8: 检查Maven模块结构"

$modules = Select-String -Path "pom.xml" -Pattern "<module>([^<]+)</module>" -AllMatches |
    ForEach-Object { $_.Matches } |
    ForEach-Object { $_.Groups[1].Value }

$moduleCount = $modules.Count
Print-Success "Maven模块总数: $moduleCount"

$commonModules = ($modules | Where-Object { $_ -like "microservices-common*" } | Measure-Object).Count
$serviceModules = ($modules | Where-Object { $_ -like "ioedream-*-service" } | Measure-Object).Count

Print-Success "公共库模块: $commonModules个"
Print-Success "业务微服务: $serviceModules个"

###############################################################################
# 9. 生成健康度评分
###############################################################################
Print-Header "技术栈健康度评分"

# 计算健康度 (简单算法)
$healthScore = 100

if ($javaxFiles.Count -gt 0) {
    $healthScore -= 20
}

if ($requiredModeCount -gt 0) {
    $healthScore -= 10
}

if ($autowiredCount -gt 50) {
    $healthScore -= 5
}

if ($bootVersion -ne "3.5.8") {
    $healthScore -= 30
}

if ($healthScore -ge 95) {
    Print-Success "健康度评分: $healthScore/100 (企业级优秀)"
    $grade = "优秀"
} elseif ($healthScore -ge 80) {
    Print-Warning "健康度评分: $healthScore/100 (良好)"
    $grade = "良好"
} else {
    Print-Error "健康度评分: $healthScore/100 (需要改进)"
    $grade = "需要改进"
}

###############################################################################
# 10. 总结报告
###############################################################################
Print-Header "验证总结"

Write-Host "通过项: " -NoNewline
Write-Host "$passes" -ForegroundColor Green

Write-Host "警告项: " -NoNewline
Write-Host "$warnings" -ForegroundColor Yellow

Write-Host "错误项: " -NoNewline
Write-Host "$violations" -ForegroundColor Red

Write-Host "`n=============================================" -ForegroundColor Cyan
Write-Host "技术栈等级: $grade" -ForegroundColor Cyan
Write-Host "健康度评分: $healthScore/100" -ForegroundColor Cyan
Write-Host "=============================================`n" -ForegroundColor Cyan

if ($violations -eq 0) {
    Write-Host "🎉 技术栈验证通过！`n" -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ 技术栈验证失败，请修复错误项`n" -ForegroundColor Red
    exit 1
}
