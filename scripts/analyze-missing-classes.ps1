# 缺失类分析和自动修复脚本
# 目的: 检测微服务中缺失的类并提供自动生成或重构方案

param(
    [string]$ServiceFilter = "",
    [switch]$GenerateFixes,
    [switch]$CreateStubs,
    [switch]$DetailedReport
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "缺失类分析和自动修复" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

# 1. 分析服务缺失类
function Get-MissingClassesAnalysis {
    param([string]$ServiceName)

    Write-Host "`n分析服务: $ServiceName" -ForegroundColor Yellow

    try {
        # 编译并收集错误信息
        $errorOutput = & mvn clean compile -pl $ServiceName -am -Dmaven.test.skip=true -Dmaven.clean.failOnError=false 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✅ $ServiceName 编译成功，无缺失类问题" -ForegroundColor Green
            return @{
                Service = $ServiceName
                Status = "Success"
                MissingClasses = @()
                MissingPackages = @()
                Suggestions = @()
            }
        }

        # 解析编译错误
        $missingClasses = @()
        $missingPackages = @()
        $suggestions = @()

        # 提取"找不到符号"错误
        $symbolErrors = $errorOutput | Select-String -Pattern "找不到符号.*类\s+(\w+)" | ForEach-Object {
            if ($_ -match '找不到符号.*类\s+(\w+)') {
                $missingClasses += @{
                    ClassName = $matches[1]
                    FullError = $_.ToString().Trim()
                    Context = $_.Context.PreContext[0]
                }
            }
        }

        # 提取"程序包不存在"错误
        $packageErrors = $errorOutput | Select-String -Pattern "程序包([\w\.]+)不存在" | ForEach-Object {
            if ($_ -match '程序包([\w\.]+)不存在') {
                $missingPackages += @{
                    PackageName = $matches[1]
                    FullError = $_.ToString().Trim()
                }
            }
        }

        # 去重并统计
        $missingClasses = $missingClasses | Group-Object -Property ClassName | ForEach-Object { $_.Group[0] }
        $missingPackages = $missingPackages | Group-Object -Property PackageName | ForEach-Object { $_.Group[0] }

        Write-Host "  发现缺失类: $($missingClasses.Count) 个" -ForegroundColor $(if($missingClasses.Count -gt 0) {"Red"} else {"Green"})
        Write-Host "  发现缺失包: $($missingPackages.Count) 个" -ForegroundColor $(if($missingPackages.Count -gt 0) {"Red"} else {"Green"})

        # 生成修复建议
        $suggestions += Generate-FixSuggestions -ServiceName $ServiceName -MissingClasses $missingClasses -MissingPackages $missingPackages

        return @{
            Service = $ServiceName
            Status = "MissingClasses"
            MissingClasses = $missingClasses
            MissingPackages = $missingPackages
            Suggestions = $suggestions
            ErrorCount = $missingClasses.Count + $missingPackages.Count
        }

    } catch {
        Write-Host "  ❌ 分析 $ServiceName 时出错: $($_.Exception.Message)" -ForegroundColor Red
        return @{
            Service = $ServiceName
            Status = "Error"
            Error = $_.Exception.Message
        }
    }
}

