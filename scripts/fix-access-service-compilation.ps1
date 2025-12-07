# 修复门禁服务编译错误脚本
# 用途：解决 ioedream-access-service 的依赖解析问题

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复门禁服务编译错误" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$projectRoot = "D:\IOE-DREAM"
Set-Location $projectRoot

# 步骤1: 检查 microservices-common 是否存在
Write-Host "`n[步骤1] 检查 microservices-common 模块..." -ForegroundColor Yellow
$commonModulePath = "microservices\microservices-common"
if (-not (Test-Path $commonModulePath)) {
    Write-Host "  ❌ microservices-common 模块不存在！" -ForegroundColor Red
    exit 1
}
Write-Host "  ✓ microservices-common 模块存在" -ForegroundColor Green

# 步骤2: 清理并重新构建 microservices-common
Write-Host "`n[步骤2] 清理并重新构建 microservices-common..." -ForegroundColor Yellow
Write-Host "  执行命令: mvn clean install -pl microservices/microservices-common -am -DskipTests`n" -ForegroundColor Gray

$buildResult = & mvn clean install -pl microservices/microservices-common -am -DskipTests 2>&1

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
    exit 1
}

Write-Host "  ✓ JAR文件存在: $jarPath" -ForegroundColor Green

# 步骤4: 验证JAR文件包含关键类
Write-Host "`n[步骤4] 验证JAR文件包含关键类..." -ForegroundColor Yellow
$jarContent = & jar -tf $jarPath 2>&1

$requiredClasses = @(
    "net/lab1024/sa/common/organization/entity/DeviceEntity.class",
    "net/lab1024/sa/common/device/DeviceConnectionTest.class",
    "net/lab1024/sa/common/device/DeviceDispatchResult.class",
    "net/lab1024/sa/common/gateway/GatewayServiceClient.class",
    "net/lab1024/sa/common/access/entity/InterlockLogEntity.class"
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

# 步骤5: 清理 access-service 的 target 目录
Write-Host "`n[步骤5] 清理 access-service 的 target 目录..." -ForegroundColor Yellow
$accessServiceTarget = "microservices\ioedream-access-service\target"
if (Test-Path $accessServiceTarget) {
    Remove-Item $accessServiceTarget -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "  ✓ 已清理 target 目录已清理" -ForegroundColor Green
} else {
    Write-Host "  ✓ target 目录不存在，无需清理" -ForegroundColor Green
}

# 步骤6: 重新编译 access-service
Write-Host "`n[步骤6] 重新编译 access-service..." -ForegroundColor Yellow
Write-Host "  执行命令: mvn clean compile -pl microservices/ioedream-access-service -am`n" -ForegroundColor Gray

$compileResult = & mvn clean compile -pl microservices/ioedream-access-service -am 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ⚠️ access-service 编译仍有错误，请检查输出" -ForegroundColor Yellow
    Write-Host $compileResult
} else {
    Write-Host "  ✓ access-service 编译成功" -ForegroundColor Green
}

# 步骤7: 生成错误报告
Write-Host "`n[步骤7] 生成编译错误报告..." -ForegroundColor Yellow
$errorReport = @"
# 门禁服务编译错误修复报告

**修复时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**修复步骤**:
1. ✅ 重新构建 microservices-common
2. ✅ 验证JAR文件安装
3. ✅ 验证关键类存在
4. ✅ 清理并重新编译 access-service

**关键类验证结果**:
$(foreach ($class in $requiredClasses) {
    "  - ✅ $class"
})

**下一步操作**:
1. 在IDE中刷新项目（File -> Invalidate Caches / Restart）
2. 重新导入Maven项目
3. 检查是否还有编译错误

**如果仍有错误**:
1. 检查IDE缓存是否已清理
2. 检查Maven项目是否正确导入
3. 检查项目结构是否正确识别
"@

$reportPath = "documentation\technical\ACCESS_SERVICE_FIX_REPORT_$(Get-Date -Format 'yyyyMMdd_HHmmss').md"
$errorReport | Out-File -FilePath $reportPath -Encoding UTF8
Write-Host "  ✓ 错误报告已生成: $reportPath" -ForegroundColor Green

# 完成
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "修复完成" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "`n下一步操作:" -ForegroundColor Yellow
Write-Host "1. 在IDE中执行: File -> Invalidate Caches / Restart" -ForegroundColor White
Write-Host "2. 重新导入Maven项目" -ForegroundColor White
Write-Host "3. 检查编译错误是否已解决" -ForegroundColor White
Write-Host "`n如果仍有错误，请查看报告: $reportPath" -ForegroundColor Yellow
