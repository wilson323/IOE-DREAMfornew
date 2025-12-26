# IOE-DREAM 全局依赖深度分析脚本
# 功能：系统性检测循环依赖、异常依赖模式
# 执行：.\scripts\comprehensive-dependency-analysis.ps1

param(
    [switch]$GenerateReport = $false,
    [string]$ReportPath = "dependency-analysis-comprehensive.md"
)

$ErrorActionPreference = "Stop"
$script:Issues = @()
$script:Cycles = @()
$script:DependencyGraph = @{}
$script:ModuleDependencies = @{}

# 颜色输出函数
function Write-ColorOutput($ForegroundColor, $Message) {
    $fc = $host.UI.RawUI.ForegroundColor
    $host.UI.RawUI.ForegroundColor = $ForegroundColor
    Write-Output $Message
    $host.UI.RawUI.ForegroundColor = $fc
}

function Write-Success($Message) {
    Write-ColorOutput Green "✅ $Message"
}

function Write-Error($Message) {
    Write-ColorOutput Red "❌ $Message"
    $script:Issues += @{
        Type    = "ERROR"
        Message = $Message
    }
}

function Write-Warning($Message) {
    Write-ColorOutput Yellow "⚠️  $Message"
    $script:Issues += @{
        Type    = "WARNING"
        Message = $Message
    }
}

# 读取模块POM文件
function Read-ModulePom {
    param([string]$ModulePath)

    $pomPath = Join-Path $ModulePath "pom.xml"
    if (-not (Test-Path $pomPath)) {
        return $null
    }

    try {
        [xml]$pom = Get-Content $pomPath -Encoding UTF8
        $ns = New-Object System.Xml.XmlNamespaceManager($pom.NameTable)
        $ns.AddNamespace("m", "http://maven.apache.org/POM/4.0.0")

        return @{
            Pom       = $pom
            Namespace = $ns
            Path      = $pomPath
        }
    }
    catch {
        Write-Warning "无法解析POM文件: $pomPath - $_"
        return $null
    }
}

# 获取模块的内部依赖
function Get-InternalDependencies {
    param([hashtable]$PomData)

    if ($null -eq $PomData) {
        return @()
    }

    $dependencies = @()
    $deps = $PomData.Pom.SelectNodes("//m:dependency", $PomData.Namespace)

    foreach ($dep in $deps) {
        $groupId = $dep.groupId
        $artifactId = $dep.artifactId

        # 只检查内部模块依赖（net.lab1024.sa）
        if ($groupId -eq "net.lab1024.sa" -and $artifactId -like "microservices-*") {
            $dependencies += $artifactId
        }
    }

    return $dependencies
}

# 获取模块artifactId
function Get-ModuleArtifactId {
    param([hashtable]$PomData)

    if ($null -eq $PomData) {
        return $null
    }

    $ns = $PomData.Namespace
    $artifactId = $PomData.Pom.SelectSingleNode("//m:artifactId", $ns)

    if ($artifactId) {
        return $artifactId.InnerText
    }

    return $null
}

# 检测循环依赖（DFS算法）
function Test-CircularDependencyDFS {
    param([hashtable]$Graph)

    $visited = @{}
    $recStack = @{}
    $cycles = @()

    function DFS-Cycle {
        param([string]$Node, [string[]]$Path)

        $visited[$Node] = $true
        $recStack[$Node] = $true
        $newPath = $Path + @($Node)

        if ($Graph.ContainsKey($Node)) {
            foreach ($neighbor in $Graph[$Node]) {
                if (-not $visited.ContainsKey($neighbor)) {
                    $result = DFS-Cycle -Node $neighbor -Path $newPath
                    if ($null -ne $result) {
                        return $result
                    }
                }
                elseif ($recStack[$neighbor]) {
                    # 发现循环依赖
                    $cycleStart = $newPath.IndexOf($neighbor)
                    $cycle = $newPath[$cycleStart..($newPath.Length - 1)] + @($neighbor)
                    return $cycle
                }
            }
        }

        $recStack[$Node] = $false
        return $null
    }

    foreach ($node in $Graph.Keys) {
        if (-not $visited.ContainsKey($node)) {
            $cycle = DFS-Cycle -Node $node -Path @()
            if ($null -ne $cycle) {
                $cycles += , $cycle
                $cycleStr = ($cycle -join " -> ")
                Write-Error "发现循环依赖: $cycleStr"
                $script:Cycles += @{
                    Path    = $cycle
                    Display = $cycleStr
                }
            }
        }
    }

    return $cycles.Count -eq 0
}

