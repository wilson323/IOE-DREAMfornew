# IOE-DREAM 技术栈合规性检查脚本
# Tech Stack Compliance Checker for IOE-DREAM

param(
    [string]$ProjectPath = ".",
    [switch]$Fix,
    [switch]$Detailed,
    [string]$OutputFile = ""
)

# 脚本配置
$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

# 日志函数
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "ERROR" { "Red" }
        "WARN" { "Yellow" }
        "SUCCESS" { "Green" }
        default { "White" }
    }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

# 技术栈违规定义
$TechStackViolations = @{
    # 依赖注入违规
    "@Autowired" = @{
        Type = "DEPENDENCY_INJECTION"
        Description = "禁止使用@Autowired，必须使用@Resource"
        Severity = "HIGH"
        Fix = "Replace @Autowired with @Resource"
    }

    # Repository违规
    "@Repository" = @{
        Type = "REPOSITORY_ANNOTATION"
        Description = "禁止使用@Repository，必须使用@Mapper"
        Severity = "HIGH"
        Fix = "Replace @Repository with @Mapper"
    }

    "Repository" = @{
        Type = "REPOSITORY_NAMING"
        Description = "禁止使用Repository后缀，必须使用Dao后缀"
        Severity = "HIGH"
        Fix = "Replace Repository suffix with Dao"
    }

    # JPA违规
    "JpaRepository" = @{
        Type = "JPA_USAGE"
        Description = "禁止使用JPA，必须使用MyBatis-Plus"
        Severity = "HIGH"
        Fix = "Replace JpaRepository with BaseMapper"
    }

    "javax.annotation" = @{
        Type = "JAVAX_ANNOTATION"
        Description = "禁止使用javax.annotation，必须使用jakarta.annotation"
        Severity = "HIGH"
        Fix = "Replace javax.annotation with jakarta.annotation"
    }

    "javax.validation" = @{
        Type = "JAVAX_VALIDATION"
        Description = "禁止使用javax.validation，必须使用jakarta.validation"
        Severity = "HIGH"
        Fix = "Replace javax.validation with jakarta.validation"
    }

    "javax.persistence" = @{
        Type = "JAVAX_PERSISTENCE"
        Description = "禁止使用javax.persistence，必须使用jakarta.persistence"
        Severity = "HIGH"
        Fix = "Replace javax.persistence with jakarta.persistence"
    }

    "javax.transaction" = @{
        Type = "JAVAX_TRANSACTION"
        Description = "禁止使用javax.transaction，必须使用jakarta.transaction"
        Severity = "HIGH"
        Fix = "Replace javax.transaction with jakarta.transaction"
    }

    "javax.servlet" = @{
        Type = "JAVAX_SERVLET"
        Description = "禁止使用javax.servlet，必须使用jakarta.servlet"
        Severity = "HIGH"
        Fix = "Replace javax.servlet with jakarta.servlet"
    }

    "javax.ejb" = @{
        Type = "JAVAX_EJB"
        Description = "禁止使用javax.ejb，必须使用jakarta.ejb"
        Severity = "HIGH"
        Fix = "Replace javax.ejb with jakarta.ejb"
    }

    "javax.jms" = @{
        Type = "JAVAX_JMS"
        Description = "禁止使用javax.jms，必须使用jakarta.jms"
        Severity = "HIGH"
        Fix = "Replace javax.jms with jakarta.jms"
    }

    "javax.mail" = @{
        Type = "JAVAX_MAIL"
        Description = "禁止使用javax.mail，必须使用jakarta.mail"
        Severity = "HIGH"
        Fix = "Replace javax.mail with jakarta.mail"
    }
}

# 违规统计
$ViolationStats = @{
    TotalFiles = 0
    ViolationFiles = 0
    TotalViolations = 0
    ViolationsByType = @{}
    FixedFiles = @()
}

# 检查单个文件
function Test-FileCompliance {
    param([string]$FilePath)

    $violations = @()
    $content = Get-Content $FilePath -Raw

    foreach ($pattern in $TechStackViolations.Keys) {
        if ($content -match [regex]::Escape($pattern)) {
            $violation = $TechStackViolations[$pattern]
            $lineNumbers = @()

            # 找到所有违规行号
            $lines = $content -split "`n"
            for ($i = 0; $i -lt $lines.Count; $i++) {
                if ($lines[$i] -match [regex]::Escape($pattern)) {
                    $lineNumbers += ($i + 1)
                }
            }

            $violations += @{
                Type = $violation.Type
                Pattern = $pattern
                Description = $violation.Description
                Severity = $violation.Severity
                Fix = $violation.Fix
                LineNumbers = $lineNumbers
                FilePath = $FilePath
            }

            # 统计违规类型
            if (-not $ViolationStats.ViolationsByType.ContainsKey($violation.Type)) {
                $ViolationStats.ViolationsByType[$violation.Type] = 0
            }
            $ViolationStats.ViolationsByType[$violation.Type] += $lineNumbers.Count
        }
    }

    return $violations
}

