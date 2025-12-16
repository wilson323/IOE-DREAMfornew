# ============================================================
# IOE-DREAM 加密环境配置设置脚本
# 自动生成所有需要的加密配置值
# ============================================================

param(
    [switch]$SkipDownload,
    [switch]$DryRun,
    [switch]$Force
)

Write-Host "========================================" -ForegroundColor Red
Write-Host "  IOE-DREAM 加密环境配置设置" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Red

# 检查Jasypt加密工具
$jasyptTool = "scripts\jasypt-encrypt.ps1"
if (-not (Test-Path $jasyptTool)) {
    Write-Host "❌ 错误: Jasypt加密工具不存在: $jasyptTool" -ForegroundColor Red
    Write-Host "请先运行: .\scripts\jasypt-encrypt.ps1 -ShowHelp" -ForegroundColor Yellow
    exit 1
}

# 默认密码和密钥
$defaultPasswords = @{
    "MYSQL_PASSWORD" = "123456"
    "MYSQL_ROOT_PASSWORD" = "123456"
    "REDIS_PASSWORD" = "redis123"
    "NACOS_PASSWORD" = "nacos"
    "RABBITMQ_PASSWORD" = "guest"
    "JASYPT_PASSWORD" = "IOE-DREAM-Jasypt-Secret-2024"
    "JWT_SECRET" = "IOE-DREAM-JWT-Secret-Key-2024-Security"
    "MFA_TOTP_SECRET" = "IOE-DREAM-MFA-TOTP-Secret-2024"
}

$secretKeys = @{
    "MYSQL" = "IOE-DREAM-MySQL-Secret-2024"
    "REDIS" = "IOE-DREAM-Redis-Secret-2024"
    "NACOS" = "IOE-DREAM-Nacos-Secret-2024"
    "RABBITMQ" = "IOE-DREAM-RabbitMQ-Secret-2024"
    "JASYPT" = "IOE-DREAM-Jasypt-Secret-2024"
    "JWT" = "IOE-DREAM-JWT-Secret-2024"
    "MFA_TOTP" = "IOE-DREAM-MFA-TOTP-Secret-2024"
}

Write-Host "`n开始生成加密配置值..." -ForegroundColor Yellow

$encryptedConfigs = @{}

