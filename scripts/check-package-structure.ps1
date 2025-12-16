# 包目录结构检查脚本
# Package Structure Check Script
# 用于检查IOE-DREAM项目的包目录结构合规性

param(
    [string]$ProjectPath = ".",
    [switch]$Verbose,
    [switch]$JsonOutput,
    [string]$OutputFile = "",
    [ValidateSet("all", "duplicate-packages", "entity-management", "manager-standards", "package-consistency")]
    [string]$CheckType = "all"
)

# 脚本配置
$ErrorActionPreference = "Stop"
$ProgressPreference = "Continue"

Write-Host "🔍 IOE-DREAM包目录结构检查工具" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

if ($JsonOutput) {
    Write-Host "📄 输出模式: JSON格式" -ForegroundColor Yellow
} else {
    Write-Host "📊 输出模式: 控制台格式" -ForegroundColor Yellow
}

Write-Host "📁 项目路径: $ProjectPath" -ForegroundColor Cyan
Write-Host "🔍 检查类型: $CheckType" -ForegroundColor Cyan
Write-Host ""

# 验证项目路径
if (!(Test-Path $ProjectPath)) {
    Write-Host "❌ 错误：项目路径不存在 - $ProjectPath" -ForegroundColor Red
    exit 1
}

# 转换为绝对路径
$ProjectPath = Resolve-Path $ProjectPath
$MicroservicesPath = Join-Path $ProjectPath "microservices"

if (!(Test-Path $MicroservicesPath)) {
    Write-Host "❌ 错误：未找到microservices目录" -ForegroundColor Red
    exit 1
}

# 初始化检查结果
$CheckResult = @{
    Timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    ProjectPath = $ProjectPath
    CheckType = $CheckType
    Summary = @{
        TotalFiles = 0
        IssuesFound = 0
        Errors = 0
        Warnings = 0
        Info = 0
    }
    Results = @()
}

# 主要执行函数
function Main {
    try {
        Write-Host "📊 开始包结构合规性检查..." -ForegroundColor Green

        # 扫描项目文件
        Scan-ProjectFiles

        # 执行各项检查
        switch ($CheckType) {
            "all" {
                Check-DuplicatePackages
                Check-EntityManagement
                Check-ManagerStandards
                Check-PackageConsistency
            }
            "duplicate-packages" {
                Check-DuplicatePackages
            }
            "entity-management" {
                Check-EntityManagement
            }
            "manager-standards" {
                Check-ManagerStandards
            }
            "package-consistency" {
                Check-PackageConsistency
            }
        }

        # 输出结果
        if ($JsonOutput) {
            Output-JsonResults
        } else {
            Output-ConsoleResults
        }

        # 保存结果到文件
        if ($OutputFile) {
            Save-ResultsToFile
        }

        # 返回适当的退出码
        if ($CheckResult.Summary.Errors -gt 0) {
            Write-Host "`n❌ 检查发现错误问题！" -ForegroundColor Red
            exit 1
        } elseif ($CheckResult.Summary.IssuesFound -gt 0) {
            Write-Host "`n⚠️ 检查发现问题，建议修复！" -ForegroundColor Yellow
            exit 2
        } else {
            Write-Host "`n✅ 包结构检查通过！" -ForegroundColor Green
            exit 0
        }

    } catch {
        Write-Host "❌ 检查执行失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "📍 错误位置: $($_.InvocationInfo.ScriptLineNumber)" -ForegroundColor Red
        exit 1
    }
}

# 扫描项目文件
function Scan-ProjectFiles {
    Write-Host "🔍 扫描项目文件..." -ForegroundColor Yellow

    $global:JavaFiles = Get-ChildItem -Path $MicroservicesPath -Recurse -Filter "*.java"
    $global:EntityFiles = $global:JavaFiles | Where-Object { $_.Name -match "Entity\.java$" }
    $global:ManagerFiles = $global:JavaFiles | Where-Object { $_.Name -match "Manager.*\.java$" }
    $global:DaoFiles = $global:JavaFiles | Where-Object { $_.Name -match "Dao\.java$" }

    $CheckResult.Summary.TotalFiles = $global:JavaFiles.Count

    Write-Host "  📄 Java文件: $($global:JavaFiles.Count)" -ForegroundColor White
    Write-Host "  🏗️ Entity文件: $($global:EntityFiles.Count)" -ForegroundColor White
    Write-Host "  ⚙️ Manager文件: $($global:ManagerFiles.Count)" -ForegroundColor White
    Write-Host "  🗄️ DAO文件: $($global:DaoFiles.Count)" -ForegroundColor White
}

