# ============================================================================
# IOE-DREAM 紧急修复脚本 - Phase 1
# 解决编译错误和依赖问题的第一响应方案
# ============================================================================

param(
    [switch]$Force,
    [switch]$SkipTests,
    [switch]$Verbose
)

Write-Host "🚀 IOE-DREAM 紧急修复开始..." -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Yellow

# 错误处理
$ErrorActionPreference = "Stop"

# 1. 预检查环境
Write-Host "📋 步骤1: 环境预检查" -ForegroundColor Cyan
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "   ✅ Java版本: $javaVersion" -ForegroundColor Green

    $mavenVersion = mvn -version | Select-String "Apache Maven"
    Write-Host "   ✅ Maven版本: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "   ❌ 环境检查失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 清理并重建公共模块
Write-Host "🔧 步骤2: 重建公共模块" -ForegroundColor Cyan
try {
    Write-Host "   清理本地Maven仓库中的旧版本..."
    Remove-Item -Path "$env:USERPROFILE\.m2\repository\net\lab1024\sa" -Recurse -Force -ErrorAction SilentlyContinue

    Write-Host "   重建 microservices-common-core..."
    mvn clean install -pl microservices/microservices-common-core -am -DskipTests -q
    if ($LASTEXITCODE -ne 0) { throw "microservices-common-core 构建失败" }

    Write-Host "   重建 microservices-common..."
    mvn clean install -pl microservices/microservices-common -am -DskipTests -q
    if ($LASTEXITCODE -ne 0) { throw "microservices-common 构建失败" }

    Write-Host "   ✅ 公共模块重建成功" -ForegroundColor Green
} catch {
    Write-Host "   ❌ 公共模块重建失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 3. 验证JAR包安装
Write-Host "🔍 步骤3: 验证JAR包安装" -ForegroundColor Cyan
$commonJar = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"
$coreJar = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common-core\1.0.0\microservices-common-core-1.0.0.jar"

if (-not (Test-Path $commonJar)) {
    Write-Host "   ❌ microservices-common JAR包未找到" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $coreJar)) {
    Write-Host "   ❌ microservices-common-core JAR包未找到" -ForegroundColor Red
    exit 1
}

Write-Host "   ✅ JAR包验证成功" -ForegroundColor Green

# 4. 批量验证关键类
Write-Host "🔍 步骤4: 验证关键类" -ForegroundColor Cyan
$classes = @(
    "net.lab1024.sa.common.entity.UserEntity",
    "net.lab1024.sa.common.consume.entity.AccountEntity",
    "net.lab1024.sa.common.visitor.entity.VisitorAppointmentEntity",
    "net.lab1024.sa.common.attendance.entity.AttendanceRecordEntity"
)

foreach ($className in $classes) {
    $jarPath = if ($className -like "*.common.*") { $commonJar } else { $coreJar }
    $result = jar -tf $jarPath | Select-String ($className.Replace(".", "/") + "\.class")

    if ($result) {
        Write-Host "   ✅ $className" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $className 未找到" -ForegroundColor Red
        if (-not $Force) {
            Write-Host "      提示: 使用 -Force 参数强制继续" -ForegroundColor Yellow
        }
    }
}

# 5. 构建验证测试
Write-Host "🧪 步骤5: 构建验证测试" -ForegroundColor Cyan
$testServices = @(
    "microservices/ioedream-access-service",
    "microservices/ioedream-attendance-service",
    "microservices/ioedream-consume-service"
)

$successCount = 0
$failCount = 0

foreach ($service in $testServices) {
    try {
        Write-Host "   测试构建: $service"
        if ($SkipTests) {
            mvn clean compile -pl $service -am -q -DskipTests
        } else {
            mvn clean compile -pl $service -am -q
        }

        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✅ $service 构建成功" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "   ❌ $service 构建失败" -ForegroundColor Red
            $failCount++
        }
    } catch {
        Write-Host "   ❌ $service 构建异常: $($_.Exception.Message)" -ForegroundColor Red
        $failCount++
    }
}

# 6. 生成修复报告
Write-Host "📊 步骤6: 生成修复报告" -ForegroundColor Cyan
$report = @{
    timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    environment = @{
        java = $javaVersion
        maven = $mavenVersion
    }
    buildResults = @{
        success = $successCount
        failed = $failCount
        total = $successCount + $failCount
    }
    jarVerification = @{
        commonJar = Test-Path $commonJar
        coreJar = Test-Path $coreJar
    }
    nextSteps = @()
}

if ($failCount -gt 0) {
    $report.nextSteps += "检查失败的 $failCount 个服务的具体错误"
    $report.nextSteps += "运行 scripts/diagnostic-check.ps1 进行详细诊断"
}

if ($successCount -eq $testServices.Count) {
    $report.nextSteps += "运行 Phase 2 架构优化脚本"
    $report.nextSteps += "执行完整的项目构建: mvn clean install"
}

$reportPath = "logs/emergency-fix-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
New-Item -ItemType Directory -Path "logs" -Force | Out-Null
$report | ConvertTo-Json -Depth 3 | Out-File -FilePath $reportPath -Encoding UTF8

Write-Host "   📄 报告已生成: $reportPath" -ForegroundColor Green

# 7. 总结
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "🎯 紧急修复完成总结:" -ForegroundColor White

if ($successCount -eq $testServices.Count) {
    Write-Host "   ✅ 所有测试服务构建成功!" -ForegroundColor Green
    Write-Host "   🚀 可以进入 Phase 2 架构优化阶段" -ForegroundColor Cyan
} else {
    Write-Host "   ⚠️  $successCount/$($testServices.Count) 服务构建成功" -ForegroundColor Yellow
    Write-Host "   🔧 需要进一步修复失败的服务" -ForegroundColor Orange
}

Write-Host "================================================" -ForegroundColor Yellow
Write-Host "下一步建议:" -ForegroundColor White
Write-Host "   1. 查看修复报告: $reportPath" -ForegroundColor Cyan
Write-Host "   2. 如有问题，运行诊断脚本: scripts/diagnostic-check.ps1" -ForegroundColor Cyan
Write-Host "   3. 继续执行 Phase 2: scripts/architecture-optimization.ps1" -ForegroundColor Cyan
Write-Host "" -ForegroundColor White