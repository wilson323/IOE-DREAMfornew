# 修复OCR依赖问题脚本
# 用途: 重新构建项目以解决腾讯云OCR SDK类导入问题

param(
    [switch]$Clean,
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复OCR依赖问题脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 设置项目根目录
$ProjectRoot = "D:\IOE-DREAM"
Set-Location $ProjectRoot

# 构建参数
$BuildArgs = @()
if ($SkipTests) {
    $BuildArgs += "-DskipTests"
}

# 1. 清理并重新构建common模块
Write-Host "步骤1: 构建microservices-common..." -ForegroundColor Yellow
if ($Clean) {
    Write-Host "  清理common模块..." -ForegroundColor Gray
    mvn clean -pl microservices/microservices-common -am
}

Write-Host "  安装common模块..." -ForegroundColor Gray
$commonBuild = mvn install -pl microservices/microservices-common -am $BuildArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ 构建microservices-common失败!" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ microservices-common构建成功" -ForegroundColor Green
Write-Host ""

# 2. 清理并重新构建visitor-service
Write-Host "步骤2: 构建ioedream-visitor-service..." -ForegroundColor Yellow
if ($Clean) {
    Write-Host "  清理visitor-service..." -ForegroundColor Gray
    mvn clean -pl microservices/ioedream-visitor-service -am
}

Write-Host "  安装visitor-service..." -ForegroundColor Gray
$visitorBuild = mvn install -pl microservices/ioedream-visitor-service -am $BuildArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ 构建ioedream-visitor-service失败!" -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ ioedream-visitor-service构建成功" -ForegroundColor Green
Write-Host ""

# 3. 验证依赖
Write-Host "步骤3: 验证腾讯云OCR依赖..." -ForegroundColor Yellow
$dependencyTree = mvn dependency:tree -pl microservices/ioedream-visitor-service 2>&1
$tencentDeps = $dependencyTree | Select-String "tencentcloud"

if ($tencentDeps) {
    Write-Host "  找到腾讯云依赖:" -ForegroundColor Gray
    $tencentDeps | ForEach-Object {
        Write-Host "    $_" -ForegroundColor Gray
    }
} else {
    Write-Host "  ⚠️  未找到腾讯云依赖，请检查pom.xml配置" -ForegroundColor Yellow
}

Write-Host ""

# 4. 检查JAR文件
Write-Host "步骤4: 检查OCR SDK JAR文件..." -ForegroundColor Yellow
$jarPath = "$env:USERPROFILE\.m2\repository\com\tencentcloudapi\tencentcloud-sdk-java-ocr\3.1.1373\tencentcloud-sdk-java-ocr-3.1.1373.jar"

if (Test-Path $jarPath) {
    Write-Host "  ✅ JAR文件存在: $jarPath" -ForegroundColor Green

    # 检查JAR文件内容
    Write-Host "  检查JAR文件内容..." -ForegroundColor Gray
    $jarContent = jar -tf $jarPath 2>&1 | Select-String "BusinessLicense"

    if ($jarContent) {
        Write-Host "  ✅ 找到BusinessLicense相关类:" -ForegroundColor Green
        $jarContent | ForEach-Object {
            Write-Host "    $_" -ForegroundColor Gray
        }
    } else {
        Write-Host "  ⚠️  未找到BusinessLicense相关类" -ForegroundColor Yellow
        Write-Host "  建议: 检查SDK版本或使用替代方案" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ❌ JAR文件不存在: $jarPath" -ForegroundColor Red
    Write-Host "  建议: 运行 'mvn dependency:resolve' 下载依赖" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  修复完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步:" -ForegroundColor Yellow
Write-Host "  1. 在IDE中刷新Maven项目" -ForegroundColor Gray
Write-Host "  2. 清理IDE缓存并重新构建" -ForegroundColor Gray
Write-Host "  3. 如果问题仍然存在，检查SDK版本" -ForegroundColor Gray
