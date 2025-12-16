# IOE-DREAM 加密配置验证脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  IOE-DREAM 加密配置验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Write-Host "`n正在验证 .env 文件中的加密配置..." -ForegroundColor Yellow

# 检查 .env 文件
$envFile = ".env"
if (-not (Test-Path $envFile)) {
    Write-Host "❌ 错误: .env 文件不存在" -ForegroundColor Red
    exit 1
}

$envContent = Get-Content $envFile

# 检查加密配置项
$encryptedConfigs = @{
    "MYSQL_PASSWORD" = "MySQL数据库密码"
    "MYSQL_ROOT_PASSWORD" = "MySQL root密码"
    "REDIS_PASSWORD" = "Redis缓存密码"
    "NACOS_PASSWORD" = "Nacos配置中心密码"
    "NACOS_AUTH_TOKEN" = "Nacos认证令牌"
    "RABBITMQ_PASSWORD" = "RabbitMQ消息队列密码"
    "JASYPT_PASSWORD" = "Jasypt加密密钥"
    "JWT_SECRET" = "JWT令牌密钥"
    "MFA_TOTP_SECRET" = "MFA TOTP密钥"
}

Write-Host "`n📋 加密配置项检查结果:" -ForegroundColor Cyan

$totalConfigs = $encryptedConfigs.Count
$checkedConfigs = 0
$passedConfigs = 0

foreach ($config in $encryptedConfigs.GetEnumerator()) {
    $configName = $config.Key
    $configDesc = $config.Value

    $checkedConfigs++

    # 查找配置项
    $configLine = $envContent | Where-Object { $_ -match "^$configName=" }

    if ($configLine) {
        if ($configLine -match "ENC\(AES256:.*\)") {
            Write-Host "  ✅ $configName - $configDesc" -ForegroundColor Green
            $passedConfigs++
        } else {
            Write-Host "  ❌ $configName - $configDesc (未加密)" -ForegroundColor Red
        }
    } else {
        Write-Host "  ⚠️  $configName - $configDesc (未找到)" -ForegroundColor Yellow
    }
}

Write-Host "`n📊 统计结果:" -ForegroundColor Cyan
Write-Host "  总配置项: $totalConfigs" -ForegroundColor White
Write-Host "  已检查: $checkedConfigs" -ForegroundColor White
Write-Host "  已加密: $passedConfigs" -ForegroundColor Green
Write-Host "  加密率: $([math]::Round($passedConfigs/$totalConfigs*100, 1))%" -ForegroundColor $(if($passedConfigs/$totalConfigs*100 -ge 80) {"Green"} elseif($passedConfigs/$totalConfigs*100 -ge 60) {"Yellow"} else {"Red"})

# 检查明文密码
Write-Host "`n🔍 明文密码检查:" -ForegroundColor Cyan

$plaintextPatterns = @(
    "PASSWORD=123456",
    "PASSWORD=password",
    "PASSWORD=admin",
    "PASSWORD=root",
    "PASSWORD=guest",
    "PASSWORD=redis123",
    "PASSWORD=nacos"
)

$foundPlaintext = $false

foreach ($pattern in $plaintextPatterns) {
    $matches = $envContent | Where-Object { $_ -match $pattern }
    if ($matches) {
        Write-Host "  ❌ 发现明文密码: $pattern" -ForegroundColor Red
        $foundPlaintext = $true
    }
}

if (-not $foundPlaintext) {
    Write-Host "  ✅ 未发现常见明文密码" -ForegroundColor Green
}

# 检查 Jasypt 配置
Write-Host "`n🔐 Jasypt 配置检查:" -ForegroundColor Cyan

$jasyptPassword = $envContent | Where-Object { $_ -match "^JASYPT_PASSWORD=" }
$jasyptAlgorithm = $envContent | Where-Object { $_ -match "^JASYPT_ALGORITHM=" }

if ($jasyptPassword -and $jasyptAlgorithm) {
    Write-Host "  ✅ Jasypt 密钥配置: 已配置" -ForegroundColor Green
    Write-Host "  ✅ Jasypt 算法配置: $jasyptAlgorithm.Split('=')[1]" -ForegroundColor Green
} else {
    Write-Host "  ❌ Jasypt 配置不完整" -ForegroundColor Red
}

# 安全建议
Write-Host "`n💡 安全建议:" -ForegroundColor Yellow

if ($passedConfigs/$totalConfigs*100 -lt 100) {
    Write-Host "  • 还有 $([math]::Round($totalConfigs-$passedConfigs)) 个配置项需要加密" -ForegroundColor Yellow
}

if ($foundPlaintext) {
    Write-Host "  • 立即修复所有明文密码配置" -ForegroundColor Red
}

Write-Host "  • 生产环境请使用强密码和随机密钥" -ForegroundColor Yellow
Write-Host "  • 定期轮换加密密钥，建议每季度一次" -ForegroundColor Yellow
Write-Host "  • 使用专业的密钥管理服务" -ForegroundColor Yellow

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  验证完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($passedConfigs/$totalConfigs*100 -ge 90 -and -not $foundPlaintext) {
    Write-Host "`n🎉 恭喜！配置安全性良好" -ForegroundColor Green
} elseif ($passedConfigs/$totalConfigs*100 -ge 60) {
    Write-Host "`n⚠️  配置安全性一般，建议继续改进" -ForegroundColor Yellow
} else {
    Write-Host "`n🚨 配置存在安全风险，请立即修复" -ForegroundColor Red
}