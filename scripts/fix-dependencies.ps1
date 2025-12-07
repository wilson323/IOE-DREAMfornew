# IOE-DREAM 依赖修复脚本
# 功能：清理错误的Maven缓存并重新下载依赖
# 作者：IOE-DREAM Team
# 日期：2025-01-30

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 依赖修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 清理错误的iText依赖缓存
Write-Host "[1/4] 清理错误的iText依赖缓存..." -ForegroundColor Yellow

$itextCoreWrongPath = "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core\9.4.0"
$html2pdfWrongPath = "$env:USERPROFILE\.m2\repository\com\itextpdf\html2pdf\9.4.0"

if (Test-Path $itextCoreWrongPath) {
    Remove-Item -Recurse -Force $itextCoreWrongPath
    Write-Host "  ✓ 已删除错误的itext-core 9.4.0缓存" -ForegroundColor Green
} else {
    Write-Host "  - itext-core 9.4.0缓存不存在，跳过" -ForegroundColor Gray
}

if (Test-Path $html2pdfWrongPath) {
    Remove-Item -Recurse -Force $html2pdfWrongPath
    Write-Host "  ✓ 已删除错误的html2pdf 9.4.0缓存" -ForegroundColor Green
} else {
    Write-Host "  - html2pdf 9.4.0缓存不存在，跳过" -ForegroundColor Gray
}

# 2. 清理腾讯云OCR依赖缓存（可选，如果需要重新下载）
Write-Host ""
Write-Host "[2/4] 清理腾讯云OCR依赖缓存（可选）..." -ForegroundColor Yellow
$tencentOcrPath = "$env:USERPROFILE\.m2\repository\com\tencentcloudapi\tencentcloud-sdk-java-ocr"
if (Test-Path $tencentOcrPath) {
    $response = Read-Host "  是否清理腾讯云OCR缓存？(y/n)"
    if ($response -eq "y" -or $response -eq "Y") {
        Remove-Item -Recurse -Force $tencentOcrPath
        Write-Host "  ✓ 已清理腾讯云OCR缓存" -ForegroundColor Green
    } else {
        Write-Host "  - 跳过腾讯云OCR缓存清理" -ForegroundColor Gray
    }
} else {
    Write-Host "  - 腾讯云OCR缓存不存在，跳过" -ForegroundColor Gray
}

# 3. 重新构建microservices-common模块
Write-Host ""
Write-Host "[3/4] 重新构建microservices-common模块..." -ForegroundColor Yellow
$commonPath = Join-Path $PSScriptRoot "..\microservices\microservices-common"
if (Test-Path $commonPath) {
    Push-Location $commonPath
    Write-Host "  执行: mvn clean install -U -DskipTests" -ForegroundColor Gray
    mvn clean install -U -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ microservices-common构建成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ microservices-common构建失败，请检查错误信息" -ForegroundColor Red
    }
    Pop-Location
} else {
    Write-Host "  ✗ 找不到microservices-common目录: $commonPath" -ForegroundColor Red
}

# 4. 验证依赖
Write-Host ""
Write-Host "[4/4] 验证依赖..." -ForegroundColor Yellow

# 检查itext7-core是否正确下载
$itext7CorePath = "$env:USERPROFILE\.m2\repository\com\itextpdf\itext7-core\9.4.0\itext7-core-9.4.0.jar"
if (Test-Path $itext7CorePath) {
    Write-Host "  ✓ itext7-core 9.4.0已正确下载" -ForegroundColor Green
} else {
    Write-Host "  ✗ itext7-core 9.4.0未找到，请检查Maven配置" -ForegroundColor Red
}

# 检查html2pdf是否正确下载
$html2pdfPath = "$env:USERPROFILE\.m2\repository\com\itextpdf\html2pdf\6.3.0\html2pdf-6.3.0.jar"
if (Test-Path $html2pdfPath) {
    Write-Host "  ✓ html2pdf 6.3.0已正确下载" -ForegroundColor Green
} else {
    Write-Host "  ✗ html2pdf 6.3.0未找到，请检查Maven配置" -ForegroundColor Red
}

# 检查腾讯云OCR是否正确下载
$tencentOcrJarPath = "$env:USERPROFILE\.m2\repository\com\tencentcloudapi\tencentcloud-sdk-java-ocr\3.1.1373\tencentcloud-sdk-java-ocr-3.1.1373.jar"
if (Test-Path $tencentOcrJarPath) {
    Write-Host "  ✓ 腾讯云OCR SDK已下载" -ForegroundColor Green

    # 检查BusinessLicenseOCRRequest类是否存在
    $jarContent = jar -tf $tencentOcrJarPath 2>$null | Select-String "BusinessLicenseOCRRequest"
    if ($jarContent) {
        Write-Host "  ✓ BusinessLicenseOCRRequest类存在" -ForegroundColor Green
    } else {
        Write-Host "  ✗ BusinessLicenseOCRRequest类不存在，可能需要更新SDK版本" -ForegroundColor Red
    }
} else {
    Write-Host "  ⚠ 腾讯云OCR SDK未找到，可能需要重新下载" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "依赖修复完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 在IDE中刷新Maven项目（右键项目 -> Maven -> Reload Project）" -ForegroundColor White
Write-Host "2. 如果问题仍然存在，请检查IDE的Maven配置" -ForegroundColor White
Write-Host "3. 查看详细修复报告: documentation\technical\DEPENDENCY_FIX_REPORT.md" -ForegroundColor White
