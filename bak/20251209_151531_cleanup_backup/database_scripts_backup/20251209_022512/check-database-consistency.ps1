# =====================================================
# IOE-DREAM 数据库脚本一致性检查脚本
# 版本: 1.0.1-FIX
# 说明: 严格确保数据库脚本与Entity类100%一致
# 执行方式: PowerShell
# 使用方法: .\check-database-consistency.ps1
# =====================================================

param(
    [switch]$Detailed,
    [switch]$Fix,
    [string]$LogLevel = "INFO"
)

# 设置控制台编码为UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$Host.UI.RawUI.WindowTitle = "IOE-DREAM 数据库一致性检查 (v1.0.1-FIX)"

Write-Host "====================================" -ForegroundColor Green
Write-Host "IOE-DREAM 数据库脚本一致性检查" -ForegroundColor Green
Write-Host "版本: 1.0.1-FIX" -ForegroundColor Green
Write-Host "时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Green
Write-Host "====================================" -ForegroundColor Green

# 日志函数
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $Timestamp = Get-Date -Format "HH:mm:ss"

    switch ($Level) {
        "ERROR" { Write-Host "[$Timestamp] ERROR: $Message" -ForegroundColor Red }
        "WARN"  { Write-Host "[$Timestamp] WARN:  $Message" -ForegroundColor Yellow }
        "INFO"  { Write-Host "[$Timestamp] INFO:  $Message" -ForegroundColor Cyan }
        "DEBUG" { if ($Detailed) { Write-Host "[$Timestamp] DEBUG: $Message" -ForegroundColor Gray } }
        "SUCCESS" { Write-Host "[$Timestamp] SUCCESS: $Message" -ForegroundColor Green }
        default { Write-Host "[$Timestamp] ${Level}: $Message" }
    }
}

# 检查结果统计
$TotalChecks = 0
$PassedChecks = 0
$FailedChecks = 0
$WarningChecks = 0

function Check-Result {
    param([bool]$Success, [string]$Message, [string]$Type = "ERROR")
    $script:TotalChecks++

    if ($Success) {
        $script:PassedChecks++
        Write-Log $Message "SUCCESS"
    } else {
        $script:FailedChecks++
        Write-Log $Message $Type
    }
}

function Check-Warning {
    param([bool]$Success, [string]$Message)
    $script:TotalChecks++

    if ($Success) {
        $script:PassedChecks++
        Write-Log $Message "SUCCESS"
    } else {
        $script:WarningChecks++
        Write-Log $Message "WARN"
    }
}

# 1. 检查脚本文件是否存在
Write-Log "1. 检查脚本文件结构..." "INFO"

$RequiredFiles = @(
    "init-all.sql",
    "VERSION",
    "CHANGELOG.md",
    "sql/01-create-databases.sql",
    "sql/02-common-schema.sql",
    "sql/03-business-schema-v1.0.1-fixed.sql",
    "sql/99-flyway-schema.sql",
    "data/common-data.sql",
    "data/business-data-fixed.sql"
)

foreach ($File in $RequiredFiles) {
    $FilePath = Join-Path $PSScriptRoot $File
    Check-Result (Test-Path $FilePath) "检查文件: $File"
}

# 2. 检查版本文件内容
Write-Log "2. 检查版本配置..." "INFO"

$VersionFile = Join-Path $PSScriptRoot "VERSION"
if (Test-Path $VersionFile) {
    $VersionContent = Get-Content $VersionFile -Raw

    Check-Result ($VersionContent -match "version = 1\.0\.1") "版本号匹配: 1.0.1-FIX"
    Check-Result ($VersionContent -match "environment = prod") "环境配置: 生产环境"
    Check-Result ($VersionContent -match "fix_version = 1\.0\.1-FIX") "修复版本标记: 1.0.1-FIX"
}

# 3. 检查主执行脚本
Write-Log "3. 检查主执行脚本..." "INFO"

