# Git诊断脚本 - 用于排查Agent Review的Git问题
# 创建时间: 2025-01-30

Write-Host "=== Git诊断工具 ===" -ForegroundColor Cyan
Write-Host ""

# 1. 检查Git版本
Write-Host "1. Git版本检查:" -ForegroundColor Yellow
try {
    $gitVersion = git --version
    Write-Host "   ✓ $gitVersion" -ForegroundColor Green
} catch {
    Write-Host "   ✗ Git未安装或不在PATH中" -ForegroundColor Red
    exit 1
}

# 2. 检查是否在Git仓库中
Write-Host "`n2. Git仓库检查:" -ForegroundColor Yellow
try {
    $isRepo = git rev-parse --git-dir 2>$null
    if ($isRepo) {
        Write-Host "   ✓ 当前目录是Git仓库" -ForegroundColor Green
        Write-Host "   Git目录: $isRepo" -ForegroundColor Gray
    } else {
        Write-Host "   ✗ 当前目录不是Git仓库" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "   ✗ 无法确定Git仓库状态" -ForegroundColor Red
    exit 1
}

# 3. 检查Git状态
Write-Host "`n3. Git状态检查:" -ForegroundColor Yellow
try {
    $status = git status --porcelain
    if ($status) {
        Write-Host "   有未提交的更改:" -ForegroundColor Yellow
        $status | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
    } else {
        Write-Host "   ✓ 工作目录干净" -ForegroundColor Green
    }
} catch {
    Write-Host "   ✗ 无法获取Git状态: $_" -ForegroundColor Red
}

# 4. 检查Git配置
Write-Host "`n4. Git配置检查:" -ForegroundColor Yellow
try {
    $userName = git config user.name
    $userEmail = git config user.email
    if ($userName) {
        Write-Host "   ✓ 用户名: $userName" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ 未设置用户名" -ForegroundColor Yellow
    }
    if ($userEmail) {
        Write-Host "   ✓ 邮箱: $userEmail" -ForegroundColor Green
    } else {
        Write-Host "   ⚠ 未设置邮箱" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ✗ 无法读取Git配置: $_" -ForegroundColor Red
}

# 5. 检查远程仓库连接
Write-Host "`n5. 远程仓库检查:" -ForegroundColor Yellow
try {
    $remotes = git remote -v
    if ($remotes) {
        Write-Host "   ✓ 远程仓库配置:" -ForegroundColor Green
        $remotes | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
        
        # 检查SSH连接（如果是SSH URL）
        $remoteUrl = git remote get-url origin
        if ($remoteUrl -match 'git@') {
            Write-Host "   ⚠ 使用SSH协议，检查SSH密钥..." -ForegroundColor Yellow
            $sshTest = ssh -T git@github.com 2>&1
            if ($sshTest -match 'successfully authenticated') {
                Write-Host "   ✓ SSH连接正常" -ForegroundColor Green
            } else {
                Write-Host "   ⚠ SSH连接可能有问题" -ForegroundColor Yellow
                Write-Host "   提示: Agent Review可能需要HTTPS协议" -ForegroundColor Gray
            }
        }
    } else {
        Write-Host "   ⚠ 未配置远程仓库" -ForegroundColor Yellow
    }
} catch {
    Write-Host "   ✗ 无法检查远程仓库: $_" -ForegroundColor Red
}

# 6. 检查Git钩子
Write-Host "`n6. Git钩子检查:" -ForegroundColor Yellow
$hooksPath = ".git\hooks"
if (Test-Path $hooksPath) {
    $hooks = Get-ChildItem $hooksPath -File | Where-Object { $_.Name -notlike '*.sample' }
    if ($hooks) {
        Write-Host "   已安装的钩子:" -ForegroundColor Yellow
        $hooks | ForEach-Object { Write-Host "   - $($_.Name)" -ForegroundColor Gray }
    } else {
        Write-Host "   ✓ 无自定义钩子" -ForegroundColor Green
    }
} else {
    Write-Host "   ✗ 钩子目录不存在" -ForegroundColor Red
}

# 7. 测试常见Git命令
Write-Host "`n7. Git命令测试:" -ForegroundColor Yellow
$testCommands = @(
    @{Name="git log"; Command="git log --oneline -1"},
    @{Name="git diff"; Command="git diff --stat"},
    @{Name="git show"; Command="git show --stat HEAD"},
    @{Name="git ls-files"; Command="git ls-files | Select-Object -First 5"}
)

foreach ($test in $testCommands) {
    try {
        $result = Invoke-Expression $test.Command 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "   ✓ $($test.Name) - 正常" -ForegroundColor Green
        } else {
            Write-Host "   ✗ $($test.Name) - 失败 (退出码: $LASTEXITCODE)" -ForegroundColor Red
        }
    } catch {
        Write-Host "   ✗ $($test.Name) - 异常: $_" -ForegroundColor Red
    }
}

# 8. 检查文件权限
Write-Host "`n8. 文件权限检查:" -ForegroundColor Yellow
try {
    $gitDir = git rev-parse --git-dir
    $canRead = Test-Path $gitDir
    $canWrite = Test-Path $gitDir -PathType Container
    if ($canRead -and $canWrite) {
        Write-Host "   ✓ Git目录可读写" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Git目录权限问题" -ForegroundColor Red
    }
} catch {
    Write-Host "   ✗ 无法检查权限: $_" -ForegroundColor Red
}

# 9. 建议
Write-Host "`n=== 诊断建议 ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "如果Agent Review仍然失败，请尝试:" -ForegroundColor Yellow
Write-Host "1. 检查Cursor/IDE的Git集成设置" -ForegroundColor White
Write-Host "2. 尝试重启Cursor/IDE" -ForegroundColor White
Write-Host "3. 检查是否有Git钩子阻止了操作" -ForegroundColor White
Write-Host "4. 如果使用SSH，考虑切换到HTTPS协议" -ForegroundColor White
Write-Host "5. 检查防火墙或安全软件是否阻止Git操作" -ForegroundColor White
Write-Host ""
Write-Host "诊断完成！" -ForegroundColor Green
