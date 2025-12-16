# 包目录结构修复脚本
# Package Structure Fix Script
# 用于修复IOE-DREAM项目的包目录结构问题

param(
    [string]$ProjectPath = ".",
    [switch]$DryRun,
    [switch]$FixAll,
    [switch]$CheckOnly,
    [string]$ServiceName = "",
    [ValidateSet("all", "duplicate-packages", "entity-management", "manager-standards")]
    [string]$FixType = "all"
)

# 脚本配置
$ErrorActionPreference = "Stop"
$ProgressPreference = "Continue"

Write-Host "🚀 IOE-DREAM包目录结构修复工具" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green

if ($CheckOnly) {
    Write-Host "🔍 检查模式：只检查不修复" -ForegroundColor Yellow
} elseif ($DryRun) {
    Write-Host "🧪 试运行模式：模拟修复过程" -ForegroundColor Yellow
} else {
    Write-Host "🔧 修复模式：执行实际修复操作" -ForegroundColor Yellow
}

Write-Host "📁 项目路径: $ProjectPath" -ForegroundColor Cyan
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

# 初始化统计信息
$Stats = @{
    DuplicatePackages = 0
    EntityIssues = 0
    ManagerIssues = 0
    FilesModified = 0
    Errors = 0
    Warnings = 0
}

# 主要执行函数
function Main {
    try {
        Write-Host "📊 开始包结构分析..." -ForegroundColor Green

        # 检测重复包名
        if ($FixType -eq "all" -or $FixType -eq "duplicate-packages") {
            Check-DuplicatePackages
        }

        # 检查Entity管理
        if ($FixType -eq "all" -or $FixType -eq "entity-management") {
            Check-EntityManagement
        }

        # 检查Manager规范
        if ($FixType -eq "all" -or $FixType -eq "manager-standards") {
            Check-ManagerStandards
        }

        # 显示统计结果
        Show-Statistics

        # 执行修复
        if ($FixAll -and !$CheckOnly -and !$DryRun) {
            Execute-Fixes
        }

        Write-Host "✅ 包结构检查/修复完成！" -ForegroundColor Green

    } catch {
        Write-Host "❌ 执行失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "📍 错误位置: $($_.InvocationInfo.ScriptLineNumber)" -ForegroundColor Red
        exit 1
    }
}

# 检测重复包名
function Check-DuplicatePackages {
    Write-Host "1️⃣ 检测重复包名问题..." -ForegroundColor Yellow

    $global:DuplicateIssues = @()
    $javaFiles = Get-ChildItem -Path $MicroservicesPath -Recurse -Filter "*.java"

    foreach ($file in $javaFiles) {
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
                        Package = $matches[0]
                        File = $file.FullName
                        Service = $serviceName
                        SubPackage = $subPackage
                        SubpackageName = $subpackageName
                        Corrected = "net.lab1024.sa.$serviceName.$subpackageName"
                        RelativePath = $file.FullName.Replace($ProjectPath, "")
                    }

                    $global:DuplicateIssues += $issue
                    $Stats.DuplicatePackages++

                    if (!$DryRun) {
                        Write-Host "  ❌ $($issue.RelativePath)" -ForegroundColor Red
                        Write-Host "     包名: $($issue.Package)" -ForegroundColor Gray
                        Write-Host "     建议: $($issue.Corrected)" -ForegroundColor Gray
                    }
                }
            }
        } catch {
            Write-Host "  ⚠️ 处理文件失败: $($file.Name)" -ForegroundColor Yellow
            $Stats.Warnings++
        }
    }

    Write-Host "  📊 发现重复包名问题: $($Stats.DuplicatePackages) 个" -ForegroundColor Cyan
}

