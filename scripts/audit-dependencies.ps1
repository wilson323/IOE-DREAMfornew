# =====================================================
# IOE-DREAM 依赖关系审计脚本
# 版本: v1.0.0
# 描述: 检查所有模块的依赖关系是否完整、正确
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory = $false)]
    [switch]$ShowDetails,

    [Parameter(Mandatory = $false)]
    [switch]$Fix,

    [Parameter(Mandatory = $false)]
    [string]$OutputFile = "dependency-audit-report.txt"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$MicroservicesDir = Join-Path $ProjectRoot "microservices"

# =====================================================
# 颜色输出函数
# =====================================================

function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

# =====================================================
# 检查模块依赖关系
# =====================================================

function Audit-ModuleDependencies {
    param([string]$ModulePath, [string]$ModuleName)

    $pomPath = Join-Path $ModulePath "pom.xml"
    if (-not (Test-Path $pomPath)) {
        return @{
            Module  = $ModuleName
            Status  = "SKIP"
            Message = "pom.xml不存在"
        }
    }

    $pomContent = Get-Content $pomPath -Raw
    $javaFiles = Get-ChildItem -Path $ModulePath -Recurse -Filter "*.java" -ErrorAction SilentlyContinue

    $issues = @()
    $missingDependencies = @()

    # 检查Java文件中的import语句
    foreach ($javaFile in $javaFiles) {
        $content = Get-Content $javaFile.FullName -Raw -ErrorAction SilentlyContinue
        if ($null -eq $content) { continue }

        # 提取所有import语句
        $imports = [regex]::Matches($content, 'import\s+net\.lab1024\.sa\.common\.([^;]+);')

        foreach ($import in $imports) {
            $importPath = $import.Groups[1].Value

            # 判断应该依赖哪个模块
            $expectedModule = $null
            if ($importPath -match "^core\.") {
                $expectedModule = "microservices-common-core"
            }
            elseif ($importPath -match "^organization\.entity|^system\.domain\.entity") {
                $expectedModule = "microservices-common-entity"
            }
            elseif ($importPath -match "^organization\.|^system\.domain\.") {
                $expectedModule = "microservices-common-business"
            }
            elseif ($importPath -match "^dto\.|^exception\.|^constant\.") {
                $expectedModule = "microservices-common-core"
            }

            if ($null -ne $expectedModule) {
                # 检查pom.xml中是否声明了该依赖
                $dependencyPattern = "<artifactId>$expectedModule</artifactId>"
                if ($pomContent -notmatch $dependencyPattern) {
                    $missingDependencies += @{
                        Module = $expectedModule
                        Import = $importPath
                        File   = $javaFile.FullName.Replace($ProjectRoot, "")
                    }
                }
            }
        }
    }

    # 去重
    $uniqueMissing = $missingDependencies | Sort-Object -Property Module -Unique

    if ($uniqueMissing.Count -gt 0) {
        $issues += "发现 $($uniqueMissing.Count) 个缺失的依赖声明"
        foreach ($missing in $uniqueMissing) {
            $issues += "  - 缺失依赖: $($missing.Module) (在 $($missing.File) 中使用)"
        }
    }

    return @{
        Module              = $ModuleName
        Status              = if ($issues.Count -eq 0) { "OK" } else { "ISSUES" }
        Issues              = $issues
        MissingDependencies = $uniqueMissing
    }
}

# =====================================================
# 检查循环依赖
# =====================================================

function Check-CircularDependencies {
    Write-ColorOutput "检查循环依赖..." "Cyan"

    try {
        Push-Location $MicroservicesDir

        # 使用Maven dependency:tree检查循环依赖
        $output = mvn dependency:tree -q 2>&1 | Out-String

        if ($output -match "circular|cycle") {
            Write-ColorOutput "  [WARN] 检测到可能的循环依赖" "Yellow"
            return $false
        }

        Write-ColorOutput "  [OK] 未检测到循环依赖" "Green"
        return $true
    }
    catch {
        Write-ColorOutput "  [ERROR] 检查循环依赖失败: $($_.Exception.Message)" "Red"
        return $false
    }
    finally {
        Pop-Location
    }
}

# =====================================================
# 验证Maven Reactor构建顺序
# =====================================================

