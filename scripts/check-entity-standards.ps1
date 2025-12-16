#!/usr/bin/env pwsh
<#
.SYNOPSIS
    IOE-DREAM Entity设计规范检查脚本

.DESCRIPTION
    检查项目中所有Entity类是否遵循设计规范：
    - Entity行数检查（≤400行上限）
    - Repository违规检查（禁止使用@Repository注解）
    - 重复Entity定义检查
    - 包结构规范检查

.PARAMETER ProjectPath
    项目根路径，默认为当前目录

.PARAMETER Fix
    是否自动修复Repository违规问题

.PARAMETER ReportPath
    报告输出路径，默认为当前目录的entity-standards-report.md

.PARAMETER Detailed
    是否输出详细检查信息

.EXAMPLE
    .\check-entity-standards.ps1

.EXAMPLE
    .\check-entity-standards.ps1 -Fix -Detailed

.EXAMPLE
    .\check-entity-standards.ps1 -ProjectPath "D:\IOE-DREAM" -ReportPath "reports\entity-report.md"

.NOTES
    作者: IOE-DREAM架构委员会
    版本: v1.0.0
    创建日期: 2025-12-16
#>

param(
    [Parameter(Mandatory=$false)]
    [string]$ProjectPath = ".",

    [Parameter(Mandatory=$false)]
    [switch]$Fix = $false,

    [Parameter(Mandatory=$false)]
    [string]$ReportPath = "entity-standards-report.md",

    [Parameter(Mandatory=$false)]
    [switch]$Detailed = $false
)

# 颜色配置
$Colors = @{
    Red = "Red"
    Yellow = "Yellow"
    Green = "Green"
    Cyan = "Cyan"
    White = "White"
}

# 结果存储
$Results = @{
    LargeEntities = @()
    RepositoryViolations = @()
    DuplicateEntities = @()
    PackageViolations = @()
    Summary = @{
        TotalEntities = 0
        TotalDaos = 0
        LargeEntityCount = 0
        HugeEntityCount = 0
        RepositoryViolationCount = 0
        DuplicateEntityCount = 0
        PackageViolationCount = 0
        ComplianceRate = 0
    }
}

Write-Host "🔍 IOE-DREAM Entity设计规范检查" -ForegroundColor $Colors.Cyan
Write-Host "================================" -ForegroundColor $Colors.Cyan
Write-Host "项目路径: $ProjectPath" -ForegroundColor $Colors.White
Write-Host "自动修复: $Fix" -ForegroundColor $Colors.White
Write-Host "详细输出: $Detailed" -ForegroundColor $Colors.White
Write-Host ""