# 检查Entity管理
function Check-EntityManagement {
    Write-Host "2️⃣ 检查Entity管理问题..." -ForegroundColor Yellow

    $global:EntityIssues = @()
    $entityFiles = Get-ChildItem -Path $MicroservicesPath -Recurse -Filter "*Entity.java"

    foreach ($file in $entityFiles) {
        try {
            # 检查是否在业务微服务中（应该移到公共模块）
            if ($file.FullName -match 'ioedream-(\w+)-service' -and
                $file.FullName -notmatch 'microservices-common') {

                $entityName = $file.BaseName
                $serviceName = $matches[1]
                $targetModule = Determine-TargetModule -EntityName $entityName

                $issue = @{
                    Type = "EntityInBusinessService"
                    EntityName = $entityName
                    FilePath = $file.FullName
                    ServiceName = $serviceName
                    Issue = "Entity在业务微服务中，应移至对应公共模块"
                    TargetModule = $targetModule
                    TargetPath = "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/$targetModule/entity"
                    RelativePath = $file.FullName.Replace($ProjectPath, "")
                }

                $global:EntityIssues += $issue
                $Stats.EntityIssues++

                if (!$DryRun) {
                    Write-Host "  ❌ $($issue.RelativePath)" -ForegroundColor Red
                    Write-Host "     Entity: $($issue.EntityName)" -ForegroundColor Gray
                    Write-Host "     目标模块: $($issue.TargetModule)" -ForegroundColor Gray
                    Write-Host "     目标路径: $($issue.TargetPath)" -ForegroundColor Gray
                }
            }
        } catch {
            Write-Host "  ⚠️ 处理Entity文件失败: $($file.Name)" -ForegroundColor Yellow
            $Stats.Warnings++
        }
    }

    Write-Host "  📊 发现Entity管理问题: $($Stats.EntityIssues) 个" -ForegroundColor Cyan
}

# 检查Manager规范
function Check-ManagerStandards {
    Write-Host "3️⃣ 检查Manager规范问题..." -ForegroundColor Yellow

    $global:ManagerIssues = @()
    $managerFiles = Get-ChildItem -Path $MicroservicesPath -Recurse -Filter "*Manager*.java"

    foreach ($file in $managerFiles) {
        try {
            $content = Get-Content $file.FullName
            $violations = @()

            # 检查每一行
            for ($i = 0; $i -lt $content.Count; $i++) {
                $line = $content[$i].Trim()
                $lineNumber = $i + 1

                # 检查禁止的Spring注解
                if ($line -match '@(Component|Service|Repository)') {
                    $violations += @{
                        LineNumber = $lineNumber
                        Issue = "Manager类禁止使用Spring注解: $($matches[1])"
                        Severity = "High"
                    }
                }

                # 检查字段注入
                if ($line -match '@(Resource|Autowired)') {
                    $violations += @{
                        LineNumber = $lineNumber
                        Issue = "Manager类应使用构造函数注入，禁止字段注入"
                        Severity = "Medium"
                    }
                }

                # 检查事务注解
                if ($line -match '@Transactional') {
                    $violations += @{
                        LineNumber = $lineNumber
                        Issue = "Manager类不应管理事务，事务应在Service层处理"
                        Severity = "Medium"
                    }
                }
            }

            # 如果发现问题，记录
            if ($violations.Count -gt 0) {
                $issue = @{
                    Type = "ManagerStandards"
                    FileName = $file.Name
                    FilePath = $file.FullName
                    Violations = $violations
                    RelativePath = $file.FullName.Replace($ProjectPath, "")
                }

                $global:ManagerIssues += $issue
                $Stats.ManagerIssues += $violations.Count

                if (!$DryRun) {
                    Write-Host "  ❌ $($issue.RelativePath)" -ForegroundColor Red
                    foreach ($violation in $violations) {
                        Write-Host "     行$($violation.LineNumber): $($violation.Issue)" -ForegroundColor Gray
                    }
                }
            }
        } catch {
            Write-Host "  ⚠️ 处理Manager文件失败: $($file.Name)" -ForegroundColor Yellow
            $Stats.Warnings++
        }
    }

    Write-Host "  📊 发现Manager规范问题: $($Stats.ManagerIssues) 个" -ForegroundColor Cyan
}

# 确定Entity的目标模块
function Determine-TargetModule {
    param([string]$EntityName)

    switch -Regex ($EntityName) {
        '^(User|Department|Area|Device)' { return "organization" }
        '^Access' { return "access" }
        '^Consume' { return "consume" }
        '^Attendance' { return "attendance" }
        '^Video' { return "video" }
        '^Visitor' { return "visitor" }
        '^(Dict|Menu|Config)' { return "system" }
        '^(Auth|Permission|Role)' { return "rbac" }
        '^(Audit|Log)' { return "audit" }
        '^(Notification|Message)' { return "notification" }
        default {
            Write-Host "  ⚠️ 未知Entity类型，默认归入core模块: $EntityName" -ForegroundColor Yellow
            return "core"
        }
    }
}

