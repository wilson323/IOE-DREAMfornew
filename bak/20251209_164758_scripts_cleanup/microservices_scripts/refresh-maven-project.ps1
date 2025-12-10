# Maven项目刷新脚本 - 解决IDE缓存问题
# 用途：清理IDE缓存并强制刷新Maven项目配置

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Maven项目刷新脚本" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

$projectPath = $PSScriptRoot
$workspacePath = Split-Path (Split-Path $projectPath -Parent) -Parent

Write-Host "[1/5] 清理Maven本地仓库中的错误缓存..." -ForegroundColor Yellow
$html2pdfWrongPath = "$env:USERPROFILE\.m2\repository\com\itextpdf\html2pdf\9.4.0"
if (Test-Path $html2pdfWrongPath) {
    Remove-Item -Recurse -Force $html2pdfWrongPath -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已删除错误的html2pdf 9.4.0缓存" -ForegroundColor Green
} else {
    Write-Host "  ✓ 未发现错误的缓存" -ForegroundColor Green
}

Write-Host ""
Write-Host "[2/5] 清理项目target目录..." -ForegroundColor Yellow
if (Test-Path "$projectPath\target") {
    Remove-Item -Recurse -Force "$projectPath\target" -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已清理target目录" -ForegroundColor Green
}

Write-Host ""
Write-Host "[3/5] 清理IDE配置文件..." -ForegroundColor Yellow
$ideFiles = @(
    "$projectPath\.classpath",
    "$projectPath\.project",
    "$projectPath\.settings",
    "$workspacePath\.classpath",
    "$workspacePath\.project",
    "$workspacePath\.settings"
)

foreach ($file in $ideFiles) {
    if (Test-Path $file) {
        Remove-Item -Recurse -Force $file -ErrorAction SilentlyContinue
        Write-Host "  ✓ 已删除: $file" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "[4/5] 验证父POM配置..." -ForegroundColor Yellow
$parentPom = "$workspacePath\pom.xml"
if (Test-Path $parentPom) {
    $parentContent = Get-Content $parentPom -Raw
    if ($parentContent -match "itext7-core\.version.*9\.4\.0" -and $parentContent -match "html2pdf\.version.*6\.3\.0") {
        Write-Host "  ✓ 父POM配置正确" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 父POM配置可能有问题，请检查" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "[5/5] 执行Maven验证..." -ForegroundColor Yellow
Push-Location $projectPath
try {
    $mvnOutput = mvn validate 2>&1 | Out-String
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ Maven验证成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Maven验证失败" -ForegroundColor Red
        Write-Host $mvnOutput
    }
} finally {
    Pop-Location
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "刷新完成！" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 在IDE中右键项目 → Maven → Reload Project" -ForegroundColor White
Write-Host "2. 或者执行: mvn clean install -DskipTests" -ForegroundColor White
Write-Host "3. 如果问题仍然存在，请重启IDE" -ForegroundColor White
Write-Host ""