# 修复文件
function Repair-FileCompliance {
    param(
        [string]$FilePath,
        [array]$Violations
    )

    $content = Get-Content $FilePath -Raw
    $originalContent = $content
    $fixes = @()

    foreach ($violation in $Violations) {
        $oldContent = $content

        switch ($violation.Pattern) {
            "@Autowired" {
                $content = $content -replace "@Autowired", "@Resource"
                $fixes += "Replaced @Autowired with @Resource"
            }

            "@Repository" {
                $content = $content -replace "@Repository", "@Mapper"
                $fixes += "Replaced @Repository with @Mapper"
            }

            "Repository" {
                # 只替换接口名称中的Repository
                $content = $content -replace "(\w+)Repository", "`$1Dao"
                $fixes += "Replaced Repository suffix with Dao"
            }

            "JpaRepository" {
                $content = $content -replace "JpaRepository", "BaseMapper"
                $fixes += "Replaced JpaRepository with BaseMapper"
            }

            { $_ -like "javax.*" } {
                $jakartaPackage = $_ -replace "^javax\.", "jakarta."
                $content = $content -replace [regex]::Escape($_), $jakartaPackage
                $fixes += "Replaced javax with jakarta package"
            }
        }

        if ($oldContent -ne $content) {
            $ViolationStats.TotalViolations--
        }
    }

    if ($content -ne $originalContent) {
        Set-Content $FilePath $content -NoNewline
        $ViolationStats.FixedFiles += $FilePath
        Write-Log "已修复文件: $FilePath" -Level "SUCCESS"
        return $fixes
    }

    return @()
}

# 扫描Java文件
function Find-JavaFiles {
    param([string]$Path)

    Write-Log "扫描Java文件: $Path"
    $javaFiles = @()

    try {
        $javaFiles = Get-ChildItem -Path $Path -Recurse -Include "*.java" -File | Where-Object {
            # 排除target、build、node_modules等目录
            $_.FullName -notmatch "\\(target|build|node_modules|\.git|\.idea)\\"
        }

        Write-Log "发现 $($javaFiles.Count) 个Java文件" -Level "SUCCESS"
        return $javaFiles
    }
    catch {
        Write-Log "扫描Java文件失败: $($_.Exception.Message)" -Level "ERROR"
        return @()
    }
}

# 生成报告
function New-ComplianceReport {
    param([array]$AllViolations)

    $report = @"
# IOE-DREAM 技术栈合规性检查报告
# Tech Stack Compliance Report for IOE-DREAM

**检查时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**项目路径**: $ProjectPath
**扫描模式**: $(if ($Fix) { "检查并修复" } else { "仅检查" })

## 📊 总体统计

- **扫描文件总数**: $($ViolationStats.TotalFiles)
- **违规文件数量**: $($ViolationStats.ViolationFiles)
- **违规总数**: $($ViolationStats.TotalViolations)
- **已修复文件数**: $($($ViolationStats.FixedFiles.Count))

## 🎯 违规类型统计

"@

    if ($ViolationStats.ViolationsByType.Count -gt 0) {
        $report += "`n| 违规类型 | 数量 | 严重程度 |`n"
        $report += "|---------|------|----------|`n"

        foreach ($type in $ViolationStats.ViolationsByType.Keys) {
            $count = $ViolationStats.ViolationsByType[$type]
            $severity = switch ($type) {
                { $_ -like "*DEPENDENCY*" -or $_ -like "*REPOSITORY*" } { "🔴 HIGH" }
                { $_ -like "*JAVAX*" } { "🔴 HIGH" }
                default { "🟡 MEDIUM" }
            }
            $report += "| $type | $count | $severity |`n"
        }
    } else {
        $report += "`n✅ **恭喜！未发现技术栈违规**`n"
    }

    if ($AllViolations.Count -gt 0) {
        $report += "`n`n## 📋 详细违规清单`n`n"

        $groupedViolations = $AllViolations | Group-Object FilePath
        foreach ($group in $groupedViolations) {
            $report += "### 📁 $($group.Name)`n`n"

            foreach ($violation in $group.Group) {
                $report += "#### $($violation.Severity) 违规: $($violation.Type)`n"
                $report += "**描述**: $($violation.Description)`n"
                $report += "**违规行号**: $($violation.LineNumbers -join ', ')`n"
                $report += "**修复建议**: $($violation.Fix)`n`n"
            }
        }
    }

    if ($ViolationStats.FixedFiles.Count -gt 0) {
        $report += "`n## 🔧 已修复文件清单`n`n"
        foreach ($file in $ViolationStats.FixedFiles) {
            $report += "- ✅ $file`n"
        }
    }

    $report += @"

## 📋 技术栈规范要求

### 🔴 强制要求
1. **依赖注入**: 必须使用 `@Resource`，禁止 `@Autowired`
2. **DAO层**: 必须使用 `@Mapper` 注解和 `Dao` 后缀命名，禁止 `@Repository`
3. **ORM框架**: 必须使用 `MyBatis-Plus` + `BaseMapper`，禁止 `JPA`
4. **包名规范**: 必须使用 `jakarta.*` 包名，禁止 `javax.*`

### 🎯 统一技术栈
- **Spring Boot**: 3.5.8
- **Jakarta EE**: 3.0+
- **ORM框架**: MyBatis-Plus 3.5.15+
- **缓存**: Redis + Caffeine
- **连接池**: Druid

---

**报告生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**检查工具**: IOE-DREAM Tech Stack Compliance Checker
"@

    return $report
}