# 显示统计结果
function Show-Statistics {
    Write-Host ""
    Write-Host "📊 检查结果统计:" -ForegroundColor Cyan
    Write-Host "================================" -ForegroundColor Cyan
    Write-Host "  📦 重复包名问题: $($Stats.DuplicatePackages)" -ForegroundColor White
    Write-Host "  🏗️ Entity管理问题: $($Stats.EntityIssues)" -ForegroundColor White
    Write-Host "  ⚙️ Manager规范问题: $($Stats.ManagerIssues)" -ForegroundColor White
    Write-Host "  📁 修改文件数: $($Stats.FilesModified)" -ForegroundColor White
    Write-Host "  ❌ 错误数: $($Stats.Errors)" -ForegroundColor White
    Write-Host "  ⚠️ 警告数: $($Stats.Warnings)" -ForegroundColor White
    Write-Host ""

    $totalIssues = $Stats.DuplicatePackages + $Stats.EntityIssues + $Stats.ManagerIssues

    if ($totalIssues -eq 0) {
        Write-Host "✅ 包结构检查通过，未发现问题！" -ForegroundColor Green
    } else {
        Write-Host "🚨 发现 $totalIssues 个问题需要处理" -ForegroundColor Red

        if ($CheckOnly) {
            Write-Host ""
            Write-Host "💡 这是检查模式，使用 -FixAll 参数执行修复" -ForegroundColor Cyan
        } elseif ($DryRun) {
            Write-Host ""
            Write-Host "💡 这是试运行模式，使用 -FixAll 参数执行实际修复" -ForegroundColor Cyan
        }
    }
}

# 执行修复
function Execute-Fixes {
    Write-Host ""
    Write-Host "🔧 开始执行修复操作..." -ForegroundColor Green
    Write-Host "================================" -ForegroundColor Green

    try {
        # 修复重复包名
        if ($Stats.DuplicatePackages -gt 0) {
            Write-Host "📦 修复重复包名..." -ForegroundColor Yellow
            Fix-DuplicatePackages
        }

        # 生成Entity迁移方案
        if ($Stats.EntityIssues -gt 0) {
            Write-Host "🏗️ 生成Entity迁移方案..." -ForegroundColor Yellow
            Generate-EntityMigrationPlan
        }

        # 修复Manager规范
        if ($Stats.ManagerIssues -gt 0) {
            Write-Host "⚙️ 修复Manager规范..." -ForegroundColor Yellow
            Fix-ManagerStandards
        }

        Write-Host "✅ 所有修复操作完成！" -ForegroundColor Green

    } catch {
        Write-Host "❌ 修复过程中发生错误: $($_.Exception.Message)" -ForegroundColor Red
        $Stats.Errors++
        throw
    }
}

# 修复重复包名
function Fix-DuplicatePackages {
    foreach ($issue in $global:DuplicateIssues) {
        try {
            Write-Host "  修复: $($issue.Package)" -ForegroundColor White

            # 1. 重命名目录
            $oldDirPath = Split-Path $issue.File -Parent
            $newDirPath = $oldDirPath.Replace("\$($issue.SubPackage)\$($issue.SubpackageName)", "\$($issue.SubpackageName)")

            if ($oldDirPath -ne $newDirPath -and !(Test-Path $newDirPath)) {
                New-Item -Path $newDirPath -ItemType Directory -Force | Out-Null
                Write-Host "    创建目录: $newDirPath" -ForegroundColor Gray
            }

            # 2. 移动文件
            if ($oldDirPath -ne $newDirPath) {
                $newFilePath = Join-Path $newDirPath (Split-Path $issue.File -Leaf)
                Move-Item -Path $issue.File -Destination $newFilePath -Force
                Write-Host "    移动文件: $(Split-Path $issue.File -Leaf)" -ForegroundColor Gray
                $Stats.FilesModified++
            }

            # 3. 更新package声明
            if ($oldDirPath -ne $newDirPath) {
                Update-PackageDeclaration -FilePath $newFilePath -NewPackage $issue.Corrected
            }

        } catch {
            Write-Host "  ❌ 修复失败: $($issue.Package) - $($_.Exception.Message)" -ForegroundColor Red
            $Stats.Errors++
        }
    }
}

# 更新package声明
function Update-PackageDeclaration {
    param(
        [string]$FilePath,
        [string]$NewPackage
    )

    try {
        $content = Get-Content $FilePath -Raw

        # 更新package声明
        $content = $content -replace 'package\s+net\.lab1024\.sa\.[^;]+;', "package $NewPackage;"

        Set-Content -Path $FilePath -Value $content -NoNewline
        Write-Host "    更新package: $NewPackage" -ForegroundColor Gray
        $Stats.FilesModified++

    } catch {
        Write-Host "  ❌ 更新package声明失败: $FilePath - $($_.Exception.Message)" -ForegroundColor Red
        $Stats.Errors++
    }
}