# 2. 生成修复建议
function Generate-FixSuggestions {
    param(
        [string]$ServiceName,
        [array]$MissingClasses,
        [array]$MissingPackages
    )

    $suggestions = @()

    # 基于缺失类名生成建议
    foreach ($class in $MissingClasses) {
        $className = $class.ClassName

        if ($className -like "*VO") {
            $suggestions += "建议在 `domain/vo/` 目录下创建 `$className` 类（视图对象）"
        } elseif ($className -like "*DTO") {
            $suggestions += "建议在 `domain/dto/` 目录下创建 `$className` 类（数据传输对象）"
        } elseif ($className -like "*Form") {
            $suggestions += "建议在 `domain/form/` 目录下创建 `$className` 类（表单对象）"
        } elseif ($className -like "*Service") {
            $suggestions += "建议在 `service/` 目录下创建 `$className` 接口和实现类"
        } elseif ($className -like "*Controller") {
            $suggestions += "建议检查 `controller/` 目录下的控制器文件"
        } elseif ($className -like "*Entity") {
            $suggestions += "建议在 `domain/entity/` 或对应的DAO模块中创建 `$className` 类"
        }
    }

    # 基于缺失包生成建议
    foreach ($package in $MissingPackages) {
        $packageName = $package.PackageName

        if ($packageName -like "*.dto") {
            $suggestions += "建议创建 `domain/dto/` 目录和相应的包结构"
        } elseif ($packageName -like "*.vo") {
            $suggestions += "建议创建 `domain/vo/` 目录和相应的包结构"
        } elseif ($packageName -like "*.form") {
            $suggestions += "建议创建 `domain/form/` 目录和相应的包结构"
        } elseif ($packageName -like "*.service") {
            $suggestions += "建议创建 `service/` 目录和相应的包结构"
        } elseif ($packageName -like "*.controller") {
            $suggestions += "建议检查 `controller/` 目录结构"
        }
    }

    # 通用建议
    if ($MissingClasses.Count -gt 10 -or $MissingPackages.Count -gt 5) {
        $suggestions += "⚠️ 大量类缺失，建议检查是否引用了错误的模块或需要重构代码"
        $suggestions += "💡 考虑删除未完成的控制器和服务实现，专注于核心功能"
    }

    return $suggestions
}

# 3. 生成类存根
function New-ClassStubs {
    param(
        [string]$ServiceName,
        [array]$MissingClasses,
        [array]$MissingPackages
    )

    $servicePath = "microservices/$ServiceName/src/main/java/net/lab1024/sa/$($ServiceName -replace '-service', '')"

    Write-Host "`n生成类存根..." -ForegroundColor White

    # 创建必要的目录结构
    $directories = @(
        "$servicePath/domain/vo",
        "$servicePath/domain/dto",
        "$servicePath/domain/form",
        "$servicePath/service",
        "$servicePath/controller"
    )

    foreach ($dir in $directories) {
        if (!(Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
            Write-Host "  创建目录: $dir" -ForegroundColor Gray
        }
    }

    # 生成缺失类的存根
    foreach ($class in $MissingClasses) {
        $className = $class.ClassName
        $packageName = ""

        # 确定包路径
        $serviceNameBase = $ServiceName -replace '-service', ''
        if ($className -like "*VO") {
            $packageName = "net.lab1024.sa.$serviceNameBase.domain.vo"
            $classPath = "$servicePath/domain/vo/$className.java"
        } elseif ($className -like "*DTO") {
            $packageName = "net.lab1024.sa.$serviceNameBase.domain.dto"
            $classPath = "$servicePath/domain/dto/$className.java"
        } elseif ($className -like "*Form") {
            $packageName = "net.lab1024.sa.$serviceNameBase.domain.form"
            $classPath = "$servicePath/domain/form/$className.java"
        } else {
            continue
        }

        # 生成类模板
        $classType = if ($className -like "*VO") { "vo" } else { "dto" }
        $classTemplate = Generate-ClassTemplate -ClassName $className -PackageName $packageName -Type $classType

        if (!(Test-Path $classPath)) {
            $classTemplate | Out-File -FilePath $classPath -Encoding UTF8
            Write-Host "  生成存根类: $className" -ForegroundColor Green
        }
    }
}

# 4. 生成类模板
function Generate-ClassTemplate {
    param(
        [string]$ClassName,
        [string]$PackageName,
        [string]$Type
    )

    $template = @"
package $packageName;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * TODO: 请添加类描述
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since $(Get-Date -Format "yyyy-MM-dd")
 */
@Data
@Accessors(chain = true)
public class $ClassName {

    // TODO: 请添加字段定义

}
"@

    return $template
}

# 5. 创建删除问题文件的脚本
function New-ProblematicFilesRemovalScript {
    param([string]$ServiceName, [array]$ProblematicFiles)

    $scriptPath = "scripts/remove-problematic-files-$ServiceName.ps1"

    $scriptContent = @"
# 删除问题文件脚本
# 目的: 删除 `$ServiceName` 中引起编译错误的未完成功能文件

param(
    [switch]$DryRun,
    [switch]$Backup
)

Write-Host "====================================" -ForegroundColor Yellow
Write-Host "删除问题文件脚本: $ServiceName" -ForegroundColor Yellow
Write-Host "====================================" -ForegroundColor Yellow

`$problematicFiles = @(
$($ProblematicFiles | ForEach-Object { "'$_'," })
)

`$backupDir = "scripts/backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"

if (`$Backup) {
    Write-Host "创建备份目录: `$backupDir" -ForegroundColor White
    New-Item -ItemType Directory -Path `$backupDir -Force | Out-Null
}

foreach (`$file in `$problematicFiles) {
    if (Test-Path `$file) {
        Write-Host "处理文件: `$file" -ForegroundColor White

        if (`$DryRun) {
            Write-Host "  [DRY RUN] 将删除: `$file" -ForegroundColor Yellow
        } else {
            if (`$Backup) {
                `$backupPath = Join-Path `$backupDir (Split-Path `$file -Leaf)
                Copy-Item `$file `$backupPath -Force
                Write-Host "  ✅ 备份到: `$backupPath" -ForegroundColor Green
            }

            Remove-Item `$file -Force
            Write-Host "  ✅ 已删除: `$file" -ForegroundColor Red
        }
    } else {
        Write-Host "  ⚠️ 文件不存在: `$file" -ForegroundColor Yellow
    }
}