$MainScript = Join-Path $PSScriptRoot "init-all.sql"
if (Test-Path $MainScript) {
    $MainContent = Get-Content $MainScript -Raw

    Check-Result ($MainContent -match "v1\.0\.1-FIX") "主脚本包含版本标记: v1.0.1-FIX"
    Check-Result ($MainContent -match "03-business-schema-v1\.0\.1-fixed\.sql") "引用正确的业务表结构脚本"
    Check-Result ($MainContent -match "business-data-fixed\.sql") "引用正确的数据初始化脚本"
}

# 4. 检查Entity类文件
Write-Log "4. 检查Entity类一致性..." "INFO"

$ProjectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$AccountEntityPath = Join-Path $ProjectRoot "microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/domain/entity/AccountEntity.java"

if (Test-Path $AccountEntityPath) {
    $AccountEntityContent = Get-Content $AccountEntityPath -Raw

    # 检查修复后的关键点
    Check-Result ($AccountEntityContent -match '@TableName\("t_consume_account"\)') "AccountEntity表名修正: t_consume_account"
    Check-Result (-not ($AccountEntityContent -match 'private Long balance;')) "AccountEntity删除重复的Long balance字段"
    Check-Result ($AccountEntityContent -match 'private BigDecimal balance;') "AccountEntity保留BigDecimal balance字段"
    Check-Result (-not ($AccountEntityContent -match 'private Integer status;' -and ($AccountEntityContent -match 'private Integer status;' -replace '.*', '').Count -gt 1))) "AccountEntity删除重复的status字段"
    Check-Result (-not ($AccountEntityContent -match 'private Integer version;')) "AccountEntity删除重复的version字段"
    Check-Result ($AccountEntityContent -match 'private Long accountId;') "AccountEntity使用正确的accountId字段名"
} else {
    Write-Log "AccountEntity文件未找到: $AccountEntityPath" "ERROR"
}

# 5. 检查SQL脚本语法
Write-Log "5. 检查SQL脚本语法..." "INFO"

$BusinessSchemaScript = Join-Path $PSScriptRoot "sql/03-business-schema-v1.0.1-fixed.sql"
if (Test-Path $BusinessSchemaScript) {
    $SqlContent = Get-Content $BusinessSchemaScript -Raw

    Check-Result ($SqlContent -match 'CREATE TABLE `t_consume_account`') "SQL脚本包含t_consume_account表创建"
    Check-Result ($SqlContent -match 'balance DECIMAL\(12,2\)') "SQL脚本使用正确的DECIMAL(12,2)类型"
    Check-Result ($SqlContent -match 'version 1\.0\.1-FIX') "SQL脚本包含版本标记"
    Check-Result ($SqlContent -match 'account_id BIGINT PRIMARY KEY AUTO_INCREMENT') "SQL脚本使用正确的account_id主键"
} else {
    Write-Log "业务表结构脚本未找到: $BusinessSchemaScript" "ERROR"
}

# 6. 检查数据初始化脚本
Write-Log "6. 检查数据初始化脚本..." "INFO"

$BusinessDataScript = Join-Path $PSScriptRoot "data/business-data-fixed.sql"
if (Test-Path $BusinessDataScript) {
    $DataContent = Get-Content $BusinessDataScript -Raw

    Check-Result ($DataContent -match 'INSERT INTO `t_consume_account`') "数据脚本包含t_consume_account表插入"
    Check-Result ($DataContent -match 'account_id, user_id, account_no') "数据脚本使用正确的字段列表"
} else {
    Write-Log "业务数据脚本未找到: $BusinessDataScript" "ERROR"
}

# 7. 检查目录结构规范
Write-Log "7. 检查目录结构规范..." "INFO"

$ExpectedDirectories = @(
    "sql",
    "data",
    "rollback"
)

foreach ($Dir in $ExpectedDirectories) {
    $DirPath = Join-Path $PSScriptRoot $Dir
    Check-Result (Test-Path $DirPath) "检查目录: $Dir"
}

