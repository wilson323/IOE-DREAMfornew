# 快速构建所有服务
# 自动化执行完整的构建和验证流程

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM Docker构建自动化脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 步骤1: 验证修复
Write-Host "步骤1: 验证Dockerfile修复..." -ForegroundColor Yellow
try {
    & powershell -ExecutionPolicy Bypass -File scripts\final-verify-dockerfiles.ps1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 验证失败，请先修复Dockerfile" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "⚠️  验证脚本执行失败，继续构建..." -ForegroundColor Yellow
}

Write-Host ""

# 步骤2: 清理之前的构建
Write-Host "步骤2: 清理之前的构建..." -ForegroundColor Yellow
docker-compose -f docker-compose-all.yml down
Write-Host "✅ 清理完成" -ForegroundColor Green
Write-Host ""

# 步骤3: 构建所有服务
Write-Host "步骤3: 构建所有服务镜像..." -ForegroundColor Yellow
Write-Host "这可能需要10-30分钟，请耐心等待..." -ForegroundColor Cyan
Write-Host ""

$buildStartTime = Get-Date
docker-compose -f docker-compose-all.yml build --no-cache

if ($LASTEXITCODE -eq 0) {
    $buildEndTime = Get-Date
    $buildDuration = $buildEndTime - $buildStartTime
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "✅ 构建成功！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "构建耗时: $($buildDuration.TotalMinutes.ToString('F2')) 分钟" -ForegroundColor Cyan
    Write-Host ""
    
    # 步骤4: 启动服务
    Write-Host "步骤4: 启动所有服务..." -ForegroundColor Yellow
    docker-compose -f docker-compose-all.yml up -d
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 服务启动命令执行成功" -ForegroundColor Green
        Write-Host ""
        Write-Host "等待服务启动（180秒）..." -ForegroundColor Yellow
        Start-Sleep -Seconds 180
        
        # 步骤5: 验证部署
        Write-Host ""
        Write-Host "步骤5: 验证部署..." -ForegroundColor Yellow
        if (Test-Path "scripts\verify-deployment-step-by-step.ps1") {
            & powershell -ExecutionPolicy Bypass -File scripts\verify-deployment-step-by-step.ps1
        } else {
            Write-Host "⚠️  验证脚本不存在，跳过验证" -ForegroundColor Yellow
        }
        
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "✅ 构建和部署完成！" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "服务访问地址:" -ForegroundColor Cyan
        Write-Host "  - 网关服务: http://localhost:8080" -ForegroundColor White
        Write-Host "  - Nacos控制台: http://localhost:8848/nacos" -ForegroundColor White
        Write-Host "  - 公共服务: http://localhost:8088" -ForegroundColor White
        Write-Host ""
        Write-Host "查看服务状态:" -ForegroundColor Cyan
        Write-Host "  docker-compose -f docker-compose-all.yml ps" -ForegroundColor White
        Write-Host ""
        Write-Host "查看服务日志:" -ForegroundColor Cyan
        Write-Host "  docker-compose -f docker-compose-all.yml logs -f" -ForegroundColor White
    } else {
        Write-Host "❌ 服务启动失败" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "❌ 构建失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "请检查构建日志:" -ForegroundColor Yellow
    $logCmd = "docker-compose -f docker-compose-all.yml build --progress=plain 2>&1 | Select-String -Pattern ERROR"
    Write-Host "  $logCmd" -ForegroundColor White
    Write-Host ""
    Write-Host "如果仍然看到pom-temp.xml错误，请执行:" -ForegroundColor Yellow
    Write-Host "  powershell -ExecutionPolicy Bypass -File scripts\nuclear-clean-rebuild.ps1" -ForegroundColor White
    exit 1
}
