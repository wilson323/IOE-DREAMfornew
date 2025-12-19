# 全局系统性分析脚本
# 目的: 全面分析IOE-DREAM项目的根源性问题

param(
    [switch]$Detailed,        # 详细模式
    [switch]$SaveReport       # 保存报告
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Red
Write-Host "🔍 IOE-DREAM 全局系统性分析" -ForegroundColor Red
Write-Host "====================================" -ForegroundColor Red

# 根目录
$rootPath = "D:/IOE-DREAM"
$microservicesPath = "$rootPath/microservices"
$reportPath = "$rootPath/analysis-reports"

# 创建报告目录
if (-not (Test-Path $reportPath)) {
    New-Item -ItemType Directory -Path $reportPath -Force | Out-Null
}

# 微服务列表
$services = @(
    "ioedream-gateway-service",
    "ioedream-common-service",
    "ioedream-device-comm-service",
    "ioedream-oa-service",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-video-service",
    "ioedream-database-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-biometric-service"
)

# 公共模块列表
$commonModules = @(
    "microservices-common",
    "microservices-common-core",
    "microservices-common-business",
    "microservices-common-cache",
    "microservices-common-data",
    "microservices-common-security",
    "microservices-common-permission",
    "microservices-common-workflow",
    "microservices-common-storage"
)

# 分析结果
$analysisResults = @{
    Microservices = @{}
    CommonModules = @{}
    GlobalIssues = @()
}

Write-Host "📊 第1步: 分析公共模块构建状态" -ForegroundColor Cyan

# 首先检查公共模块
foreach ($module in $commonModules) {
    $modulePath = "$microservicesPath/$module"
    $result = @{
        Exists = Test-Path $modulePath
        Compiles = $false
        ErrorCount = 0
        BuildTime = 0
        MainErrors = @()
    }

    if ($result.Exists) {
        Write-Host -NoNewline "检查 $module ... " -ForegroundColor Gray

        try {
            Push-Location $modulePath
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
            $buildOutput = mvn clean install -q -DskipTests 2>&1
            $stopwatch.Stop()
            $exitCode = $LASTEXITCODE

            $result.BuildTime = $stopwatch.ElapsedMilliseconds

            if ($exitCode -eq 0 -and $buildOutput -match "BUILD SUCCESS") {
                $result.Compiles = $true
                Write-Host "✅ 成功 ($($result.BuildTime)ms)" -ForegroundColor Green
            } else {
                $errorLines = $buildOutput -split "`n" | Where-Object { $_ -match "ERROR" }
                $result.ErrorCount = $errorLines.Count
                $result.MainErrors = $errorLines | Select-Object -First 5
                Write-Host "❌ 失败 ($($result.ErrorCount) 错误)" -ForegroundColor Red
            }
            Pop-Location
        }
        catch {
            Write-Host "💥 异常 $($_.Exception.Message)" -ForegroundColor Red
            Pop-Location -ErrorAction SilentlyContinue
        }
    }
    else {
        Write-Host "❌ 目录不存在" -ForegroundColor Red
    }

    $analysisResults.CommonModules[$module] = $result
}

Write-Host "`n📊 第2步: 分析微服务构建状态" -ForegroundColor Cyan

# 分析微服务
$totalServiceErrors = 0
$successfulServices = 0

foreach ($service in $services) {
    $servicePath = "$microservicesPath/$service"
    $result = @{
        Exists = Test-Path $servicePath
        Compiles = $false
        ErrorCount = 0
        WarningCount = 0
        BuildTime = 0
        MainErrors = @()
        DependsOnCommon = $false
    }

    if ($result.Exists) {
        Write-Host -NoNewline "检查 $service ... " -ForegroundColor Gray

        # 检查依赖公共模块
        $pomContent = Get-Content "$servicePath/pom.xml" -Raw -ErrorAction SilentlyContinue
        if ($pomContent -match "microservices-common") {
            $result.DependsOnCommon = $true
        }

        try {
            Push-Location $servicePath
            $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
            $buildOutput = mvn clean compile -q -DskipTests 2>&1
            $stopwatch.Stop()
            $exitCode = $LASTEXITCODE

            $result.BuildTime = $stopwatch.ElapsedMilliseconds

            # 分析输出
            $errorLines = $buildOutput -split "`n" | Where-Object { $_ -match "ERROR" }
            $warningLines = $buildOutput -split "`n" | Where-Object { $_ -match "WARN" }
            $result.ErrorCount = $errorLines.Count
            $result.WarningCount = $warningLines.Count
            $result.MainErrors = $errorLines | Select-Object -First 10

            if ($exitCode -eq 0 -and $buildOutput -match "BUILD SUCCESS") {
                $result.Compiles = $true
                $successfulServices++
                Write-Host "✅ 成功 ($($result.BuildTime)ms)" -ForegroundColor Green
            }
            else {
                $totalServiceErrors += $result.ErrorCount
                Write-Host "❌ 失败 ($($result.ErrorCount)E/$($result.WarningCount)W)" -ForegroundColor Red

                if ($Detailed) {
                    Write-Host "   主要错误:" -ForegroundColor Yellow
                    foreach ($error in $result.MainErrors) {
                        Write-Host "     - $error" -ForegroundColor DarkRed
                    }
                }
            }
            Pop-Location
        }
        catch {
            Write-Host "💥 异常 $($_.Exception.Message)" -ForegroundColor Red
            Pop-Location -ErrorAction SilentlyContinue
        }
    }
    else {
        Write-Host "❌ 目录不存在" -ForegroundColor Red
    }

    $analysisResults.Microservices[$service] = $result
}

Write-Host "`n📊 第3步: 分析项目结构问题" -ForegroundColor Cyan

# 检查重复文件和目录结构问题
$duplicateAnalysis = @{
    DuplicateEntities = @{}
    PackageInconsistencies = @()
    OrphanFiles = @()
}

# 查找重复的Entity类
Write-Host "检查重复Entity类..." -ForegroundColor Gray
$entityFiles = Get-ChildItem -Path $microservicesPath -Recurse -Filter "*Entity.java" -File

foreach ($file in $entityFiles) {
    $className = $file.Name
    if ($duplicateAnalysis.DuplicateEntities.ContainsKey($className)) {
        $duplicateAnalysis.DuplicateEntities[$className] += $file.FullName
    } else {
        $duplicateAnalysis.DuplicateEntities[$className] = @($file.FullName)
    }
}

$duplicateCount = ($duplicateAnalysis.DuplicateEntities.GetEnumerator() | Where-Object { $_.Value.Count -gt 1 }).Count

if ($duplicateCount -gt 0) {
    Write-Host "发现 $duplicateCount 个重复的Entity类" -ForegroundColor Red
    if ($Detailed) {
        foreach ($dup in $duplicateAnalysis.DuplicateEntities.GetEnumerator() | Where-Object { $_.Value.Count -gt 1 }) {
            Write-Host "  $($dup.Key): $($dup.Value.Count) 个文件" -ForegroundColor Yellow
        }
    }
}

Write-Host "`n🎯 第4步: 根源性问题识别" -ForegroundColor Cyan

# 识别核心问题
$coreIssues = @()

# 问题1: 公共模块未构建
$failedCommonModules = $analysisResults.CommonModules.GetEnumerator() | Where-Object { -not $_.Value.Compiles }
if ($failedCommonModules.Count -gt 0) {
    $coreIssues += @{
        Type = "DEPENDENCY_FAILURE"
        Severity = "CRITICAL"
        Description = "$($failedCommonModules.Count) 个公共模块构建失败，影响所有依赖服务"
        Count = $failedCommonModules.Count
    }
}

# 问题2: 微服务大规模构建失败
if ($totalServiceErrors -gt 100) {
    $coreIssues += @{
        Type = "MASSIVE_COMPILATION_FAILURE"
        Severity = "CRITICAL"
        Description = "微服务总计 $totalServiceErrors 个编译错误，项目处于不可构建状态"
        Count = $totalServiceErrors
    }
}

# 问题3: 重复代码
if ($duplicateCount -gt 0) {
    $coreIssues += @{
        Type = "CODE_DUPLICATION"
        Severity = "HIGH"
        Description = "$duplicateCount 个重复Entity类，破坏架构一致性"
        Count = $duplicateCount
    }
}

# 生成分析报告
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$report = @"
# IOE-DREAM 项目全局系统性分析报告
生成时间: $timestamp

## 📊 执行摘要

### 公共模块状态
- 总数: $($commonModules.Count)
- 构建成功: $($analysisResults.CommonModules.GetEnumerator() | Where-Object { $_.Value.Compiles }).Count
- 构建失败: $($analysisResults.CommonModules.GetEnumerator() | Where-Object { -not $_.Value.Compiles }).Count

### 微服务状态
- 总数: $($services.Count)
- 构建成功: $successfulServices
- 构建失败: $($services.Count - $successfulServices)
- 总错误数: $totalServiceErrors

### 代码质量
- 重复Entity类: $duplicateCount

## 🚨 关键问题

"@

foreach ($issue in $coreIssues) {
    $severity = switch ($issue.Severity) {
        "CRITICAL" { "🔴 CRITICAL" }
        "HIGH" { "🟠 HIGH" }
        "MEDIUM" { "🟡 MEDIUM" }
        "LOW" { "🟢 LOW" }
    }

    $report += @"

### $($severity) - $($issue.Type)
- **描述**: $($issue.Description)
- **影响范围**: $($issue.Count) 个组件
"@
}

$report += @"

## 📋 详细数据

### 公共模块详情
| 模块 | 状态 | 错误数 | 构建时间 |
|------|------|--------|----------|
"@

foreach ($module in $analysisResults.CommonModules.GetEnumerator()) {
    $status = if ($module.Value.Compiles) { "✅ 成功" } else { "❌ 失败" }
    $report += "| $($module.Key) | $status | $($module.Value.ErrorCount) | $($module.Value.BuildTime)ms |`n"
}

$report += @"

### 微服务详情
| 服务 | 状态 | 错误数 | 警告数 | 依赖公共模块 | 构建时间 |
|------|------|--------|--------|--------------|----------|
"@

foreach ($service in $analysisResults.Microservices.GetEnumerator()) {
    $status = if ($service.Value.Compiles) { "✅ 成功" } else { "❌ 失败" }
    $dependsOn = if ($service.Value.DependsOnCommon) { "是" } else { "否" }
    $report += "| $($service.Key) | $status | $($service.Value.ErrorCount) | $($service.Value.WarningCount) | $dependsOn | $($service.Value.BuildTime)ms |`n"
}

Write-Host "`n====================================" -ForegroundColor Cyan
Write-Host "📊 全局分析完成" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

Write-Host "📈 关键指标:" -ForegroundColor White
Write-Host "  - 公共模块成功率: $($([math]::Round(($analysisResults.CommonModules.GetEnumerator() | Where-Object { $_.Value.Compiles }).Count / $commonModules.Count * 100, 1))%" -ForegroundColor $(if (($analysisResults.CommonModules.GetEnumerator() | Where-Object { $_.Value.Compiles }).Count / $commonModules.Count * 100) -ge 80) { "Green" } else { "Red" })
Write-Host "  - 微服务成功率: $($([math]::Round($successfulServices / $services.Count * 100, 1))%" -ForegroundColor $(if ($successfulServices / $services.Count * 100 -ge 50) { "Green" } else { "Red" })
Write-Host "  - 总编译错误: $totalServiceErrors" -ForegroundColor $(if ($totalServiceErrors -lt 50) { "Green" } else { "Red" })
Write-Host "  - 重复代码问题: $duplicateCount" -ForegroundColor $(if ($duplicateCount -eq 0) { "Green" } else { "Red" })

Write-Host ""
Write-Host "🎯 根源问题:" -ForegroundColor White
foreach ($issue in $coreIssues) {
    $color = switch ($issue.Severity) {
        "CRITICAL" { "Red" }
        "HIGH" { "Yellow" }
        "MEDIUM" { "Gray" }
        "LOW" { "White" }
    }
    Write-Host "  $($issue.Type): $($issue.Description)" -ForegroundColor $color
}

if ($SaveReport) {
    $reportFile = "$reportPath/global-analysis-$(Get-Date -Format 'yyyyMMdd-HHmmss').md"
    $report | Out-File -FilePath $reportFile -Encoding UTF8
    Write-Host "`n📄 报告已保存到: $reportFile" -ForegroundColor Green
}

Write-Host "`n✨ 建议下一步行动:" -ForegroundColor Cyan
Write-Host "1. 首先修复公共模块构建问题 (P0)" -ForegroundColor White
Write-Host "2. 系统性修复微服务编译错误 (P1)" -ForegroundColor White
Write-Host "3. 解决代码重复和架构问题 (P2)" -ForegroundColor White

exit 0