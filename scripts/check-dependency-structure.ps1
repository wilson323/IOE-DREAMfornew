# 模块依赖结构检查脚本
# 用途：检查模块依赖结构，确保无循环依赖、无冗余依赖、版本一致
# 执行：.\scripts\check-dependency-structure.ps1

param(
    [switch]$FailOnError = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"
$script:HasError = $false
$script:Warnings = @()

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
    $script:HasError = $true
}

function Write-Warning($Message) {
    Write-ColorOutput Yellow "⚠️  $Message"
    $script:Warnings += $Message
}

function Write-Info($Message) {
    if ($Verbose) {
        Write-ColorOutput Cyan "ℹ️  $Message"
    }
}

# 检查Maven是否可用
function Test-MavenAvailable {
    try {
        $mvnVersion = mvn --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Maven可用"
            return $true
        }
    }
    catch {
        Write-Error "Maven不可用，请先安装Maven"
        return $false
    }
    return $false
}

# 获取所有模块的依赖关系
function Get-ModuleDependencies {
    param([string]$ModulePath)

    $dependencies = @()
    $pomPath = Join-Path $ModulePath "pom.xml"

    if (-not (Test-Path $pomPath)) {
        return $dependencies
    }

    try {
        [xml]$pom = Get-Content $pomPath
        $ns = New-Object System.Xml.XmlNamespaceManager($pom.NameTable)
        $ns.AddNamespace("m", "http://maven.apache.org/POM/4.0.0")

        $deps = $pom.SelectNodes("//m:dependency", $ns)
        foreach ($dep in $deps) {
            $groupId = $dep.groupId
            $artifactId = $dep.artifactId
            $version = $dep.version

            # 只检查内部模块依赖
            if ($groupId -eq "net.lab1024.sa" -and $artifactId -like "microservices-*") {
                $dependencies += @{
                    GroupId    = $groupId
                    ArtifactId = $artifactId
                    Version    = $version
                }
            }
        }
    }
    catch {
        Write-Warning "无法解析 $pomPath : $_"
    }

    return $dependencies
}

# 检查循环依赖
function Test-CircularDependency {
    param([hashtable]$DependencyGraph)

    Write-Info "检查循环依赖..."
    $visited = @{}
    $recStack = @{}

    function Test-Cycle {
        param([string]$Module)

        $visited[$Module] = $true
        $recStack[$Module] = $true

        if ($DependencyGraph.ContainsKey($Module)) {
            foreach ($dep in $DependencyGraph[$Module]) {
                if (-not $visited.ContainsKey($dep)) {
                    if (Test-Cycle $dep) {
                        return $true
                    }
                }
                elseif ($recStack[$dep]) {
                    Write-Error "发现循环依赖: $Module -> $dep"
                    return $true
                }
            }
        }

        $recStack[$Module] = $false
        return $false
    }

    foreach ($module in $DependencyGraph.Keys) {
        if (-not $visited.ContainsKey($module)) {
            if (Test-Cycle $module) {
                return $false
            }
        }
    }

    Write-Success "无循环依赖"
    return $true
}

# 检查冗余依赖
function Test-RedundantDependency {
    param([hashtable]$ServiceDependencies)

    Write-Info "检查冗余依赖..."
    $hasRedundant = $false

    foreach ($service in $ServiceDependencies.Keys) {
        $deps = $ServiceDependencies[$service]
        $hasCommon = $deps | Where-Object { $_.ArtifactId -eq "microservices-common" }
        $hasGranular = $deps | Where-Object { $_.ArtifactId -like "microservices-common-*" }

        # 网关服务允许同时依赖microservices-common和细粒度模块
        if ($service -eq "ioedream-gateway-service") {
            continue
        }

        if ($hasCommon -and $hasGranular) {
            Write-Error "$service 同时依赖 microservices-common 和细粒度模块（冗余依赖）"
            $hasRedundant = $true
        }
    }

    if (-not $hasRedundant) {
        Write-Success "无冗余依赖"
    }

    return -not $hasRedundant
}

