# ============================================================
# IOE-DREAM Jasypt加密工具
# 用于生成Jasypt AES256加密的配置值
# ============================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$Password,

    [string]$SecretKey = "IOE-DREAM-Jasypt-Secret-2024",

    [switch]$ShowHelp
)

# 显示帮助信息
if ($ShowHelp) {
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  IOE-DREAM Jasypt加密工具" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "用法:" -ForegroundColor White
    Write-Host "  .\scripts\jasypt-encrypt.ps1 -Password '明文密码'" -ForegroundColor Gray
    Write-Host "  .\scripts\jasypt-encrypt.ps1 -Password '明文密码' -SecretKey '自定义密钥'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "示例:" -ForegroundColor White
    Write-Host "  .\scripts\jasypt-encrypt.ps1 -Password '123456'" -ForegroundColor Gray
    Write-Host "  .\scripts\jasypt-encrypt.ps1 -Password 'redis123' -SecretKey 'MySecretKey'" -ForegroundColor Gray
    Write-Host ""
    exit 0
}

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  IOE-DREAM Jasypt加密工具" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# 检查Java是否安装
try {
    $javaVersion = & java -version 2>&1
    Write-Host "✓ Java版本: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ 错误: 未找到Java，请先安装Java" -ForegroundColor Red
    exit 1
}

# 检查Jasypt库是否存在
$jasyptPath = "lib/jasypt-1.9.3.jar"
if (-not (Test-Path $jasyptPath)) {
    Write-Host "下载Jasypt库..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "lib" -Force | Out-Null

    try {
        $webClient = New-Object System.Net.WebClient
        $downloadUrl = "https://repo1.maven.org/maven2/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar"
        Write-Host "  下载地址: $downloadUrl" -ForegroundColor Gray
        $webClient.DownloadFile($downloadUrl, $jasyptPath)
        Write-Host "✓ Jasypt库下载完成" -ForegroundColor Green
    } catch {
        Write-Host "❌ 错误: 下载Jasypt库失败: $_" -ForegroundColor Red
        exit 1
    }
}

# 加密密码
Write-Host "`n正在加密密码..." -ForegroundColor Yellow
Write-Host "  密钥: $SecretKey" -ForegroundColor Gray
Write-Host "  密码: $Password" -ForegroundColor Gray

try {
    # 使用Jasypt加密
    $encryptCommand = "java -cp `"$jasyptPath`" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=`"$Password` password=`"$SecretKey` algorithm=PBEWithMD5AndDES"
    $result = Invoke-Expression $encryptCommand

    if ($LASTEXITCODE -eq 0) {
        # 提取加密后的值
        $lines = $result -split "`n"
        foreach ($line in $lines) {
            if ($line.Trim() -match "----") {
                $encryptedValue = ($line -split "---- ")[1]).Trim()
                Write-Host "`n========================================" -ForegroundColor Green
                Write-Host "  加密结果:" -ForegroundColor Green
                Write-Host "========================================" -ForegroundColor Green
                Write-Host "  明文: $Password" -ForegroundColor White
                Write-Host "  加密: ENC($encryptedValue)" -ForegroundColor Cyan
                Write-Host ""
                Write-Host "请将以下配置添加到.env文件中:" -ForegroundColor Yellow
                Write-Host "MYSQL_PASSWORD=ENC($encryptedValue)" -ForegroundColor Gray
                Write-Host ""
                Write-Host "环境变量设置:" -ForegroundColor Yellow
                Write-Host "export MYSQL_ENCRYPTED_PASSWORD=`$encryptedValue`" -ForegroundColor Gray
                Write-Host ""
                Write-Host "✓ 加密完成!" -ForegroundColor Green
                break
            }
        }
    } else {
        Write-Host "❌ 加密失败，退出码: $LASTEXITCODE" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ 加密过程中发生错误: $_" -ForegroundColor Red
    exit 1
}