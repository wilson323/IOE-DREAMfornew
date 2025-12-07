# 完整推送代码到GitHub脚本
# 用途：将本地所有代码推送到GitHub仓库

Write-Host "=== 推送代码到GitHub ===" -ForegroundColor Cyan

$projectRoot = Get-Location
Set-Location $projectRoot

# 检查Git仓库
if (-not (Test-Path ".git")) {
    Write-Host "错误: 当前目录不是Git仓库" -ForegroundColor Red
    exit 1
}

# 1. 检查并设置远程仓库（使用HTTPS）
Write-Host "`n1. 检查远程仓库配置..." -ForegroundColor Yellow
$remoteUrl = git remote get-url origin 2>$null

if (-not $remoteUrl) {
    Write-Host "添加远程仓库..." -ForegroundColor Yellow
    git remote add origin https://github.com/wilson323/IOE-DREAMfornew.git
    Write-Host "✓ 已添加远程仓库" -ForegroundColor Green
} else {
    Write-Host "当前远程仓库: $remoteUrl" -ForegroundColor Cyan
    
    # 如果是SSH，切换到HTTPS
    if ($remoteUrl -like "git@*") {
        Write-Host "检测到SSH协议，切换到HTTPS..." -ForegroundColor Yellow
        git remote set-url origin https://github.com/wilson323/IOE-DREAMfornew.git
        Write-Host "✓ 已切换到HTTPS协议" -ForegroundColor Green
    }
}

# 验证远程仓库
$finalUrl = git remote get-url origin
Write-Host "远程仓库URL: $finalUrl" -ForegroundColor Green

# 2. 检查当前分支
Write-Host "`n2. 检查当前分支..." -ForegroundColor Yellow
$currentBranch = git branch --show-current
Write-Host "当前分支: $currentBranch" -ForegroundColor Cyan

# 3. 检查工作区状态
Write-Host "`n3. 检查工作区状态..." -ForegroundColor Yellow
$status = git status --porcelain
if ($status) {
    Write-Host "发现未提交的更改:" -ForegroundColor Yellow
    Write-Host $status -ForegroundColor Gray
    
    $commit = Read-Host "`n是否先提交这些更改? (y/n)"
    if ($commit -eq "y" -or $commit -eq "Y") {
        Write-Host "`n提交更改..." -ForegroundColor Yellow
        
        # 添加所有更改
        git add .
        
        # 获取提交信息
        $commitMsg = Read-Host "请输入提交信息 (直接回车使用默认)"
        if ([string]::IsNullOrWhiteSpace($commitMsg)) {
            $commitMsg = "Update: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
        }
        
        git commit -m $commitMsg
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ 更改已提交" -ForegroundColor Green
        } else {
            Write-Host "✗ 提交失败" -ForegroundColor Red
            exit 1
        }
    }
} else {
    Write-Host "✓ 工作区干净，无未提交更改" -ForegroundColor Green
}

# 4. 检查是否有提交
Write-Host "`n4. 检查提交历史..." -ForegroundColor Yellow
$commitCount = (git rev-list --count HEAD 2>$null)
if ($commitCount -eq 0) {
    Write-Host "警告: 仓库中没有提交记录" -ForegroundColor Yellow
    Write-Host "需要先进行初始提交" -ForegroundColor Yellow
    
    $initCommit = Read-Host "是否创建初始提交? (y/n)"
    if ($initCommit -eq "y" -or $initCommit -eq "Y") {
        git add .
        git commit -m "Initial commit: IOE-DREAM project"
        Write-Host "✓ 初始提交已创建" -ForegroundColor Green
    } else {
        Write-Host "操作已取消" -ForegroundColor Yellow
        exit 0
    }
} else {
    Write-Host "提交记录数: $commitCount" -ForegroundColor Green
}

# 5. 获取所有分支
Write-Host "`n5. 检查本地分支..." -ForegroundColor Yellow
$branches = git branch --format='%(refname:short)'
Write-Host "本地分支:" -ForegroundColor Cyan
$branches | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }

# 6. 推送代码
Write-Host "`n6. 开始推送代码..." -ForegroundColor Yellow
Write-Host "目标仓库: $finalUrl" -ForegroundColor Cyan
Write-Host "目标分支: $currentBranch" -ForegroundColor Cyan

Write-Host "`n注意: 如果提示输入用户名和密码" -ForegroundColor Yellow
Write-Host "用户名: 输入你的GitHub用户名" -ForegroundColor White
Write-Host "密码: 输入GitHub Personal Access Token (不是账户密码)" -ForegroundColor White
Write-Host "获取Token: https://github.com/settings/tokens" -ForegroundColor White

# 推送当前分支
Write-Host "`n推送当前分支: $currentBranch" -ForegroundColor Yellow
git push -u origin $currentBranch

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ 推送成功!" -ForegroundColor Green
    
    # 询问是否推送其他分支
    $otherBranches = $branches | Where-Object { $_ -ne $currentBranch }
    if ($otherBranches) {
        $pushAll = Read-Host "`n是否推送所有其他分支? (y/n)"
        if ($pushAll -eq "y" -or $pushAll -eq "Y") {
            Write-Host "`n推送所有分支..." -ForegroundColor Yellow
            git push --all origin
        }
    }
    
    # 询问是否推送标签
    $tags = git tag
    if ($tags) {
        $pushTags = Read-Host "`n是否推送所有标签? (y/n)"
        if ($pushTags -eq "y" -or $pushTags -eq "Y") {
            Write-Host "`n推送所有标签..." -ForegroundColor Yellow
            git push --tags origin
        }
    }
    
    Write-Host "`n=== 推送完成 ===" -ForegroundColor Green
    Write-Host "查看仓库: https://github.com/wilson323/IOE-DREAMfornew" -ForegroundColor Cyan
} else {
    Write-Host "`n✗ 推送失败" -ForegroundColor Red
    Write-Host "`n可能的原因和解决方案:" -ForegroundColor Yellow
    Write-Host "1. 认证失败:" -ForegroundColor White
    Write-Host "   - 使用Personal Access Token代替密码" -ForegroundColor Gray
    Write-Host "   - 获取Token: https://github.com/settings/tokens" -ForegroundColor Gray
    Write-Host "   - 需要 'repo' 权限" -ForegroundColor Gray
    Write-Host "`n2. 网络问题:" -ForegroundColor White
    Write-Host "   - 检查网络连接" -ForegroundColor Gray
    Write-Host "   - 尝试使用代理" -ForegroundColor Gray
    Write-Host "`n3. 权限问题:" -ForegroundColor White
    Write-Host "   - 确认有推送权限" -ForegroundColor Gray
    Write-Host "   - 确认仓库存在" -ForegroundColor Gray
    exit 1
}

Write-Host "`n=== 操作完成 ===" -ForegroundColor Cyan