# 为每个密码生成加密值
foreach ($key in $defaultPasswords.Keys) {
    $password = $defaultPasswords[$key]
    $secretKey = $secretKeys[$key.Split('_')[0]]

    Write-Host "`n生成 $key 的加密值..." -ForegroundColor Cyan

    try {
        if ($DryRun) {
            Write-Host "  [预览] 将加密: $password (密钥: $secretKey)" -ForegroundColor Yellow
            $encryptedValue = "ENC(AES256:模拟加密值-$key)"
        } else {
            # 实际加密
            $result = & powershell -ExecutionPolicy Bypass -File $jasyptTool -Password $password -SecretKey $secretKey
            if ($LASTEXITCODE -eq 0) {
                # 提取加密值
                $lines = $result -split "`n"
                foreach ($line in $lines) {
                    if ($line -match "ENC\(AES256:(.+)\)") {
                        $encryptedValue = $matches[1]
                        break
                    }
                }
            } else {
                Write-Host "  ❌ 加密失败: $key" -ForegroundColor Red
                continue
            }
        }

        $encryptedConfigs[$key] = $encryptedValue
        Write-Host "  ✓ 加密完成: $key" -ForegroundColor Green

    } catch {
        Write-Host "  ❌ 加密失败: $key - $_" -ForegroundColor Red
    }
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  加密配置值生成完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 输出加密配置
Write-Host "`n加密后的配置值:" -ForegroundColor Cyan
foreach ($key in $encryptedConfigs.Keys) {
    Write-Host "  $key = $($encryptedConfigs[$key])" -ForegroundColor White
}

# 生成.env.encrypted模板文件
Write-Host "`n生成加密配置模板..." -ForegroundColor Yellow

$encryptedEnvTemplate = @"
# ============================================================
# IOE-DREAM 加密环境配置模板
# 请将以下值替换为实际的加密配置
# 生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
# ============================================================

# 数据库密码配置
MYSQL_PASSWORD=$($encryptedConfigs['MYSQL_PASSWORD'])
MYSQL_ROOT_PASSWORD=$($encryptedConfigs['MYSQL_ROOT_PASSWORD'])

# Redis缓存密码配置
REDIS_PASSWORD=$($encryptedConfigs['REDIS_PASSWORD'])

# Nacos配置中心密码
NACOS_PASSWORD=$($encryptedConfigs['NACOS_PASSWORD'])
NACOS_AUTH_TOKEN=ENC(AES256:这里放置加密后的Nacos认证令牌)

# RabbitMQ消息队列密码
RABBITMQ_PASSWORD=$($encryptedConfigs['RABBITMQ_PASSWORD'])

# Jasypt加密密钥
JASYPT_PASSWORD=$($encryptedConfigs['JASYPT_PASSWORD'])
JASYPT_ALGORITHM=PBEWithMD5AndDES

# JWT密钥
JWT_SECRET=$($encryptedConfigs['JWT_SECRET'])

# MFA TOTP密钥
MFA_TOTP_SECRET=$($encryptedConfigs['MFA_TOTP_SECRET'])

# ==================== 环境变量设置指南 ====================
# Windows PowerShell (临时设置)
`$env:MYSQL_ENCRYPTED_PASSWORD = "$($encryptedConfigs['MYSQL_PASSWORD'].Replace('ENC(AES256:', '').Replace(')', ''))"
`$env:REDIS_ENCRYPTED_PASSWORD = "$($encryptedConfigs['REDIS_PASSWORD'].Replace('ENC(AES256:', '').Replace(')', ''))"
`$env:NACOS_ENCRYPTED_PASSWORD = "$($encryptedConfigs['NACOS_PASSWORD'].Replace('ENC(AES256:', '').Replace(')', ''))"

# Linux/macOS (临时设置)
export MYSQL_ENCRYPTED_PASSWORD="$($encryptedConfigs['MYSQL_PASSWORD'].Replace('ENC(AES256:', '').Replace(')', ''))"
export REDIS_ENCRYPTED_PASSWORD="$($encryptedConfigs['REDIS_PASSWORD'].Replace('ENC(AES256:', '').Replace(')', ''))"
export NACOS_ENCRYPTED_PASSWORD="$($encryptedConfigs['NACOS_PASSWORD'].Replace('ENC(AES256:', '').Replace(')', ''))"

# ==================== Docker Compose环境变量 ====================
# 在docker-compose.yml中使用:
environment:
  - MYSQL_PASSWORD=`$($encryptedConfigs['MYSQL_PASSWORD'])`
  - REDIS_PASSWORD=`$($encryptedConfigs['REDIS_PASSWORD'])`
  - NACOS_PASSWORD=`$($encryptedConfigs['NACOS_PASSWORD'])`
"@

if (-not $DryRun) {
    Set-Content -Path ".env.encrypted" -Value $encryptedEnvTemplate -Encoding UTF8
    Write-Host "  ✓ 创建模板: .env.encrypted" -ForegroundColor Green
} else {
    Write-Host "  ✓ 预览模式: 将创建 .env.encrypted 模板" -ForegroundColor Yellow
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  设置完成!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

Write-Host "下一步操作:" -ForegroundColor Cyan
Write-Host "1. 复制上面的环境变量到系统环境" -ForegroundColor White
Write-Host "2. 或者使用 .env.encrypted 模板更新配置" -ForegroundColor White
Write-Host "3. 重启相关服务以应用新配置" -ForegroundColor White

if (-not $DryRun) {
    Write-Host ""
    Write-Host "验证配置:" -ForegroundColor Yellow
    Write-Host "powershell -ExecutionPolicy Bypass -File `".\scripts\security-check.ps1`" -ForegroundColor Gray
}