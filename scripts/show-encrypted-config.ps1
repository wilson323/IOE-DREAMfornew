# IOE-DREAM 加密配置示例
Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  生成加密配置示例" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# 示例加密值（实际使用时需要用Jasypt工具生成）
$encryptedConfigs = @{
    "MYSQL_PASSWORD" = "ENC(AES256:v2xK8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)"
    "REDIS_PASSWORD" = "ENC(AES256:d4F7jK8mN2pQr6sT3wJ5yZ1aF4bG9cH)"
    "NACOS_PASSWORD" = "ENC(AES256:u7K8L9mN5pQr8sT3wJ6yZ2aF4bG8cH)"
    "JWT_SECRET" = "ENC(AES256:x9K8L9mN5pQr7sT3wJ6yZ1aF4bG8cH)"
    "MFA_TOTP_SECRET" = "ENC(AES256:y7K8L9mN5pQr9sT3wJ6yZ1aF4bG8cH)"
}

Write-Host "`n示例加密配置:" -ForegroundColor Cyan
foreach ($key in $encryptedConfigs.Keys) {
    Write-Host "  $key = $($encryptedConfigs[$key])" -ForegroundColor White
}

Write-Host "`n环境变量设置示例:" -ForegroundColor Yellow
Write-Host "# Windows PowerShell" -ForegroundColor Gray
foreach ($key in $encryptedConfigs.Keys) {
    $envVar = $key + "_ENCRYPTED"
    $value = $encryptedConfigs[$key].Replace("ENC(AES256:", "").Replace(")", "")
    Write-Host "`$envVar = `"$value`"" -ForegroundColor White
}

Write-Host "`n# 生成实际的加密值:" -ForegroundColor Yellow
Write-Host "powershell -ExecutionPolicy Bypass -File `"scripts/jasypt-encrypt.ps1`" -Password `123456`" -ForegroundColor Gray

