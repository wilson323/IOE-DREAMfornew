# IOE-DREAM 统一构建脚本
# 强制确保 microservices-common 先构建，避免依赖解析问题
# 使用方法: .\scripts\build-all.ps1 [-Clean] [-SkipTests] [-Service <service-name>]

# 设置PowerShell输出编码为UTF-8，解决中文乱码问题
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

param(
    [switch]$Clean = $false,
    [switch]$SkipTests = $false,
    [string]$Service = ""
)

$ErrorActionPreference = "Stop"
$projectRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 统一构建脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $projectRoot

# 步骤1: 验证 microservices-common 模块存在
Write-Host "[步骤1] 验证 microservices-common 模块..." -ForegroundColor Yellow
$commonModulePath = "microservices\microservices-common"
if (-not (Test-Path $commonModulePath)) {
    Write-Host "  ❌ microservices-common 模块不存在！" -ForegroundColor Red
    exit 1
}
Write-Host "  ✓ microservices-common 模块存在" -ForegroundColor Green

# 步骤2: 强制先构建 microservices-common（关键步骤）
Write-Host "`n[步骤2] 强制先构建 microservices-common..." -ForegroundColor Yellow
Write-Host "  这是关键步骤，确保所有依赖都能正确解析`n" -ForegroundColor Gray

$mavenGoals = @()
if ($Clean) {
    $mavenGoals += "clean"
}
$mavenGoals += "install"

if ($SkipTests) {
    $mavenGoals += "-DskipTests"
}

$commonBuildCmd = "mvn $($mavenGoals -join ' ') -pl microservices/microservices-common -am"
Write-Host "  执行命令: $commonBuildCmd`n" -ForegroundColor Gray

$buildResult = & mvn $mavenGoals -pl microservices/microservices-common -am 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ microservices-common 构建失败！" -ForegroundColor Red
    Write-Host $buildResult
    exit 1
}

Write-Host "  ✓ microservices-common 构建成功" -ForegroundColor Green

# 步骤3: 验证JAR文件已安装
Write-Host "`n[步骤3] 验证JAR文件已安装到本地仓库..." -ForegroundColor Yellow
$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

if (-not (Test-Path $jarPath)) {
    Write-Host "  ❌ JAR文件不存在: $jarPath" -ForegroundColor Red
    Write-Host "  请检查Maven本地仓库配置" -ForegroundColor Yellow
    exit 1
}

Write-Host "  ✓ JAR文件存在: $jarPath" -ForegroundColor Green

# 步骤4: 验证关键类存在
Write-Host "`n[步骤4] 验证关键类存在..." -ForegroundColor Yellow
$jarContent = & jar -tf $jarPath 2>&1

$requiredClasses = @(
    "net/lab1024/sa/common/organization/entity/DeviceEntity.class",
    "net/lab1024/sa/common/device/DeviceConnectionTest.class",
    "net/lab1024/sa/common/device/DeviceDispatchResult.class",
    "net/lab1024/sa/common/gateway/GatewayServiceClient.class"
)

$missingClasses = @()
foreach ($class in $requiredClasses) {
    if ($jarContent -notmatch [regex]::Escape($class)) {
        $missingClasses += $class
    }
}

if ($missingClasses.Count -gt 0) {
    Write-Host "  ❌ 以下类在JAR文件中缺失:" -ForegroundColor Red
    foreach ($class in $missingClasses) {
        Write-Host "    - $class" -ForegroundColor Red
    }
    exit 1
}

Write-Host "  ✓ 所有关键类都存在" -ForegroundColor Green

# 步骤5: 构建指定服务或所有服务
if ($Service -ne "") {
    Write-Host "`n[步骤5] 构建指定服务: $Service..." -ForegroundColor Yellow
    $servicePath = "microservices\$Service"

    if (-not (Test-Path $servicePath)) {
        Write-Host "  ❌ 服务不存在: $servicePath" -ForegroundColor Red
        exit 1
    }

    $serviceBuildCmd = "mvn $($mavenGoals -join ' ') -pl microservices/$Service -am"
    Write-Host "  执行命令: $serviceBuildCmd`n" -ForegroundColor Gray

    & mvn $mavenGoals -pl "microservices/$Service" -am 2>&1 | Out-Host

    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ❌ $Service 构建失败！" -ForegroundColor Red
        exit 1
    }

    Write-Host "  ✓ $Service 构建成功" -ForegroundColor Green
} else {
    Write-Host "`n[步骤5] 构建所有服务..." -ForegroundColor Yellow
    Write-Host "  执行命令: mvn $($mavenGoals -join ' ')`n" -ForegroundColor Gray

    & mvn $mavenGoals 2>&1 | Out-Host

    if ($LASTEXITCODE -ne 0) {
        Write-Host "  ⚠️ 部分服务构建失败，请检查输出" -ForegroundColor Yellow
    } else {
        Write-Host "  ✓ 所有服务构建成功" -ForegroundColor Green
    }
}

# 完成
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "构建完成" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`n提示: 如果IDE仍有错误，请执行以下操作:" -ForegroundColor Yellow
Write-Host "1. IntelliJ IDEA: File -> Invalidate Caches / Restart" -ForegroundColor White
Write-Host "2. VS Code: 重新加载窗口 (Ctrl+Shift+P -> Reload Window)" -ForegroundColor White
Write-Host "3. 重新导入Maven项目" -ForegroundColor White
