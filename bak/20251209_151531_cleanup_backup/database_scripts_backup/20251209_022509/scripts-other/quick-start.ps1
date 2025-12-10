# IOE-DREAM 快速启动脚本（简化版）
# 一键启动前后端移动端
# 
# 使用方法：
#   .\quick-start.ps1                    # 启动所有服务
#   .\quick-start.ps1 -Backend          # 仅启动后端
#   .\quick-start.ps1 -Frontend         # 仅启动前端
#   .\quick-start.ps1 -Mobile          # 仅启动移动端

# 设置编码
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

param(
    [switch]$Backend = $false,
    [switch]$Frontend = $false,
    [switch]$Mobile = $false
)

$ErrorActionPreference = "Continue"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 快速启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 如果没有指定参数，启动所有服务
if (-not $Backend -and -not $Frontend -and -not $Mobile) {
    $Backend = $true
    $Frontend = $true
    $Mobile = $true
}

# 启动后端
if ($Backend) {
    Write-Host "🚀 启动后端微服务..." -ForegroundColor Yellow
    & "$PSScriptRoot\start-all-complete.ps1" -BackendOnly
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 后端启动失败" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ 后端服务启动完成" -ForegroundColor Green
    Write-Host "   等待60秒让服务完全启动..." -ForegroundColor Gray
    Start-Sleep -Seconds 60
    Write-Host ""
}

# 启动前端
if ($Frontend) {
    Write-Host "🚀 启动前端管理后台..." -ForegroundColor Yellow
    & "$PSScriptRoot\start-all-complete.ps1" -FrontendOnly
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 前端启动失败" -ForegroundColor Red
    } else {
        Write-Host "✅ 前端启动完成" -ForegroundColor Green
    }
    Write-Host ""
}

# 启动移动端
if ($Mobile) {
    Write-Host "🚀 启动移动端应用..." -ForegroundColor Yellow
    & "$PSScriptRoot\start-all-complete.ps1" -MobileOnly
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 移动端启动失败" -ForegroundColor Red
    } else {
        Write-Host "✅ 移动端启动完成" -ForegroundColor Green
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ 所有服务启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📱 访问地址:" -ForegroundColor Yellow
Write-Host "  前端管理后台: http://localhost:3000" -ForegroundColor White
Write-Host "  移动端应用:   http://localhost:8081" -ForegroundColor White
Write-Host "  API网关:      http://localhost:8080" -ForegroundColor White
Write-Host "  Nacos控制台:  http://localhost:8848/nacos" -ForegroundColor White
Write-Host ""
Write-Host "💡 提示: 使用 .\scripts\start-all-complete.ps1 -CheckOnly 检查服务状态" -ForegroundColor Gray
Write-Host ""
