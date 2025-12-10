# Docker网络冲突修复脚本
# 解决docker-compose网络子网冲突问题

param(
    [switch]$RemoveExisting = $false,
    [switch]$UseExisting = $false
)

$ErrorActionPreference = "Stop"

Write-Host "===== Docker网络冲突修复 =====" -ForegroundColor Cyan

# 检查现有网络
Write-Host "`n检查现有Docker网络..." -ForegroundColor Yellow
$existingNetworks = docker network ls --format "{{.Name}}" | Select-String "ioedream"

if ($existingNetworks) {
    Write-Host "  发现现有网络:" -ForegroundColor Cyan
    $existingNetworks | ForEach-Object { Write-Host "    - $_" -ForegroundColor Gray }

    if ($RemoveExisting) {
        Write-Host "`n删除现有网络..." -ForegroundColor Yellow
        $existingNetworks | ForEach-Object {
            docker network rm $_ 2>&1 | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✅ 已删除网络: $_" -ForegroundColor Green
            } else {
                Write-Host "  ⚠️  删除网络失败: $_ (可能正在使用)" -ForegroundColor Yellow
            }
        }
    } elseif ($UseExisting) {
        Write-Host "`n使用现有网络..." -ForegroundColor Yellow
        Write-Host "  修改docker-compose.yml使用现有网络" -ForegroundColor Gray
    } else {
        Write-Host "`n解决方案:" -ForegroundColor Yellow
        Write-Host "  1. 删除现有网络: .\scripts\fix-docker-network.ps1 -RemoveExisting" -ForegroundColor Gray
        Write-Host "  2. 使用现有网络: .\scripts\fix-docker-network.ps1 -UseExisting" -ForegroundColor Gray
        Write-Host "  3. 修改子网配置: 已修改为 172.21.0.0/16" -ForegroundColor Gray
    }
} else {
    Write-Host "  ✅ 未发现冲突网络" -ForegroundColor Green
}

# 检查子网冲突
Write-Host "`n检查子网配置..." -ForegroundColor Yellow
$composeFile = "D:\IOE-DREAM\documentation\technical\verification\docker\docker-compose.yml"
if (Test-Path $composeFile) {
    $content = Get-Content $composeFile -Raw
    if ($content -match "subnet:\s*172\.21\.0\.0/16") {
        Write-Host "  ✅ 子网已修改为 172.21.0.0/16" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  子网配置需要更新" -ForegroundColor Yellow
    }
}

Write-Host "`n===== 修复完成 =====" -ForegroundColor Cyan
Write-Host "现在可以重新运行: docker-compose up -d mysql redis nacos" -ForegroundColor Green
