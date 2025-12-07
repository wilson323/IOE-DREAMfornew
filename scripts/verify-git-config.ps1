# Git 配置验证脚本
# 用于验证 Git 相关配置是否正确设置

Write-Host "=== Git 配置验证 ===" -ForegroundColor Cyan

# 检查 diff.renameLimit 配置
Write-Host "`n1. 检查 diff.renameLimit 配置:" -ForegroundColor Yellow
$renameLimit = git config --get diff.renameLimit
if ($renameLimit) {
    Write-Host "   ✓ diff.renameLimit = $renameLimit" -ForegroundColor Green
    if ([int]$renameLimit -ge 1814) {
        Write-Host "   ✓ 配置值满足要求 (>= 1814)" -ForegroundColor Green
    } else {
        Write-Host "   ✗ 配置值不足，建议设置为至少 1814" -ForegroundColor Red
    }
} else {
    Write-Host "   ✗ diff.renameLimit 未设置" -ForegroundColor Red
    Write-Host "   建议执行: git config diff.renameLimit 2000" -ForegroundColor Yellow
}

# 检查 Windows 保留名称文件
Write-Host "`n2. 检查 Windows 保留名称文件:" -ForegroundColor Yellow
$reservedNames = @("nul", "CON", "PRN", "AUX", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9")
$foundReserved = $false
foreach ($name in $reservedNames) {
    if (Test-Path $name) {
        Write-Host "   ✗ 发现保留名称文件: $name" -ForegroundColor Red
        $foundReserved = $true
    }
}
if (-not $foundReserved) {
    Write-Host "   ✓ 未发现 Windows 保留名称文件" -ForegroundColor Green
}

# 检查 .gitignore 配置
Write-Host "`n3. 检查 .gitignore 配置:" -ForegroundColor Yellow
if (Test-Path ".gitignore") {
    $gitignoreContent = Get-Content ".gitignore" -Raw
    $allReservedFound = $true
    foreach ($name in $reservedNames) {
        if ($gitignoreContent -notmatch "^\s*$name\s*$" -and $gitignoreContent -notmatch "^\s*$name\s*`r?`n") {
            Write-Host "   ✗ .gitignore 中缺少: $name" -ForegroundColor Red
            $allReservedFound = $false
        }
    }
    if ($allReservedFound) {
        Write-Host "   ✓ .gitignore 已正确配置所有 Windows 保留名称" -ForegroundColor Green
    }
} else {
    Write-Host "   ✗ .gitignore 文件不存在" -ForegroundColor Red
}

# 检查 Git 状态
Write-Host "`n4. 检查 Git 状态:" -ForegroundColor Yellow
$gitStatus = git status --porcelain 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "   ✓ Git 状态正常" -ForegroundColor Green
    $statusLines = $gitStatus | Measure-Object -Line
    Write-Host "   未跟踪/已修改文件数: $($statusLines.Lines)" -ForegroundColor Cyan
} else {
    Write-Host "   ✗ Git 状态检查失败" -ForegroundColor Red
    Write-Host "   错误信息: $gitStatus" -ForegroundColor Red
}

Write-Host "`n=== 验证完成 ===" -ForegroundColor Cyan
