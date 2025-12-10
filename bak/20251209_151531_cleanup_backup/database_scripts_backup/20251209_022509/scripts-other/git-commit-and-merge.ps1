# Git提交并合并到主分支脚本
# 功能：提交所有更改并合并到主分支

param(
    [string]$CommitMessage = "feat: 优化启动脚本和测试脚本"
)

Set-Location $PSScriptRoot\..

Write-Host "=== Git提交并合并到主分支 ===" -ForegroundColor Green

# 1. 检查Git仓库状态
Write-Host "`n1. 检查Git状态..." -ForegroundColor Yellow
$status = git status --porcelain
if ($status) {
    Write-Host "发现未提交的更改:" -ForegroundColor Cyan
    Write-Host $status
} else {
    Write-Host "没有未提交的更改" -ForegroundColor Green
    exit 0
}

# 2. 添加所有更改
Write-Host "`n2. 添加所有更改到暂存区..." -ForegroundColor Yellow
git add -A
if ($LASTEXITCODE -ne 0) {
    Write-Host "添加文件失败" -ForegroundColor Red
    exit 1
}

# 3. 提交更改
Write-Host "`n3. 提交更改..." -ForegroundColor Yellow
git commit -m $CommitMessage
if ($LASTEXITCODE -ne 0) {
    Write-Host "提交失败" -ForegroundColor Red
    exit 1
}
Write-Host "提交成功" -ForegroundColor Green

# 4. 获取当前分支名
$currentBranch = git branch --show-current
Write-Host "`n当前分支: $currentBranch" -ForegroundColor Cyan

# 5. 推送到远程
Write-Host "`n4. 推送到远程仓库..." -ForegroundColor Yellow
git push origin $currentBranch
if ($LASTEXITCODE -ne 0) {
    Write-Host "推送失败，请检查远程仓库配置" -ForegroundColor Red
    exit 1
}
Write-Host "推送成功" -ForegroundColor Green

# 6. 检查主分支名称（main或master）
Write-Host "`n5. 检查主分支..." -ForegroundColor Yellow
$mainBranch = $null
if (git branch -a | Select-String -Pattern "remotes/origin/main") {
    $mainBranch = "main"
} elseif (git branch -a | Select-String -Pattern "remotes/origin/master") {
    $mainBranch = "master"
} else {
    Write-Host "未找到主分支（main或master）" -ForegroundColor Red
    exit 1
}

Write-Host "主分支: $mainBranch" -ForegroundColor Cyan

# 7. 如果当前不在主分支，则切换到主分支并合并
if ($currentBranch -ne $mainBranch) {
    Write-Host "`n6. 切换到主分支并合并..." -ForegroundColor Yellow
    git checkout $mainBranch
    if ($LASTEXITCODE -ne 0) {
        Write-Host "切换到主分支失败" -ForegroundColor Red
        exit 1
    }
    
    git merge $currentBranch -m "merge: 合并 $currentBranch 到 $mainBranch"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "合并失败，可能存在冲突" -ForegroundColor Red
        exit 1
    }
    
    # 推送主分支
    git push origin $mainBranch
    if ($LASTEXITCODE -ne 0) {
        Write-Host "推送主分支失败" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "合并成功并已推送到远程" -ForegroundColor Green
} else {
    Write-Host "`n当前已在主分支，无需合并" -ForegroundColor Green
}

Write-Host "`n=== 完成 ===" -ForegroundColor Green
