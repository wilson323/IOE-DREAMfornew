# =====================================================
# Service类Manager依赖扫描脚本
# 版本: v1.0.0
# 描述: 扫描所有Service类中的@Resource注入的Manager依赖
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
$outputFile = Join-Path $OutputDir "SERVICE_MANAGER_DEPENDENCY_REPORT.md"

Write-Host "[INFO] 开始扫描Service类的Manager依赖..." -ForegroundColor Green

# 获取所有ServiceImpl类文件
$serviceFiles = Get-ChildItem -Path "microservices" -Recurse -Filter "*ServiceImpl.java" | Where-Object {
    $_.FullName -notmatch "\\test\\" -and $_.FullName -match "service\\impl"
}

Write-Host "[INFO] 找到 $($serviceFiles.Count) 个Service实现类" -ForegroundColor Cyan

# 解析Service类的Manager依赖
$serviceDependencies = @{}

foreach ($file in $serviceFiles) {
    try {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8

        # 提取类名
        if ($content -match "public\s+class\s+(\w+ServiceImpl)") {
            $className = $matches[1]
            $packageName = ""

            # 提取包名
            if ($content -match "package\s+([^;]+)") {
                $packageName = $matches[1]
            }

            # 确定微服务名称
            $serviceName = "unknown"
            if ($file.FullName -match "ioedream-(\w+)-service") {
                $serviceName = $matches[1]
            }

            # 提取@Resource注入的Manager
            $managerDeps = @()

            # 匹配@Resource private XXXManager xxxManager;
            $resourcePattern = [regex]::new("@Resource\s+private\s+(\w+Manager)\s+(\w+);", [System.Text.RegularExpressions.RegexOptions]::Multiline)
            $resourceMatches = $resourcePattern.Matches($content)

            foreach ($match in $resourceMatches) {
                $managerType = $match.Groups[1].Value
                $managerName = $match.Groups[2].Value

                $managerDeps += @{
                    ManagerType = $managerType
                    ManagerName = $managerName
                }
            }

            if ($managerDeps.Count -gt 0) {
                if (-not $serviceDependencies.ContainsKey($serviceName)) {
                    $serviceDependencies[$serviceName] = @()
                }

                $serviceDependencies[$serviceName] += @{
                    ClassName = $className
                    FullClassName = "$packageName.$className"
                    FilePath = $file.FullName.Replace((Get-Location).Path + "\", "")
                    Managers = $managerDeps
                    ManagerCount = $managerDeps.Count
                }
            }
        }
    } catch {
        Write-Host "[WARN] 解析文件失败: $($file.FullName) - $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

# 生成Markdown文档
$markdown = @"
# Service类Manager依赖报告

**生成时间**: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
**Service总数**: $(($serviceDependencies.Values | Measure-Object -Property ManagerCount -Sum).Count)
**Manager依赖总数**: $(($serviceDependencies.Values | ForEach-Object { $_.ManagerCount } | Measure-Object -Sum).Sum)

---

## 概述

本文档记录了所有Service实现类中通过`@Resource`注入的Manager依赖，用于：
- 了解Service对Manager的依赖关系
- 识别缺失的Bean注册
- 指导配置类完善

---

## 按微服务分组的依赖

"@

foreach ($serviceName in $serviceDependencies.Keys | Sort-Object) {
    $services = $serviceDependencies[$serviceName]
    $totalManagers = ($services | ForEach-Object { $_.ManagerCount } | Measure-Object -Sum).Sum

    $markdown += @"

### $serviceName-service

**Service数量**: $($services.Count)
**Manager依赖总数**: $totalManagers

| Service类 | Manager依赖 |
|----------|------------|
"@

    foreach ($service in $services | Sort-Object ClassName) {
        $managerList = ($service.Managers | ForEach-Object { "`$($_.ManagerType) `$($_.ManagerName)`" }) -join ", "
        $markdown += "| $($service.ClassName) | $managerList |`n"
    }
}

$markdown += @"


---

## Manager使用统计

"@

# 统计每个Manager被哪些Service使用
$managerUsage = @{}
foreach ($serviceName in $serviceDependencies.Keys) {
    foreach ($service in $serviceDependencies[$serviceName]) {
        foreach ($manager in $service.Managers) {
            $managerType = $manager.ManagerType
            if (-not $managerUsage.ContainsKey($managerType)) {
                $managerUsage[$managerType] = @{
                    Services = @()
                    ServiceCount = 0
                }
            }

            if ($service.FullClassName -notin $managerUsage[$managerType].Services) {
                $managerUsage[$managerType].Services += $service.FullClassName
                $managerUsage[$managerType].ServiceCount++
            }
        }
    }
}

$markdown += "| Manager类型 | 使用Service数量 | 使用的Service |`n|------------|----------------|--------------|`n"

foreach ($managerType in $managerUsage.Keys | Sort-Object) {
    $serviceList = ($managerUsage[$managerType].Services | ForEach-Object { "`$_`" }) -join ", "
    if ($serviceList.Length -gt 100) {
        $serviceList = $serviceList.Substring(0, 100) + "..."
    }
    $markdown += "| $managerType | $($managerUsage[$managerType].ServiceCount) | $serviceList |`n"
}

$markdown += @"


---

## 详细依赖信息

"@

foreach ($serviceName in $serviceDependencies.Keys | Sort-Object) {
    $services = $serviceDependencies[$serviceName]

    $markdown += @"

### $serviceName-service

"@

    foreach ($service in $services | Sort-Object ClassName) {
        $markdown += @"
#### $($service.ClassName)

- **完整类名**: `$($service.FullClassName)`
- **文件路径**: `$($service.FilePath)`
- **Manager依赖数量**: $($service.ManagerCount)

**Manager依赖列表**:

"@
        foreach ($manager in $service.Managers) {
            $markdown += "- `$manager.ManagerType` `$manager.ManagerName``n"
        }
        $markdown += "`n"
    }
}

# 确保输出目录存在
$outputDir = Split-Path $outputFile -Parent
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

# 写入文件
$markdown | Out-File -FilePath $outputFile -Encoding UTF8 -NoNewline

Write-Host "[OK] Service依赖报告已生成: $outputFile" -ForegroundColor Green

$totalServices = ($serviceDependencies.Values | Measure-Object).Count
$totalManagers = ($serviceDependencies.Values | ForEach-Object { $_.ManagerCount } | Measure-Object -Sum).Sum

Write-Host "[INFO] 共扫描 $totalServices 个Service类，发现 $totalManagers 个Manager依赖" -ForegroundColor Cyan

# 输出统计信息
Write-Host "`n[统计] 按微服务分组:" -ForegroundColor Yellow
foreach ($serviceName in $serviceDependencies.Keys | Sort-Object) {
    $services = $serviceDependencies[$serviceName]
    $count = ($services | ForEach-Object { $_.ManagerCount } | Measure-Object -Sum).Sum
    Write-Host "  $serviceName-service : $($services.Count) 个Service, $count 个Manager依赖" -ForegroundColor Gray
}

