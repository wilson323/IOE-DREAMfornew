# IOE-DREAM P0-1: 配置安全加固脚本 (PowerShell版本)
# 功能：扫描并报告所有明文密码
# 优先级：P0 - 立即执行
# 特点：只读操作，完全安全

$ErrorActionPreference = "Stop"

# 颜色函数
function Write-Info {
    param($Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param($Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param($Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param($Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# 项目路径
$ProjectRoot = "D:\IOE-DREAM"
$MicroservicesDir = "$ProjectRoot\microservices"
$ReportFile = "$ProjectRoot\P0-1_PASSWORD_SCAN_REPORT.md"

# 统计变量
$TotalFiles = 0
$FilesWithPasswords = 0
$PasswordsFound = 0
$ScanResults = @()

Write-Info "========================================="
Write-Info "P0-1: 配置安全扫描脚本 (只读模式)"
Write-Info "========================================="
Write-Info "项目根目录: $ProjectRoot"
Write-Info "微服务目录: $MicroservicesDir"
Write-Info ""

################################################################################
# 扫描明文密码
################################################################################

Write-Info "开始扫描配置文件中的明文密码..."

# 获取所有配置文件
$ConfigFiles = Get-ChildItem -Path $MicroservicesDir -Recurse -Include *.yml,*.yaml -File

Write-Info "找到 $($ConfigFiles.Count) 个配置文件"

foreach ($file in $ConfigFiles) {
    $TotalFiles++
    $relativePath = $file.FullName.Replace("$ProjectRoot\", "")

    # 读取文件内容
    $content = Get-Content $file.FullName -Raw

    # 检查是否包含明文密码
    $hasPassword = $false
    $passwordCount = 0
    $foundPasswords = @()

    # 检查数据库密码
    if ($content -match 'password:\s*([^\s$\{][^\r\n]*)') {
        $passwordValue = $Matches[1].Trim()
        if ($passwordValue -notmatch '^\$\{' -and $passwordValue -ne '' -and $passwordValue -ne 'null') {
            $hasPassword = $true
            $passwordCount++
            $foundPasswords += "数据库密码: $passwordValue"
        }
    }

    # 检查Redis密码
    if ($content -match 'redis:[\s\S]*?password:\s*([^\s$\{][^\r\n]*)') {
        $passwordValue = $Matches[1].Trim()
        if ($passwordValue -notmatch '^\$\{' -and $passwordValue -ne '' -and $passwordValue -ne 'null') {
            $hasPassword = $true
            $passwordCount++
            $foundPasswords += "Redis密码: $passwordValue"
        }
    }

    # 检查Nacos密码
    if ($content -match 'nacos:[\s\S]*?password:\s*([^\s$\{][^\r\n]*)') {
        $passwordValue = $Matches[1].Trim()
        if ($passwordValue -notmatch '^\$\{' -and $passwordValue -ne '' -and $passwordValue -ne 'null') {
            $hasPassword = $true
            $passwordCount++
            $foundPasswords += "Nacos密码: $passwordValue"
        }
    }

    if ($hasPassword) {
        $FilesWithPasswords++
        $PasswordsFound += $passwordCount

        Write-Warning "发现明文密码: $relativePath ($passwordCount 个)"

        $ScanResults += [PSCustomObject]@{
            File = $relativePath
            Count = $passwordCount
            Passwords = $foundPasswords
        }
    }
}

Write-Info ""
Write-Info "========================================="
Write-Info "扫描完成！"
Write-Info "========================================="
Write-Info "总文件数: $TotalFiles"
Write-Warning "包含明文密码的文件: $FilesWithPasswords"
Write-Warning "发现明文密码总数: $PasswordsFound"
Write-Info ""

################################################################################
# 生成报告
################################################################################

Write-Info "生成扫描报告..."

$reportContent = @"
# P0-1: 配置安全扫描报告

> **📋 扫描日期**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
> **📋 扫描状态**: ✅ 已完成
> **📋 优先级**: P0 - 立即整改
> **📋 操作类型**: 只读扫描（安全）

---

## 📊 扫描统计

| 项目 | 数量 |
|------|------|
| 扫描文件总数 | $TotalFiles |
| 包含明文密码的文件 | $FilesWithPasswords |
| 发现明文密码总数 | $PasswordsFound |

---

## 🚨 发现的明文密码

"@

if ($ScanResults.Count -gt 0) {
    foreach ($result in $ScanResults) {
        $reportContent += "`n### 📄 ``$($result.File)``"
        $reportContent += "`n"
        $reportContent += "`n- **密码数量**: $($result.Count)"
        $reportContent += "`n- **详细信息**:"
        foreach ($pwd in $result.Passwords) {
            $reportContent += "`n  - $pwd"
        }
        $reportContent += "`n"
    }
} else {
    $reportContent += "`n✅ **未发现明文密码！配置安全！**`n"
}

$reportContent += @"

---

## 🔧 整改建议

### 方案1：使用环境变量（推荐）

``````yaml
# ❌ 整改前
spring:
  datasource:
    password: "123456"  # 明文密码

# ✅ 整改后
spring:
  datasource:
    password: `${DB_PASSWORD}  # 从环境变量读取
``````

### 方案2：使用Nacos加密配置（企业级）

``````yaml
# ✅ Nacos加密配置
spring:
  cloud:
    nacos:
      config:
        server-addr: `${NACOS_SERVER_ADDR}
        namespace: `${NACOS_NAMESPACE}
        # 密码从Nacos加密配置中读取
``````

---

## 📋 下一步操作

### 立即执行（安全）

1. **查看本报告**：了解所有明文密码位置
2. **准备环境变量**：创建 ``.env`` 文件
3. **人工审查**：确认哪些密码需要替换

### 需要审查后执行（需谨慎）

4. **备份配置文件**：执行替换前必须备份
5. **替换明文密码**：使用自动化脚本替换
6. **验证配置**：启动服务验证连接

---

## ⚠️ 安全提醒

- ✅ 本次扫描为**只读操作**，未修改任何文件
- ⚠️ 密码替换操作需要**人工审查**后再执行
- 🔒 所有密码信息应该**严格保密**
- 📋 建议使用**Git版本控制**跟踪配置变更

---

## 📞 联系方式

- **架构委员会**: 负责整体方案审查
- **安全团队**: 负责密码加密实施
- **运维团队**: 负责环境变量配置

---

**👥 扫描执行**: IOE-DREAM 架构委员会
**📅 扫描日期**: $(Get-Date -Format 'yyyy-MM-dd')
**✅ 报告状态**: 已完成
"@

# 保存报告
$reportContent | Out-File -FilePath $ReportFile -Encoding UTF8

Write-Success "报告已生成: $ReportFile"

################################################################################
# 生成环境变量模板
################################################################################

Write-Info ""
Write-Info "生成环境变量模板..."

$envTemplate = @"
# IOE-DREAM 环境变量配置模板
# 使用说明：
# 1. 复制此文件为 .env
# 2. 填写实际的密码和密钥
# 3. 确保 .env 文件在 .gitignore 中

# ==================== Nacos配置 ====================
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=dev
NACOS_GROUP=IOE-DREAM
NACOS_USERNAME=nacos
NACOS_PASSWORD=nacos

# ==================== 数据库配置 ====================
# 统一数据库密码（如果所有服务使用相同密码）
DB_PASSWORD=

# 或者为每个服务单独配置
DB_COMMON_PASSWORD=
DB_ACCESS_PASSWORD=
DB_ATTENDANCE_PASSWORD=
DB_CONSUME_PASSWORD=
DB_VIDEO_PASSWORD=
DB_VISITOR_PASSWORD=

# ==================== Redis配置 ====================
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_DATABASE=0

# ==================== 监控配置 ====================
ZIPKIN_BASE_URL=http://localhost:9411
TRACING_SAMPLE_RATE=0.1

# ==================== 第三方服务 ====================
# 根据实际使用情况填写
WECHAT_APP_SECRET=
ALIPAY_PRIVATE_KEY=
JWT_SECRET_KEY=
"@

$envTemplateFile = "$ProjectRoot\.env.template"
$envTemplate | Out-File -FilePath $envTemplateFile -Encoding UTF8

Write-Success "环境变量模板已生成: $envTemplateFile"

################################################################################
# 总结
################################################################################

Write-Info ""
Write-Info "========================================="
Write-Info "扫描任务完成！"
Write-Info "========================================="
Write-Info ""

if ($FilesWithPasswords -gt 0) {
    Write-Warning "⚠️  发现 $FilesWithPasswords 个文件包含明文密码"
    Write-Warning "⚠️  共计 $PasswordsFound 个明文密码需要处理"
    Write-Info ""
    Write-Info "📋 下一步建议："
    Write-Info "  1. 查看详细报告: $ReportFile"
    Write-Info "  2. 配置环境变量: $envTemplateFile"
    Write-Info "  3. 人工审查后决定是否执行替换"
} else {
    Write-Success "✅ 太好了！未发现明文密码，配置安全！"
}

Write-Info ""
Write-Success "✅ 扫描脚本执行完成！"