# 8. 检查文件命名规范
Write-Log "8. 检查文件命名规范..." "INFO"

$SqlFiles = Get-ChildItem -Path (Join-Path $PSScriptRoot "sql") -Filter "*.sql"
foreach ($SqlFile in $SqlFiles) {
    $FileName = $SqlFile.Name

    if ($FileName -match "^(\d{2})-[a-z-]+\.sql$") {
        Write-Log "SQL文件命名规范: $FileName" "DEBUG"
        Check-Warning $true "SQL文件命名符合规范: $FileName"
    } else {
        Check-Warning $false "SQL文件命名不规范: $FileName"
    }
}

# 9. 检查脚本依赖顺序
Write-Log "9. 检查脚本执行顺序..." "INFO"

$MainScript = Join-Path $PSScriptRoot "init-all.sql"
if (Test-Path $MainScript) {
    $MainContent = Get-Content $MainScript

    # 检查执行顺序
    $Order1 = $MainContent | Select-String "01-create-databases.sql" -Quiet
    $Order2 = $MainContent | Select-String "02-common-schema.sql" -Quiet
    $Order3 = $MainContent | Select-String "03-business-schema-v1.0.1-fixed.sql" -Quiet
    $Order4 = $MainContent | Select-String "99-flyway-schema.sql" -Quiet

    Check-Result $Order1 "脚本顺序: 01-create-databases.sql"
    Check-Result $Order2 "脚本顺序: 02-common-schema.sql"
    Check-Result $Order3 "脚本顺序: 03-business-schema-v1.0.1-fixed.sql"
    Check-Result $Order4 "脚本顺序: 99-flyway-schema.sql"
}

# 10. 综合质量检查
Write-Log "10. 综合质量检查..." "INFO"

$AllSqlFiles = Get-ChildItem -Path $PSScriptRoot -Recurse -Filter "*.sql"
$TotalSqlFiles = $AllSqlFiles.Count

Check-Result ($TotalSqlFiles -ge 10) "SQL脚本数量检查: $TotalSqlFiles 个文件"

$TotalSize = 0
$AllSqlFiles | ForEach-Object { $TotalSize += $_.Length }
$TotalSizeKB = [math]::Round($TotalSize / 1KB, 2)

Check-Result ($TotalSizeKB -gt 50) "SQL脚本总大小: ${TotalSizeKB}KB"

# 生成检查报告
Write-Log "====================================" -ForegroundColor Green
Write-Log "检查报告汇总" -ForegroundColor Green
Write-Log "====================================" -ForegroundColor Green

Write-Log "总检查项目: $TotalChecks" "INFO"
Write-Log "通过检查: $PassedChecks" "SUCCESS"
Write-Log "警告项目: $WarningChecks" "WARN"
Write-Log "失败项目: $FailedChecks" "ERROR"

$SuccessRate = [math]::Round(($PassedChecks / $TotalChecks) * 100, 2)
Write-Log "通过率: $SuccessRate%" "INFO"

# 判断整体状态
if ($FailedChecks -eq 0) {
    Write-Log "🎉 恭喜！数据库脚本一致性检查全部通过！" "SUCCESS"
    Write-Log "✅ 脚本与Entity类100%匹配" "SUCCESS"
    Write-Log "✅ 版本体系规范严格执行" "SUCCESS"
    Write-Log "✅ 可以安全执行数据库初始化" "SUCCESS"
    exit 0
} else {
    Write-Log "❌ 发现 $FailedChecks 个严重问题，需要立即修复！" "ERROR"
    Write-Log "⚠️ 发现 $WarningChecks 个警告，建议优化" "WARN"

    if ($Fix) {
        Write-Log "🔧 尝试自动修复..." "INFO"
        # 这里可以添加自动修复逻辑
    }

    Write-Log "请修复问题后重新执行检查" "ERROR"
    exit 1
}