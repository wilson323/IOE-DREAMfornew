# ============================================================
# IOE-DREAM 数据库迁移设置验证脚本
# 版本: v1.0.0
# 作者: IOE-DREAM 架构团队
# 创建时间: 2025-12-15
# 功能: 全面验证数据库迁移设置的准确性和完整性
# ============================================================

# 设置执行策略
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# 导入工具函数
$ScriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$UtilitiesPath = Join-Path $ScriptRoot "database-utilities.ps1"

if (Test-Path $UtilitiesPath) {
    . $UtilitiesPath
    Write-Host "✅ 已加载数据库工具函数" -ForegroundColor Green
} else {
    Write-Host "❌ 数据库工具函数文件不存在: $UtilitiesPath" -ForegroundColor Red
    exit 1
}

# 验证结果统计
$ValidationResults = @{
    TotalChecks = 0
    PassedChecks = 0
    FailedChecks = 0
    Warnings = 0
    CriticalIssues = @()
}

# 日志记录函数
function Write-ValidationLog {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Message,

        [ValidateSet("INFO", "SUCCESS", "WARNING", "ERROR", "CRITICAL")]
        [string]$Level = "INFO"
    )

    $ValidationResults.TotalChecks++

    switch ($Level) {
        "SUCCESS" {
            $ValidationResults.PassedChecks++
            Write-Host "✅ $Message" -ForegroundColor Green
        }
        "WARNING" {
            $ValidationResults.Warnings++
            Write-Host "⚠️ $Message" -ForegroundColor Yellow
        }
        "ERROR" {
            $ValidationResults.FailedChecks++
            Write-Host "❌ $Message" -ForegroundColor Red
        }
        "CRITICAL" {
            $ValidationResults.FailedChecks++
            $ValidationResults.CriticalIssues += $Message
            Write-Host "🚨 $Message" -ForegroundColor Red
        }
        default {
            Write-Host "ℹ️ $Message" -ForegroundColor Cyan
        }
    }
}

# 1. 验证项目结构
function Test-ProjectStructure {
    Write-Host "`n🏗️ 验证项目结构..." -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    $ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptRoot)

    # 检查关键目录
    $RequiredDirectories = @(
        "microservices",
        "microservices/microservices-common",
        "microservices/ioedream-db-init",
        "scripts/database",
        ".claude/skills"
    )

    foreach ($Dir in $RequiredDirectories) {
        $Path = Join-Path $ProjectRoot $Dir
        if (Test-Path $Path) {
            Write-ValidationLog "目录存在: $Dir" -Level "SUCCESS"
        } else {
            Write-ValidationLog "关键目录缺失: $Dir" -Level "CRITICAL"
        }
    }

    # 检查微服务数量
    $ServicesPath = Join-Path $ProjectRoot "microservices"
    $ServiceCount = (Get-ChildItem -Path $ServicesPath -Directory -Filter "ioedream-*-service" | Measure-Object).Count

    if ($ServiceCount -ge 9) {
        Write-ValidationLog "找到 $ServiceCount 个微服务" -Level "SUCCESS"
    } else {
        Write-ValidationLog "微服务数量不足: $ServiceCount (期望 >= 9)" -Level "WARNING"
    }
}

# 2. 验证Flyway配置
function Test-FlywayConfiguration {
    Write-Host "`n⚙️ 验证Flyway配置..." -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    $ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptRoot)
    $ServicesPath = Join-Path $ProjectRoot "microservices"

    $ExpectedServices = @(
        "ioedream-gateway-service",
        "ioedream-common-service",
        "ioedream-device-comm-service",
        "ioedream-oa-service",
        "ioedream-access-service",
        "ioedream-attendance-service",
        "ioedream-video-service",
        "ioedream-consume-service",
        "ioedream-visitor-service"
    )

    $ConfiguredServices = 0

    foreach ($Service in $ExpectedServices) {
        $ConfigPath = Join-Path $ServicesPath "$Service\src\main\resources\application.yml"

        if (Test-Path $ConfigPath) {
            $Content = Get-Content $ConfigPath -Raw
            if ($Content -match "flyway:") {
                $ConfiguredServices++
                Write-ValidationLog "Flyway配置已就绪: $Service" -Level "SUCCESS"
            } else {
                Write-ValidationLog "Flyway配置缺失: $Service" -Level "ERROR"
            }
        } else {
            Write-ValidationLog "配置文件不存在: $Service" -Level "WARNING"
        }
    }

    Write-Host "`n📊 Flyway配置覆盖率: $ConfiguredServices/$($ExpectedServices.Count) ($([math]::Round($ConfiguredServices/$ExpectedServices.Count*100, 1))%)" -ForegroundColor Cyan

    if ($ConfiguredServices -eq $ExpectedServices.Count) {
        Write-ValidationLog "所有微服务Flyway配置完成" -Level "SUCCESS"
    } else {
        Write-ValidationLog "部分微服务缺少Flyway配置" -Level "WARNING"
    }
}

