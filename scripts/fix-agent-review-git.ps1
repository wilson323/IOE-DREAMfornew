# Agent Review Git问题修复脚本
# 创建时间: 2025-01-30
# 用途: 修复Cursor Agent Review的Git相关问题

param(
    [switch]$DisableHooks,
    [switch]$EnableHooks,
    [switch]$CheckOnly
)

Write-Host "=== Agent Review Git问题修复工具 ===" -ForegroundColor Cyan
Write-Host ""

$hooksPath = ".git\hooks"
$hooksBackupPath = ".git\hooks.backup"

# 检查模式
if ($CheckOnly) {
    Write-Host "检查模式: 仅诊断问题，不进行修复" -ForegroundColor Yellow
    Write-Host ""
    
    # 检查钩子
    if (Test-Path $hooksPath) {
        $hooks = Get-ChildItem $hooksPath -File | Where-Object { $_.Name -notlike '*.sample' }
        if ($hooks) {
            Write-Host "发现以下Git钩子:" -ForegroundColor Yellow
            foreach ($hook in $hooks) {
                $firstLine = Get-Content $hook.FullName -First 1 -ErrorAction SilentlyContinue
                $isBash = $firstLine -match '#!/bin/(sh|bash)'
                if ($isBash) {
                    Write-Host "  ⚠ $($hook.Name) - 使用bash脚本（Windows可能不兼容）" -ForegroundColor Yellow
                } else {
                    Write-Host "  ✓ $($hook.Name) - 脚本类型正常" -ForegroundColor Green
                }
            }
        }
    }
    
    # 检查Git命令
    Write-Host "`n测试Git命令:" -ForegroundColor Yellow
    $testCommands = @(
        "git --version",
        "git status",
        "git log --oneline -1",
        "git diff --stat"
    )
    
    foreach ($cmd in $testCommands) {
        try {
            $result = Invoke-Expression $cmd 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  ✓ $cmd" -ForegroundColor Green
            } else {
                Write-Host "  ✗ $cmd (退出码: $LASTEXITCODE)" -ForegroundColor Red
            }
        } catch {
            Write-Host "  ✗ $cmd (异常: $_)" -ForegroundColor Red
        }
    }
    
    Write-Host "`n检查完成！" -ForegroundColor Green
    exit 0
}

# 禁用钩子
if ($DisableHooks) {
    Write-Host "禁用Git钩子..." -ForegroundColor Yellow
    
    if (Test-Path $hooksPath) {
        if (Test-Path $hooksBackupPath) {
            Write-Host "  备份目录已存在，跳过备份" -ForegroundColor Gray
        } else {
            Copy-Item -Path $hooksPath -Destination $hooksBackupPath -Recurse -Force
            Write-Host "  ✓ 已备份钩子到 $hooksBackupPath" -ForegroundColor Green
        }
        
        # 重命名钩子目录
        $hooksDisabledPath = ".git\hooks.disabled"
        if (Test-Path $hooksDisabledPath) {
            Write-Host "  钩子已禁用" -ForegroundColor Gray
        } else {
            Rename-Item -Path $hooksPath -NewName "hooks.disabled" -Force
            Write-Host "  ✓ 钩子已禁用" -ForegroundColor Green
        }
    } else {
        Write-Host "  ⚠ 钩子目录不存在" -ForegroundColor Yellow
    }
    
    Write-Host "`n提示: 禁用钩子后，Agent Review应该可以正常工作" -ForegroundColor Cyan
    Write-Host "      但提交代码时将不会执行钩子检查" -ForegroundColor Yellow
    exit 0
}

# 启用钩子
if ($EnableHooks) {
    Write-Host "启用Git钩子..." -ForegroundColor Yellow
    
    $hooksDisabledPath = ".git\hooks.disabled"
    if (Test-Path $hooksDisabledPath) {
        if (Test-Path $hooksPath) {
            Write-Host "  ⚠ 钩子目录已存在，先删除" -ForegroundColor Yellow
            Remove-Item -Path $hooksPath -Recurse -Force
        }
        Rename-Item -Path $hooksDisabledPath -NewName "hooks" -Force
        Write-Host "  ✓ 钩子已启用" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ 未找到禁用的钩子目录" -ForegroundColor Yellow
    }
    exit 0
}

# 默认：自动修复
Write-Host "自动修复模式" -ForegroundColor Yellow
Write-Host ""

# 1. 检查问题
Write-Host "1. 诊断问题..." -ForegroundColor Yellow
$hasBashHooks = $false
if (Test-Path $hooksPath) {
    $hooks = Get-ChildItem $hooksPath -File | Where-Object { $_.Name -notlike '*.sample' }
    foreach ($hook in $hooks) {
        $firstLine = Get-Content $hook.FullName -First 1 -ErrorAction SilentlyContinue
        if ($firstLine -match '#!/bin/(sh|bash)') {
            $hasBashHooks = $true
            Write-Host "   发现bash钩子: $($hook.Name)" -ForegroundColor Yellow
        }
    }
}

if ($hasBashHooks) {
    Write-Host "`n2. 建议修复方案:" -ForegroundColor Yellow
    Write-Host "   方案A: 临时禁用钩子（快速修复）" -ForegroundColor White
    Write-Host "   方案B: 保持钩子，但确保Agent Review不触发钩子" -ForegroundColor White
    Write-Host ""
    Write-Host "   执行以下命令之一:" -ForegroundColor Cyan
    Write-Host "   .\scripts\fix-agent-review-git.ps1 -DisableHooks  # 禁用钩子" -ForegroundColor White
    Write-Host "   .\scripts\fix-agent-review-git.ps1 -CheckOnly     # 仅检查" -ForegroundColor White
} else {
    Write-Host "   ✓ 未发现明显的钩子兼容性问题" -ForegroundColor Green
}

# 2. 测试Git命令
Write-Host "`n3. 测试Git命令..." -ForegroundColor Yellow
$gitCommands = @(
    @{Name="git --version"; Cmd="git --version"},
    @{Name="git status"; Cmd="git status --porcelain"},
    @{Name="git log"; Cmd="git log --oneline -1"}
)

$allPassed = $true
foreach ($test in $gitCommands) {
    try {
        $result = Invoke-Expression $test.Cmd 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✓ $($test.Name)" -ForegroundColor Green
        } else {
            Write-Host "   ✗ $($test.Name) (退出码: $LASTEXITCODE)" -ForegroundColor Red
            $allPassed = $false
        }
    } catch {
        Write-Host "   ✗ $($test.Name) (异常: $_)" -ForegroundColor Red
        $allPassed = $false
    }
}

# 3. 建议
Write-Host "`n=== 修复建议 ===" -ForegroundColor Cyan
if ($hasBashHooks -or -not $allPassed) {
    Write-Host ""
    Write-Host "如果Agent Review仍然失败，请尝试:" -ForegroundColor Yellow
    Write-Host "1. 临时禁用钩子: .\scripts\fix-agent-review-git.ps1 -DisableHooks" -ForegroundColor White
    Write-Host "2. 重启Cursor IDE" -ForegroundColor White
    Write-Host "3. 检查Cursor的Git集成设置" -ForegroundColor White
    Write-Host "4. 清除Cursor缓存后重启" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "Git基本功能正常，如果Agent Review仍然失败:" -ForegroundColor Yellow
    Write-Host "1. 重启Cursor IDE" -ForegroundColor White
    Write-Host "2. 检查Cursor的Git集成设置" -ForegroundColor White
    Write-Host "3. 查看Cursor的错误日志" -ForegroundColor White
}

Write-Host "`n修复脚本执行完成！" -ForegroundColor Green
