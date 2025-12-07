# 更新GitHub远程仓库地址脚本
# 用途：将Git远程仓库更新到新的GitHub地址

Write-Host "=== 更新GitHub远程仓库 ===" -ForegroundColor Cyan

# 切换到项目目录
Set-Location $PSScriptRoot\..
$projectRoot = Get-Location

Write-Host "项目目录: $projectRoot" -ForegroundColor Green

# 检查Git仓库
if (-not (Test-Path ".git")) {
    Write-Host "错误: 当前目录不是Git仓库" -ForegroundColor Red
    exit 1
}

# 新的远程仓库地址
$newRemoteUrl = "https://github.com/wilson323/IOE-DREAMfornew.git"

Write-Host "`n1. 检查当前远程仓库配置..." -ForegroundColor Yellow
$currentRemotes = git remote -v
if ($currentRemotes) {
    Write-Host $currentRemotes
} else {
    Write-Host "当前没有配置远程仓库" -ForegroundColor Yellow
}

# 更新或添加远程仓库
Write-Host "`n2. 更新远程仓库地址..." -ForegroundColor Yellow
$existingOrigin = git remote get-url origin 2>$null
if ($LASTEXITCODE -eq 0) {
    Write-Host "当前origin: $existingOrigin" -ForegroundColor Cyan
    git remote set-url origin $newRemoteUrl
    Write-Host "已更新origin到: $newRemoteUrl" -ForegroundColor Green
} else {
    git remote add origin $newRemoteUrl
    Write-Host "已添加origin: $newRemoteUrl" -ForegroundColor Green
}

# 验证配置
Write-Host "`n3. 验证远程仓库配置..." -ForegroundColor Yellow
$verifiedUrl = git remote get-url origin
Write-Host "当前origin地址: $verifiedUrl" -ForegroundColor Green

# 检查当前分支
Write-Host "`n4. 检查当前分支..." -ForegroundColor Yellow
$currentBranch = git branch --show-current
Write-Host "当前分支: $currentBranch" -ForegroundColor Cyan

# 检查是否有未提交的更改
Write-Host "`n5. 检查工作区状态..." -ForegroundColor Yellow
$status = git status --short
if ($status) {
    Write-Host "警告: 存在未提交的更改" -ForegroundColor Yellow
    Write-Host $status
} else {
    Write-Host "工作区干净，无未提交更改" -ForegroundColor Green
}

Write-Host "`n=== 远程仓库更新完成 ===" -ForegroundColor Cyan
Write-Host "`n下一步操作建议:" -ForegroundColor Yellow
Write-Host "1. 如果这是新仓库，需要先推送: git push -u origin $currentBranch" -ForegroundColor White
Write-Host "2. 如果仓库已存在，可以推送: git push origin $currentBranch" -ForegroundColor White
Write-Host "3. 推送所有分支: git push --all origin" -ForegroundColor White
Write-Host "4. 推送所有标签: git push --tags origin" -ForegroundColor White
