# IOE-DREAM 依赖验证脚本
# 功能：验证所有微服务的依赖配置是否正确
# 作者：AI Assistant
# 日期：2025-01-30

$ErrorActionPreference = "Continue"
$workspaceRoot = $PSScriptRoot

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 依赖验证脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allPassed = $true

# 验证1: 检查父POM配置
Write-Host "[验证1] 检查父POM配置..." -ForegroundColor Yellow
$parentPom = Join-Path $workspaceRoot "pom.xml"
if (Test-Path $parentPom) {
    $content = Get-Content $parentPom -Raw -Encoding UTF-8

    $checks = @{
        "itext7-core版本属性" = $content -match "itext7-core\.version.*9\.4\.0"
        "html2pdf版本属性" = $content -match "html2pdf\.version.*6\.3\.0"
        "itext7-core依赖管理" = $content -match "artifactId\s*>\s*itext7-core\s*<"
        "html2pdf依赖管理" = $content -match "artifactId\s*>\s*html2pdf\s*<"
    }

    foreach ($check in $checks.GetEnumerator()) {
        if ($check.Value) {
            Write-Host "  ✓ $($check.Key)" -ForegroundColor Green
        } else {
            Write-Host "  ✗ $($check.Key)" -ForegroundColor Red
            $allPassed = $false
        }
    }
} else {
    Write-Host "  ✗ 父POM不存在" -ForegroundColor Red
    $allPassed = $false
}

Write-Host ""

# 验证2: 检查microservices-common配置
Write-Host "[验证2] 检查microservices-common配置..." -ForegroundColor Yellow
$commonPom = Join-Path $workspaceRoot "microservices-common\pom.xml"
if (Test-Path $commonPom) {
    $content = Get-Content $commonPom -Raw -Encoding UTF-8

    if ($content -match "artifactId\s*>\s*itext7-core\s*<" -and
        $content -notmatch "artifactId\s*>\s*itext-core\s*<") {
        Write-Host "  ✓ 正确使用itext7-core" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 配置错误：使用了itext-core或缺少itext7-core" -ForegroundColor Red
        $allPassed = $false
    }

    if ($content -match "artifactId\s*>\s*html2pdf\s*<") {
        Write-Host "  ✓ 正确配置html2pdf" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 缺少html2pdf配置" -ForegroundColor Red
        $allPassed = $false
    }
} else {
    Write-Host "  ✗ microservices-common/pom.xml不存在" -ForegroundColor Red
    $allPassed = $false
}

Write-Host ""

# 验证3: 检查业务服务配置
Write-Host "[验证3] 检查业务服务配置..." -ForegroundColor Yellow
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-video-service",
    "analytics"
)

$serviceErrors = 0
foreach ($service in $services) {
    $servicePom = Join-Path $workspaceRoot $service "pom.xml"
    if (-not (Test-Path $servicePom)) {
        Write-Host "  ⚠ $service/pom.xml 不存在，跳过" -ForegroundColor Yellow
        continue
    }

    $content = Get-Content $servicePom -Raw -Encoding UTF-8

    # 检查是否有错误的itext-core引用
    if ($content -match 'artifactId\s*>\s*itext-core\s*<') {
        Write-Host "  ✗ $service 使用了错误的itext-core" -ForegroundColor Red
        $serviceErrors++
        $allPassed = $false
    } elseif ($content -match 'microservices-common') {
        Write-Host "  ✓ $service 通过common间接依赖（正确）" -ForegroundColor Green
    } elseif ($content -match 'itext7-core') {
        Write-Host "  ✓ $service 直接依赖itext7-core（可接受）" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ $service 未使用itext相关功能" -ForegroundColor Yellow
    }
}

if ($serviceErrors -eq 0) {
    Write-Host "  ✓ 所有服务配置正确" -ForegroundColor Green
}

Write-Host ""

# 验证4: 检查Maven本地仓库
Write-Host "[验证4] 检查Maven本地仓库..." -ForegroundColor Yellow
$mavenRepo = "$env:USERPROFILE\.m2\repository\com\itextpdf"

# 检查错误的缓存
$wrongCache = @(
    "itext-core\9.4.0",
    "html2pdf\9.4.0"
)

$hasWrongCache = $false
foreach ($cache in $wrongCache) {
    $cachePath = Join-Path $mavenRepo $cache
    if (Test-Path $cachePath) {
        Write-Host "  ✗ 发现错误的缓存: $cache" -ForegroundColor Red
        Write-Host "    建议运行: .\fix-itext-dependencies.ps1 -CleanCache" -ForegroundColor Yellow
        $hasWrongCache = $true
        $allPassed = $false
    }
}

# 检查正确的依赖
$correctCache = @(
    "itext7-core\9.4.0",
    "html2pdf\6.3.0"
)

foreach ($cache in $correctCache) {
    $cachePath = Join-Path $mavenRepo $cache
    if (Test-Path $cachePath) {
        Write-Host "  ✓ 正确的缓存存在: $cache" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ 缓存不存在: $cache（将在首次构建时下载）" -ForegroundColor Yellow
    }
}

if (-not $hasWrongCache) {
    Write-Host "  ✓ 没有发现错误的缓存" -ForegroundColor Green
}

Write-Host ""

# 总结
Write-Host "========================================" -ForegroundColor Cyan
if ($allPassed) {
    Write-Host "验证结果: ✅ 全部通过" -ForegroundColor Green
    Write-Host ""
    Write-Host "下一步操作：" -ForegroundColor Yellow
    Write-Host "1. 如果IDE仍显示错误，请按照 IDE_REFRESH_GUIDE.md 刷新IDE" -ForegroundColor White
    Write-Host "2. 运行构建验证: mvn clean compile -DskipTests" -ForegroundColor White
} else {
    Write-Host "验证结果: ❌ 发现问题" -ForegroundColor Red
    Write-Host ""
    Write-Host "修复建议：" -ForegroundColor Yellow
    Write-Host "1. 运行修复脚本: .\fix-itext-dependencies.ps1 -ForceUpdate" -ForegroundColor White
    Write-Host "2. 检查上述错误并修复" -ForegroundColor White
    Write-Host "3. 重新运行验证: .\verify-dependencies.ps1" -ForegroundColor White
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

exit ($allPassed ? 0 : 1)