# 检查重复包名
function Check-DuplicatePackages {
    Write-Host "1️⃣ 检查重复包名..." -ForegroundColor Yellow

    $duplicateIssues = @()

    foreach ($file in $global:JavaFiles) {
        try {
            $content = Get-Content $file.FullName -Raw

            # 检查重复包名模式
            if ($content -match 'package\s+net\.lab1024\.sa\.(\w+)\.(\w+)\.(\w+)') {
                $serviceName = $matches[1]
                $subPackage = $matches[2]
                $subpackageName = $matches[3]

                # 检查是否为重复包名 (service.service.xxx 模式)
                if ($serviceName -eq $subPackage) {
                    $issue = @{
                        Type = "DuplicatePackage"
                        Severity = "Error"
                        File = $file.FullName
                        RelativePath = $file.FullName.Replace($ProjectPath, "")
                        Package = $matches[0]
                        Service = $serviceName
                        SubPackage = $subPackage
                        SubpackageName = $subpackageName
                        Corrected = "net.lab1024.sa.$serviceName.$subpackageName"
                        Description = "发现重复包名，建议修复为: $($issue.Corrected)"
                        LineNumber = 1
                    }

                    $duplicateIssues += $issue
                    $CheckResult.Summary.Errors++
                    $CheckResult.Summary.IssuesFound++
                }
            }
        } catch {
            if ($Verbose) {
                Write-Host "  ⚠️ 处理文件失败: $($file.Name)" -ForegroundColor Yellow
            }
            $CheckResult.Summary.Warnings++
        }
    }

    $CheckResult.Results += @{
        CheckType = "DuplicatePackages"
        Status = if ($duplicateIssues.Count -eq 0) { "Pass" } else { "Fail" }
        Issues = $duplicateIssues
        Summary = "发现 $($duplicateIssues.Count) 个重复包名问题"
    }

    Write-Host "  📊 重复包名: $($duplicateIssues.Count) 个" -ForegroundColor $(if ($duplicateIssues.Count -eq 0) { "Green" } else { "Red" })
}

# 检查Entity管理
function Check-EntityManagement {
    Write-Host "2️⃣ 检查Entity管理..." -ForegroundColor Yellow

    $entityIssues = @()

    foreach ($file in $global:EntityFiles) {
        try {
            # 检查是否在业务微服务中（应该移到公共模块）
            if ($file.FullName -match 'ioedream-(\w+)-service' -and
                $file.FullName -notmatch 'microservices-common') {

                $entityName = $file.BaseName
                $serviceName = $matches[1]
                $targetModule = Determine-TargetModule -EntityName $entityName

                # 检查是否在正确的业务模块包中（临时允许）
                if ($file.FullName -notmatch "$serviceName\\entity" -and
                    $file.FullName -notmatch "$serviceName\\$serviceName\\entity") {

                    $issue = @{
                        Type = "EntityLocationIssue"
                        Severity = "Warning"
                        File = $file.FullName
                        RelativePath = $file.FullName.Replace($ProjectPath, "")
                        EntityName = $entityName
                        ServiceName = $serviceName
                        Issue = "Entity位置不规范，建议统一管理"
                        TargetModule = $targetModule
                        TargetPath = "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/$targetModule/entity"
                        Description = "Entity应在公共模块中统一管理，当前分散在业务服务中"
                        LineNumber = 1
                    }

                    $entityIssues += $issue
                    $CheckResult.Summary.Warnings++
                    $CheckResult.Summary.IssuesFound++
                }

                # 检查重复包名中的Entity（严重问题）
                if ($file.FullName -match "\\$serviceName\\$serviceName\\entity") {
                    $issue = @{
                        Type = "EntityInDuplicatePackage"
                        Severity = "Error"
                        File = $file.FullName
                        RelativePath = $file.FullName.Replace($ProjectPath, "")
                        EntityName = $entityName
                        ServiceName = $serviceName
                        Issue = "Entity位于重复包名中"
                        CurrentPackage = "$serviceName.$serviceName.entity"
                        CorrectedPackage = "$serviceName.entity"
                        Description = "Entity在重复包名中，需要移动并修复包名"
                        LineNumber = 1
                    }

                    $entityIssues += $issue
                    $CheckResult.Summary.Errors++
                    $CheckResult.Summary.IssuesFound++
                }
            }
        } catch {
            if ($Verbose) {
                Write-Host "  ⚠️ 处理Entity文件失败: $($file.Name)" -ForegroundColor Yellow
            }
            $CheckResult.Summary.Warnings++
        }
    }

    $CheckResult.Results += @{
        CheckType = "EntityManagement"
        Status = if ($entityIssues.Count -eq 0) { "Pass" } else { "Fail" }
        Issues = $entityIssues
        Summary = "发现 $($entityIssues.Count) 个Entity管理问题"
    }

    Write-Host "  📊 Entity管理: $($entityIssues.Count) 个问题" -ForegroundColor $(if ($entityIssues.Count -eq 0) { "Green" } else { "Red" })
}