# 检查版本一致性
function Test-VersionConsistency {
    param([hashtable]$ServiceDependencies)

    Write-Info "检查版本一致性..."
    $versionMap = @{}
    $hasInconsistent = $false

    foreach ($service in $ServiceDependencies.Keys) {
        foreach ($dep in $ServiceDependencies[$service]) {
            $key = "$($dep.GroupId):$($dep.ArtifactId)"
            if (-not $versionMap.ContainsKey($key)) {
                $versionMap[$key] = $dep.Version
            }
            elseif ($versionMap[$key] -ne $dep.Version) {
                Write-Error "版本不一致: $key ($($versionMap[$key]) vs $($dep.Version))"
                $hasInconsistent = $true
            }
        }
    }

    if (-not $hasInconsistent) {
        Write-Success "版本一致"
    }

    return -not $hasInconsistent
}

# 检查依赖层次
function Test-DependencyLayers {
    param([hashtable]$DependencyGraph)

    Write-Info "检查依赖层次..."

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

    $hasViolation = $false

    # 检查第1层不能依赖其他层
    foreach ($module in $layer1) {
        if ($DependencyGraph.ContainsKey($module)) {
            foreach ($dep in $DependencyGraph[$module]) {
                if ($dep -notin $layer1) {
                    Write-Error "$module (第1层) 不能依赖 $dep"
                    $hasViolation = $true
                }
            }
        }
    }

    # 检查第2层不能依赖第3层
    foreach ($module in $layer2) {
        if ($DependencyGraph.ContainsKey($module)) {
            foreach ($dep in $DependencyGraph[$module]) {
                if ($dep -in $layer3) {
                    Write-Error "$module (第2层) 不能依赖 $dep (第3层)"
                    $hasViolation = $true
                }
            }
        }
    }

    if (-not $hasViolation) {
        Write-Success "依赖层次正确"
    }

    return -not $hasViolation
}

# 主函数
function Main {
    Write-Output "=========================================="
    Write-Output "模块依赖结构检查"
    Write-Output "=========================================="
    Write-Output ""

    # 检查Maven
    if (-not (Test-MavenAvailable)) {
        exit 1
    }

    # 获取项目根目录
    $projectRoot = Split-Path -Parent $PSScriptRoot
    $microservicesPath = Join-Path $projectRoot "microservices"

    if (-not (Test-Path $microservicesPath)) {
        Write-Error "找不到microservices目录: $microservicesPath"
        exit 1
    }

    # 构建依赖图
    Write-Info "构建依赖图..."
    $dependencyGraph = @{}
    $serviceDependencies = @{}

    $modules = Get-ChildItem -Path $microservicesPath -Directory
    foreach ($module in $modules) {
        $moduleName = $module.Name
        $deps = Get-ModuleDependencies -ModulePath $module.FullName

        if ($deps.Count -gt 0) {
            $dependencyGraph[$moduleName] = @()
            foreach ($dep in $deps) {
                $dependencyGraph[$moduleName] += $dep.ArtifactId
            }
        }

        # 收集服务依赖（用于冗余检查）
        if ($moduleName -like "ioedream-*-service") {
            $serviceDependencies[$moduleName] = $deps
        }
    }

    Write-Output ""
    Write-Output "=========================================="
    Write-Output "检查结果"
    Write-Output "=========================================="
    Write-Output ""

    # 执行检查
    $allPassed = $true

    $allPassed = $allPassed -and (Test-CircularDependency -DependencyGraph $dependencyGraph)
    $allPassed = $allPassed -and (Test-RedundantDependency -ServiceDependencies $serviceDependencies)
    $allPassed = $allPassed -and (Test-VersionConsistency -ServiceDependencies $serviceDependencies)
    $allPassed = $allPassed -and (Test-DependencyLayers -DependencyGraph $dependencyGraph)

    Write-Output ""
    Write-Output "=========================================="

    if ($allPassed) {
        Write-Success "所有检查通过！"
        if ($script:Warnings.Count -gt 0) {
            Write-Output ""
            Write-Output "警告:"
            foreach ($warning in $script:Warnings) {
                Write-Warning $warning
            }
        }
        exit 0
    }
    else {
        Write-Error "检查失败，请修复上述问题"
        if ($FailOnError) {
            exit 1
        }
        else {
            exit 0
        }
    }
}

# 执行主函数
Main

