# ============================================================
# IOE-DREAM Nacos密码加密工具
# 使用AES256加密密码，生成Nacos兼容的加密格式
# 重要：本工具仅生成加密值，不修改任何配置文件
# ============================================================

param(
    [Parameter(Mandatory = $true)]
    [string]$Password,

    [string]$SecretKey = "IOE-DREAM-Nacos-Encryption-Key-2025",

    [switch]$ShowHelp
)

$ErrorActionPreference = "Stop"

if ($ShowHelp) {
    Write-Host @"
============================================================
IOE-DREAM Nacos密码加密工具
============================================================

用法:
  .\nacos-encrypt-password.ps1 -Password "明文密码"
  .\nacos-encrypt-password.ps1 -Password "明文密码" -SecretKey "自定义密钥"

参数:
  -Password      必需。要加密的明文密码
  -SecretKey     可选。加密密钥（默认: IOE-DREAM-Nacos-Encryption-Key-2025）
  -ShowHelp      显示此帮助信息

示例:
  # 加密MySQL密码
  .\nacos-encrypt-password.ps1 -Password "123456"

  # 加密Redis密码（使用自定义密钥）
  .\nacos-encrypt-password.ps1 -Password "redis123" -SecretKey "MySecretKey"

输出格式:
  ENC(AES256:加密后的值)

注意:
  - 加密后的值需要配置到Nacos配置中心
  - 确保SecretKey在生产环境中安全存储
  - 不要将SecretKey提交到代码仓库

============================================================
"@
    exit 0
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Nacos密码加密工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 检查Java环境
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "`nJava环境: $javaVersion" -ForegroundColor Green
}
catch {
    Write-Host "`n错误: 未找到Java环境，请先安装Java 17+" -ForegroundColor Red
    exit 1
}

# 检查Jasypt库
$jasyptPath = "$PSScriptRoot\..\lib\jasypt-1.9.3.jar"
if (-not (Test-Path $jasyptPath)) {
    Write-Host "`n下载Jasypt加密库..." -ForegroundColor Yellow

    $libDir = Split-Path $jasyptPath -Parent
    if (-not (Test-Path $libDir)) {
        New-Item -ItemType Directory -Path $libDir -Force | Out-Null
    }

    try {
        $jasyptUrl = "https://repo1.maven.org/maven2/org/jasypt/jasypt/1.9.3/jasypt-1.9.3.jar"
        Invoke-WebRequest -Uri $jasyptUrl -OutFile $jasyptPath -UseBasicParsing
        Write-Host "  ✓ Jasypt库下载完成" -ForegroundColor Green
    }
    catch {
        Write-Host "  ✗ Jasypt库下载失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "  请手动下载: $jasyptUrl" -ForegroundColor Yellow
        exit 1
    }
}

# 加密密码
Write-Host "`n正在加密密码..." -ForegroundColor Yellow
Write-Host "  原始密码: $Password" -ForegroundColor Gray
Write-Host "  加密密钥: $SecretKey" -ForegroundColor Gray

try {
    # 使用Jasypt进行加密
    $encryptCommand = "java -cp `"$jasyptPath`" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI input=`"$Password`" password=`"$SecretKey`" algorithm=PBEWITHHMACSHA512ANDAES_256"

    $encryptOutput = Invoke-Expression $encryptCommand 2>&1

    if ($LASTEXITCODE -eq 0) {
        # 提取加密后的值
        $encryptedValue = ($encryptOutput | Select-String -Pattern "----OUTPUT----\s*(.+)" | ForEach-Object { $_.Matches.Groups[1].Value }).Trim()

        if ($encryptedValue) {
            $encryptedFormat = "ENC(AES256:$encryptedValue)"

            Write-Host "`n========================================" -ForegroundColor Green
            Write-Host "  加密成功" -ForegroundColor Green
            Write-Host "========================================" -ForegroundColor Green
            Write-Host "`n加密后的值:" -ForegroundColor Cyan
            Write-Host $encryptedFormat -ForegroundColor Yellow
            Write-Host "`n使用方法:" -ForegroundColor Cyan
            Write-Host "1. 将加密值配置到Nacos配置中心" -ForegroundColor White
            Write-Host "2. 在application.yml中使用: password: `${NACOS_CONFIG_KEY:ENC(AES256:$encryptedValue)}" -ForegroundColor White
            Write-Host "`n注意: 请妥善保管加密密钥，不要提交到代码仓库" -ForegroundColor Yellow

            # 复制到剪贴板（如果可用）
            try {
                $encryptedFormat | Set-Clipboard
                Write-Host "`n✓ 加密值已复制到剪贴板" -ForegroundColor Green
            }
            catch {
                # 剪贴板操作失败，忽略
            }

            exit 0
        }
        else {
            Write-Host "`n✗ 加密失败: 无法提取加密值" -ForegroundColor Red
            Write-Host "输出: $encryptOutput" -ForegroundColor Gray
            exit 1
        }
    }
    else {
        Write-Host "`n✗ 加密失败: $encryptOutput" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "`n✗ 加密过程出错: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

