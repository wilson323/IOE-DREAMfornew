# IOE-DREAM 强制下载依赖脚本
# 版本: v1.0.0
# 功能: 强制下载iText和腾讯云OCR依赖到本地仓库

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 强制下载依赖脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 清理iText依赖缓存
Write-Host "[1/4] 清理iText依赖缓存..." -ForegroundColor Yellow
$itextPaths = @(
    "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core",
    "$env:USERPROFILE\.m2\repository\com\itextpdf\itext7-core",
    "$env:USERPROFILE\.m2\repository\com\itextpdf\html2pdf"
)

foreach ($path in $itextPaths) {
    if (Test-Path $path) {
        Write-Host "  删除: $path" -ForegroundColor Gray
        Remove-Item -Recurse -Force $path -ErrorAction SilentlyContinue
    }
}

# 2. 清理腾讯云OCR依赖缓存
Write-Host "[2/4] 清理腾讯云OCR依赖缓存..." -ForegroundColor Yellow
$tencentPath = "$env:USERPROFILE\.m2\repository\com\tencentcloudapi\tencentcloud-sdk-java-ocr"
if (Test-Path $tencentPath) {
    Write-Host "  删除: $tencentPath" -ForegroundColor Gray
    Remove-Item -Recurse -Force $tencentPath -ErrorAction SilentlyContinue
}

# 3. 强制下载iText依赖
Write-Host "[3/4] 强制下载iText依赖..." -ForegroundColor Yellow
$commonPath = "D:\IOE-DREAM\microservices\microservices-common"
if (Test-Path $commonPath) {
    Push-Location $commonPath
    try {
        Write-Host "  下载 itext7-core:9.4.0..." -ForegroundColor Gray
        & mvn dependency:get -Dartifact=com.itextpdf:itext7-core:9.4.0 -Dtransitive=false 2>&1 | Out-Null

        Write-Host "  下载 html2pdf:6.3.0..." -ForegroundColor Gray
        & mvn dependency:get -Dartifact=com.itextpdf:html2pdf:6.3.0 -Dtransitive=false 2>&1 | Out-Null

        Write-Host "  ✓ iText依赖下载完成" -ForegroundColor Green
    } finally {
        Pop-Location
    }
} else {
    Write-Host "  ⚠ 找不到microservices-common目录" -ForegroundColor Yellow
}

# 4. 强制下载腾讯云OCR依赖
Write-Host "[4/4] 强制下载腾讯云OCR依赖..." -ForegroundColor Yellow
$visitorPath = "D:\IOE-DREAM\microservices\ioedream-visitor-service"
if (Test-Path $visitorPath) {
    Push-Location $visitorPath
    try {
        Write-Host "  下载 tencentcloud-sdk-java-ocr:3.1.1373..." -ForegroundColor Gray
        & mvn dependency:get -Dartifact=com.tencentcloudapi:tencentcloud-sdk-java-ocr:3.1.1373 -Dtransitive=false 2>&1 | Out-Null

        Write-Host "  ✓ 腾讯云OCR依赖下载完成" -ForegroundColor Green
    } finally {
        Pop-Location
    }
} else {
    Write-Host "  ⚠ 找不到ioedream-visitor-service目录" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "依赖下载完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 在IDE中刷新Maven项目" -ForegroundColor White
Write-Host "2. File → Invalidate Caches / Restart (IntelliJ IDEA)" -ForegroundColor White
Write-Host "3. 重新构建项目" -ForegroundColor White
Write-Host ""