# 检查Manager规范
function Check-ManagerStandards {
    Write-Host "3️⃣ 检查Manager规范..." -ForegroundColor Yellow

    $managerIssues = @()

    foreach ($file in $global:ManagerFiles) {
        try {
            $content = Get-Content $file.FullName
            $fileViolations = @()

            # 检查每一行
            for ($i = 0; $i -lt $content.Count; $i++) {
                $line = $content[$i].Trim()
                $lineNumber = $i + 1

                # 跳过注释行
                if ($line.StartsWith("//") -or $line.StartsWith("/*") -or $line.StartsWith("*")) {
                    continue
                }

                # 检查禁止的Spring注解
                if ($line -match '@(Component|Service|Repository)') {
                    $fileViolations += @{
                        LineNumber = $lineNumber
                        Issue = "Manager类禁止使用Spring注解: $($matches[1])"
                        Severity = "Error"
                        Type = "SpringAnnotation"
                    }
                }

                # 检查字段注入
                if ($line -match '@(Resource|Autowired)') {
                    $fileViolations += @{
                        LineNumber = $lineNumber
                        Issue = "Manager类应使用构造函数注入，禁止字段注入"
                        Severity = "Warning"
                        Type = "FieldInjection"
                    }
                }

                # 检查事务注解
                if ($line -match '@Transactional') {
                    $fileViolations += @{
                        LineNumber = $lineNumber
                        Issue = "Manager类不应管理事务，事务应在Service层处理"
                        Severity = "Warning"
                        Type = "TransactionAnnotation"
                    }
                }
            }

            # 如果发现问题，记录
            if ($fileViolations.Count -gt 0) {
                $issue = @{
                    Type = "ManagerStandards"
                    Severity = if ($fileViolations | Where-Object { $_.Severity -eq "Error" }) { "Error" } else { "Warning" }
                    File = $file.FullName
                    RelativePath = $file.FullName.Replace($ProjectPath, "")
                    FileName = $file.Name
                    Violations = $fileViolations
                    Issue = "Manager规范问题"
                    Description = "发现 $($fileViolations.Count) 个Manager规范问题"
                }

                $managerIssues += $issue

                # 统计问题
                foreach ($violation in $fileViolations) {
                    if ($violation.Severity -eq "Error") {
                        $CheckResult.Summary.Errors++
                    } else {
                        $CheckResult.Summary.Warnings++
                    }
                }
                $CheckResult.Summary.IssuesFound += $fileViolations.Count
            }
        } catch {
            if ($Verbose) {
                Write-Host "  ⚠️ 处理Manager文件失败: $($file.Name)" -ForegroundColor Yellow
            }
            $CheckResult.Summary.Warnings++
        }
    }

    $CheckResult.Results += @{
        CheckType = "ManagerStandards"
        Status = if ($managerIssues.Count -eq 0) { "Pass" } else { "Fail" }
        Issues = $managerIssues
        Summary = "发现 $($managerIssues.Count) 个Manager文件存在规范问题"
    }

    Write-Host "  📊 Manager规范: $($managerIssues.Count) 个文件存在问题" -ForegroundColor $(if ($managerIssues.Count -eq 0) { "Green" } else { "Red" })
}