Write-Host "`n问题文件处理完成！" -ForegroundColor Cyan
"@

    $scriptContent | Out-File -FilePath $scriptPath -Encoding UTF8
    Write-Host "删除脚本已生成: $scriptPath" -ForegroundColor Green
    return $scriptPath
}

# 6. 生成修复报告
function New-MissingClassesReport {
    param([array]$AnalysisResults)

    $reportPath = "scripts/reports/missing-classes-analysis-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $reportDir = Split-Path $reportPath -Parent

    if (!(Test-Path $reportDir)) {
        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    }

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

    $report = @"
# 缺失类分析报告

**生成时间**: $timestamp
**分析服务**: $($AnalysisResults.Count) 个微服务

## 执行摘要

本报告分析了IOE-DREAM微服务项目中的缺失类问题，并提供了自动修复建议。

## 分析结果

"@

    foreach ($result in $AnalysisResults) {
        $report += @"

### $($result.Service)

- **状态**: $($result.Status)
- **缺失类数量**: $($result.MissingClasses.Count)
- **缺失包数量**: $($result.MissingPackages.Count)
"@

        if ($result.MissingClasses.Count -gt 0) {
            $report += @"

#### 缺失类详情
| 类名 | 上下文 |
|------|--------|
"
            foreach ($class in $result.MissingClasses | Select-Object -First 10) {
                $report += "| $($class.ClassName) | $($class.Context) |`n"
            }
            if ($result.MissingClasses.Count -gt 10) {
                $report += "| ... | $($result.MissingClasses.Count - 10) 个更多类 |`n"
            }
        }

        if ($result.MissingPackages.Count -gt 0) {
            $report += @"

#### 缺失包详情
| 包名 |
|------|
"
            foreach ($package in $result.MissingPackages) {
                $report += "| $($package.PackageName) |`n"
            }
        }

        if ($result.Suggestions.Count -gt 0) {
            $report += @"

#### 修复建议
"
            foreach ($suggestion in $result.Suggestions) {
                $report += "- $suggestion`n"
            }
        }
    }

    $report += @"

## 总体统计

| 服务 | 状态 | 缺失类数 | 缺失包数 |
|------|------|----------|----------|
"

    foreach ($result in $AnalysisResults) {
        $statusIcon = if ($result.Status -eq "Success") { "✅" } elseif ($result.Status -eq "MissingClasses") { "❌" } else { "⚠️" }
        $report += "| $($result.Service) | $statusIcon $($result.Status) | $($result.MissingClasses.Count) | $($result.MissingPackages.Count) |`n"
    }

    $totalMissing = ($AnalysisResults | Measure-Object -Property ErrorCount -Sum).Sum
    $report += @"

**总计缺失类/包**: $totalMissing

## 修复策略

### 立即可执行的修复
1. 运行生成类存根脚本创建基础类结构
2. 删除未完成的功能文件以恢复编译
3. 专注于核心业务功能，避免过度设计

### 长期改进建议
1. 建立代码评审机制，防止未完成代码提交
2. 使用骨架生成工具快速创建标准结构
3. 分阶段开发，确保每个阶段都能编译通过

## 自动化工具

以下脚本可用于自动化修复：
- `analyze-missing-classes.ps1` - 本分析脚本
- `remove-problematic-files-*.ps1` - 删除问题文件脚本
- P4工程治理脚本 - 持续监控编译质量

---
*本报告由缺失类分析脚本自动生成*
"@

    $report | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Host "分析报告已生成: $reportPath" -ForegroundColor Green
    return $reportPath
}

