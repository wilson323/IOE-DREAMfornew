# IOE-DREAM 编译错误修复验证脚本
# 生成时间: 2025-01-30

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 编译错误修复验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 检查Spring Cloud版本配置
Write-Host "[1/4] 检查Spring Cloud版本配置..." -ForegroundColor Yellow
$rootPom = "D:\IOE-DREAM\microservices\pom.xml"
if (Test-Path $rootPom) {
    $content = Get-Content $rootPom -Raw
    if ($content -match 'spring-cloud\.version>2025\.0\.0') {
        Write-Host "  ✅ Spring Cloud版本已修复为2025.0.0" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Spring Cloud版本配置异常" -ForegroundColor Red
    }
} else {
    Write-Host "  ❌ 找不到父POM文件" -ForegroundColor Red
}

# 2. 检查spring-cloud-commons版本配置
Write-Host "[2/4] 检查spring-cloud-commons版本配置..." -ForegroundColor Yellow
$commonPom = "D:\IOE-DREAM\microservices\microservices-common\pom.xml"
if (Test-Path $commonPom) {
    $content = Get-Content $commonPom -Raw
    if ($content -match 'spring-cloud-commons' -and $content -notmatch 'spring-cloud-commons.*version') {
        Write-Host "  ✅ spring-cloud-commons已移除显式版本号" -ForegroundColor Green
    } elseif ($content -match 'spring-cloud-commons.*version.*spring-cloud\.version') {
        Write-Host "  ❌ spring-cloud-commons仍使用显式版本号" -ForegroundColor Red
    } else {
        Write-Host "  ⚠️  未找到spring-cloud-commons依赖" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ 找不到microservices-common POM文件" -ForegroundColor Red
}

# 3. 清理Maven缓存
Write-Host "[3/4] 清理Maven本地缓存..." -ForegroundColor Yellow
$cachePath = "$env:USERPROFILE\.m2\repository\org\springframework\cloud\spring-cloud-dependencies\5.0.0"
if (Test-Path $cachePath) {
    Remove-Item -Recurse -Force $cachePath -ErrorAction SilentlyContinue
    Write-Host "  ✅ 已清理Spring Cloud 5.0.0的失败缓存" -ForegroundColor Green
} else {
    Write-Host "  ℹ️  无需清理缓存（缓存不存在）" -ForegroundColor Gray
}

# 4. 验证Maven依赖解析
Write-Host "[4/4] 验证Maven依赖解析..." -ForegroundColor Yellow
Set-Location "D:\IOE-DREAM\microservices"
$mvnOutput = mvn dependency:resolve -pl microservices-common -U -q 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "  ✅ Maven依赖解析成功" -ForegroundColor Green
} else {
    Write-Host "  ❌ Maven依赖解析失败" -ForegroundColor Red
    Write-Host "  错误信息:" -ForegroundColor Red
    $mvnOutput | Select-Object -Last 10 | ForEach-Object { Write-Host "    $_" -ForegroundColor Red }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "验证完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作:" -ForegroundColor Yellow
Write-Host "1. 在IDE中重新导入Maven项目" -ForegroundColor White
Write-Host "2. 检查编译错误是否已消失" -ForegroundColor White
Write-Host "3. 如果仍有问题，请查看详细错误信息" -ForegroundColor White
