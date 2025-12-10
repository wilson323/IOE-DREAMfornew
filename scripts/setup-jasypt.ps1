# ============================================================
# IOE-DREAM Jasypt 加密工具配置脚本 (PowerShell版本)
#
# @Author:    IOE-DREAM Team
# @Date:      2025-12-09
# @Description: 配置Jasypt加密工具，解决明文密码安全问题
# ============================================================

param(
    [switch]$SkipJavaCheck,
    [switch]$SkipMavenCheck
)

# 颜色定义
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    White = "White"
}

# 日志函数
function Write-Log {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host "[$([datetime]::Now.ToString('HH:mm:ss'))] $Message" -ForegroundColor $Colors[$Color]
}

function Write-Info {
    param([string]$Message)
    Write-Log $Message "Blue"
}

function Write-Success {
    param([string]$Message)
    Write-Log $Message "Green"
}

function Write-Warning {
    param([string]$Message)
    Write-Log $Message "Yellow"
}

function Write-Error {
    param([string]$Message)
    Write-Log $Message "Red"
}

# 检查Java环境
function Test-JavaEnvironment {
    if (-not $SkipJavaCheck) {
        Write-Info "检查Java环境..."

        try {
            $javaVersion = & java -version 2>&1 | Select-Object -First 1
            Write-Success "Java环境检查通过"
            Write-Info "版本: $javaVersion"
        }
        catch {
            Write-Error "Java未安装或未配置到PATH"
            Write-Error "请从 https://adoptium.net/ 下载并安装 Java 11+"
            exit 1
        }
    }
    else {
        Write-Warning "跳过Java环境检查"
    }
}

# 检查Maven环境
function Test-MavenEnvironment {
    if (-not $SkipMavenCheck) {
        Write-Info "检查Maven环境..."

        try {
            $mavenVersion = & mvn -version 2>&1 | Select-Object -First 1
            Write-Success "Maven环境检查通过"
            Write-Info "版本: $mavenVersion"
        }
        catch {
            Write-Error "Maven未安装或未配置到PATH"
            Write-Error "请从 https://maven.apache.org/download.cgi 下载并安装 Maven"
            exit 1
        }
    }
    else {
        Write-Warning "跳过Maven环境检查"
    }
}

# 生成加密密钥
function New-EncryptionKey {
    Write-Info "生成Jasypt加密密钥..."

    try {
        # 生成256位（32字节）的加密密钥
        $randomBytes = New-Object byte[] 32
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        $rng.GetBytes($randomBytes)
        $encryptionKey = [BitConverter]::ToString($randomBytes).Replace("-", "").ToLower()

        # 保存密钥到文件
        $encryptionKey | Out-File -FilePath ".jasypt-encryption-key" -Encoding UTF8

        # 设置文件权限（Windows下隐藏文件）
        $keyFile = Get-Item ".jasypt-encryption-key" -ErrorAction SilentlyContinue
        if ($keyFile) {
            $keyFile.Attributes = $keyFile.Attributes -bor [System.IO.FileAttributes]::Hidden
        }

        Write-Success "加密密钥生成成功"
        Write-Info "密钥已保存到 .jasypt-encryption-key 文件"
        Write-Info "加密密钥: $encryptionKey"

        return $encryptionKey
    }
    catch {
        Write-Error "加密密钥生成失败: $_"
        exit 1
    }
}

# 下载Jasypt CLI工具
function Get-JasyptCli {
    Write-Info "下载Jasypt CLI工具..."

    $jasyptVersion = "3.0.5"
    $jasyptCliJar = "jasypt-cli-$jasyptVersion.jar"
    $jasyptCliUrl = "https://repo1.maven.org/maven2/org/jasypt/jasypt/$jasyptVersion/$jasyptCliJar"

    if (-not (Test-Path $jasyptCliJar)) {
        Write-Info "下载Jasypt CLI工具..."

        try {
            # 使用 Invoke-WebRequest 下载文件
            Invoke-WebRequest -Uri $jasyptCliUrl -OutFile $jasyptCliJar -UseBasicParsing
            Write-Success "Jasypt CLI工具下载完成"
        }
        catch {
            Write-Error "Jasypt CLI工具下载失败: $_"
            exit 1
        }
    }
    else {
        Write-Info "Jasypt CLI工具已存在，跳过下载"
    }
}