# 生成Entity迁移方案
function Generate-EntityMigrationPlan {
    $migrationPlan = @{
        GeneratedAt = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
        TotalEntities = $Stats.EntityIssues
        MigrationSteps = @()
    }

    # 按目标模块分组
    $groupedEntities = $global:EntityIssues | Group-Object { $_.TargetModule }

    foreach ($group in $groupedEntities) {
        $step = @{
            StepNumber = $migrationPlan.MigrationSteps.Count + 1
            TargetModule = $group.Name
            TargetPath = "microservices/microservices-common-business/src/main/java/net/lab1024/sa/common/$($group.Name)/entity"
            Entities = @()
        }

        foreach ($issue in $group.Group) {
            $step.Entities += @{
                Name = $issue.EntityName
                SourcePath = $issue.FilePath
                RelativePath = $issue.RelativePath
            }
        }

        $migrationPlan.MigrationSteps += $step
    }

    # 保存迁移方案
    $planPath = Join-Path $ProjectPath "entity-migration-plan.json"
    $migrationPlan | ConvertTo-Json -Depth 4 | Out-File -FilePath $planPath -Encoding UTF8

    Write-Host "  📄 Entity迁移方案已保存: $planPath" -ForegroundColor Green
    Write-Host "  📊 总计 $($Stats.EntityIssues) 个Entity需要迁移" -ForegroundColor White
}

# 修复Manager规范
function Fix-ManagerStandards {
    foreach ($issue in $global:ManagerIssues) {
        try {
            Write-Host "  修复: $($issue.FileName)" -ForegroundColor White

            $content = Get-Content $issue.FilePath
            $modified = $false

            foreach ($violation in $issue.Violations) {
                $lineIndex = $violation.LineNumber - 1
                $originalLine = $content[$lineIndex]

                switch -Regex ($violation.Issue) {
                    "禁止使用Spring注解" {
                        # 移除Spring注解
                        $content[$lineIndex] = $originalLine -replace '@\w+', ''
                        $modified = $true
                        Write-Host "    移除Spring注解: 行$($violation.LineNumber)" -ForegroundColor Gray
                    }
                    "禁止字段注入" {
                        # 注释掉字段注入（需要手动改为构造函数注入）
                        $content[$lineIndex] = "// TODO: 改为构造函数注入 - " + $originalLine
                        $modified = $true
                        Write-Host "    标记字段注入: 行$($violation.LineNumber)" -ForegroundColor Gray
                    }
                    "不应管理事务" {
                        # 移除事务注解
                        $content[$lineIndex] = $originalLine -replace '@Transactional[^;]*;', ''
                        $modified = $true
                        Write-Host "    移除事务注解: 行$($violation.LineNumber)" -ForegroundColor Gray
                    }
                }
            }

            if ($modified) {
                Set-Content -Path $issue.FilePath -Value $content
                $Stats.FilesModified++
            }

        } catch {
            Write-Host "  ❌ 修复Manager规范失败: $($issue.FileName) - $($_.Exception.Message)" -ForegroundColor Red
            $Stats.Errors++
        }
    }
}

# 显示帮助信息
function Show-Help {
    Write-Host "IOE-DREAM包目录结构修复工具" -ForegroundColor Green
    Write-Host ""
    Write-Host "用法:" -ForegroundColor Cyan
    Write-Host "  .\fix-package-structure.ps1 [参数]"
    Write-Host ""
    Write-Host "参数:" -ForegroundColor Cyan
    Write-Host "  -ProjectPath <路径>    项目根目录 (默认: 当前目录)"
    Write-Host "  -DryRun               试运行模式，不执行实际修复"
    Write-Host "  -CheckOnly            仅检查，不生成修复方案"
    Write-Host "  -FixAll               执行所有修复操作"
    Write-Host "  -ServiceName <名称>    指定服务名称"
    Write-Host "  -FixType <类型>        修复类型: all, duplicate-packages, entity-management, manager-standards"
    Write-Host ""
    Write-Host "示例:" -ForegroundColor Cyan
    Write-Host "  .\fix-package-structure.ps1 -CheckOnly"
    Write-Host "  .\fix-package-structure.ps1 -DryRun"
    Write-Host "  .\fix-package-structure.ps1 -FixAll"
    Write-Host "  .\fix-package-structure.ps1 -FixType duplicate-packages -DryRun"
    Write-Host ""
}

# 检查是否请求帮助
if ($args -contains "-h" -or $args -contains "-help" -or $args -contains "--help") {
    Show-Help
    exit 0
}

# 执行主函数
Main