# 主执行逻辑
function Main {
    Write-Log "=== IOE-DREAM 技术栈合规性检查 ===" -Level "SUCCESS"
    Write-Log "项目路径: $ProjectPath"
    Write-Log "检查模式: $(if ($Fix) { '检查并修复' } else { '仅检查' })"
    Write-Log "详细模式: $(if ($Detailed) { '启用' } else { '禁用' })"

    # 验证项目路径
    if (-not (Test-Path $ProjectPath)) {
        Write-Log "项目路径不存在: $ProjectPath" -Level "ERROR"
        exit 1
    }

    # 扫描Java文件
    $javaFiles = Find-JavaFiles $ProjectPath
    if ($javaFiles.Count -eq 0) {
        Write-Log "未找到Java文件，退出检查" -Level "WARN"
        exit 0
    }

    $ViolationStats.TotalFiles = $javaFiles.Count
    $allViolations = @()

    # 检查每个文件
    Write-Log "开始检查技术栈合规性..."
    foreach ($file in $javaFiles) {
        $violations = Test-FileCompliance $file.FullName

        if ($violations.Count -gt 0) {
            $ViolationStats.ViolationFiles++
            $ViolationStats.TotalViolations += $violations.Count
            $allViolations += $violations

            if ($Detailed) {
                Write-Log "发现违规: $($file.FullName) - $($violations.Count) 个违规" -Level "WARN"
                foreach ($violation in $violations) {
                    Write-Log "  - $($violation.Type): $($violation.Description) (行: $($violation.LineNumbers -join ', '))" -Level "WARN"
                }
            }

            # 自动修复
            if ($Fix) {
                $fixes = Repair-FileCompliance $file.FullName $violations
                if ($fixes.Count -gt 0) {
                    Write-Log "修复完成: $($file.FullName)" -Level "SUCCESS"
                    foreach ($fix in $fixes) {
                        Write-Log "  - $fix" -Level "SUCCESS"
                    }
                }
            }
        }
    }

    # 生成报告
    Write-Log "生成合规性报告..."
    $report = New-ComplianceReport $allViolations

    # 输出报告
    if ($OutputFile) {
        try {
            Set-Content $OutputFile $report -Encoding UTF8
            Write-Log "报告已保存到: $OutputFile" -Level "SUCCESS"
        }
        catch {
            Write-Log "保存报告失败: $($_.Exception.Message)" -Level "ERROR"
        }
    }

    # 显示摘要
    Write-Log "=== 检查完成 ===" -Level "SUCCESS"
    Write-Log "扫描文件: $($ViolationStats.TotalFiles)"
    Write-Log "违规文件: $($ViolationStats.ViolationFiles)"
    Write-Log "违规总数: $($ViolationStats.TotalViolations)"

    if ($Fix) {
        Write-Log "修复文件: $($($ViolationStats.FixedFiles.Count))" -Level "SUCCESS"
    }

    if ($ViolationStats.TotalViolations -eq 0) {
        Write-Log "🎉 恭喜！未发现技术栈违规" -Level "SUCCESS"
    } else {
        Write-Log "⚠️ 发现技术栈违规，请查看详细报告" -Level "WARN"
        exit 1
    }
}

# 执行主函数
try {
    Main
}
catch {
    Write-Log "脚本执行失败: $($_.Exception.Message)" -Level "ERROR"
    exit 1
}