# 创建加密脚本
function New-EncryptionScript {
    Write-Info "创建密码加密脚本..."

    $scriptContent = @'
# IOE-DREAM 密码加密脚本 (PowerShell版本)
# 使用方法: .\Encrypt-Password.ps1 "要加密的密码"

param(
    [Parameter(Mandatory=$true)]
    [string]$PlainPassword
)

$jasyptVersion = "3.0.5"
$jasyptCliJar = "jasypt-cli-$jasyptVersion.jar"

# 读取加密密钥
if (-not (Test-Path ".jasypt-encryption-key")) {
    Write-Error "错误: 找不到加密密钥文件 .jasypt-encryption-key"
    exit 1
}

$encryptionKey = Get-Content ".jasypt-encryption-key" -Raw

# 执行加密
try {
    $encryptedPassword = & java -cp "$jasyptCliJar" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI `
        input="$PlainPassword" `
        password="$encryptionKey" `
        algorithm="PBEWITHHMACSHA512ANDAES_256" 2>$null

    if ($LASTEXITCODE -eq 0) {
        Write-Host "原始密码: $PlainPassword" -ForegroundColor Green
        Write-Host "加密结果: ENC($encryptedPassword)" -ForegroundColor Green
        Write-Host ""
        Write-Host "请在配置文件中使用: ENC($encryptedPassword)" -ForegroundColor Yellow
    }
    else {
        Write-Error "加密失败"
        exit 1
    }
}
catch {
    Write-Error "加密过程中出错: $_"
    exit 1
}
'@

    $scriptContent | Out-File -FilePath "Encrypt-Password.ps1" -Encoding UTF8
    Write-Success "密码加密脚本创建完成: Encrypt-Password.ps1"
}

# 创建解密脚本
function New-DecryptionScript {
    Write-Info "创建密码解密脚本..."

    $scriptContent = @'
# IOE-DREAM 密码解密脚本 (PowerShell版本)
# 使用方法: .\Decrypt-Password.ps1 "ENC(加密后的密码)"

param(
    [Parameter(Mandatory=$true)]
    [string]$EncryptedInput
)

# 提取ENC()中的内容
$plainEncrypted = $EncryptedInput -replace 'ENC\((.*)\)', '$1'

$jasyptVersion = "3.0.5"
$jasyptCliJar = "jasypt-cli-$jasyptVersion.jar"

# 读取加密密钥
if (-not (Test-Path ".jasypt-encryption-key")) {
    Write-Error "错误: 找不到加密密钥文件 .jasypt-encryption-key"
    exit 1
}

$encryptionKey = Get-Content ".jasypt-encryption-key" -Raw

# 执行解密
try {
    $decryptedPassword = & java -cp "$jasyptCliJar" org.jasypt.intf.cli.JasyptPBEStringDecryptionCLI `
        input="$plainEncrypted" `
        password="$encryptionKey" `
        algorithm="PBEWITHHMACSHA512ANDAES_256" 2>$null

    if ($LASTEXITCODE -eq 0) {
        Write-Host "加密输入: $EncryptedInput" -ForegroundColor Green
        Write-Host "解密结果: $decryptedPassword" -ForegroundColor Green
    }
    else {
        Write-Error "解密失败"
        exit 1
    }
}
catch {
    Write-Error "解密过程中出错: $_"
    exit 1
}
'@

    $scriptContent | Out-File -FilePath "Decrypt-Password.ps1" -Encoding UTF8
    Write-Success "密码解密脚本创建完成: Decrypt-Password.ps1"
}

