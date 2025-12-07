# IOE-DREAM 修复common模块依赖错误脚本
# 功能：构建microservices-common模块，解决IDE无法解析net.lab1024.sa.common的问题
# 作者：AI Assistant
# 日期：2025-01-30

$ErrorActionPreference = "Continue"
$workspaceRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复 common 模块依赖错误" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 检查JAR文件是否存在
Write-Host "[步骤1] 检查 microservices-common JAR文件..." -ForegroundColor Yellow
$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

if (Test-Path $jarPath) {
    $jarSize = (Get-Item $jarPath).Length
    Write-Host "  ✓ JAR文件已存在: $jarSize bytes" -ForegroundColor Green
    Write-Host "    路径: $jarPath" -ForegroundColor Gray

    Write-Host ""
    Write-Host "  是否重新构建? (Y/N): " -ForegroundColor Yellow -NoNewline
    $response = Read-Host
    if ($response -ne "Y" -and $response -ne "y") {
        Write-Host "  跳过构建，使用现有JAR文件" -ForegroundColor Gray
        exit 0
    }
} else {
    Write-Host "  ✗ JAR文件不存在，需要构建" -ForegroundColor Red
}

Write-Host ""

# 步骤2: 构建 microservices-common
Write-Host "[步骤2] 构建 microservices-common 模块..." -ForegroundColor Yellow
Write-Host "  这可能需要几分钟时间..." -ForegroundColor Gray
Write-Host ""

Set-Location $workspaceRoot

try {
    $buildOutput = mvn clean install -pl microservices/microservices-common -am -DskipTests 2>&1

    # 检查构建结果
    if ($LASTEXITCODE -eq 0 -or ($buildOutput -match "BUILD SUCCESS")) {
        Write-Host "  ✓ 构建成功！" -ForegroundColor Green

        # 验证JAR文件
        if (Test-Path $jarPath) {
            $jarSize = (Get-Item $jarPath).Length
            Write-Host "  ✓ JAR文件已生成: $jarSize bytes" -ForegroundColor Green
        } else {
            Write-Host "  ⚠ JAR文件未找到，但构建显示成功" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  ✗ 构建失败！" -ForegroundColor Red
        Write-Host "  错误信息:" -ForegroundColor Yellow
        $buildOutput | Select-String -Pattern "ERROR|FAILURE" | ForEach-Object {
            Write-Host "    $_" -ForegroundColor Red
        }
        exit 1
    }
} catch {
    Write-Host "  ✗ 构建过程出错: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤3: 验证关键类是否存在
Write-Host "[步骤3] 验证关键类是否存在..." -ForegroundColor Yellow

$requiredClasses = @(
    "net/lab1024/sa/common/dto/ResponseDTO.class",
    "net/lab1024/sa/common/organization/entity/DeviceEntity.class",
    "net/lab1024/sa/common/device/DeviceConnectionTest.class",
    "net/lab1024/sa/common/device/DeviceDispatchResult.class"
)

$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

if (Test-Path $jarPath) {
    $jarContent = jar -tf $jarPath 2>&1

    foreach ($class in $requiredClasses) {
        if ($jarContent -match [regex]::Escape($class)) {
            Write-Host "  ✓ $class" -ForegroundColor Green
        } else {
            Write-Host "  ✗ $class (未找到)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "  ⚠ 无法验证：JAR文件不存在" -ForegroundColor Yellow
}

Write-Host ""

# 步骤4: 生成修复建议
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复建议" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. 在IntelliJ IDEA中执行以下操作:" -ForegroundColor Yellow
Write-Host "   a) File → Invalidate Caches / Restart..." -ForegroundColor White
Write-Host "   b) 选择 'Invalidate and Restart'" -ForegroundColor White
Write-Host "   c) 等待IDE重启并重新索引项目" -ForegroundColor White
Write-Host ""

Write-Host "2. 重新导入Maven项目:" -ForegroundColor Yellow
Write-Host "   a) 打开 Maven 工具窗口 (View → Tool Windows → Maven)" -ForegroundColor White
Write-Host "   b) 点击 'Reload All Maven Projects' 按钮" -ForegroundColor White
Write-Host "   c) 等待Maven项目重新导入完成" -ForegroundColor White
Write-Host ""

Write-Host "3. 如果问题仍然存在:" -ForegroundColor Yellow
Write-Host "   a) 检查项目设置: File → Project Structure → Modules" -ForegroundColor White
Write-Host "   b) 确认 microservices-common 模块已正确添加为依赖" -ForegroundColor White
Write-Host "   c) 检查Maven设置: File → Settings → Build Tools → Maven" -ForegroundColor White
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