# 3. 验证迁移脚本
function Test-MigrationScripts {
    Write-Host "`n📄 验证迁移脚本..." -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    $ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptRoot)
    $MigrationPath = Join-Path $ProjectRoot "microservices\ioedream-db-init\src\main\resources\db\migration"

    if (Test-Path $MigrationPath) {
        $ScriptFiles = Get-ChildItem -Path $MigrationPath -Filter "V*.sql" | Sort-Object Name
        $ScriptCount = $ScriptFiles.Count

        Write-ValidationLog "找到 $ScriptCount 个迁移脚本" -Level "SUCCESS"

        # 验证脚本命名规范
        $NamingIssues = 0
        foreach ($Script in $ScriptFiles) {
            if ($Script.Name -match "^V\d+_\d+_\d+__.*\.sql$") {
                Write-ValidationLog "命名规范: $($Script.Name)" -Level "SUCCESS"
            } else {
                $NamingIssues++
                Write-ValidationLog "命名不规范: $($Script.Name)" -Level "WARNING"
            }
        }

        # 验证脚本内容完整性
        $ContentIssues = 0
        foreach ($Script in $ScriptFiles) {
            $Content = Get-Content $Script.FullName -Raw

            if ($Content -match "CREATE TABLE" -or $Content -match "ALTER TABLE" -or $Content -match "INSERT INTO") {
                Write-ValidationLog "包含SQL操作: $($Script.Name)" -Level "SUCCESS"
            } else {
                $ContentIssues++
                Write-ValidationLog "缺少SQL操作: $($Script.Name)" -Level "WARNING"
            }
        }

        if ($NamingIssues -eq 0 -and $ContentIssues -eq 0) {
            Write-ValidationLog "所有迁移脚本验证通过" -Level "SUCCESS"
        }
    } else {
        Write-ValidationLog "迁移脚本目录不存在: $MigrationPath" -Level "CRITICAL"
    }
}

# 4. 验证Entity映射
function Test-EntityMapping {
    Write-Host "`n🗃️ 验证Entity映射..." -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    $ProjectRoot = Split-Path -Parent (Split-Path -Parent $ScriptRoot)
    $EntityPath = Join-Path $ProjectRoot "microservices\microservices-common\src\main\java\net\lab1024\sa\common\entity"

    if (Test-Path $EntityPath) {
        $EntityFiles = Get-ChildItem -Path $EntityPath -Filter "*Entity.java" -Recurse
        $EntityCount = $EntityFiles.Count

        Write-ValidationLog "找到 $EntityCount 个Entity类" -Level "SUCCESS"

        # 检查关键Entity
        $CriticalEntities = @(
            "UserEntity",
            "RoleEntity",
            "MenuEntity",
            "AreaEntity",
            "DeviceEntity"
        )

        foreach ($EntityName in $CriticalEntities) {
            $Found = $EntityFiles | Where-Object { $_.Name -eq "$EntityName.java" }
            if ($Found) {
                Write-ValidationLog "关键Entity存在: $EntityName" -Level "SUCCESS"
            } else {
                Write-ValidationLog "关键Entity缺失: $EntityName" -Level "WARNING"
            }
        }

        # 验证Entity注解
        $AnnotationIssues = 0
        foreach ($Entity in $EntityFiles) {
            $Content = Get-Content $Entity.FullName -Raw

            if ($Content -match "@TableName") {
                Write-ValidationLog "包含@TableName注解: $($Entity.Name)" -Level "SUCCESS"
            } else {
                $AnnotationIssues++
                Write-ValidationLog "缺少@TableName注解: $($Entity.Name)" -Level "WARNING"
            }
        }

    } else {
        Write-ValidationLog "Entity目录不存在: $EntityPath" -Level "CRITICAL"
    }
}

