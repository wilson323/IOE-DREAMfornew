# 快速切换到SSH协议脚本
# 用途：将Git远程仓库从HTTPS切换到SSH

Write-Host "=== 切换到SSH协议 ===" -ForegroundColor Cyan

# SSH URL
$sshUrl = "git@github.com:wilson323/IOE-DREAMfornew.git"

# 检查当前配置
$currentUrl = git remote get-url origin 2>$null
Write-Host "`n当前远程仓库: $currentUrl" -ForegroundColor Cyan

# 如果已经是SSH，提示
if ($currentUrl -like "git@*") {
    Write-Host "`n✓ 已经使用SSH协议" -ForegroundColor Green
    exit 0
}

# 切换到SSH
Write-Host "`n切换到SSH协议..." -ForegroundColor Yellow
git remote set-url origin $sshUrl

# 验证
$newUrl = git remote get-url origin
Write-Host "`n✓ 已切换到SSH协议" -ForegroundColor Green
Write-Host "新URL: $newUrl" -ForegroundColor Cyan

# 测试SSH连接
Write-Host "`n测试SSH连接..." -ForegroundColor Yellow
$sshTest = ssh -T git@github.com 2>&1
if ($sshTest -like "*successfully authenticated*" -or $sshTest -like "*Hi*") {
    Write-Host "✓ SSH连接成功" -ForegroundColor Green
    Write-Host "`n可以开始推送代码:" -ForegroundColor Cyan
    Write-Host "  git push -u origin main" -ForegroundColor White
} else {
    Write-Host "✗ SSH连接失败" -ForegroundColor Red
    Write-Host "`n需要配置SSH密钥:" -ForegroundColor Yellow
    Write-Host "1. 生成密钥: ssh-keygen -t ed25519 -C `"your_email@example.com`"" -ForegroundColor White
    Write-Host "2. 查看公钥: cat $env:USERPROFILE\.ssh\id_ed25519.pub" -ForegroundColor White
    Write-Host "3. 添加到GitHub: https://github.com/settings/keys" -ForegroundColor White
}

Write-Host "`n=== 完成 ===" -ForegroundColor Cyan
