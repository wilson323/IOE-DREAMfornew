# =====================================================
# IOE-DREAM 构建顺序验证脚本
# 版本: v1.0.0
# 描述: 验证Maven Reactor构建顺序是否正确
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory = $false)]
    [switch]$ShowDetails,

    [Parameter(Mandatory = $false)]
    [switch]$Fix,

    [Parameter(Mandatory = $false)]
    [string]$OutputFile = "build-order-verification-report.txt"
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
# 获取Maven Reactor构建顺序
# =====================================================

function Get-ReactorBuildOrder {
    Write-ColorOutput "获取Maven Reactor构建顺序..." "Cyan"

    try {
        Push-Location $MicroservicesDir

        # 使用Maven dependency:tree获取依赖关系，然后分析构建顺序
        # 或者使用Maven compile的dry-run模式获取构建顺序
        $output = mvn clean compile -DskipTests -q 2>&1 | Out-String

        # 如果编译失败，尝试使用dependency:tree
        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput "  [INFO] 编译失败，使用dependency:tree分析依赖关系" "Yellow"
            $output = mvn dependency:tree -q 2>&1 | Out-String
        }

        if ($LASTEXITCODE -ne 0) {
            Write-ColorOutput "  [ERROR] 获取构建顺序失败" "Red"
            if ($ShowDetails) {
                Write-ColorOutput $output "Red"
            }
            return $null
        }

        # 解析输出，提取模块顺序
        $buildOrder = @()
        $lines = $output -split "`n"

        # 方法1: 从编译输出中提取构建顺序
        foreach ($line in $lines) {
            if ($line -match '\[INFO\] Building\s+(\S+)\s+') {
                $moduleName = $matches[1]
                # 提取模块名称（去掉版本号）
                if ($moduleName -match '^([^-]+(-[^-]+)*)') {
                    $moduleName = $matches[1]
                    if ($moduleName -notin $buildOrder) {
                        $buildOrder += $moduleName
                    }
                }
            }
        }

        # 方法2: 如果方法1失败，从pom.xml中读取modules顺序
        if ($buildOrder.Count -eq 0) {
            Write-ColorOutput "  [INFO] 从pom.xml读取modules顺序" "Yellow"
            $parentPom = Join-Path $MicroservicesDir "pom.xml"
            if (Test-Path $parentPom) {
                $pomContent = Get-Content $parentPom -Raw
                $moduleMatches = [regex]::Matches($pomContent, '<module>([^<]+)</module>')
                foreach ($match in $moduleMatches) {
                    $buildOrder += $match.Groups[1].Value
                }
            }
        }

        Write-ColorOutput "  [OK] 成功获取构建顺序" "Green"
        return $buildOrder
    }
    catch {
        Write-ColorOutput "  [ERROR] 获取构建顺序异常: $($_.Exception.Message)" "Red"
        return $null
    }
    finally {
        Pop-Location
    }
}

# =====================================================
# 验证构建顺序合理性
# =====================================================

function Verify-BuildOrderReasonableness {
    param([string[]]$BuildOrder)

    Write-ColorOutput "验证构建顺序合理性..." "Cyan"

    # 定义理想的构建顺序层次
    $idealLayers = @{
        "Layer1" = @("microservices-common-core", "microservices-common-entity")
        "Layer2" = @("microservices-common-data", "microservices-common-security", "microservices-common-cache",
            "microservices-common-monitor", "microservices-common-export", "microservices-common-workflow")
        "Layer3" = @("microservices-common-business", "microservices-common-permission")
        "Layer4" = @("microservices-common")
        "Layer5" = @("ioedream-db-init")
        "Layer6" = @("ioedream-gateway-service", "ioedream-common-service", "ioedream-device-comm-service",
            "ioedream-oa-service", "ioedream-access-service", "ioedream-attendance-service",
            "ioedream-video-service", "ioedream-consume-service", "ioedream-visitor-service",
            "ioedream-database-service", "ioedream-biometric-service")
    }

    $issues = @()

    # 检查Layer1是否在最前面
    $layer1Index = -1
    foreach ($module in $BuildOrder) {
        if ($module -in $idealLayers["Layer1"]) {
            $layer1Index = $BuildOrder.IndexOf($module)
            break
        }
    }

    if ($layer1Index -eq -1) {
        $issues += "Layer1模块（common-core, common-entity）未找到"
    }
    elseif ($layer1Index -gt 2) {
        $issues += "Layer1模块应该在构建顺序的前面，但实际位置: $layer1Index"
    }

    # 检查Layer6（业务服务）是否在最后
    $layer6Count = 0
    $layer6FirstIndex = -1
    foreach ($module in $BuildOrder) {
        if ($module -in $idealLayers["Layer6"]) {
            $layer6Count++
            if ($layer6FirstIndex -eq -1) {
                $layer6FirstIndex = $BuildOrder.IndexOf($module)
            }
        }
    }

    if ($layer6FirstIndex -ne -1 -and $layer6FirstIndex -lt ($BuildOrder.Count - $layer6Count)) {
        $issues += "Layer6模块（业务服务）应该在构建顺序的最后，但实际位置: $layer6FirstIndex"
    }

    # 检查common-business是否在common-core之后
    $coreIndex = $BuildOrder.IndexOf("microservices-common-core")
    $businessIndex = $BuildOrder.IndexOf("microservices-common-business")

    if ($coreIndex -ne -1 -and $businessIndex -ne -1) {
        if ($businessIndex -lt $coreIndex) {
            $issues += "common-business应该在common-core之后构建，但实际顺序相反"
        }
    }

    if ($issues.Count -eq 0) {
        Write-ColorOutput "  [OK] 构建顺序合理" "Green"
        return $true
    }
    else {
        Write-ColorOutput "  [WARN] 发现构建顺序问题" "Yellow"
        foreach ($issue in $issues) {
            Write-ColorOutput "    - $issue" "Yellow"
        }
        return $false
    }
}