# 5. 验证数据库工具
function Test-DatabaseTools {
    Write-Host "`n🔧 验证数据库工具..." -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    # 检查MySQL路径检测
    if ($Global:MySQLExecutable) {
        Write-ValidationLog "MySQL客户端路径: $Global:MySQLExecutable" -Level "SUCCESS"

        # 测试版本
        try {
            $Version = & $Global:MySQLExecutable --version 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-ValidationLog "MySQL版本检测成功: $Version" -Level "SUCCESS"
            }
        } catch {
            Write-ValidationLog "MySQL版本检测失败" -Level "WARNING"
        }
    } else {
        Write-ValidationLog "未找到MySQL命令行工具" -Level "WARNING"
    }

    if ($Global:MysqldumpExecutable) {
        Write-ValidationLog "mysqldump工具路径: $Global:MysqldumpExecutable" -Level "SUCCESS"
    } else {
        Write-ValidationLog "未找到mysqldump工具" -Level "WARNING"
    }

    # 检查PowerShell工具
    $RequiredTools = @(
        "database-utilities.ps1",
        "migration-automation.ps1",
        "config/migration-config.json"
    )

    foreach ($Tool in $RequiredTools) {
        $ToolPath = Join-Path $ScriptRoot $Tool
        if (Test-Path $ToolPath) {
            Write-ValidationLog "工具文件存在: $Tool" -Level "SUCCESS"
        } else {
            Write-ValidationLog "工具文件缺失: $Tool" -Level "ERROR"
        }
    }
}

# 6. 验证配置文件
function Test-ConfigurationFiles {
    Write-Host "`n📋 验证配置文件..." -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    $ConfigPath = Join-Path $ScriptRoot "config\migration-config.json"

    if (Test-Path $ConfigPath) {
        try {
            $Config = Get-Content $ConfigPath -Raw | ConvertFrom-Json
            Write-ValidationLog "配置文件格式正确: JSON解析成功" -Level "SUCCESS"

            # 验证关键配置项
            if ($Config.project.name) {
                Write-ValidationLog "项目名称: $($Config.project.name)" -Level "SUCCESS"
            } else {
                Write-ValidationLog "项目名称配置缺失" -Level "ERROR"
            }

            if ($Config.microservices -and $Config.microservices.Count -gt 0) {
                Write-ValidationLog "微服务配置: $($Config.microservices.Count) 个服务" -Level "SUCCESS"
            } else {
                Write-ValidationLog "微服务配置缺失或为空" -Level "ERROR"
            }

            if ($Config.environment.dev -and $Config.environment.test -and $Config.environment.prod) {
                Write-ValidationLog "环境配置完整: dev/test/prod" -Level "SUCCESS"
            } else {
                Write-ValidationLog "环境配置不完整" -Level "WARNING"
            }

        } catch {
            Write-ValidationLog "配置文件JSON解析失败" -Level "ERROR"
        }
    } else {
        Write-ValidationLog "配置文件不存在: $ConfigPath" -Level "CRITICAL"
    }
}

# 7. 数据库连接测试
function Test-DatabaseConnection {
    Write-Host "`n🔗 数据库连接测试..." -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    $ConfigPath = Join-Path $ScriptRoot "config\migration-config.json"

    if (Test-Path $ConfigPath) {
        try {
            $Config = Get-Content $ConfigPath -Raw | ConvertFrom-Json
            $DbConfig = $Config.environment.dev

            # 使用环境变量或默认值
            $Host = if ($env:MYSQL_HOST) { $env:MYSQL_HOST } else { $DbConfig.mysql_host }
            $Port = if ($env:MYSQL_PORT) { $env:MYSQL_PORT } else { $DbConfig.mysql_port }
            $Username = if ($env:MYSQL_USERNAME) { $env:MYSQL_USERNAME } else { $DbConfig.mysql_username }
            $Password = if ($env:MYSQL_PASSWORD) { $env:MYSQL_PASSWORD } else { $DbConfig.mysql_password }
            $Database = if ($env:MYSQL_DATABASE) { $env:MYSQL_DATABASE } else { $DbConfig.mysql_database }

            Write-Host "连接信息: $Host:$Port -> $Database" -ForegroundColor Yellow

            $ConnectionResult = Test-DatabaseConnection -Host $Host -Port $Port -Username $Username -Password $Password -Database $Database

            if ($ConnectionResult) {
                Write-ValidationLog "数据库连接测试成功" -Level "SUCCESS"

                # 进行健康检查
                $HealthStatus = Test-DatabaseHealth -Host $Host -Port $Port -Username $Username -Password $Password -Database $Database

                if ($HealthStatus.Version -ne "Unknown") {
                    Write-ValidationLog "数据库版本: $($HealthStatus.Version)" -Level "SUCCESS"
                }

                if ($HealthStatus.FlywayTable) {
                    Write-ValidationLog "Flyway历史表存在" -Level "SUCCESS"
                } else {
                    Write-ValidationLog "Flyway历史表不存在（首次运行正常）" -Level "WARNING"
                }
            } else {
                Write-ValidationLog "数据库连接测试失败" -Level "ERROR"
                Write-Host "💡 请检查:" -ForegroundColor Yellow
                Write-Host "   1. MySQL服务是否启动" -ForegroundColor Yellow
                Write-Host "   2. 数据库连接参数是否正确" -ForegroundColor Yellow
                Write-Host "   3. 用户权限是否足够" -ForegroundColor Yellow
            }

        } catch {
            Write-ValidationLog "配置文件读取失败: $($_.Exception.Message)" -Level "ERROR"
        }
    } else {
        Write-ValidationLog "跳过数据库连接测试（配置文件不存在）" -Level "WARNING"
    }
}

