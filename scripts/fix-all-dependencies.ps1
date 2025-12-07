# IOE-DREAM 全局依赖修复脚本
# 版本: v2.0.0
# 更新时间: 2025-01-30
# 功能: 修复iText PDF依赖、腾讯云OCR依赖、清理Maven缓存

param(
    [switch]$CleanCache,
    [switch]$SkipTests,
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 全局依赖修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 清理iText依赖缓存
Write-Host "[1/5] 清理iText PDF依赖缓存..." -ForegroundColor Yellow
$itextPaths = @(
    "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core\9.4.0",
    "$env:USERPROFILE\.m2\repository\com\itextpdf\html2pdf\9.4.0",
    "$env:USERPROFILE\.m2\repository\com\itextpdf\itext7-core"
)

foreach ($path in $itextPaths) {
    if (Test-Path $path) {
        Write-Host "  删除: $path" -ForegroundColor Gray
        Remove-Item -Recurse -Force $path -ErrorAction SilentlyContinue
    }
}

# 2. 清理腾讯云OCR依赖缓存
Write-Host "[2/5] 清理腾讯云OCR依赖缓存..." -ForegroundColor Yellow
$tencentPath = "$env:USERPROFILE\.m2\repository\com\tencentcloudapi\tencentcloud-sdk-java-ocr"
if (Test-Path $tencentPath) {
    Write-Host "  删除: $tencentPath" -ForegroundColor Gray
    Remove-Item -Recurse -Force $tencentPath -ErrorAction SilentlyContinue
}

# 3. 验证POM配置
Write-Host "[3/5] 验证POM配置..." -ForegroundColor Yellow
$rootPom = "D:\IOE-DREAM\pom.xml"
$microservicesPom = "D:\IOE-DREAM\microservices\pom.xml"

if (-not (Test-Path $rootPom)) {
    Write-Host "  错误: 找不到根POM文件: $rootPom" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $microservicesPom)) {
    Write-Host "  错误: 找不到microservices POM文件: $microservicesPom" -ForegroundColor Red
    exit 1
}

# 检查iText版本配置
$microservicesPomContent = Get-Content $microservicesPom -Raw
if ($microservicesPomContent -match 'itext7-core\.version.*9\.4\.0' -and
    $microservicesPomContent -match 'html2pdf\.version.*6\.3\.0') {
    Write-Host "  ✓ iText版本配置正确" -ForegroundColor Green
} else {
    Write-Host "  ⚠ iText版本配置可能有问题，请检查" -ForegroundColor Yellow
}

# 4. 重新构建microservices-common（必须先构建）
Write-Host "[4/5] 重新构建microservices-common..." -ForegroundColor Yellow
$commonPath = "D:\IOE-DREAM\microservices\microservices-common"
if (Test-Path $commonPath) {
    Push-Location $commonPath
    try {
        $buildArgs = @("clean", "install")
        if ($SkipTests) {
            $buildArgs += "-DskipTests"
        }
        if ($Verbose) {
            $buildArgs += "-X"
        }

        Write-Host "  执行: mvn $($buildArgs -join ' ')" -ForegroundColor Gray
        & mvn $buildArgs 2>&1 | ForEach-Object {
            if ($_ -match "ERROR|FAILURE") {
                Write-Host $_ -ForegroundColor Red
            } elseif ($_ -match "SUCCESS|BUILD SUCCESS") {
                Write-Host $_ -ForegroundColor Green
            } elseif ($Verbose) {
                Write-Host $_ -ForegroundColor Gray
            }
        }

        if ($LASTEXITCODE -ne 0) {
            Write-Host "  ✗ microservices-common构建失败" -ForegroundColor Red
            exit 1
        }
        Write-Host "  ✓ microservices-common构建成功" -ForegroundColor Green
    } finally {
        Pop-Location
    }
} else {
    Write-Host "  ⚠ 找不到microservices-common目录" -ForegroundColor Yellow
}

# 5. 验证依赖解析
Write-Host "[5/5] 验证依赖解析..." -ForegroundColor Yellow
$services = @(
    "microservices-common",
    "ioedream-visitor-service",
    "ioedream-consume-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-gateway-service",
    "ioedream-video-service"
)

foreach ($service in $services) {
    $servicePath = "D:\IOE-DREAM\microservices\$service"
    if (Test-Path $servicePath) {
        Push-Location $servicePath
        try {
            Write-Host "  检查: $service" -ForegroundColor Gray
            & mvn dependency:resolve -q 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-Host "    ✓ $service 依赖解析成功" -ForegroundColor Green
            } else {
                Write-Host "    ✗ $service 依赖解析失败" -ForegroundColor Red
            }
        } finally {
            Pop-Location
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 在IDE中刷新Maven项目" -ForegroundColor White
Write-Host "2. 清理并重新构建项目" -ForegroundColor White
Write-Host "3. 如果问题仍然存在，请检查网络连接和Maven仓库配置" -ForegroundColor White
Write-Host ""
