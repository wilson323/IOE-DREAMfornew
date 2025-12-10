# 推送代码到GitHub仓库脚本
# 用途：将本地代码推送到GitHub远程仓库

Write-Host "=== 推送代码到GitHub ===" -ForegroundColor Cyan

# 切换到项目目录
Set-Location $PSScriptRoot\..
$projectRoot = Get-Location

Write-Host "项目目录: $projectRoot" -ForegroundColor Green

# 检查Git仓库
if (-not (Test-Path ".git")) {
    Write-Host "错误: 当前目录不是Git仓库" -ForegroundColor Red
    exit 1
}

# 获取当前分支
$currentBranch = git branch --show-current
Write-Host "`n当前分支: $currentBranch" -ForegroundColor Cyan

# 检查远程仓库
Write-Host "`n检查远程仓库配置..." -ForegroundColor Yellow
$remoteUrl = git remote get-url origin
Write-Host "远程仓库: $remoteUrl" -ForegroundColor Green

# 检查是否有未提交的更改
Write-Host "`n检查工作区状态..." -ForegroundColor Yellow
$status = git status --porcelain
if ($status) {
    Write-Host "警告: 存在未提交的更改" -ForegroundColor Yellow
    Write-Host $status
    Write-Host "`n建议先提交更改，然后再推送" -ForegroundColor Yellow
    $continue = Read-Host "是否继续推送? (y/n)"
    if ($continue -ne "y" -and $continue -ne "Y") {
        Write-Host "操作已取消" -ForegroundColor Yellow
        exit 0
    }
} else {
    Write-Host "工作区干净" -ForegroundColor Green
}

# 检查是否有提交
$commitCount = (git rev-list --count HEAD 2>$null)
if ($commitCount -eq 0) {
    Write-Host "`n警告: 仓库中没有提交记录" -ForegroundColor Yellow
    Write-Host "请先进行初始提交" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n提交记录数: $commitCount" -ForegroundColor Green

# 推送代码
Write-Host "`n开始推送代码到GitHub..." -ForegroundColor Yellow
Write-Host "目标仓库: $remoteUrl" -ForegroundColor Cyan
Write-Host "目标分支: $currentBranch" -ForegroundColor Cyan

# 首次推送使用 -u 参数设置上游分支
Write-Host "`n执行: git push -u origin $currentBranch" -ForegroundColor Yellow
git push -u origin $currentBranch

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n=== 推送成功 ===" -ForegroundColor Green
    Write-Host "代码已成功推送到: $remoteUrl" -ForegroundColor Green
} else {
    Write-Host "`n=== 推送失败 ===" -ForegroundColor Red
    Write-Host "错误代码: $LASTEXITCODE" -ForegroundColor Red
    Write-Host "`n可能的原因:" -ForegroundColor Yellow
    Write-Host "1. 远程仓库不存在或没有访问权限" -ForegroundColor White
    Write-Host "2. 需要配置GitHub认证（SSH密钥或Personal Access Token）" -ForegroundColor White
    Write-Host "3. 网络连接问题" -ForegroundColor White
    Write-Host "`n建议检查:" -ForegroundColor Yellow
    Write-Host "- 确认GitHub仓库已创建" -ForegroundColor White
    Write-Host "- 确认有推送权限" -ForegroundColor White
    Write-Host "- 检查Git认证配置" -ForegroundColor White
    exit 1
}

# 显示推送后的状态
Write-Host "`n推送后的远程分支信息:" -ForegroundColor Yellow
git branch -r | Select-String "origin/$currentBranch"

Write-Host "`n=== 操作完成 ===" -ForegroundColor Cyan
