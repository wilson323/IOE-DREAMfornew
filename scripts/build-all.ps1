# =====================================================
# IOE-DREAM 一键构建脚本
# 版本: v2.0.0
# 描述: 自动按正确顺序构建并安装所有模块，最后构建整个项目
# 创建时间: 2025-12-21
# =====================================================

param(
    [Parameter(Mandatory = $false)]
    [switch]$Clean,

    [Parameter(Mandatory = $false)]
    [switch]$SkipTests,

    [Parameter(Mandatory = $false)]
    [switch]$SkipQuality,

    [Parameter(Mandatory = $false)]
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$BuildScript = Join-Path $ScriptDir "build-ordered.ps1"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 一键构建" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  使用改进的构建脚本: build-ordered.ps1" -ForegroundColor Gray
Write-Host ""

# 构建参数
$buildArgs = @("full")

if ($Clean) {
    $buildArgs += "-Clean"
}

if ($SkipTests) {
    $buildArgs += "-SkipTests"
}
else {
    Write-Host "  提示: 使用 -SkipTests 可跳过测试以加快构建速度" -ForegroundColor Yellow
}

if ($SkipQuality) {
    $buildArgs += "-SkipQuality"
}

if ($Verbose) {
    $buildArgs += "-Verbose"
}

# 执行构建脚本
& $BuildScript @buildArgs

exit $LASTEXITCODE

