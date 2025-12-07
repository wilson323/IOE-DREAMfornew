# GitHub连接修复脚本
# 用途：修复GitHub连接问题，提供多种解决方案

Write-Host "=== GitHub连接修复工具 ===" -ForegroundColor Cyan

$projectRoot = Get-Location
Set-Location $projectRoot

# 当前远程仓库URL
$currentUrl = git remote get-url origin 2>$null
Write-Host "`n当前远程仓库: $currentUrl" -ForegroundColor Cyan

# 方案1: 切换到SSH（推荐）
Write-Host "`n=== 方案1: 切换到SSH协议（推荐） ===" -ForegroundColor Yellow

$sshUrl = "git@github.com:wilson323/IOE-DREAMfornew.git"
Write-Host "SSH URL: $sshUrl" -ForegroundColor Gray

# 检查SSH密钥
Write-Host "`n检查SSH密钥..." -ForegroundColor Yellow
$sshKeyPath = "$env:USERPROFILE\.ssh\id_ed25519"
$sshKeyPathRsa = "$env:USERPROFILE\.ssh\id_rsa"

if (Test-Path $sshKeyPath) {
    Write-Host "✓ 找到SSH密钥: $sshKeyPath" -ForegroundColor Green
    $useSsh = Read-Host "是否切换到SSH? (y/n)"
    if ($useSsh -eq "y" -or $useSsh -eq "Y") {
        git remote set-url origin $sshUrl
        Write-Host "✓ 已切换到SSH协议" -ForegroundColor Green
        Write-Host "  新URL: $sshUrl" -ForegroundColor Gray
        
        # 测试SSH连接
        Write-Host "`n测试SSH连接..." -ForegroundColor Yellow
        $sshTest = ssh -T git@github.com 2>&1
        if ($sshTest -like "*successfully authenticated*" -or $sshTest -like "*Hi*") {
            Write-Host "✓ SSH连接成功" -ForegroundColor Green
            Write-Host "  可以尝试推送: git push -u origin main" -ForegroundColor Cyan
        } else {
            Write-Host "✗ SSH连接失败" -ForegroundColor Red
            Write-Host "  需要配置SSH密钥到GitHub" -ForegroundColor Yellow
            Write-Host "`n配置步骤:" -ForegroundColor Yellow
            Write-Host "1. 查看公钥: cat $sshKeyPath.pub" -ForegroundColor White
            Write-Host "2. 复制公钥内容" -ForegroundColor White
            Write-Host "3. 访问: https://github.com/settings/keys" -ForegroundColor White
            Write-Host "4. 点击 'New SSH key' 添加公钥" -ForegroundColor White
        }
    }
} elseif (Test-Path $sshKeyPathRsa) {
    Write-Host "✓ 找到SSH密钥: $sshKeyPathRsa" -ForegroundColor Green
    $useSsh = Read-Host "是否切换到SSH? (y/n)"
    if ($useSsh -eq "y" -or $useSsh -eq "Y") {
        git remote set-url origin $sshUrl
        Write-Host "✓ 已切换到SSH协议" -ForegroundColor Green
    }
} else {
    Write-Host "✗ 未找到SSH密钥" -ForegroundColor Yellow
    Write-Host "`n生成SSH密钥步骤:" -ForegroundColor Yellow
    Write-Host "1. 运行: ssh-keygen -t ed25519 -C `"your_email@example.com`"" -ForegroundColor White
    Write-Host "2. 按提示操作（可直接回车使用默认路径）" -ForegroundColor White
    Write-Host "3. 查看公钥: cat $env:USERPROFILE\.ssh\id_ed25519.pub" -ForegroundColor White
    Write-Host "4. 将公钥添加到GitHub: https://github.com/settings/keys" -ForegroundColor White
}

# 方案2: 配置代理（如果需要）
Write-Host "`n=== 方案2: 配置HTTP代理 ===" -ForegroundColor Yellow
$useProxy = Read-Host "是否需要配置代理? (y/n)"
if ($useProxy -eq "y" -or $useProxy -eq "Y") {
    $proxyHost = Read-Host "请输入代理地址 (例如: proxy.example.com:8080)"
    if ($proxyHost) {
        git config --global http.proxy "http://$proxyHost"
        git config --global https.proxy "https://$proxyHost"
        Write-Host "✓ 代理已配置" -ForegroundColor Green
        Write-Host "  可以尝试推送: git push -u origin main" -ForegroundColor Cyan
    }
}

# 方案3: 清除代理配置
Write-Host "`n=== 方案3: 清除代理配置 ===" -ForegroundColor Yellow
$clearProxy = Read-Host "是否需要清除代理配置? (y/n)"
if ($clearProxy -eq "y" -or $clearProxy -eq "Y") {
    git config --global --unset http.proxy
    git config --global --unset https.proxy
    Write-Host "✓ 代理配置已清除" -ForegroundColor Green
}

# 方案4: 增加超时时间
Write-Host "`n=== 方案4: 增加Git超时时间 ===" -ForegroundColor Yellow
git config --global http.postBuffer 524288000
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 999999
Write-Host "✓ Git超时配置已优化" -ForegroundColor Green

# 显示最终配置
Write-Host "`n=== 当前配置 ===" -ForegroundColor Cyan
$finalUrl = git remote get-url origin
Write-Host "远程仓库: $finalUrl" -ForegroundColor Green

Write-Host "`n=== 下一步操作 ===" -ForegroundColor Cyan
Write-Host "1. 如果使用SSH，确保SSH密钥已添加到GitHub" -ForegroundColor White
Write-Host "2. 尝试推送: git push -u origin main" -ForegroundColor White
Write-Host "3. 如果仍有问题，检查网络连接和防火墙设置" -ForegroundColor White

Write-Host "`n=== 修复完成 ===" -ForegroundColor Cyan