# 函数：检查Entity文件
function Test-EntityStandards {
    param([string]$FilePath)

    try {
        $content = Get-Content $FilePath -Raw -Encoding UTF8
        $lines = ($content -split "`n").Count
        $relativePath = (Resolve-Path $FilePath -Relative).Replace("\", "/")

        # 基础信息
        $entityInfo = @{
            File = $FilePath
            RelativePath = $relativePath
            Lines = $lines
            Fields = 0
            HasBusinessLogic = $false
            HasStaticMethods = $false
            ExtendsBaseEntity = $false
            TableName = $false
            Status = "Unknown"
        }

        # 检查字段数量
        $fieldMatches = [regex]::Matches($content, 'private\s+\w+\s+\w+;')
        $entityInfo.Fields = $fieldMatches.Count

        # 检查是否继承BaseEntity
        if ($content -match 'extends\s+BaseEntity') {
            $entityInfo.ExtendsBaseEntity = $true
        }

        # 检查@TableName注解
        if ($content -match '@TableName') {
            $entityInfo.TableName = $true
        }

        # 检查业务逻辑方法
        if ($content -match 'public\s+\w+\s+\w+\([^)]*\)\s*\{') {
            $methodMatches = [regex]::Matches($content, 'public\s+\w+\s+\w+\([^)]*\)\s*\{')
            foreach ($match in $methodMatches) {
                $methodContent = $content.Substring($match.Index, [Math]::Min(500, $content.Length - $match.Index))
                if ($methodContent -match 'calculate|process|validate|format|parse|convert') {
                    $entityInfo.HasBusinessLogic = $true
                    break
                }
            }
        }

        # 检查静态方法
        if ($content -match 'public\s+static\s+\w+\s+\w+\(') {
            $entityInfo.HasStaticMethods = $true
        }

        # 判断状态
        if ($lines -gt 400) {
            $entityInfo.Status = "超大Entity"
            $Results.Summary.HugeEntityCount++
        }
        elseif ($lines -gt 200) {
            $entityInfo.Status = "大型Entity"
            $Results.Summary.LargeEntityCount++
        }
        elseif ($entityInfo.HasBusinessLogic -or $entityInfo.HasStaticMethods) {
            $entityInfo.Status = "包含业务逻辑"
        }
        elseif ($entityInfo.Fields -gt 30) {
            $entityInfo.Status = "字段过多"
        }
        elseif (-not $entityInfo.ExtendsBaseEntity) {
            $entityInfo.Status = "未继承BaseEntity"
        }
        elseif (-not $entityInfo.TableName) {
            $entityInfo.Status = "缺少@TableName"
        }
        else {
            $entityInfo.Status = "符合规范"
        }

        return $entityInfo

    } catch {
        Write-Warning "检查Entity文件失败: $FilePath - $($_.Exception.Message)"
        return $null
    }
}

# 函数：检查DAO文件
function Test-DaoStandards {
    param([string]$FilePath)

    try {
        $content = Get-Content $FilePath -Raw -Encoding UTF8
        $relativePath = (Resolve-Path $FilePath -Relative).Replace("\", "/")

        $daoInfo = @{
            File = $FilePath
            RelativePath = $relativePath
            HasRepositoryAnnotation = $false
            HasMapperAnnotation = $false
            ExtendsBaseMapper = $false
            Status = "Unknown"
        }

        # 检查注解
        if ($content -match '@Repository') {
            $daoInfo.HasRepositoryAnnotation = $true
        }

        if ($content -match '@Mapper') {
            $daoInfo.HasMapperAnnotation = $true
        }

        # 检查继承
        if ($content -match 'extends\s+BaseMapper') {
            $daoInfo.ExtendsBaseMapper = $true
        }

        # 判断状态
        if ($daoInfo.HasRepositoryAnnotation) {
            $daoInfo.Status = "违规使用@Repository"
        }
        elseif (-not $daoInfo.HasMapperAnnotation) {
            $daoInfo.Status = "缺少@Mapper注解"
        }
        elseif (-not $daoInfo.ExtendsBaseMapper) {
            $daoInfo.Status = "未继承BaseMapper"
        }
        else {
            $daoInfo.Status = "符合规范"
        }

        return $daoInfo

    } catch {
        Write-Warning "检查DAO文件失败: $FilePath - $($_.Exception.Message)"
        return $null
    }
}

# 函数：查找重复Entity
function Find-DuplicateEntities {
    param([array]$EntityFiles)

    $entityNames = @{}
    $duplicates = @()

    foreach ($file in $EntityFiles) {
        try {
            $content = Get-Content $file -Raw -Encoding UTF8
            if ($content -match 'class\s+(\w+Entity)\s+extends') {
                $entityName = $matches[1]
                $relativePath = (Resolve-Path $file -Relative).Replace("\", "/")

                if ($entityNames.ContainsKey($entityName)) {
                    $duplicates += @{
                        EntityName = $entityName
                        Files = @($entityNames[$entityName], $relativePath)
                    }
                } else {
                    $entityNames[$entityName] = $relativePath
                }
            }
        } catch {
            Write-Warning "解析Entity文件失败: $file"
        }
    }

    return $duplicates
}

# 函数：自动修复Repository违规
function Repair-RepositoryViolations {
    param([array]$Violations)

    $fixedCount = 0

    foreach ($violation in $Violations) {
        try {
            $content = Get-Content $violation.File -Raw -Encoding UTF8

            if ($content -match '@Repository') {
                $content = $content -replace '@Repository', '@Mapper'
                $content | Set-Content $violation.File -NoNewline -Encoding UTF8

                Write-Host "  ✅ 修复: $($violation.RelativePath)" -ForegroundColor $Colors.Green
                $fixedCount++
            }
        } catch {
            Write-Host "  ❌ 修复失败: $($violation.File) - $($_.Exception.Message)" -ForegroundColor $Colors.Red
        }
    }

    return $fixedCount
}

# 主检查逻辑
Write-Host "📁 扫描项目文件..." -ForegroundColor $Colors.White

# 查找Entity文件
$entityFiles = @()
Get-ChildItem -Path $ProjectPath -Recurse -Filter "*Entity.java" | ForEach-Object {
    if ($_.FullName -notmatch "target\\|build\\|node_modules\\") {
        $entityFiles += $_.FullName
    }
}

# 查找DAO文件
$daoFiles = @()
Get-ChildItem -Path $ProjectPath -Recurse -Filter "*Dao.java" | ForEach-Object {
    if ($_.FullName -notmatch "target\\|build\\|node_modules\\") {
        $daoFiles += $_.FullName
    }
}

$Results.Summary.TotalEntities = $entityFiles.Count
$Results.Summary.TotalDaos = $daoFiles.Count

Write-Host "发现Entity文件: $($entityFiles.Count)个" -ForegroundColor $Colors.White
Write-Host "发现DAO文件: $($daoFiles.Count)个" -ForegroundColor $Colors.White
Write-Host ""

# 检查Entity规范
Write-Host "🔍 检查Entity设计规范..." -ForegroundColor $Colors.Cyan

foreach ($file in $entityFiles) {
    $entityInfo = Test-EntityStandards -FilePath $file
    if ($entityInfo) {
        if ($entityInfo.Status -ne "符合规范") {
            $Results.LargeEntities += $entityInfo
        }

        if ($Detailed) {
            $statusColor = if ($entityInfo.Status -eq "符合规范") { $Colors.Green } else { $Colors.Yellow }
            Write-Host "  $($entityInfo.Status): $($entityInfo.RelativePath) ($($entityInfo.Lines)行, $($entityInfo.Fields)字段)" -ForegroundColor $statusColor
        }
    }
}

# 检查DAO规范
Write-Host "`n🔍 检查DAO设计规范..." -ForegroundColor $Colors.Cyan

foreach ($file in $daoFiles) {
    $daoInfo = Test-DaoStandards -FilePath $file
    if ($daoInfo) {
        if ($daoInfo.Status -ne "符合规范") {
            $Results.RepositoryViolations += $daoInfo
            $Results.Summary.RepositoryViolationCount++
        }

        if ($Detailed) {
            $statusColor = if ($daoInfo.Status -eq "符合规范") { $Colors.Green } else { $Colors.Red }
            Write-Host "  $($daoInfo.Status): $($daoInfo.RelativePath)" -ForegroundColor $statusColor
        }
    }
}

# 检查重复Entity
Write-Host "`n🔍 检查重复Entity定义..." -ForegroundColor $Colors.Cyan

$Results.DuplicateEntities = Find-DuplicateEntities -EntityFiles $entityFiles
$Results.Summary.DuplicateEntityCount = $Results.DuplicateEntities.Count

if ($Results.DuplicateEntities.Count -gt 0) {
    Write-Host "  发现重复Entity:" -ForegroundColor $Colors.Yellow
    foreach ($duplicate in $Results.DuplicateEntities) {
        Write-Host "    $($duplicate.EntityName):" -ForegroundColor $Colors.Yellow
        foreach ($file in $duplicate.Files) {
            Write-Host "      - $file" -ForegroundColor $Colors.White
        }
    }
} else {
    Write-Host "  ✅ 无重复Entity定义" -ForegroundColor $Colors.Green
}

# 计算合规率
$totalIssues = $Results.Summary.HugeEntityCount + $Results.Summary.LargeEntityCount + $Results.Summary.RepositoryViolationCount + $Results.Summary.DuplicateEntityCount
$totalChecks = $Results.Summary.TotalEntities + $Results.Summary.TotalDaos
$Results.Summary.ComplianceRate = [Math]::Round((($totalChecks - $totalIssues) / $totalChecks) * 100, 2)

# 输出检查结果
Write-Host "`n📊 Entity规范检查报告" -ForegroundColor $Colors.Cyan
Write-Host "========================" -ForegroundColor $Colors.Cyan

Write-Host "`n📈 总体统计:" -ForegroundColor $Colors.White
Write-Host "  Entity文件总数: $($Results.Summary.TotalEntities)" -ForegroundColor $Colors.White
Write-Host "  DAO文件总数: $($Results.Summary.TotalDaos)" -ForegroundColor $Colors.White
Write-Host "  超大Entity数量: $($Results.Summary.HugeEntityCount)" -ForegroundColor $Colors.Red
Write-Host "  大型Entity数量: $($Results.Summary.LargeEntityCount)" -ForegroundColor $Colors.Yellow
Write-Host "  Repository违规数量: $($Results.Summary.RepositoryViolationCount)" -ForegroundColor $Colors.Red
Write-Host "  重复Entity数量: $($Results.Summary.DuplicateEntityCount)" -ForegroundColor $Colors.Yellow
Write-Host "  合规率: $($Results.Summary.ComplianceRate)%" -ForegroundColor $(if($Results.Summary.ComplianceRate -ge 95) {$Colors.Green} elseif($Results.Summary.ComplianceRate -ge 80) {$Colors.Yellow} else {$Colors.Red})

# 输出问题详情
if ($Results.Summary.HugeEntityCount -gt 0) {
    Write-Host "`n🚨 P0级问题 - 超大Entity:" -ForegroundColor $Colors.Red
    foreach ($entity in $Results.LargeEntities) {
        if ($entity.Status -eq "超大Entity") {
            Write-Host "  ❌ $($entity.RelativePath) - $($entity.Lines)行 (必须拆分)" -ForegroundColor $Colors.Red
        }
    }
}

if ($Results.Summary.RepositoryViolationCount -gt 0) {
    Write-Host "`n🚨 P0级问题 - Repository违规:" -ForegroundColor $Colors.Red
    foreach ($violation in $Results.RepositoryViolations) {
        Write-Host "  ❌ $($violation.RelativePath) - 必须使用@Mapper注解" -ForegroundColor $Colors.Red
    }
}

if ($Results.Summary.LargeEntityCount -gt 0) {
    Write-Host "`n⚠️ P1级问题 - 大型Entity:" -ForegroundColor $Colors.Yellow
    foreach ($entity in $Results.LargeEntities) {
        if ($entity.Status -eq "大型Entity") {
            Write-Host "  ⚠️ $($entity.RelativePath) - $($entity.Lines)行 (建议优化)" -ForegroundColor $Colors.Yellow
        }
    }
}

# 自动修复
if ($Fix -and $Results.Summary.RepositoryViolationCount -gt 0) {
    Write-Host "`n🔧 开始自动修复Repository违规..." -ForegroundColor $Colors.Green

    $fixedCount = Repair-RepositoryViolations -Violations $Results.RepositoryViolations

    if ($fixedCount -gt 0) {
        Write-Host "✅ 自动修复完成，修复了 $fixedCount 个文件" -ForegroundColor $Colors.Green
        $Results.Summary.RepositoryViolationCount -= $fixedCount
    } else {
        Write-Host "⚠️ 没有文件被修复" -ForegroundColor $Colors.Yellow
    }
}

# 生成Markdown报告
$reportContent = @"
# IOE-DREAM Entity设计规范检查报告

> **生成时间**: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
> **检查路径**: $ProjectPath
> **脚本版本**: v1.0.0

## 📊 检查概览

| 指标 | 数量 | 说明 |
|------|------|------|
| Entity文件总数 | $($Results.Summary.TotalEntities) | 项目中所有Entity类 |
| DAO文件总数 | $($Results.Summary.TotalDaos) | 数据访问层接口 |
| 超大Entity(>400行) | $($Results.Summary.HugeEntityCount) | 🔴 严重违规 |
| 大型Entity(200-400行) | $($Results.Summary.LargeEntityCount) | 🟡 需要优化 |
| Repository违规 | $($Results.Summary.RepositoryViolationCount) | 🔴 严重违规 |
| 重复Entity定义 | $($Results.Summary.DuplicateEntityCount) | 🟡 需要清理 |
| **合规率** | **$($Results.Summary.ComplianceRate)%** | $(if($Results.Summary.ComplianceRate -ge 95) {'✅ 优秀'} elseif($Results.Summary.ComplianceRate -ge 80) {'⚠️ 良好'} else {'❌ 需改进'})

## 🚨 严重问题 (P0级)

### 超大Entity文件

$(
    if ($Results.Summary.HugeEntityCount -gt 0) {
        $hugeEntities = $Results.LargeEntities | Where-Object { $_.Status -eq "超大Entity" }
        $hugeEntities | ForEach-Object {
            "- **$($_.RelativePath)** - $($_.Lines)行`n"
        }
    } else {
        "✅ 无超大Entity文件"
    }
)

### Repository注解违规

$(
    if ($Results.Summary.RepositoryViolationCount -gt 0) {
        $Results.RepositoryViolations | ForEach-Object {
            "- **$($_.RelativePath)** - 使用了@Repository注解`n"
        }
    } else {
        "✅ 无Repository违规"
    }
)

## ⚠️ 一般问题 (P1级)

### 大型Entity文件

$(
    if ($Results.Summary.LargeEntityCount -gt 0) {
        $largeEntities = $Results.LargeEntities | Where-Object { $_.Status -eq "大型Entity" }
        $largeEntities | ForEach-Object {
            "- **$($_.RelativePath)** - $($_.Lines)行`n"
        }
    } else {
        "✅ 无大型Entity文件"
    }
)

### 重复Entity定义

$(
    if ($Results.Summary.DuplicateEntityCount -gt 0) {
        $Results.DuplicateEntities | ForEach-Object {
            "- **$($_.EntityName)**`n"
            $_.Files | ForEach-Object {
                "  - $_`n"
            }
        }
    } else {
        "✅ 无重复Entity定义"
    }
)

## 📋 详细问题清单

### Entity详细检查结果

| 文件路径 | 行数 | 字段数 | 继承BaseEntity | @TableName | 状态 |
|---------|------|--------|----------------|-----------|------|
$(
    $allEntities = @()
    foreach ($file in $entityFiles) {
        $entityInfo = Test-EntityStandards -FilePath $file
        if ($entityInfo) {
            $baseEntitySymbol = if ($entityInfo.ExtendsBaseEntity) { "✅" } else { "❌" }
            $tableNameSymbol = if ($entityInfo.TableName) { "✅" } else { "❌" }
            $statusColor = if ($entityInfo.Status -eq "符合规范") { "✅" } else { "❌" }

            $allEntities += "| $($entityInfo.RelativePath) | $($entityInfo.Lines) | $($entityInfo.Fields) | $baseEntitySymbol | $tableNameSymbol | $statusColor $($entityInfo.Status) |`n"
        }
    }
    $allEntities
)

### DAO详细检查结果

| 文件路径 | @Mapper | @Repository | 继承BaseMapper | 状态 |
|---------|--------|------------|----------------|------|
$(
    $allDaos = @()
    foreach ($file in $daoFiles) {
        $daoInfo = Test-DaoStandards -FilePath $file
        if ($daoInfo) {
            $mapperSymbol = if ($daoInfo.HasMapperAnnotation) { "✅" } else { "❌" }
            $repositorySymbol = if ($daoInfo.HasRepositoryAnnotation) { "❌" } else { "✅" }
            $baseMapperSymbol = if ($daoInfo.ExtendsBaseMapper) { "✅" } else { "❌" }
            $statusSymbol = if ($daoInfo.Status -eq "符合规范") { "✅" } else { "❌" }

            $allDaos += "| $($daoInfo.RelativePath) | $mapperSymbol | $repositorySymbol | $baseMapperSymbol | $statusSymbol $($daoInfo.Status) |`n"
        }
    }
    $allDaos
)

## 🛠️ 修复建议

### 立即修复 (P0级)

1. **修复Repository违规**
   ```powershell
   # 自动修复
   .\check-entity-standards.ps1 -Fix

   # 或手动修复：将@Repository替换为@Mapper
   ```

2. **拆分超大Entity**
   - AreaUserEntity (488行) → 拆分为基础Entity + Manager
   - 将业务逻辑移到Manager层
   - 将静态工具方法移到Util类

### 计划修复 (P1级)

1. **优化大型Entity**
   - 将配置字段分离到独立表
   - 使用JSON字段存储扩展属性
   - 保持Entity≤200行

2. **清理重复Entity**
   - 统一在microservices-common模块管理
   - 使用适配器模式保持向后兼容
   - 逐步迁移引用

### 长期改进

1. **建立质量门禁**
   - CI/CD集成自动检查
   - 代码审查检查清单
   - 定期重构计划

2. **团队培训**
   - Entity设计规范培训
   - 最佳实践分享
   - 工具使用培训

## 📈 质量趋势

建议定期运行此检查脚本，跟踪质量改进趋势：

- **目标合规率**: 100%
- **当前合规率**: $($Results.Summary.ComplianceRate)%
- **改进空间**: $((100 - $Results.Summary.ComplianceRate))%

---

**📝 报告说明**:
- 🔴 P0级问题：立即修复，影响代码质量和架构合规性
- 🟡 P1级问题：计划修复，建议在下次迭代中完成
- ✅ 已符合规范：继续保持，作为最佳实践参考

**🚀 IOE-DREAM架构委员会**
**企业级代码质量保障**
"@

# 保存报告
try {
    $reportContent | Out-File -FilePath $ReportPath -Encoding UTF8
    Write-Host "`n📄 详细报告已保存到: $ReportPath" -ForegroundColor $Colors.Green
} catch {
    Write-Warning "保存报告失败: $($_.Exception.Message)"
}

# 最终状态
Write-Host "`n🎯 检查完成！" -ForegroundColor $Colors.Cyan

if ($Results.Summary.ComplianceRate -ge 95) {
    Write-Host "✅ 优秀！Entity设计完全符合规范" -ForegroundColor $Colors.Green
} elseif ($Results.Summary.ComplianceRate -ge 80) {
    Write-Host "⚠️ 良好！大部分Entity符合规范，建议优化剩余问题" -ForegroundColor $Colors.Yellow
} else {
    Write-Host "❌ 需要改进！存在较多规范问题，请及时修复" -ForegroundColor $Colors.Red
}

if ($Results.Summary.HugeEntityCount -gt 0 -or $Results.Summary.RepositoryViolationCount -gt 0) {
    Write-Host "`n🚨 发现严重问题，建议立即修复" -ForegroundColor $Colors.Red
    Write-Host "   运行修复命令: .\check-entity-standards.ps1 -Fix" -ForegroundColor $Colors.White
}

Write-Host "`n📚 更多信息请参考: ENTITY_DESIGN_STANDARDS_COMPLIANCE_GUIDE.md" -ForegroundColor $Colors.Cyan
Write-Host ""