# =====================================================
# 检查模块依赖关系
# =====================================================

function Check-ModuleDependencies {
    param([string[]]$BuildOrder)

    Write-ColorOutput "检查模块依赖关系..." "Cyan"

    $issues = @()

    # 检查关键依赖关系
    $keyDependencies = @{
        "microservices-common-business" = @("microservices-common-core", "microservices-common-entity")
        "microservices-common"          = @("microservices-common-business")
        "ioedream-access-service"       = @("microservices-common-core", "microservices-common-business")
    }

    foreach ($module in $keyDependencies.Keys) {
        $moduleIndex = $BuildOrder.IndexOf($module)
        if ($moduleIndex -eq -1) { continue }

        $requiredDeps = $keyDependencies[$module]
        foreach ($dep in $requiredDeps) {
            $depIndex = $BuildOrder.IndexOf($dep)
            if ($depIndex -eq -1) {
                $issues += "$module 依赖 $dep，但 $dep 不在构建顺序中"
            }
            elseif ($depIndex -gt $moduleIndex) {
                $issues += "$module 依赖 $dep，但 $dep 在构建顺序中排在 $module 之后（位置: $depIndex > $moduleIndex）"
            }
        }
    }

    if ($issues.Count -eq 0) {
        Write-ColorOutput "  [OK] 模块依赖关系正确" "Green"
        return $true
    }
    else {
        Write-ColorOutput "  [WARN] 发现依赖关系问题" "Yellow"
        foreach ($issue in $issues) {
            Write-ColorOutput "    - $issue" "Yellow"
        }
        return $false
    }
}

# =====================================================
# 主程序
# =====================================================

$startTime = Get-Date

Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput "  IOE-DREAM 构建顺序验证" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  开始时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" "Gray"

# 1. 获取Maven Reactor构建顺序
Write-ColorOutput "" "White"
$buildOrder = Get-ReactorBuildOrder

if ($null -eq $buildOrder -or $buildOrder.Count -eq 0) {
    Write-ColorOutput "  [ERROR] 无法获取构建顺序，验证失败" "Red"
    exit 1
}

Write-ColorOutput "  构建顺序（共 $($buildOrder.Count) 个模块）:" "Gray"
if ($ShowDetails) {
    for ($i = 0; $i -lt $buildOrder.Count; $i++) {
        Write-ColorOutput "    $($i + 1). $($buildOrder[$i])" "Gray"
    }
}

# 2. 验证构建顺序合理性
Write-ColorOutput "" "White"
$reasonablenessCheck = Verify-BuildOrderReasonableness -BuildOrder $buildOrder

# 3. 检查模块依赖关系
Write-ColorOutput "" "White"
$dependencyCheck = Check-ModuleDependencies -BuildOrder $buildOrder

# 4. 生成报告
Write-ColorOutput "" "White"
Write-ColorOutput "生成验证报告..." "Cyan"

$report = @"
IOE-DREAM 构建顺序验证报告
生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

====================================================
一、Maven Reactor构建顺序
====================================================
总模块数: $($buildOrder.Count)

构建顺序:
"@

for ($i = 0; $i -lt $buildOrder.Count; $i++) {
    $report += "$($i + 1). $($buildOrder[$i])`n"
}

$report += @"

====================================================
二、构建顺序合理性验证
====================================================
状态: $(if ($reasonablenessCheck) { "✅ 通过" } else { "❌ 失败" })

====================================================
三、模块依赖关系检查
====================================================
状态: $(if ($dependencyCheck) { "✅ 通过" } else { "❌ 失败" })

====================================================
四、总结
====================================================
构建顺序验证: $(if ($reasonablenessCheck -and $dependencyCheck) { "✅ 通过" } else { "❌ 失败" })

建议:
1. 确保所有依赖关系在pom.xml中正确声明
2. 确保父POM的modules顺序符合依赖关系
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
Write-ColorOutput "  验证结果" "Cyan"
Write-ColorOutput "======================================================" "Cyan"
Write-ColorOutput ""
Write-ColorOutput "  构建顺序合理性: $(if ($reasonablenessCheck) { "✅ 通过" } else { "❌ 失败" })" $(if ($reasonablenessCheck) { "Green" } else { "Red" })
Write-ColorOutput "  模块依赖关系: $(if ($dependencyCheck) { "✅ 通过" } else { "❌ 失败" })" $(if ($dependencyCheck) { "Green" } else { "Red" })
Write-ColorOutput "  耗时: $([math]::Round($duration.TotalSeconds, 1)) 秒" "Gray"
Write-ColorOutput ""

if ($reasonablenessCheck -and $dependencyCheck) {
    Write-ColorOutput "  [SUCCESS] 构建顺序验证通过！" "Green"
    exit 0
}
else {
    Write-ColorOutput "  [WARN] 构建顺序验证失败，请查看报告: $outputPath" "Yellow"
    exit 1
}