function Verify-ReactorBuildOrder {
    Write-ColorOutput "验证Maven Reactor构建顺序..." "Cyan"

    try {
        Push-Location $MicroservicesDir

        # 使用Maven help:reactor计算构建顺序
        $output = mvn help:reactor -q 2>&1 | Out-String

        if ($output -match "BUILD SUCCESS") {
            Write-ColorOutput "  [OK] Maven Reactor构建顺序正确" "Green"
            if ($ShowDetails) {
                Write-ColorOutput $output "Gray"
            }
            return $true
        }
        else {
            Write-ColorOutput "  [WARN] Maven Reactor构建顺序可能有问题" "Yellow"
            if ($ShowDetails) {
                Write-ColorOutput $output "Yellow"
            }
            return $false
        }
    }
    catch {
        Write-ColorOutput "  [ERROR] 验证构建顺序失败: $($_.Exception.Message)" "Red"
        return $false
    }
    finally {
        Pop-Location
    }
}

# =====================================================
# 主程序
# =====================================================

$startTime = Get-Date

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 依赖关系审计" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  开始时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"

# 1. 检查循环依赖
Write-ColorOutput "" "White"
$circularCheck = Check-CircularDependencies

# 2. 验证Maven Reactor构建顺序
Write-ColorOutput "" "White"
$reactorCheck = Verify-ReactorBuildOrder

# 3. 审计各模块依赖关系
Write-ColorOutput "" "White"
Write-ColorOutput "审计各模块依赖关系..." "Cyan"

$modules = @(
    "microservices-common-core",
    "microservices-common-entity",
    "microservices-common-business",
    "microservices-common",
    "ioedream-access-service",
    "ioedream-attendance-service",
    "ioedream-consume-service",
    "ioedream-visitor-service",
    "ioedream-video-service",
    "ioedream-database-service"
)

$results = @()
$totalIssues = 0

foreach ($module in $modules) {
    $modulePath = Join-Path $MicroservicesDir $module
    if (-not (Test-Path $modulePath)) {
        Write-ColorOutput "  [SKIP] $module - 目录不存在" "Yellow"
        continue
    }

    $result = Audit-ModuleDependencies -ModulePath $modulePath -ModuleName $module
    $results += $result

    if ($result.Status -eq "ISSUES") {
        Write-ColorOutput "  [ISSUES] $module" "Yellow"
        $totalIssues += $result.Issues.Count
        if ($Verbose) {
            foreach ($issue in $result.Issues) {
                Write-ColorOutput "    - $issue" "Gray"
            }
        }
    }
    elseif ($result.Status -eq "OK") {
        Write-ColorOutput "  [OK] $module" "Green"
    }
}

# 4. 生成报告
Write-ColorOutput "" "White"
Write-ColorOutput "生成审计报告..." "Cyan"

$report = @"
IOE-DREAM 依赖关系审计报告
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

====================================================
一、循环依赖检查
====================================================
状态: $(if ($circularCheck) { "✅ 通过" } else { "❌ 失败" })

====================================================
二、Maven Reactor构建顺序验证
====================================================
状态: $(if ($reactorCheck) { "✅ 通过" } else { "❌ 失败" })

====================================================
三、模块依赖关系审计
====================================================
"@

foreach ($result in $results) {
    $report += "`n模块: $($result.Module)`n"
    $report += "状态: $($result.Status)`n"
    if ($result.Issues.Count -gt 0) {
        $report += "问题:`n"
        foreach ($issue in $result.Issues) {
            $report += "  - $issue`n"
        }
    }
    $report += "`n"
}

$report += @"
====================================================
四、总结
====================================================
总模块数: $($results.Count)
有问题模块: $(($results | Where-Object { $_.Status -eq "ISSUES" }).Count)
总问题数: $totalIssues

建议:
1. 修复缺失的依赖声明
2. 确保所有依赖都在pom.xml中正确声明
3. 使用Maven Reactor自动计算构建顺序
"@

$outputPath = Join-Path $ProjectRoot $OutputFile
$report | Out-File -FilePath $outputPath -Encoding UTF8

Write-ColorOutput "  [OK] 报告已生成: $outputPath" "Green"

# 5. 输出总结
$endTime = Get-Date
$duration = $endTime - $startTime

Write-ColorOutput "" "White"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  审计结果" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  总模块数: $($results.Count)" "Gray"
Write-ColorOutput "  有问题模块: $(($results | Where-Object { $_.Status -eq "ISSUES" }).Count)" $(if ($totalIssues -gt 0) { "Yellow" } else { "Green" })
Write-ColorOutput "  总问题数: $totalIssues" $(if ($totalIssues -gt 0) { "Yellow" } else { "Green" })
Write-ColorOutput "  耗时: $([math]::Round($duration.TotalSeconds, 1)) 秒" "Gray"
Write-ColorOutput ""

if ($totalIssues -gt 0) {
    Write-ColorOutput "  [WARN] 发现依赖关系问题，请查看报告: $outputPath" "Yellow"
    exit 1
}
else {
    Write-ColorOutput "  [SUCCESS] 依赖关系审计通过！" "Green"
    exit 0
}

