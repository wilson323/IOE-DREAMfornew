# IOE-DREAM 快速构建脚本
# 简化版本，专注于快速编译和基本验证

param(
    [string]$Service = "",
    [switch]$Clean,
    [switch]$Help
)

function Show-Usage {
    Write-Host "IOE-DREAM 快速构建脚本" -ForegroundColor Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor Yellow
    Write-Host "  .\scripts\quick-build.ps1 [-Service <服务名>] [-Clean]"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Yellow
    Write-Host "  .\scripts\quick-build.ps1                                    # 构建所有服务"
    Write-Host "  .\scripts\quick-build.ps1 -Service ioedream-access-service    # 构建指定服务"
    Write-Host "  .\scripts\quick-build.ps1 -Clean                              # 清理后构建"
    Write-Host ""
}

$timestamp = Get-Date -Format "HH:mm:ss"
Write-Host "[$timestamp] 🚀 开始快速构建..." -ForegroundColor Green

if ($Help) {
    Show-Usage
    exit 0
}

# 检查基本条件
if (-not (Test-Path "microservices\pom.xml")) {
    Write-Host "❌ 错误: 找不到 microservices\pom.xml" -ForegroundColor Red
    exit 1
}

# 构建命令
$mavenArgs = @()
if ($Service) {
    $mavenArgs += "-pl", $Service, "-am"
}

if ($Clean) {
    $mavenArgs += "clean"
}

$mavenArgs += "install", "-DskipTests", "-Dpmd.skip=true", "-q"

$serviceDesc = if ($Service) { "服务: $Service" } else { "所有服务" }
Write-Host "[$timestamp] 📦 构建 $serviceDesc..." -ForegroundColor Cyan

try {
    $buildTime = Measure-Command {
        & mvn $mavenArgs -f "microservices\pom.xml"
    }

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[$timestamp] ✅ 构建成功！" -ForegroundColor Green
        Write-Host "[$timestamp] ⏱️  构建耗时: $($buildTime.TotalSeconds.ToString('F2'))秒" -ForegroundColor Gray

        # 显示构建产物
        if (-not $Service) {
            $jarCount = (Get-ChildItem -Path "microservices" -Recurse -Filter "*-1.0.0.jar" -File).Count
            Write-Host "[$timestamp] 📦 生成了 $jarCount 个JAR文件" -ForegroundColor Gray
        }

    } else {
        Write-Host "[$timestamp] ❌ 构建失败！退出码: $LASTEXITCODE" -ForegroundColor Red
        exit $LASTEXITCODE
    }
} catch {
    Write-Host "[$timestamp] ❌ 构建异常: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "[$timestamp] 🎉 快速构建完成！" -ForegroundColor Green