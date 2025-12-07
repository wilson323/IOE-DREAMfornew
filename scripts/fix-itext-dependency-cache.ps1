# IOE-DREAM 修复itext依赖缓存问题脚本
# 功能：清理Maven缓存并强制下载正确的itext依赖
# 作者：AI Assistant
# 日期：2025-01-30

$ErrorActionPreference = "Stop"
$workspaceRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复 itext 依赖缓存问题" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $workspaceRoot

# 步骤1: 清理Maven本地仓库中的itext缓存
Write-Host "[步骤1] 清理Maven本地仓库缓存..." -ForegroundColor Yellow

$mavenRepo = "$env:USERPROFILE\.m2\repository\com\itextpdf"

# 清理itext-core缓存
$itextCorePath = Join-Path $mavenRepo "itext-core"
if (Test-Path $itextCorePath) {
    Remove-Item -Path $itextCorePath -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已清理: itext-core" -ForegroundColor Green
}

# 清理html2pdf缓存
$html2pdfPath = Join-Path $mavenRepo "html2pdf"
if (Test-Path $html2pdfPath) {
    Remove-Item -Path $html2pdfPath -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已清理: html2pdf" -ForegroundColor Green
}

Write-Host ""

# 步骤2: 强制更新依赖（使用-U参数）
Write-Host "[步骤2] 强制更新依赖..." -ForegroundColor Yellow
Write-Host "  使用 -U 参数强制Maven更新依赖" -ForegroundColor Gray
Write-Host ""

cd microservices\microservices-common

try {
    # 先尝试解析依赖
    Write-Host "  正在解析依赖..." -ForegroundColor Gray
    $resolveOutput = mvn dependency:resolve -U 2>&1 | Out-String

    if ($resolveOutput -match "itext-core.*9\.4\.0" -or $LASTEXITCODE -eq 0) {
        Write-Host "  ✓ 依赖解析成功" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ 依赖解析可能有问题，继续构建..." -ForegroundColor Yellow
    }

    Write-Host ""

    # 构建项目
    Write-Host "  正在构建项目..." -ForegroundColor Gray
    $buildOutput = mvn clean install -DskipTests -U 2>&1 | Out-String

    if ($LASTEXITCODE -eq 0 -or $buildOutput -match "BUILD SUCCESS") {
        Write-Host "  ✓ 构建成功！" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 构建失败！" -ForegroundColor Red
        Write-Host "  错误信息:" -ForegroundColor Yellow
        $buildOutput | Select-String -Pattern "ERROR|FAILURE|itext" | ForEach-Object {
            Write-Host "    $_" -ForegroundColor Red
        }

        # 如果还是失败，尝试手动下载依赖
        Write-Host ""
        Write-Host "  尝试手动下载依赖..." -ForegroundColor Yellow
        mvn dependency:get -Dartifact=com.itextpdf:itext-core:9.4.0 -DremoteRepositories=https://repo1.maven.org/maven2/ 2>&1 | Out-Null
        mvn dependency:get -Dartifact=com.itextpdf:html2pdf:6.3.0 -DremoteRepositories=https://repo1.maven.org/maven2/ 2>&1 | Out-Null

        # 再次尝试构建
        Write-Host "  重新构建..." -ForegroundColor Yellow
        $buildOutput2 = mvn clean install -DskipTests -U 2>&1 | Out-String

        if ($LASTEXITCODE -eq 0 -or $buildOutput2 -match "BUILD SUCCESS") {
            Write-Host "  ✓ 构建成功！" -ForegroundColor Green
        } else {
            Write-Host "  ✗ 构建仍然失败" -ForegroundColor Red
            exit 1
        }
    }
} catch {
    Write-Host "  ✗ 构建过程出错: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤3: 验证JAR文件
Write-Host "[步骤3] 验证JAR文件..." -ForegroundColor Yellow
$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

if (Test-Path $jarPath) {
    $jarSize = (Get-Item $jarPath).Length
    Write-Host "  ✓ JAR文件已生成: $jarSize bytes" -ForegroundColor Green
} else {
    Write-Host "  ✗ JAR文件未生成" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤4: 验证itext依赖
Write-Host "[步骤4] 验证itext依赖..." -ForegroundColor Yellow
$itextJar = "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core\9.4.0\itext-core-9.4.0.jar"

if (Test-Path $itextJar) {
    Write-Host "  ✓ itext-core:9.4.0 已下载" -ForegroundColor Green
} else {
    Write-Host "  ✗ itext-core:9.4.0 未找到" -ForegroundColor Red
    Write-Host "  尝试从Maven Central手动下载..." -ForegroundColor Yellow

    # 手动下载
    mvn dependency:get -Dartifact=com.itextpdf:itext-core:9.4.0 -DremoteRepositories=https://repo1.maven.org/maven2/ 2>&1 | Out-Null

    if (Test-Path $itextJar) {
        Write-Host "  ✓ 手动下载成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 手动下载失败，请检查网络连接" -ForegroundColor Red
    }
}

Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