# 8. 生成验证报告
function New-ValidationReport {
    Write-Host "`n📊 验证报告" -ForegroundColor Cyan
    Write-Host "=" * 50 -ForegroundColor Gray

    $SuccessRate = if ($ValidationResults.TotalChecks -gt 0) {
        [math]::Round($ValidationResults.PassedChecks / $ValidationResults.TotalChecks * 100, 1)
    } else { 0 }

    Write-Host "总检查项目: $($ValidationResults.TotalChecks)" -ForegroundColor White
    Write-Host "通过检查: $($ValidationResults.PassedChecks)" -ForegroundColor Green
    Write-Host "失败检查: $($ValidationResults.FailedChecks)" -ForegroundColor Red
    Write-Host "警告项目: $($ValidationResults.Warnings)" -ForegroundColor Yellow
    Write-Host "成功率: $SuccessRate%" -ForegroundColor $(if ($SuccessRate -ge 90) { "Green" } elseif ($SuccessRate -ge 70) { "Yellow" } else { "Red" })

    if ($ValidationResults.CriticalIssues.Count -gt 0) {
        Write-Host "`n🚨 严重问题:" -ForegroundColor Red
        foreach ($Issue in $ValidationResults.CriticalIssues) {
            Write-Host "   • $Issue" -ForegroundColor Red
        }
    }

    # 总体评估
    Write-Host "`n🎯 总体评估:" -ForegroundColor Cyan
    if ($ValidationResults.FailedChecks -eq 0) {
        Write-Host "   ✅ 数据库迁移设置验证通过，可以安全执行迁移" -ForegroundColor Green
    } elseif ($ValidationResults.CriticalIssues.Count -eq 0) {
        Write-Host "   ⚠️ 发现一些问题，建议修复后执行迁移" -ForegroundColor Yellow
    } else {
        Write-Host "   ❌ 存在严重问题，必须修复后才能执行迁移" -ForegroundColor Red
    }

    # 保存报告到文件
    $ReportPath = Join-Path $ScriptRoot "logs\validation-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
    if (!(Test-Path (Split-Path $ReportPath))) {
        New-Item -ItemType Directory -Path (Split-Path $ReportPath) -Force | Out-Null
    }

    $ReportContent = @"
IOE-DREAM 数据库迁移设置验证报告
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

验证统计:
- 总检查项目: $($ValidationResults.TotalChecks)
- 通过检查: $($ValidationResults.PassedChecks)
- 失败检查: $($ValidationResults.FailedChecks)
- 警告项目: $($ValidationResults.Warnings)
- 成功率: $SuccessRate%

$($ValidationResults.CriticalIssues.Count -gt 0 ? "严重问题:`r`n" + ($ValidationResults.CriticalIssues -join "`r`n") : "无严重问题")
"@

    $ReportContent | Out-File -FilePath $ReportPath -Encoding UTF8
    Write-Host "`n📄 详细报告已保存: $ReportPath" -ForegroundColor Cyan
}

# 主执行流程
function Main {
    Write-Host "🚀 IOE-DREAM 数据库迁移设置验证开始..." -ForegroundColor Green
    Write-Host "⏰ 开始时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    Write-Host ""

    try {
        Test-ProjectStructure
        Test-FlywayConfiguration
        Test-MigrationScripts
        Test-EntityMapping
        Test-DatabaseTools
        Test-ConfigurationFiles
        Test-DatabaseConnection
        New-ValidationReport

        Write-Host "`n🎉 验证完成!" -ForegroundColor Green

        # 根据结果设置退出代码
        if ($ValidationResults.CriticalIssues.Count -gt 0) {
            Write-Host "🚨 发现严重问题，请修复后重新运行" -ForegroundColor Red
            exit 1
        } elseif ($ValidationResults.FailedChecks -gt 0) {
            Write-Host "⚠️ 发现一些问题，建议修复" -ForegroundColor Yellow
            exit 2
        } else {
            Write-Host "✅ 验证通过，数据库迁移设置准确完整" -ForegroundColor Green
            exit 0
        }

    } catch {
        Write-Host "❌ 验证过程中发生异常: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "📍 异常位置: $($_.InvocationInfo.ScriptLineNumber):$($_.InvocationInfo.OffsetInLine)" -ForegroundColor Red
        exit 3
    }
}

# 执行主函数
Main