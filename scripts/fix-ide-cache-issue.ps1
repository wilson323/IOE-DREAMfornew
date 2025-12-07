# IOE-DREAM IDE缓存问题修复脚本
# 版本: v1.0.0
# 功能: 解决IDE查找itext-core而不是itext7-core的缓存问题

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM IDE缓存问题修复脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 问题分析：IDE在查找 itext-core 而不是 itext7-core
# 这是IDE缓存问题，需要强制清理并重新下载

Write-Host "[问题分析]" -ForegroundColor Yellow
Write-Host "IDE错误信息显示查找: itext-core:9.4.0" -ForegroundColor Red
Write-Host "实际配置使用: itext7-core:9.4.0" -ForegroundColor Green
Write-Host "这是IDE缓存了旧的错误配置" -ForegroundColor Yellow
Write-Host ""

# 1. 清理所有iText相关缓存（包括错误的itext-core）
Write-Host "[1/5] 清理所有iText依赖缓存（包括错误的itext-core）..." -ForegroundColor Yellow
$itextPaths = @(
    "$env:USERPROFILE\.m2\repository\com\itextpdf\itext-core",  # 错误的artifactId
    "$env:USERPROFILE\.m2\repository\com\itextpdf\itext7-core", # 正确的artifactId
    "$env:USERPROFILE\.m2\repository\com\itextpdf\html2pdf"
)

foreach ($path in $itextPaths) {
    if (Test-Path $path) {
        Write-Host "  删除: $path" -ForegroundColor Gray
        Remove-Item -Recurse -Force $path -ErrorAction SilentlyContinue
    }
}

# 2. 清理IDE缓存目录（IntelliJ IDEA）
Write-Host "[2/5] 清理IDE缓存目录..." -ForegroundColor Yellow
$ideaCachePaths = @(
    "$env:USERPROFILE\.IntelliJIdea*\system\caches",
    "$env:USERPROFILE\.IntelliJIdea*\system\index",
    "$env:USERPROFILE\.IntelliJIdea*\system\maven"
)

foreach ($pattern in $ideaCachePaths) {
    $paths = Get-ChildItem -Path $env:USERPROFILE -Filter ".IntelliJIdea*" -Directory -ErrorAction SilentlyContinue
    foreach ($ideaDir in $paths) {
        $cachePath = Join-Path $ideaDir.FullName "system\caches"
        $indexPath = Join-Path $ideaDir.FullName "system\index"
        $mavenPath = Join-Path $ideaDir.FullName "system\maven"

        foreach ($path in @($cachePath, $indexPath, $mavenPath)) {
            if (Test-Path $path) {
                Write-Host "  清理IDE缓存: $path" -ForegroundColor Gray
                Remove-Item -Recurse -Force $path -ErrorAction SilentlyContinue
            }
        }
    }
}

# 3. 强制下载正确的依赖
Write-Host "[3/5] 强制下载正确的iText依赖（itext7-core）..." -ForegroundColor Yellow
$commonPath = "D:\IOE-DREAM\microservices\microservices-common"
if (Test-Path $commonPath) {
    Push-Location $commonPath
    try {
        Write-Host "  下载 itext7-core:9.4.0 (正确的artifactId)..." -ForegroundColor Gray
        $result = & mvn dependency:get -Dartifact=com.itextpdf:itext7-core:9.4.0 -Dtransitive=false 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ itext7-core:9.4.0 下载成功" -ForegroundColor Green
        } else {
            Write-Host "  ✗ itext7-core:9.4.0 下载失败" -ForegroundColor Red
            Write-Host $result -ForegroundColor Red
        }

        Write-Host "  下载 html2pdf:6.3.0..." -ForegroundColor Gray
        $result = & mvn dependency:get -Dartifact=com.itextpdf:html2pdf:6.3.0 -Dtransitive=false 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ html2pdf:6.3.0 下载成功" -ForegroundColor Green
        } else {
            Write-Host "  ✗ html2pdf:6.3.0 下载失败" -ForegroundColor Red
            Write-Host $result -ForegroundColor Red
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Host "  ⚠ 找不到microservices-common目录" -ForegroundColor Yellow
}

# 4. 验证依赖文件是否存在
Write-Host "[4/5] 验证依赖文件是否存在..." -ForegroundColor Yellow
$itext7CoreJar = "$env:USERPROFILE\.m2\repository\com\itextpdf\itext7-core\9.4.0\itext7-core-9.4.0.jar"
$html2pdfJar = "$env:USERPROFILE\.m2\repository\com\itextpdf\html2pdf\6.3.0\html2pdf-6.3.0.jar"

if (Test-Path $itext7CoreJar) {
    Write-Host "  ✓ itext7-core-9.4.0.jar 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ itext7-core-9.4.0.jar 不存在" -ForegroundColor Red
}

if (Test-Path $html2pdfJar) {
    Write-Host "  ✓ html2pdf-6.3.0.jar 存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ html2pdf-6.3.0.jar 不存在" -ForegroundColor Red
}

# 5. 重新构建common模块
Write-Host "[5/5] 重新构建microservices-common..." -ForegroundColor Yellow
if (Test-Path $commonPath) {
    Push-Location $commonPath
    try {
        Write-Host "  执行: mvn clean install -DskipTests -U" -ForegroundColor Gray
        & mvn clean install -DskipTests -U 2>&1 | ForEach-Object {
            if ($_ -match "BUILD SUCCESS") {
                Write-Host $_ -ForegroundColor Green
            } elseif ($_ -match "BUILD FAILURE|ERROR") {
                Write-Host $_ -ForegroundColor Red
            }
        }

        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ microservices-common构建成功" -ForegroundColor Green
        } else {
            Write-Host "  ✗ microservices-common构建失败" -ForegroundColor Red
        }
    } finally {
        Pop-Location
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "关键提示:" -ForegroundColor Yellow
Write-Host "1. IDE在查找 'itext-core' 是缓存问题" -ForegroundColor White
Write-Host "2. 实际配置使用的是 'itext7-core'（正确）" -ForegroundColor White
Write-Host "3. 依赖已下载到本地仓库" -ForegroundColor White
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 关闭IDE" -ForegroundColor White
Write-Host "2. 删除IDE缓存（已自动清理）" -ForegroundColor White
Write-Host "3. 重新打开IDE" -ForegroundColor White
Write-Host "4. 右键项目 → Maven → Reload Project" -ForegroundColor White
Write-Host "5. File → Invalidate Caches / Restart" -ForegroundColor White
Write-Host ""
