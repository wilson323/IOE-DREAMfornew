# =====================================================
# Manager依赖关系扫描脚本
# 版本: v1.0.0
# 描述: 扫描所有Manager类的构造函数依赖，生成依赖矩阵
# 创建时间: 2025-12-11
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [string]$OutputDir = "documentation/technical",

    [Parameter(Mandatory=$false)]
    [switch]$Verbose
)

$ErrorActionPreference = "Stop"

# 设置输出文件路径
$outputFile = Join-Path $OutputDir "MANAGER_DEPENDENCY_MATRIX.md"
$commonModulePath = "microservices/microservices-common/src/main/java"

Write-Host "[INFO] 开始扫描Manager类依赖关系..." -ForegroundColor Green

# 获取所有Manager类文件
$managerFiles = Get-ChildItem -Path $commonModulePath -Recurse -Filter "*Manager.java" | Where-Object {
    $_.FullName -notmatch "\\test\\"
}

Write-Host "[INFO] 找到 $($managerFiles.Count) 个Manager类" -ForegroundColor Cyan

# 解析Manager类的构造函数依赖
$managerDependencies = @()

foreach ($file in $managerFiles) {
    try {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8

        # 提取类名
        if ($content -match "public\s+class\s+(\w+Manager)") {
            $className = $matches[1]
            $packageName = ""

            # 提取包名
            if ($content -match "package\s+([^;]+)") {
                $packageName = $matches[1]
            }

            $fullClassName = "$packageName.$className"

            # 提取构造函数参数
            $dependencies = @()

            # 匹配所有构造函数
            $constructorPattern = [regex]::new("public\s+$className\s*\((.*?)\)\s*\{", [System.Text.RegularExpressions.RegexOptions]::Singleline)
            $constructorMatches = $constructorPattern.Matches($content)

            foreach ($match in $constructorMatches) {
                $params = $match.Groups[1].Value
                if ($params -and $params.Trim()) {
                    # 解析参数列表
                    $paramPattern = [regex]::new("(\w+)\s+(\w+)", [System.Text.RegularExpressions.RegexOptions]::Multiline)
                    $paramMatches = $paramPattern.Matches($params)

                    foreach ($paramMatch in $paramMatches) {
                        $paramType = $paramMatch.Groups[1].Value
                        $paramName = $paramMatch.Groups[2].Value

                        # 只收集非基础类型的依赖
                        if ($paramType -notmatch "^(int|long|String|boolean|Integer|Long|Boolean|Double|Float|BigDecimal)$") {
                            $dependencies += @{
                                Type = $paramType
                                Name = $paramName
                            }
                        }
                    }
                } else {
                    # 无参构造函数
                    $dependencies = @()
                }
            }

            # 如果没有找到构造函数，假设无参
            if ($constructorMatches.Count -eq 0) {
                $dependencies = @()
            }

            $managerDependencies += @{
                ClassName = $className
                FullClassName = $fullClassName
                Package = $packageName
                FilePath = $file.FullName.Replace((Get-Location).Path + "\", "")
                Dependencies = $dependencies
                DependencyCount = $dependencies.Count
            }
        }
    } catch {
        Write-Host "[WARN] 解析文件失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 生成Markdown文档
$markdown = @"
# Manager依赖关系矩阵

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**Manager总数**: $($managerDependencies.Count)

---

## 概述

本文档记录了所有Manager类的构造函数依赖关系，用于：
- 了解Manager类的依赖结构
- 指导Bean注册配置
- 避免依赖缺失问题

---

## Manager依赖矩阵

| Manager类 | 包名 | 依赖数量 | 依赖列表 |
|----------|------|---------|---------|
"@

foreach ($manager in $managerDependencies | Sort-Object ClassName) {
    $depList = if ($manager.Dependencies.Count -eq 0) {
        "无依赖"
    } else {
        ($manager.Dependencies | ForEach-Object { "$($_.Type) $($_.Name)" }) -join ", "
    }

    $markdown += "`n| $($manager.ClassName) | $($manager.Package) | $($manager.DependencyCount) | $depList |"
}

$markdown += @"


---

## 详细依赖信息

"@

foreach ($manager in $managerDependencies | Sort-Object ClassName) {
    $markdown += @"

### $($manager.ClassName)

- **完整类名**: `$($manager.FullClassName)`
- **文件路径**: `$($manager.FilePath)`
- **依赖数量**: $($manager.DependencyCount)

"@

    if ($manager.Dependencies.Count -eq 0) {
        $markdown += "- **依赖**: 无`n`n"
    } else {
        $markdown += "**构造函数依赖**:`n`n"
        foreach ($dep in $manager.Dependencies) {
            $markdown += "- `$dep.Type` `$dep.Name``n"
        }
        $markdown += "`n"
    }
}

$markdown += @"


---

## 依赖类型统计

"@

# 统计依赖类型
$depTypeStats = @{}
foreach ($manager in $managerDependencies) {
    foreach ($dep in $manager.Dependencies) {
        $depType = $dep.Type
        if ($depTypeStats.ContainsKey($depType)) {
            $depTypeStats[$depType]++
        } else {
            $depTypeStats[$depType] = 1
        }
    }
}

$markdown += "| 依赖类型 | 使用次数 |`n|---------|---------|`n"
foreach ($depType in $depTypeStats.Keys | Sort-Object) {
    $markdown += "| $depType | $($depTypeStats[$depType]) |`n"
}

# 确保输出目录存在
$outputDir = Split-Path $outputFile -Parent
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

# 写入文件
$markdown | Out-File -FilePath $outputFile -Encoding UTF8 -NoNewline

Write-Host "[OK] 依赖矩阵文档已生成: $outputFile" -ForegroundColor Green
Write-Host "[INFO] 共扫描 $($managerDependencies.Count) 个Manager类" -ForegroundColor Cyan

# 输出统计信息
Write-Host "`n[统计] 依赖类型分布:" -ForegroundColor Yellow
foreach ($depType in $depTypeStats.Keys | Sort-Object) {
    Write-Host "  $depType : $($depTypeStats[$depType])" -ForegroundColor Gray
}

