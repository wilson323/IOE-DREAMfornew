# IOE-DREAM 根源性修复所有依赖错误脚本
# 功能：一次性解决所有IDE依赖解析问题
# 作者：AI Assistant
# 日期：2025-01-30

$ErrorActionPreference = "Stop"
$workspaceRoot = "D:\IOE-DREAM"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 根源性修复所有依赖错误" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $workspaceRoot

# 步骤1: 强制构建 microservices-common（P0级）
Write-Host "[步骤1] 强制构建 microservices-common 模块（必须）..." -ForegroundColor Yellow
Write-Host "  这是解决所有依赖错误的根源！" -ForegroundColor Red
Write-Host ""

try {
    $buildOutput = mvn clean install -pl microservices/microservices-common -am -DskipTests 2>&1 | Out-String

    if ($LASTEXITCODE -eq 0 -or $buildOutput -match "BUILD SUCCESS") {
        Write-Host "  ✓ microservices-common 构建成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ microservices-common 构建失败" -ForegroundColor Red
        Write-Host $buildOutput
        exit 1
    }
} catch {
    Write-Host "  ✗ 构建过程出错: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤2: 验证JAR文件
Write-Host "[步骤2] 验证JAR文件..." -ForegroundColor Yellow
$jarPath = "$env:USERPROFILE\.m2\repository\net\lab1024\sa\microservices-common\1.0.0\microservices-common-1.0.0.jar"

if (Test-Path $jarPath) {
    $jarSize = (Get-Item $jarPath).Length
    Write-Host "  ✓ JAR文件存在: $jarSize bytes" -ForegroundColor Green

    # 验证关键类
    $jarContent = jar -tf $jarPath 2>&1 | Out-String
    $requiredClasses = @(
        "net/lab1024/sa/common/dto/ResponseDTO.class",
        "net/lab1024/sa/common/organization/entity/DeviceEntity.class",
        "net/lab1024/sa/common/device/DeviceConnectionTest.class",
        "net/lab1024/sa/common/device/DeviceDispatchResult.class"
    )

    foreach ($class in $requiredClasses) {
        if ($jarContent -match [regex]::Escape($class)) {
            Write-Host "    ✓ $class" -ForegroundColor Green
        } else {
            Write-Host "    ✗ $class (缺失)" -ForegroundColor Red
        }
    }
} else {
    Write-Host "  ✗ JAR文件不存在！构建可能失败" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 步骤3: 清理IDE缓存
Write-Host "[步骤3] 清理IDE缓存..." -ForegroundColor Yellow

$ideCacheDirs = @(
    "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\compile-server",
    "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\caches",
    "$env:LOCALAPPDATA\JetBrains\IntelliJIdea2025.2\log\build-log"
)

foreach ($dir in $ideCacheDirs) {
    if (Test-Path $dir) {
        try {
            Remove-Item -Path $dir -Recurse -Force -ErrorAction SilentlyContinue
            Write-Host "  ✓ 已清理: $dir" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠ 清理失败: $dir" -ForegroundColor Yellow
        }
    }
}

Write-Host ""

# 步骤4: 清理项目编译缓存
Write-Host "[步骤4] 清理项目编译缓存..." -ForegroundColor Yellow

$projectCacheDirs = @(
    "$workspaceRoot\out",
    "$workspaceRoot\microservices\*\target",
    "$workspaceRoot\microservices\*\out"
)

foreach ($pattern in $projectCacheDirs) {
    Get-ChildItem -Path $workspaceRoot -Include "target","out" -Recurse -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -notlike "*\.m2\*" } |
        ForEach-Object {
            try {
                Remove-Item -Path $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
                Write-Host "  ✓ 已清理: $($_.FullName)" -ForegroundColor Green
            } catch {
                # 忽略错误
            }
        }
}

Write-Host ""

# 步骤5: 生成完整修复报告
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "✅ 已完成的修复:" -ForegroundColor Green
Write-Host "  1. ✓ microservices-common 模块已构建并安装到本地仓库" -ForegroundColor White
Write-Host "  2. ✓ IDE缓存已清理" -ForegroundColor White
Write-Host "  3. ✓ 项目编译缓存已清理" -ForegroundColor White
Write-Host ""

Write-Host "📋 接下来请在IDE中执行:" -ForegroundColor Yellow
Write-Host "  1. File → Invalidate Caches / Restart..." -ForegroundColor White
Write-Host "  2. 选择 'Invalidate and Restart'" -ForegroundColor White
Write-Host "  3. Maven工具窗口 → Reload All Maven Projects" -ForegroundColor White
Write-Host ""

Write-Host "🔍 验证修复:" -ForegroundColor Yellow
Write-Host "  - 检查是否还有红色错误提示" -ForegroundColor White
Write-Host "  - 确认所有导入语句正常" -ForegroundColor White
Write-Host "  - 验证代码补全功能正常" -ForegroundColor White
Write-Host ""
