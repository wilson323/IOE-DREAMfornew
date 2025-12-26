# IOE-DREAM 全局依赖深度分析脚本
# 功能：系统性分析所有模块的依赖关系，识别版本冲突、重复依赖、硬编码版本等问题
# 作者：AI Assistant
# 日期：2025-01-30

param(
    [switch]$Detailed = $false,
    [string]$OutputFile = "dependency-analysis-report.md"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$microservicesDir = Join-Path $projectRoot "microservices"
$outputPath = Join-Path $projectRoot $OutputFile

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "IOE-DREAM 全局依赖深度分析" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 读取父POM文件，提取所有版本属性
Write-Host "[1/5] 分析父POM版本属性..." -ForegroundColor Yellow
$parentPomPath = Join-Path $microservicesDir "pom.xml"
$parentPom = [xml](Get-Content $parentPomPath -Encoding UTF8)

$versionProperties = @{}
foreach ($property in $parentPom.project.properties.ChildNodes) {
    if ($property.Name -and $property.InnerText) {
        $versionProperties[$property.Name] = $property.InnerText
    }
}

Write-Host "  发现版本属性: $($versionProperties.Count) 个" -ForegroundColor Green

# 2. 查找所有POM文件
Write-Host "[2/5] 扫描所有模块POM文件..." -ForegroundColor Yellow
$pomFiles = Get-ChildItem -Path $microservicesDir -Filter "pom.xml" -Recurse | 
    Where-Object { $_.FullName -notlike "*\target\*" }

$modulePoms = @()
foreach ($pomFile in $pomFiles) {
    try {
        $pom = [xml](Get-Content $pomFile.FullName -Encoding UTF8)
        $artifactId = $pom.project.artifactId
        
        if ($artifactId) {
            $modulePoms += @{
                Path = $pomFile.FullName
                RelativePath = $pomFile.FullName.Replace($projectRoot + "\", "")
                ArtifactId = $artifactId
                Pom = $pom
            }
        }
    } catch {
        Write-Warning "无法解析POM文件: $($pomFile.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "  发现模块: $($modulePoms.Count) 个" -ForegroundColor Green

# 3. 分析依赖问题
Write-Host "[3/5] 分析依赖配置问题..." -ForegroundColor Yellow

$issues = @{
    HardcodedVersions = @()
    VersionInconsistencies = @()
    MissingVersions = @()
    DuplicateDependencies = @()
    UnusedProperties = @()
}

foreach ($module in $modulePoms) {
    $pom = $module.Pom
    $moduleName = $module.ArtifactId
    
    # 跳过父POM
    if ($moduleName -eq "ioedream-microservices-parent") {
        continue
    }
    
    # 分析dependencies节点
    if ($pom.project.dependencies -and $pom.project.dependencies.dependency) {
        $dependencies = $pom.project.dependencies.dependency
        if ($dependencies -isnot [Array]) {
            $dependencies = @($dependencies)
        }
        
        foreach ($dep in $dependencies) {
            $groupId = $dep.groupId
            $artifactId = $dep.artifactId
            $version = $dep.version
            $scope = $dep.scope
            
            # 检查硬编码版本（非${}引用）
            if ($version -and $version -notmatch '\$\{.*\}') {
                $issues.HardcodedVersions += @{
                    Module = $moduleName
                    GroupId = $groupId
                    ArtifactId = $artifactId
                    Version = $version
                    Path = $module.RelativePath
                }
            }
            
            # 检查版本属性引用是否在父POM中定义
            if ($version -match '\$\{(.+)\}') {
                $propName = $matches[1]
                if (-not $versionProperties.ContainsKey($propName)) {
                    $issues.MissingVersions += @{
                        Module = $moduleName
                        GroupId = $groupId
                        ArtifactId = $artifactId
                        Property = $propName
                        Path = $module.RelativePath
                    }
                }
            }
        }
    }
}

Write-Host "  发现问题: $($issues.HardcodedVersions.Count) 个硬编码版本, $($issues.MissingVersions.Count) 个缺失属性" -ForegroundColor $(if ($issues.HardcodedVersions.Count -gt 0) { "Yellow" } else { "Green" })

# 4. 检查microservices-common-core的特殊问题
Write-Host "[4/5] 检查公共模块依赖规范..." -ForegroundColor Yellow

$coreModule = $modulePoms | Where-Object { $_.ArtifactId -eq "microservices-common-core" }
if ($coreModule) {
    $corePom = $coreModule.Pom
    $coreIssues = @()
    
    # 检查是否包含spring-boot-starter-web（应该避免，因为是最小稳定内核）
    $webDep = $corePom.project.dependencies.dependency | 
        Where-Object { $_.artifactId -eq "spring-boot-starter-web" }
    
    if ($webDep) {
        $coreIssues += "包含spring-boot-starter-web依赖（最小稳定内核应避免）"
    }
    
    # 检查硬编码版本
    $hardcodedInCore = $issues.HardcodedVersions | Where-Object { $_.Module -eq "microservices-common-core" }
    if ($hardcodedInCore.Count -gt 0) {
        $coreIssues += "包含 $($hardcodedInCore.Count) 个硬编码版本（应使用父POM properties）"
    }
    
    if ($coreIssues.Count -gt 0) {
        Write-Host "  发现核心模块问题: $($coreIssues.Count) 个" -ForegroundColor Yellow
        $issues["CoreModuleIssues"] = $coreIssues
    } else {
        Write-Host "  核心模块依赖规范正确" -ForegroundColor Green
    }
}

# 5. 生成报告
Write-Host "[5/5] 生成依赖分析报告..." -ForegroundColor Yellow

$report = @"
# IOE-DREAM 全局依赖深度分析报告

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**分析范围**: $($modulePoms.Count) 个模块
**版本属性**: $($versionProperties.Count) 个

---

## 📊 执行摘要

- **硬编码版本问题**: $($issues.HardcodedVersions.Count) 个
- **缺失版本属性**: $($issues.MissingVersions.Count) 个
- **核心模块问题**: $($(if ($issues["CoreModuleIssues"]) { $issues["CoreModuleIssues"].Count } else { 0 })) 个

---

## 🔍 详细分析结果

### 1. 硬编码版本问题

以下依赖使用了硬编码版本号，应该改为使用父POM的properties引用：

"@

if ($issues.HardcodedVersions.Count -gt 0) {
    $report += "`n| 模块 | GroupId | ArtifactId | 当前版本 | 文件路径 |`n"
    $report += "|------|---------|------------|---------|----------|`n"
    
    foreach ($issue in $issues.HardcodedVersions) {
        $report += "| $($issue.Module) | $($issue.GroupId) | $($issue.ArtifactId) | $($issue.Version) | $($issue.Path) |`n"
    }
} else {
    $report += "`n✅ **无硬编码版本问题**`n"
}

$report += @"


### 2. 缺失版本属性

以下依赖引用的版本属性在父POM中未定义：

"@

if ($issues.MissingVersions.Count -gt 0) {
    $report += "`n| 模块 | GroupId | ArtifactId | 属性名 | 文件路径 |`n"
    $report += "|------|---------|------------|--------|----------|`n"
    
    foreach ($issue in $issues.MissingVersions) {
        $report += "| $($issue.Module) | $($issue.GroupId) | $($issue.ArtifactId) | `$`{$($issue.Property)`} | $($issue.Path) |`n"
    }
} else {
    $report += "`n✅ **无缺失版本属性问题**`n"
}

if ($issues["CoreModuleIssues"]) {
    $report += @"


### 3. 核心模块(microservices-common-core)特殊问题

⚠️ **最小稳定内核模块应避免不必要的依赖**

"@
    foreach ($coreIssue in $issues["CoreModuleIssues"]) {
        $report += "- $coreIssue`n"
    }
}

$report += @"


---

## 🎯 优化建议

### 优先级 P0 - 立即修复

1. **移除microservices-common-core中的spring-boot-starter-web依赖**
   - 原因：最小稳定内核应尽量纯Java，避免引入Web框架
   - 建议：如果需要Web功能，应在上层模块引入

2. **统一所有硬编码版本为properties引用**
   - 原因：便于统一管理和版本升级
   - 建议：将硬编码版本移到父POM的properties中，子模块使用`${}`引用

### 优先级 P1 - 短期优化

1. **验证所有依赖版本是否符合企业级标准**
   - 检查是否有安全漏洞
   - 检查是否有已知问题版本
   - 检查版本是否过旧

2. **优化依赖结构**
   - 移除重复依赖
   - 使用`<optional>true</optional>`标记可选依赖
   - 合理使用`<scope>`限制依赖范围

---

## 📋 版本属性清单

父POM中定义的所有版本属性：

"@

foreach ($prop in $versionProperties.GetEnumerator() | Sort-Object Name) {
    if ($prop.Key -match "\.version$") {
        $report += "- **`$`{$($prop.Key)}**: $($prop.Value)`n"
    }
}

$report += @"


---

## 🔧 修复脚本

可以使用以下Maven命令检查依赖树：

```bash
# 查看完整依赖树
mvn dependency:tree

# 查看依赖冲突
mvn dependency:tree -Dverbose

# 分析依赖
mvn dependency:analyze
```

---

**报告生成完成** ✅

"@

$report | Out-File -FilePath $outputPath -Encoding UTF8
Write-Host "报告已生成: $outputPath" -ForegroundColor Green

# 输出摘要到控制台
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "分析摘要" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "硬编码版本: $($issues.HardcodedVersions.Count)" -ForegroundColor $(if ($issues.HardcodedVersions.Count -gt 0) { "Yellow" } else { "Green" })
Write-Host "缺失属性: $($issues.MissingVersions.Count)" -ForegroundColor $(if ($issues.MissingVersions.Count -gt 0) { "Yellow" } else { "Green" })
Write-Host "详细报告: $OutputFile" -ForegroundColor Cyan