# 检查异常依赖模式
function Test-AnomalousDependencies {
    param([hashtable]$ModuleDeps)

    $hasAnomalies = $false

    # 定义依赖层次
    $layer1 = @("microservices-common-core", "microservices-common-entity")
    $layer2 = @(
        "microservices-common-storage",
        "microservices-common-data",
        "microservices-common-security",
        "microservices-common-cache",
        "microservices-common-monitor",
        "microservices-common-export",
        "microservices-common-workflow",
        "microservices-common-business",
        "microservices-common-permission"
    )
    $layer3 = @("microservices-common")

    foreach ($module in $ModuleDeps.Keys) {
        $deps = $ModuleDeps[$module]

        # 检查1: common-core不能依赖任何其他common模块
        if ($module -eq "microservices-common-core") {
            foreach ($dep in $deps) {
                if ($dep -like "microservices-*") {
                    Write-Error "$module (第1层核心模块) 禁止依赖其他common模块，但依赖了: $dep"
                    $hasAnomalies = $true
                }
            }
        }

        # 检查2: 第1层模块不能依赖第2层或第3层
        if ($module -in $layer1) {
            foreach ($dep in $deps) {
                if ($dep -in $layer2 -or $dep -in $layer3) {
                    Write-Error "$module (第1层) 禁止依赖 $dep ($(if ($dep -in $layer2) { '第2层' } else { '第3层' }))"
                    $hasAnomalies = $true
                }
            }
        }

        # 检查3: 细粒度模块（第2层）不能依赖microservices-common（第3层）
        if ($module -in $layer2) {
            foreach ($dep in $deps) {
                if ($dep -eq "microservices-common") {
                    Write-Error "$module (第2层细粒度模块) 禁止依赖 microservices-common (第3层配置类容器)"
                    $hasAnomalies = $true
                }
            }
        }

        # 检查4: 同层模块相互依赖检查
        if ($module -in $layer2) {
            foreach ($dep in $deps) {
                if ($dep -in $layer2 -and $dep -ne $module) {
                    Write-Warning "$module (第2层) 依赖同层模块 $dep (建议审查是否必要)"
                    # 同层依赖不是错误，但需要审查
                }
            }
        }

        # 检查5: entity模块不能依赖business（应该相反）
        if ($module -eq "microservices-common-entity") {
            foreach ($dep in $deps) {
                if ($dep -eq "microservices-common-business") {
                    Write-Error "$module 禁止依赖 $dep (依赖方向错误，应该是business依赖entity)"
                    $hasAnomalies = $true
                }
            }
        }
    }

    return -not $hasAnomalies
}