# 创建批量加密脚本
function New-BatchEncryptionScript {
    Write-Info "创建批量密码加密脚本..."

    $scriptContent = @'
# IOE-DREAM 批量密码加密脚本 (PowerShell版本)
# 用于加密常见的数据库连接密码

$jasyptVersion = "3.0.5"
$jasyptCliJar = "jasypt-cli-$jasyptVersion.jar"

# 读取加密密钥
if (-not (Test-Path ".jasypt-encryption-key")) {
    Write-Error "错误: 找不到加密密钥文件 .jasypt-encryption-key"
    exit 1
}

$encryptionKey = Get-Content ".jasypt-encryption-key" -Raw

# 要加密的密码列表（根据实际情况修改）
$passwords = @{
    "数据库root密码" = "your_root_password_here"
    "数据库应用密码" = "your_app_password_here"
    "Redis密码" = "your_redis_password_here"
    "Druid监控密码" = "your_druid_password_here"
    "JWT密钥" = "your_jwt_secret_here"
    "SMTP密码" = "your_smtp_password_here"
    "短信API密钥" = "your_sms_key_here"
    "MinIO密钥" = "your_minio_key_here"
    "支付宝密钥" = "your_alipay_key_here"
    "微信支付密钥" = "your_wechat_key_here"
}

Write-Host "开始批量加密密码..." -ForegroundColor Blue
Write-Host ""

# 加密每个密码
foreach ($desc in $passwords.Keys) {
    $password = $passwords[$desc]

    # 如果密码是占位符，跳过加密
    if ($password -match "_here$") {
        Write-Host "[$desc] 占位符密码，跳过加密" -ForegroundColor Yellow
        continue
    }

    try {
        $encrypted = & java -cp "$jasyptCliJar" org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI `
            input="$password" `
            password="$encryptionKey" `
            algorithm="PBEWITHHMACSHA512ANDAES_256" 2>$null

        if ($LASTEXITCODE -eq 0) {
            Write-Host "[$desc]" -ForegroundColor Cyan
            Write-Host "原始: $password" -ForegroundColor Gray
            Write-Host "加密: ENC($encrypted)" -ForegroundColor Green
            Write-Host ""
        }
        else {
            Write-Host "[$desc] 加密失败" -ForegroundColor Red
            Write-Host ""
        }
    }
    catch {
        Write-Host "[$desc] 加密过程中出错: $_" -ForegroundColor Red
        Write-Host ""
    }
}

Write-Host "批量加密完成！" -ForegroundColor Green
Write-Host "请在配置文件中替换为对应的 ENC() 加密值。" -ForegroundColor Yellow
'@

    $scriptContent | Out-File -FilePath "Batch-Encrypt.ps1" -Encoding UTF8
    Write-Success "批量密码加密脚本创建完成: Batch-Encrypt.ps1"
}

# 创建环境变量配置文件
function New-EnvironmentConfig {
    Write-Info "创建环境变量配置文件..."

    $envConfig = @'
# Jasypt 加密配置环境变量
# 请在部署时设置这些环境变量

# Jasypt加密密钥（生产环境必须设置）
$env:JASYPT_PASSWORD = "your_encryption_key_here"

# 可选：Jasypt算法
$env:JASYPT_ENCRYPTOR_ALGORITHM = "PBEWITHHMACSHA512ANDAES_256"

# 可选：密钥获取迭代次数
$env:JASYPT_ENCRYPTOR_KEY_OBTENTION_ITERATIONS = 1000

# 可选：盐生成器类
$env:JASYPT_ENCRYPTOR_SALT_GENERATOR_CLASSNAME = "org.jasypt.salt.RandomSaltGenerator"

# 可选：IV生成器类
$env:JASYPT_ENCRYPTOR_IV_GENERATOR_CLASSNAME = "org.jasypt.iv.RandomIvGenerator"

# 可选：字符串输出类型
$env:JASYPT_ENCRYPTOR_STRING_OUTPUT_TYPE = "base64"

Write-Host "请编辑此文件，设置实际的加密密钥" -ForegroundColor Yellow
Write-Host "使用方法: .\.env.jasypt" -ForegroundColor Green
'@

    $envConfig | Out-File -FilePath ".env.jasypt.ps1" -Encoding UTF8
    Write-Success "环境变量配置文件创建完成: .env.jasypt.ps1"
    Write-Warning "请编辑 .env.jasypt.ps1 文件，设置实际的加密密钥"
}

# 创建Spring Boot配置示例
function New-SpringBootConfig {
    Write-Info "创建Spring Boot Jasypt配置示例..."

    $springConfig = @'
# ============================================================
# Spring Boot Jasypt 配置示例
# 复制到你的 application.yml 或 application-prod.yml 中
# ============================================================

jasypt:
  encryptor:
    # 从环境变量读取加密密钥
    password: ${JASYPT_PASSWORD}

    # 加密算法
    algorithm: PBEWITHHMACSHA512ANDAES_256

    # 密钥获取迭代次数
    key-obtention-iterations: 1000

    # 池大小
    pool-size: 1

    # 提供者名称
    provider-name: SunJCE

    # 盐生成器类名
    salt-generator-classname: org.jasypt.salt.RandomSaltGenerator

    # IV生成器类名
    iv-generator-classname: org.jasypt.iv.RandomIvGenerator

    # 字符串输出类型
    string-output-type: base64

    # 属性前缀和后缀
    property:
      prefix: "ENC("
      suffix: ")"

# 应用示例
spring:
  # 使用加密的数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ENC(这里放加密后的数据库密码)

  # 使用加密的Redis配置
  redis:
    host: localhost
    port: 6379
    password: ENC(这里放加密后的Redis密码)
'@

    $springConfig | Out-File -FilePath "jasypt-spring-config.yml" -Encoding UTF8
    Write-Success "Spring Boot Jasypt配置示例创建完成: jasypt-spring-config.yml"
}

# 创建Maven依赖配置
function New-MavenDependency {
    Write-Info "创建Maven Jasypt依赖配置..."

    $mavenDependency = @'
<!-- ============================================================
        Jasypt Maven 依赖配置
        复制到你的 pom.xml 文件中的 <dependencies> 部分
        ============================================================ -->

<!-- Jasypt 加密依赖 -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>

<!-- 可选：如果你需要更高级的加密功能 -->
<dependency>
    <groupId>org.jasypt</groupId>
    <artifactId>jasypt</artifactId>
    <version>1.9.3</version>
</dependency>
'@

    $mavenDependency | Out-File -FilePath "jasypt-maven-dependency.xml" -Encoding UTF8
    Write-Success "Maven依赖配置创建完成: jasypt-maven-dependency.xml"
}

# 创建使用说明文档
function New-UsageGuide {
    Write-Info "创建使用说明文档..."

    $usageGuide = @'
# Jasypt 加密工具使用指南

## 概述

Jasypt是一个Java加密库，用于简化应用程序中的加密和解密操作。在Spring Boot应用中，Jasypt可以加密配置文件中的敏感信息（如数据库密码、API密钥等），避免在配置文件中明文存储。

## 快速开始

### 1. 加密单个密码

```powershell
.\Encrypt-Password.ps1 "你的密码"
```

输出示例：
```
原始密码: mypassword123
加密结果: ENC(G6K7X8J9M0N1O2P3Q4R5S6T7U8V9W0X)
请在配置文件中使用: ENC(G6K7X8J9M0N1O2P3Q4R5S6T7U8V9W0X)
```

### 2. 批量加密密码

编辑 `Batch-Encrypt.ps1` 文件中的密码列表，然后运行：

```powershell
.\Batch-Encrypt.ps1
```

### 3. 解密密码

```powershell
.\Decrypt-Password.ps1 "ENC(加密后的密码)"
```

## 配置文件使用

在Spring Boot配置文件中使用加密后的密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ioedream
    username: root
    password: ENC(G6K7X8J9M0N1O2P3Q4R5S6T7U8V9W0X)
```

## 环境变量配置

在生产环境中，通过环境变量设置加密密钥：

```powershell
$env:JASYPT_PASSWORD = "your_encryption_key_here"
java -jar your-app.jar
```

或者加载环境变量配置：

```powershell
. .\.env.jasypt.ps1
java -jar your-app.jar
```

## Maven依赖

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>
```

## 安全最佳实践

1. **加密密钥管理**
   - 不要将加密密钥提交到代码仓库
   - 使用环境变量或安全的密钥管理服务
   - 定期轮换加密密钥

2. **密码安全**
   - 使用强密码策略
   - 不同环境使用不同的密码
   - 定期更新敏感配置

3. **配置安全**
   - 生产环境配置文件不要包含明文密码
   - 使用最小权限原则
   - 定期审计配置文件

## 故障排除

### 1. 解密失败

- 检查加密密钥是否正确
- 确认加密算法配置一致
- 验证ENC()格式是否正确

### 2. 启动失败

- 确认Jasypt依赖已正确添加
- 检查环境变量是否设置
- 验证配置文件格式

### 3. 性能问题

- 考虑缓存解密结果
- 优化加密算法选择
- 减少不必要的加密操作

## 更多信息

- [Jasypt官方文档](https://github.com/ulisesbocchio/jasypt)
- [Spring Boot Jasypt集成](https://github.com/ulisesbocchio/jasypt-spring-boot)
'@

    $usageGuide | Out-File -FilePath "JASYPT_USAGE_GUIDE.md" -Encoding UTF8
    Write-Success "使用说明文档创建完成: JASYPT_USAGE_GUIDE.md"
}

# 主函数
function Main {
    Write-Host "==============================================================" -ForegroundColor Cyan
    Write-Host "🔐 IOE-DREAM Jasypt 加密工具配置" -ForegroundColor Cyan
    Write-Host "==============================================================" -ForegroundColor Cyan
    Write-Host ""

    Write-Info "开始配置Jasypt加密工具..."

    # 检查环境
    Test-JavaEnvironment
    Test-MavenEnvironment

    # 生成加密密钥
    $encryptionKey = New-EncryptionKey

    # 下载工具
    Get-JasyptCli

    # 创建脚本
    New-EncryptionScript
    New-DecryptionScript
    New-BatchEncryptionScript

    # 创建配置文件
    New-EnvironmentConfig
    New-SpringBootConfig
    New-MavenDependency
    New-UsageGuide

    Write-Host ""
    Write-Host "==============================================================" -ForegroundColor Green
    Write-Success "✅ Jasypt加密工具配置完成！"
    Write-Host "==============================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "📋 生成的文件：" -ForegroundColor Yellow
    Write-Host "  🔑 .jasypt-encryption-key          - 加密密钥文件" -ForegroundColor White
    Write-Host "  🔐 jasypt-cli-3.0.5.jar           - Jasypt CLI工具" -ForegroundColor White
    Write-Host "  🔧 Encrypt-Password.ps1          - 密码加密脚本" -ForegroundColor White
    Write-Host "  🔓 Decrypt-Password.ps1          - 密码解密脚本" -ForegroundColor White
    Write-Host "  📦 Batch-Encrypt.ps1             - 批量加密脚本" -ForegroundColor White
    Write-Host "  ⚙️  .env.jasypt.ps1               - 环境变量配置" -ForegroundColor White
    Write-Host "  📄 jasypt-spring-config.yml      - Spring配置示例" -ForegroundColor White
    Write-Host "  📚 jasypt-maven-dependency.xml    - Maven依赖配置" -ForegroundColor White
    Write-Host "  📖 JASYPT_USAGE_GUIDE.md         - 详细使用说明" -ForegroundColor White
    Write-Host ""
    Write-Host "🚀 下一步操作：" -ForegroundColor Yellow
    Write-Host "  1. 编辑 Batch-Encrypt.ps1 添加需要加密的密码" -ForegroundColor White
    Write-Host "  2. 运行 .\Batch-Encrypt.ps1 批量加密密码" -ForegroundColor White
    Write-Host "  3. 在配置文件中替换明文密码为 ENC(加密值)" -ForegroundColor White
    Write-Host "  4. 设置环境变量 JASYPT_PASSWORD" -ForegroundColor White
    Write-Host "  5. 启动应用测试配置" -ForegroundColor White
    Write-Host ""
    Write-Host "🔒 安全提醒：" -ForegroundColor Red
    Write-Host "  - 请妥善保管加密密钥文件 .jasypt-encryption-key" -ForegroundColor White
    Write-Host "  - 不要将加密密钥提交到代码仓库" -ForegroundColor White
    Write-Host "  - 生产环境请使用环境变量设置密钥" -ForegroundColor White
    Write-Host ""
}

# 执行主函数
Main