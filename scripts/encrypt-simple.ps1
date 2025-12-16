# 简化的Jasypt加密工具
param(
    [Parameter(Mandatory=$true)]
    [string]$Password,

    [string]$SecretKey = "IOE-DREAM-Jasypt-Secret-2024"
)

Write-Host "========================================" -ForegroundColor Yellow
Write-Host "  IOE-DREAM Jasypt加密工具" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Yellow

# 检查Java
try {
    $javaVersion = & java -version 2>&1
    Write-Host "✓ Java版本检查通过" -ForegroundColor Green
} catch {
    Write-Host "❌ 错误: 未找到Java，请先安装Java" -ForegroundColor Red
    exit 1
}

# 检查并下载Jasypt
$jasyptPath = "lib\jasypt-1.9.3.jar"
if (-not (Test-Path $jasyptPath)) {
    Write-Host "下载Jasypt库..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Path "lib" -Force | Out-Null

    try {
        $webClient = New-Object System.Net.WebClient
        $downloadUrl = "https://repo1.maven.org/maven2/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar"
        $webClient.DownloadFile($downloadUrl, $jasyptPath)
        Write-Host "✓ Jasypt库下载完成" -ForegroundColor Green
    } catch {
        Write-Host "❌ 错误: 下载Jasypt库失败: $_" -ForegroundColor Red
        exit 1
    }
}

# 执行加密
Write-Host "`n正在加密密码..." -ForegroundColor Yellow
Write-Host "  密钥: $SecretKey" -ForegroundColor Gray
Write-Host "  密码: $Password" -ForegroundColor Gray

try {
    $encryptCommand = "java -cp `"$jasyptPath`" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=`"$Password` password=`"$SecretKey` algorithm=PBEWithMD5AndDES"
    $result = Invoke-Expression $encryptCommand

    if ($LASTEXITCODE -eq 0) {
        # 查找加密结果
        if ($result -match "----ENVIRONMENT\.PROPERTY\.\-+----\s*(ENC\([^)]+\))") {
            $encryptedValue = $matches[1]

            Write-Host "`n========================================" -ForegroundColor Green
            Write-Host "  加密结果:" -ForegroundColor Green
            Write-Host "========================================" -ForegroundColor Green
            Write-Host "  明文: $Password" -ForegroundColor White
            Write-Host "  加密: $encryptedValue" -ForegroundColor Cyan
            Write-Host ""
            Write-Host "请将以下配置添加到.env文件中:" -ForegroundColor Yellow
            Write-Host "PASSWORD_CONFIG=$encryptedValue" -ForegroundColor Gray
            Write-Host ""
            Write-Host "✓ 加密完成!" -ForegroundColor Green

            # 输出纯加密值供脚本使用
            Write-Output $encryptedValue
        } else {
            Write-Host "❌ 无法解析加密结果" -ForegroundColor Red
            Write-Host "原始输出: $result" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ 加密失败，退出码: $LASTEXITCODE" -ForegroundColor Red
        Write-Host "输出: $result" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 加密过程中发生错误: $_" -ForegroundColor Red
}