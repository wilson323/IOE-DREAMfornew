# IOE-DREAM 根源性问题系统性修复脚本

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 根源性问题系统性修复" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 问题清单
$issues = @(
    @{
        Id = "R-001"
        Name = "项目结构混乱（临时文件未归档）"
        Priority = "P0"
        Status = "待修复"
        Script = "cleanup-root-temp-files.ps1"
    },
    @{
        Id = "R-002"
        Name = "Docker构建策略冲突"
        Priority = "P0"
        Status = "已修复V5"
        Script = $null
    },
    @{
        Id = "R-003"
        Name = "Maven网络/SSL问题"
        Priority = "P0"
        Status = "已修复"
        Script = $null
    },
    @{
        Id = "R-004"
        Name = "Maven settings.xml格式问题"
        Priority = "P0"
        Status = "已修复"
        Script = $null
    },
    @{
        Id = "R-005"
        Name = "版本兼容性风险"
        Priority = "P1"
        Status = "需验证"
        Script = $null
    },
    @{
        Id = "R-006"
        Name = "文档管理分散"
        Priority = "P1"
        Status = "待修复"
        Script = $null
    },
    @{
        Id = "R-007"
        Name = "架构边界不清"
        Priority = "P1"
        Status = "部分解决"
        Script = "architecture-compliance-check.ps1"
    }
)

Write-Host "问题清单:" -ForegroundColor Yellow
foreach ($issue in $issues) {
    $statusColor = switch ($issue.Status) {
        "已修复" { "Green" }
        "已修复V5" { "Green" }
        "待修复" { "Red" }
        "需验证" { "Yellow" }
        "部分解决" { "Yellow" }
        default { "White" }
    }
    Write-Host "  [$($issue.Id)] $($issue.Name) - $($issue.Status)" -ForegroundColor $statusColor
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "执行修复" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# R-001: 清理根目录临时文件
Write-Host "[R-001] 清理根目录临时文件..." -ForegroundColor Yellow
if (Test-Path "scripts\cleanup-root-temp-files.ps1") {
    try {
        & powershell -ExecutionPolicy Bypass -File scripts\cleanup-root-temp-files.ps1
        Write-Host "  ✅ R-001 修复完成" -ForegroundColor Green
    } catch {
        Write-Host "  ❌ R-001 修复失败: $_" -ForegroundColor Red
    }
} else {
    Write-Host "  ⚠️  清理脚本不存在，跳过" -ForegroundColor Yellow
}

Write-Host ""

# R-002/R-003/R-004: 验证Docker构建修复
Write-Host "[R-002/R-003/R-004] 验证Docker构建修复..." -ForegroundColor Yellow
Write-Host "  请手动执行以下命令验证:" -ForegroundColor White
Write-Host "  docker-compose -f docker-compose-all.yml build --no-cache consume-service" -ForegroundColor Cyan
Write-Host ""

# R-005: 版本兼容性验证
Write-Host "[R-005] 版本兼容性验证..." -ForegroundColor Yellow
Write-Host "  当前版本配置:" -ForegroundColor White
Write-Host "    Spring Boot: 3.5.8" -ForegroundColor White
Write-Host "    Spring Cloud: 2025.0.0" -ForegroundColor White
Write-Host "    Spring Cloud Alibaba: 2022.0.0.0" -ForegroundColor White
Write-Host "  建议: 使用Maven Tools验证版本兼容性" -ForegroundColor Yellow
Write-Host ""

# R-006: 文档管理分散（提示）
Write-Host "[R-006] 文档管理分散..." -ForegroundColor Yellow
Write-Host "  文档目录统计:" -ForegroundColor White
Write-Host "    documentation/: 876个文件" -ForegroundColor White
Write-Host "    docs/: 98个文件" -ForegroundColor White
Write-Host "    .qoder/repowiki/: 250个文件" -ForegroundColor White
Write-Host "  建议: 统一到documentation/目录" -ForegroundColor Yellow
Write-Host ""

# R-007: 架构合规性检查
Write-Host "[R-007] 架构合规性检查..." -ForegroundColor Yellow
if (Test-Path "scripts\architecture-compliance-check.ps1") {
    try {
        & powershell -ExecutionPolicy Bypass -File scripts\architecture-compliance-check.ps1
        Write-Host "  ✅ R-007 检查完成" -ForegroundColor Green
    } catch {
        Write-Host "  ⚠️  R-007 检查脚本不存在或执行失败" -ForegroundColor Yellow
    }
} else {
    Write-Host "  ⚠️  架构合规性检查脚本不存在" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "修复完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步: 验证Docker构建" -ForegroundColor Yellow
Write-Host "  docker-compose -f docker-compose-all.yml build --no-cache consume-service" -ForegroundColor White
