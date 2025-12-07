# 修复编译问题 - 步骤3: 清理并编译
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "清理并重新编译" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$projectRoot = "D:\IOE-DREAM"
Set-Location $projectRoot

# 步骤3.1: 清理jasypt依赖缓存
Write-Host "`n[3.1] 清理jasypt依赖缓存..." -ForegroundColor Yellow
$m2Repo = "$env:USERPROFILE\.m2\repository"
$jasyptPath = "$m2Repo\org\jasypt\jasypt-spring-boot-starter\3.0.5"

if (Test-Path $jasyptPath) {
    Remove-Item $jasyptPath -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已清理jasypt缓存" -ForegroundColor Green
} else {
    Write-Host "  ✓ jasypt缓存不存在" -ForegroundColor Green
}

# 步骤3.2: 清理target目录
Write-Host "`n[3.2] 清理target目录..." -ForegroundColor Yellow
if (Test-Path "microservices\microservices-common\target") {
    Remove-Item "microservices\microservices-common\target" -Recurse -Force
    Write-Host "  ✓ 已清理target目录" -ForegroundColor Green
}

# 步骤3.3: 更新Maven依赖
Write-Host "`n[3.3] 更新Maven依赖..." -ForegroundColor Yellow
& mvn dependency:purge-local-repository -DmanualInclude="org.jasypt:jasypt-spring-boot-starter" -DreResolve=true 2>&1 | Out-Null
Write-Host "  ✓ Maven依赖已更新" -ForegroundColor Green

# 步骤3.4: 重新编译
Write-Host "`n[3.4] 重新编译microservices-common..." -ForegroundColor Yellow
Write-Host "  执行命令: mvn clean install -pl microservices/microservices-common -am -DskipTests`n" -ForegroundColor Gray

& mvn clean install -pl microservices/microservices-common -am -DskipTests

Write-Host "`n✓ 步骤3完成" -ForegroundColor Green