# 主函数
function Main {
    Write-Output "=========================================="
    Write-Output "IOE-DREAM 全局依赖深度分析"
    Write-Output "=========================================="
    Write-Output ""

    # 获取项目根目录
    $projectRoot = Split-Path -Parent $PSScriptRoot
    $microservicesPath = Join-Path $projectRoot "microservices"

    if (-not (Test-Path $microservicesPath)) {
        Write-Error "找不到microservices目录: $microservicesPath"
        exit 1
    }

    Write-Output "[1/4] 扫描所有模块..."
    $modules = Get-ChildItem -Path $microservicesPath -Directory | Where-Object {
        $_.Name -like "microservices-*" -or $_.Name -like "ioedream-*"
    }

    Write-Output "  发现模块: $($modules.Count) 个"
    Write-Output ""

    Write-Output "[2/4] 构建依赖图..."
    foreach ($module in $modules) {
        $pomData = Read-ModulePom -ModulePath $module.FullName
        if ($null -ne $pomData) {
            $artifactId = Get-ModuleArtifactId -PomData $pomData
            if ($artifactId) {
                $deps = Get-InternalDependencies -PomData $pomData
                if ($deps.Count -gt 0) {
                    $script:DependencyGraph[$artifactId] = $deps
                    $script:ModuleDependencies[$artifactId] = $deps
                    Write-Output "  $artifactId -> $($deps -join ', ')"
                }
                else {
                    $script:DependencyGraph[$artifactId] = @()
                    $script:ModuleDependencies[$artifactId] = @()
                }
            }
        }
    }

    Write-Output ""
    Write-Output "[3/4] 检测循环依赖..."
    $noCycles = Test-CircularDependencyDFS -Graph $script:DependencyGraph
    if ($noCycles) {
        Write-Success "无循环依赖"
    }
    else {
        Write-Error "发现 $($script:Cycles.Count) 个循环依赖"
    }

    Write-Output ""
    Write-Output "[4/4] 检查异常依赖模式..."
    $noAnomalies = Test-AnomalousDependencies -ModuleDeps $script:ModuleDependencies
    if ($noAnomalies) {
        Write-Success "无异常依赖模式"
    }
    else {
        Write-Error "发现异常依赖模式"
    }

    Write-Output ""
    Write-Output "=========================================="
    Write-Output "分析结果汇总"
    Write-Output "=========================================="
    Write-Output ""

    $errorCount = ($script:Issues | Where-Object { $_.Type -eq "ERROR" }).Count
    $warningCount = ($script:Issues | Where-Object { $_.Type -eq "WARNING" }).Count

    Write-Output "总模块数: $($script:DependencyGraph.Keys.Count)"
    Write-Output "循环依赖: $($script:Cycles.Count) 个"
    Write-Output "错误: $errorCount 个"
    Write-Output "警告: $warningCount 个"
    Write-Output ""

    if ($errorCount -eq 0 -and $script:Cycles.Count -eq 0) {
        Write-Success "✅ 所有检查通过！依赖结构健康"
        exit 0
    }
    else {
        Write-Error "检查失败，请修复上述问题"

        if ($GenerateReport) {
            Generate-Report
        }

        exit 1
    }
}

# 生成详细报告
function Generate-Report {
    $reportPath = Join-Path (Split-Path -Parent $PSScriptRoot) $ReportPath

    $report = @"
# IOE-DREAM 全局依赖分析报告

生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

## 📊 分析概要

- **总模块数**: $($script:DependencyGraph.Keys.Count)
- **循环依赖数**: $($script:Cycles.Count)
- **错误数**: $(($script:Issues | Where-Object { $_.Type -eq "ERROR" }).Count)
- **警告数**: $(($script:Issues | Where-Object { $_.Type -eq "WARNING" }).Count)

## 🔄 循环依赖详情

"@

    if ($script:Cycles.Count -eq 0) {
        $report += "✅ 无循环依赖`n`n"
    }
    else {
        foreach ($cycle in $script:Cycles) {
            $report += "### 循环路径 $($script:Cycles.IndexOf($cycle) + 1)`n"
            $report += "```\n"
            $report += "$($cycle.Display)\n"
            $report += "```\n\n"
        }
    }

    $report += "## ❌ 错误详情`n`n"
    $errors = $script:Issues | Where-Object { $_.Type -eq "ERROR" }
    if ($errors.Count -eq 0) {
        $report += "✅ 无错误`n`n"
    }
    else {
        foreach ($error in $errors) {
            $report += "- $($error.Message)`n"
        }
        $report += "`n"
    }

    $report += "## ⚠️ 警告详情`n`n"
    $warnings = $script:Issues | Where-Object { $_.Type -eq "WARNING" }
    if ($warnings.Count -eq 0) {
        $report += "✅ 无警告`n`n"
    }
    else {
        foreach ($warning in $warnings) {
            $report += "- $($warning.Message)`n"
        }
        $report += "`n"
    }

    $report += "## 📈 依赖关系图`n`n"
    $report += "```\n"
    foreach ($module in $script:DependencyGraph.Keys | Sort-Object) {
        $deps = $script:DependencyGraph[$module]
        if ($deps.Count -gt 0) {
            $report += "$module`n"
            foreach ($dep in $deps) {
                $report += "  -> $dep`n"
            }
        }
        else {
            $report += "$module (无内部依赖)`n"
        }
    }
    $report += "```\n"

    $report | Out-File -FilePath $reportPath -Encoding UTF8
    Write-Output "报告已生成: $reportPath"
}

# 执行主函数
Main

