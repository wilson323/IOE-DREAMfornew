# Git状态验证脚本
# 功能：验证Git提交和合并操作是否成功

Set-Location $PSScriptRoot\..

Write-Host "=== Git状态验证 ===" -ForegroundColor Green

# 1. 检查当前分支
Write-Host "`n1. 当前分支:" -ForegroundColor Yellow
$currentBranch = git branch --show-current
Write-Host "   $currentBranch" -ForegroundColor Cyan

# 2. 检查未提交的更改
Write-Host "`n2. 未提交的更改:" -ForegroundColor Yellow
$status = git status --porcelain
if ($status) {
    Write-Host "   存在未提交的更改:" -ForegroundColor Red
    Write-Host $status
} else {
    Write-Host "   工作区干净，无未提交更改" -ForegroundColor Green
}

# 3. 检查最近的提交
Write-Host "`n3. 最近的5个提交:" -ForegroundColor Yellow
git log --oneline -5

# 4. 检查远程仓库状态
Write-Host "`n4. 远程仓库状态:" -ForegroundColor Yellow
$remoteStatus = git status -sb
Write-Host $remoteStatus

# 5. 检查远程分支
Write-Host "`n5. 远程分支:" -ForegroundColor Yellow
git branch -r

# 6. 检查主分支
Write-Host "`n6. 主分支检查:" -ForegroundColor Yellow
$mainExists = git branch -a | Select-String -Pattern "remotes/origin/main"
$masterExists = git branch -a | Select-String -Pattern "remotes/origin/master"

if ($mainExists) {
    Write-Host "   主分支: main" -ForegroundColor Cyan
    $mainAhead = git rev-list --left-right --count origin/main...HEAD
    Write-Host "   本地与远程差异: $mainAhead" -ForegroundColor Cyan
} elseif ($masterExists) {
    Write-Host "   主分支: master" -ForegroundColor Cyan
    $masterAhead = git rev-list --left-right --count origin/master...HEAD
    Write-Host "   本地与远程差异: $masterAhead" -ForegroundColor Cyan
} else {
    Write-Host "   未找到主分支" -ForegroundColor Red
}

Write-Host "`n=== 验证完成 ===" -ForegroundColor Green