# 执行主流程
try {
    Write-Host "开始缺失类分析..." -ForegroundColor Cyan

    # 获取所有服务
    $allServices = @(
        "ioedream-access-service",
        "ioedream-attendance-service",
        "ioedream-consume-service",
        "ioedream-visitor-service",
        "ioedream-video-service",
        "ioedream-database-service",
        "ioedream-biometric-service"
    )

    if ($ServiceFilter) {
        $allServices = $allServices | Where-Object { $_ -like "*$ServiceFilter*" }
    }

    $analysisResults = @()
    $problematicFiles = @()

    # 分析每个服务
    foreach ($service in $allServices) {
        $result = Get-MissingClassesAnalysis -ServiceName $service
        $analysisResults += $result

        # 收集问题文件
        if ($result.MissingClasses.Count -gt 0) {
            $result.MissingClasses | ForEach-Object {
                if ($_.Context -match '([^:]+):\d+') {
                    $filePath = $matches[1]
                    if ($filePath -and (Test-Path $filePath)) {
                        $problematicFiles += $filePath
                    }
                }
            }
        }
    }

    # 生成存根类
    if ($CreateStubs -and $analysisResults.MissingClasses.Count -gt 0) {
        Write-Host "`n生成类存根..." -ForegroundColor Cyan
        $resultsWithMissing = $analysisResults | Where-Object { $_.Status -eq "MissingClasses" }
        foreach ($result in $resultsWithMissing) {
            New-ClassStubs -ServiceName $result.Service -MissingClasses $result.MissingClasses -MissingPackages $result.MissingPackages
        }
    }

    # 生成删除问题文件脚本
    if ($problematicFiles.Count -gt 0) {
        $uniqueFiles = $problematicFiles | Get-Unique
        Write-Host "`n发现问题文件: $($uniqueFiles.Count) 个" -ForegroundColor Yellow

        if ($GenerateFixes) {
            $servicesWithProblems = $analysisResults | Where-Object { $_.Status -eq "MissingClasses" } | Select-Object -ExpandProperty Service
            foreach ($service in $servicesWithProblems) {
                Write-Host "为 $service 创建删除脚本..." -ForegroundColor White
                New-ProblematicFilesRemovalScript -ServiceName $service -ProblematicFiles $uniqueFiles
            }
        }
    }

    # 生成分析报告
    New-MissingClassesReport -AnalysisResults $analysisResults

    # 输出总结
    Write-Host "`n====================================" -ForegroundColor Cyan
    Write-Host "缺失类分析完成" -ForegroundColor Cyan
    Write-Host "====================================" -ForegroundColor Cyan

    $successCount = ($analysisResults | Where-Object { $_.Status -eq "Success" }).Count
    $problemCount = $analysisResults.Count - $successCount

    Write-Host "正常服务: $successCount" -ForegroundColor Green
    Write-Host "有问题服务: $problemCount" -ForegroundColor $(if($problemCount -gt 0) {"Red"} else {"Green"})

    if ($CreateStubs) {
        Write-Host "已生成类存根: ✅" -ForegroundColor Green
    }

    if ($GenerateFixes) {
        Write-Host "已生成修复脚本: ✅" -ForegroundColor Green
    }

    Write-Host "报告生成: scripts/reports/missing-classes-analysis-*.md" -ForegroundColor Cyan

} catch {
    Write-Host "`n❌ 缺失类分析失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "详细错误信息: $($_.Exception.StackTrace)" -ForegroundColor DarkRed
    exit 1
}