# 检查包一致性
function Check-PackageConsistency {
    Write-Host "4️⃣ 检查包一致性..." -ForegroundColor Yellow

    $consistencyIssues = @()

    # 检查微服务包结构一致性
    $serviceDirs = Get-ChildItem -Path $MicroservicesPath -Directory | Where-Object { $_.Name -match "^ioedream-.*-service$" }

    foreach ($serviceDir in $serviceDirs) {
        $serviceName = $serviceDir.Name.Replace("-service", "")
        $serviceJavaPath = Join-Path $serviceDir.FullName "src/main/java/net/lab1024/sa"

        if (!(Test-Path $serviceJavaPath)) {
            $issue = @{
                Type = "MissingJavaPackage"
                Severity = "Error"
                Service = $serviceName
                Issue = "缺少标准Java包结构"
                Description = "服务缺少标准包结构: net/lab1024/sa"
                ExpectedPath = "src/main/java/net/lab1024/sa"
            }

            $consistencyIssues += $issue
            $CheckResult.Summary.Errors++
            $CheckResult.Summary.IssuesFound++
            continue
        }

        # 检查标准包目录是否存在
        $expectedPackages = @("controller", "service", "dao", "domain")
        $servicePackagePath = Join-Path $serviceJavaPath $serviceName

        foreach ($package in $expectedPackages) {
            $packagePath = Join-Path $servicePackagePath $package
            if (!(Test-Path $packagePath)) {
                $issue = @{
                    Type = "MissingStandardPackage"
                    Severity = "Warning"
                    Service = $serviceName
                    Package = $package
                    Issue = "缺少标准包目录"
                    Description = "服务缺少标准包目录: $package"
                    ExpectedPath = "$serviceName/$package"
                }

                $consistencyIssues += $issue
                $CheckResult.Summary.Warnings++
                $CheckResult.Summary.IssuesFound++
            }
        }
    }

    $CheckResult.Results += @{
        CheckType = "PackageConsistency"
        Status = if ($consistencyIssues.Count -eq 0) { "Pass" } else { "Fail" }
        Issues = $consistencyIssues
        Summary = "发现 $($consistencyIssues.Count) 个包一致性问题"
    }

    Write-Host "  📊 包一致性: $($consistencyIssues.Count) 个问题" -ForegroundColor $(if ($consistencyIssues.Count -eq 0) { "Green" } else { "Red" })
}

# 确定Entity的目标模块
function Determine-TargetModule {
    param([string]$EntityName)

    switch -Regex ($EntityName) {
        '^(User|Department|Area|Device|Employee)' { return "organization" }
        '^Access' { return "access" }
        '^Consume' { return "consume" }
        '^Attendance|WorkShift' { return "attendance" }
        '^Video' { return "video" }
        '^Visitor' { return "visitor" }
        '^(Dict|DictType|DictData|Config)' { return "system" }
        '^(Auth|Permission|Role|UserRole|Menu|RoleMenu)' { return "rbac" }
        '^(Audit|Log|UserSession)' { return "auth" }
        '^(Notification|NotificationConfig|NotificationTemplate)' { return "notification" }
        '^(ScheduledJob|Scheduler)' { return "scheduler" }
        default {
            if ($Verbose) {
                Write-Host "  ⚠️ 未知Entity类型，默认归入core模块: $EntityName" -ForegroundColor Yellow
            }
            return "core"
        }
    }
}

# 输出控制台结果
function Output-ConsoleResults {
    Write-Host ""
    Write-Host "📊 检查结果详情:" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan

    foreach ($result in $CheckResult.Results) {
        Write-Host ""
        Write-Host "🔍 $($result.CheckType): $($result.Status)" -ForegroundColor $(if ($result.Status -eq "Pass") { "Green" } else { "Red" })
        Write-Host "📝 $($result.Summary)" -ForegroundColor White

        if ($result.Issues.Count -gt 0) {
            foreach ($issue in $result.Issues) {
                $color = switch ($issue.Severity) {
                    "Error" { "Red" }
                    "Warning" { "Yellow" }
                    default { "White" }
                }

                Write-Host "  ❌ $($issue.RelativePath)" -ForegroundColor $color

                if ($issue.EntityName) {
                    Write-Host "     Entity: $($issue.EntityName)" -ForegroundColor Gray
                }

                if ($issue.Violations) {
                    foreach ($violation in $issue.Violations) {
                        $violationColor = switch ($violation.Severity) {
                            "Error" { "Red" }
                            "Warning" { "Yellow" }
                            default { "Gray" }
                        }
                        Write-Host "     行$($violation.LineNumber): $($violation.Issue)" -ForegroundColor $violationColor
                    }
                } else {
                    Write-Host "     问题描述: $($issue.Description)" -ForegroundColor Gray
                }
            }
        }
    }

    # 显示汇总统计
    Write-Host ""
    Write-Host "📊 汇总统计:" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "  📄 总文件数: $($CheckResult.Summary.TotalFiles)" -ForegroundColor White
    Write-Host "  🚨 问题总数: $($CheckResult.Summary.IssuesFound)" -ForegroundColor $(if ($CheckResult.Summary.IssuesFound -eq 0) { "Green" } else { "Red" })
    Write-Host "  ❌ 错误数: $($CheckResult.Summary.Errors)" -ForegroundColor $(if ($CheckResult.Summary.Errors -eq 0) { "Green" } else { "Red" })
    Write-Host "  ⚠️ 警告数: $($CheckResult.Summary.Warnings)" -ForegroundColor $(if ($CheckResult.Summary.Warnings -eq 0) { "Green" } else { "Yellow" })
}

