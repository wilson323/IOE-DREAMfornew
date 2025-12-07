# GitHub连接诊断脚本
# 用途：诊断GitHub连接问题并提供解决方案

Write-Host "=== GitHub连接诊断 ===" -ForegroundColor Cyan

# 1. 检查网络连接
Write-Host "`n1. 检查网络连接..." -ForegroundColor Yellow
$pingResult = Test-Connection -ComputerName github.com -Count 2 -ErrorAction SilentlyContinue
if ($pingResult) {
    Write-Host "✓ GitHub.com 网络连接正常" -ForegroundColor Green
    $pingResult | ForEach-Object { Write-Host "  延迟: $($_.ResponseTime)ms" -ForegroundColor Gray }
} else {
    Write-Host "✗ 无法连接到 GitHub.com" -ForegroundColor Red
    Write-Host "  可能原因: 网络问题、防火墙或DNS问题" -ForegroundColor Yellow
}

# 2. 检查DNS解析
Write-Host "`n2. 检查DNS解析..." -ForegroundColor Yellow
try {
    $dnsResult = Resolve-DnsName github.com -ErrorAction Stop
    Write-Host "✓ DNS解析成功" -ForegroundColor Green
    $dnsResult | Where-Object { $_.Type -eq 'A' } | ForEach-Object {
        Write-Host "  IP地址: $($_.IPAddress)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ DNS解析失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 测试HTTPS连接
Write-Host "`n3. 测试HTTPS连接..." -ForegroundColor Yellow
try {
    $webRequest = [System.Net.WebRequest]::Create("https://github.com")
    $webRequest.Timeout = 5000
    $response = $webRequest.GetResponse()
    Write-Host "✓ HTTPS连接成功" -ForegroundColor Green
    Write-Host "  状态码: $($response.StatusCode)" -ForegroundColor Gray
    $response.Close()
} catch {
    Write-Host "✗ HTTPS连接失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 检查Git配置
Write-Host "`n4. 检查Git配置..." -ForegroundColor Yellow
$gitConfig = git config --list | Select-String -Pattern "http|proxy|url"
if ($gitConfig) {
    Write-Host "当前Git配置:" -ForegroundColor Cyan
    $gitConfig | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
} else {
    Write-Host "未发现特殊Git配置" -ForegroundColor Gray
}

# 5. 检查远程仓库配置
Write-Host "`n5. 检查远程仓库配置..." -ForegroundColor Yellow
$remoteUrl = git remote get-url origin 2>$null
if ($remoteUrl) {
    Write-Host "当前远程仓库: $remoteUrl" -ForegroundColor Cyan
    if ($remoteUrl -like "https://*") {
        Write-Host "  使用HTTPS协议" -ForegroundColor Gray
    } elseif ($remoteUrl -like "git@*") {
        Write-Host "  使用SSH协议" -ForegroundColor Gray
    }
} else {
    Write-Host "未配置远程仓库" -ForegroundColor Yellow
}

# 6. 提供解决方案
Write-Host "`n=== 解决方案建议 ===" -ForegroundColor Cyan

Write-Host "`n方案1: 使用SSH代替HTTPS（推荐）" -ForegroundColor Yellow
Write-Host "1. 生成SSH密钥（如果还没有）:" -ForegroundColor White
Write-Host "   ssh-keygen -t ed25519 -C `"your_email@example.com`"" -ForegroundColor Gray
Write-Host "2. 将公钥添加到GitHub:" -ForegroundColor White
Write-Host "   cat ~/.ssh/id_ed25519.pub" -ForegroundColor Gray
Write-Host "3. 更改远程仓库为SSH:" -ForegroundColor White
Write-Host "   git remote set-url origin git@github.com:wilson323/IOE-DREAMfornew.git" -ForegroundColor Gray

Write-Host "`n方案2: 配置HTTP代理（如果需要）" -ForegroundColor Yellow
Write-Host "git config --global http.proxy http://proxy.example.com:8080" -ForegroundColor Gray
Write-Host "git config --global https.proxy https://proxy.example.com:8080" -ForegroundColor Gray

Write-Host "`n方案3: 检查防火墙和网络设置" -ForegroundColor Yellow
Write-Host "- 检查Windows防火墙设置" -ForegroundColor White
Write-Host "- 检查公司/学校网络代理设置" -ForegroundColor White
Write-Host "- 尝试使用移动热点测试" -ForegroundColor White

Write-Host "`n方案4: 使用GitHub CLI（gh）" -ForegroundColor Yellow
Write-Host "gh auth login" -ForegroundColor Gray
Write-Host "然后使用: gh repo sync" -ForegroundColor Gray

Write-Host "`n=== 诊断完成 ===" -ForegroundColor Cyan