# 输出JSON结果
function Output-JsonResults {
    $json = $CheckResult | ConvertTo-Json -Depth 4
    Write-Host $json
}

# 保存结果到文件
function Save-ResultsToFile {
    try {
        $resultPath = if ($OutputFile) { $OutputFile } else { Join-Path $ProjectPath "package-structure-check-result.json" }

        if ($JsonOutput) {
            $CheckResult | ConvertTo-Json -Depth 4 | Out-File -FilePath $resultPath -Encoding UTF8
        } else {
            # 生成Markdown格式的报告
            Generate-MarkdownReport | Out-File -FilePath $resultPath -Encoding UTF8
        }

        Write-Host "📄 检查结果已保存: $resultPath" -ForegroundColor Green
    } catch {
        Write-Host "❌ 保存结果失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 生成Markdown报告
function Generate-MarkdownReport {
    $report = @()
    $report += "# IOE-DREAM包目录结构检查报告"
    $report += ""
    $report += "**检查时间**: $($CheckResult.Timestamp)"
    $report += "**项目路径**: $($CheckResult.ProjectPath)"
    $report += "**检查类型**: $($CheckResult.CheckType)"
    $report += ""
    $report += "## 检查摘要"
    $report += ""
    $report += "| 指标 | 数量 |"
    $report += "|------|------|"
    $report += "| 📄 总文件数 | $($CheckResult.Summary.TotalFiles) |"
    $report += "| 🚨 问题总数 | $($CheckResult.Summary.IssuesFound) |"
    $report += "| ❌ 错误数 | $($CheckResult.Summary.Errors) |"
    $report += "| ⚠️ 警告数 | $($CheckResult.Summary.Warnings) |"
    $report += ""
    $report += "## 详细结果"
    $report += ""

    foreach ($result in $CheckResult.Results) {
        $statusIcon = if ($result.Status -eq "Pass") { "✅" } else { "❌" }
        $report += "### $($result.CheckType) $statusIcon"
        $report += ""
        $report += "**状态**: $($result.Status)"
        $report += ""
        $report += "**摘要**: $($result.Summary)"
        $report += ""

        if ($result.Issues.Count -gt 0) {
            $report += "| 文件 | 问题 | 严重程度 |"
            $report += "|------|------|----------|"

            foreach ($issue in $result.Issues) {
                $severityIcon = switch ($issue.Severity) {
                    "Error" { "❌" }
                    "Warning" { "⚠️" }
                    default { "ℹ️" }
                }

                if ($issue.Violations) {
                    foreach ($violation in $issue.Violations) {
                        $report += "| $($issue.RelativePath) | 行$($violation.LineNumber): $($violation.Issue) | $severityIcon $($violation.Severity) |"
                    }
                } else {
                    $report += "| $($issue.RelativePath) | $($issue.Description) | $severityIcon $($issue.Severity) |"
                }
            }
            $report += ""
        }
    }

    $report += "---"
    $report += "**报告生成时间**: $(Get-Date)"
    $report += "**生成工具**: IOE-DREAM包目录结构检查脚本"

    return $report -join "`n"
}

# 显示帮助信息
function Show-Help {
    Write-Host "IOE-DREAM包目录结构检查工具" -ForegroundColor Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor Cyan
    Write-Host "  .\check-package-structure.ps1 [参数]"
    Write-Host ""
    Write-Host "参数:" -ForegroundColor Cyan
    Write-Host "  -ProjectPath <路径>    项目根目录 (默认: 当前目录)"
    Write-Host "  -Verbose               显示详细信息"
    Write-Host "  -JsonOutput            输出JSON格式结果"
    Write-Host "  -OutputFile <文件>     保存结果到指定文件"
    Write-Host "  -CheckType <类型>      检查类型: all, duplicate-packages, entity-management, manager-standards, package-consistency"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Cyan
    Write-Host "  .\check-package-structure.ps1"
    Write-Host "  .\check-package-structure.ps1 -Verbose"
    Write-Host "  .\check-package-structure.ps1 -JsonOutput"
    Write-Host "  .\check-package-structure.ps1 -CheckType duplicate-packages"
    Write-Host "  .\check-package-structure.ps1 -OutputFile report.json -JsonOutput"
    Write-Host ""
}

# 检查是否请求帮助
if ($args -contains "-h" -or $args -contains "-help" -or $args -contains "--help") {
    Show-Help
    exit 0
}

# 执行主函数
Main