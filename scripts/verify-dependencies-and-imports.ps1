# 依赖和导入路径验证脚本
# 用途：验证所有微服务的pom.xml依赖声明与Java代码中的import语句是否完全匹配
# 执行：.\scripts\verify-dependencies-and-imports.ps1

param(
    [switch]$FailOnError = $false,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Stop"
$script:HasError = $false
$script:Warnings = @()
$script:ImportIssues = @()

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

# 获取模块的依赖列表
function Get-ModuleDependencies {
    param([string]$ModulePath)
    
    $dependencies = @{}
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
            $scope = if ($dep.scope) { $dep.scope } else { "compile" }
            
            # 只检查内部模块依赖（compile和provided scope）
            if ($groupId -eq "net.lab1024.sa" -and ($scope -eq "compile" -or $scope -eq "")) {
                $key = "${groupId}:${artifactId}"
                $dependencies[$key] = @{
                    GroupId = $groupId
                    ArtifactId = $artifactId
                    Scope = $scope
                }
            }
        }
    } catch {
        Write-Warning "无法解析 $pomPath : $_"
    }
    
    return $dependencies
}

# 获取Java文件中的import语句
function Get-JavaImports {
    param([string]$JavaFile)
    
    $imports = @()
    
    if (-not (Test-Path $JavaFile)) {
        return $imports
    }
    
    try {
        $content = Get-Content $JavaFile -Raw
        # 匹配import语句
        $matches = [regex]::Matches($content, '^import\s+(.+?);', [System.Text.RegularExpressions.RegexOptions]::Multiline)
        
        foreach ($match in $matches) {
            $importPath = $match.Groups[1].Value.Trim()
            # 只检查内部包的import
            if ($importPath -like "net.lab1024.sa.common.*") {
                $imports += $importPath
            }
        }
    } catch {
        Write-Warning "无法读取 $JavaFile : $_"
    }
    
    return $imports
}

# 根据import路径推断需要的模块
function Get-RequiredModuleFromImport {
    param([string]$ImportPath)
    
    # 导入路径到模块的映射规则
    $mapping = @{
        "net.lab1024.sa.common.core" = "microservices-common-core"
        "net.lab1024.sa.common.entity" = "microservices-common-entity"
        "net.lab1024.sa.common.organization" = "microservices-common-business"
        "net.lab1024.sa.common.auth" = "microservices-common-security"
        "net.lab1024.sa.common.security" = "microservices-common-security"
        "net.lab1024.sa.common.permission" = "microservices-common-permission"
        "net.lab1024.sa.common.data" = "microservices-common-data"
        "net.lab1024.sa.common.cache" = "microservices-common-cache"
        "net.lab1024.sa.common.monitor" = "microservices-common-monitor"
        "net.lab1024.sa.common.export" = "microservices-common-export"
        "net.lab1024.sa.common.workflow" = "microservices-common-workflow"
        "net.lab1024.sa.common.storage" = "microservices-common-storage"
        "net.lab1024.sa.common.gateway" = "microservices-common"
        "net.lab1024.sa.common.config" = "microservices-common"
        "net.lab1024.sa.common.openapi" = "microservices-common"
        "net.lab1024.sa.common.edge" = "microservices-common"
        "net.lab1024.sa.common.factory" = "microservices-common"
    }
    
    foreach ($pattern in $mapping.Keys) {
        if ($ImportPath -like "$pattern*") {
            return $mapping[$pattern]
        }
    }
    
    # 默认情况：如果以common开头，可能需要common-core或common-business
    if ($ImportPath -like "net.lab1024.sa.common.*") {
        return "microservices-common-core"  # 默认假设
    }
    
    return $null
}

# 验证服务的依赖和导入
function Test-ServiceDependenciesAndImports {
    param(
        [string]$ServicePath,
        [hashtable]$AllModules
    )
    
    $serviceName = Split-Path -Leaf $ServicePath
    Write-Info "检查服务: $serviceName"
    
    # 获取服务的依赖
    $dependencies = Get-ModuleDependencies -ModulePath $ServicePath
    $dependencyModules = @()
    foreach ($key in $dependencies.Keys) {
        $dep = $dependencies[$key]
        $dependencyModules += $dep.ArtifactId
    }
    
    # 获取所有Java文件的import
    $javaFiles = Get-ChildItem -Path $ServicePath -Filter "*.java" -Recurse -ErrorAction SilentlyContinue
    $allImports = @{}
    
    foreach ($javaFile in $javaFiles) {
        $imports = Get-JavaImports -JavaFile $javaFile.FullName
        foreach ($importPath in $imports) {
            if (-not $allImports.ContainsKey($importPath)) {
                $allImports[$importPath] = @()
            }
            $allImports[$importPath] += $javaFile.FullName
        }
    }
    
    # 验证每个import是否有对应的依赖
    $missingDependencies = @{}
    
    foreach ($importPath in $allImports.Keys) {
        $requiredModule = Get-RequiredModuleFromImport -ImportPath $importPath
        
        if ($requiredModule -and $requiredModule -notin $dependencyModules) {
            if (-not $missingDependencies.ContainsKey($requiredModule)) {
                $missingDependencies[$requiredModule] = @()
            }
            $missingDependencies[$requiredModule] += $importPath
        }
    }
    
    # 报告问题
    if ($missingDependencies.Count -gt 0) {
        Write-Error "$serviceName 缺少依赖:"
        foreach ($module in $missingDependencies.Keys) {
            Write-Error "  - 缺少模块: $module"
            Write-Error "    使用的导入: $($missingDependencies[$module] -join ', ')"
            $script:ImportIssues += @{
                Service = $serviceName
                MissingModule = $module
                Imports = $missingDependencies[$module]
            }
        }
    } else {
        Write-Success "$serviceName 依赖和导入匹配"
    }
    
    return $missingDependencies.Count -eq 0
}

# 主函数
function Main {
    Write-Output "=========================================="
    Write-Output "依赖和导入路径验证"
    Write-Output "=========================================="
    Write-Output ""
    
    # 获取项目根目录
    $projectRoot = Split-Path -Parent $PSScriptRoot
    $microservicesPath = Join-Path $projectRoot "microservices"
    
    if (-not (Test-Path $microservicesPath)) {
        Write-Error "找不到microservices目录: $microservicesPath"
        exit 1
    }
    
    # 获取所有模块信息
    Write-Info "扫描所有模块..."
    $allModules = @{}
    $modules = Get-ChildItem -Path $microservicesPath -Directory
    foreach ($module in $modules) {
        if ($module.Name -like "microservices-*" -or $module.Name -like "ioedream-*-service") {
            $allModules[$module.Name] = $module.FullName
        }
    }
    
    Write-Output ""
    Write-Output "=========================================="
    Write-Output "检查结果"
    Write-Output "=========================================="
    Write-Output ""
    
    # 检查每个服务
    $allPassed = $true
    
    foreach ($serviceName in $allModules.Keys) {
        if ($serviceName -like "ioedream-*-service") {
            $servicePath = $allModules[$serviceName]
            $passed = Test-ServiceDependenciesAndImports -ServicePath $servicePath -AllModules $allModules
            $allPassed = $allPassed -and $passed
        }
    }
    
    Write-Output ""
    Write-Output "=========================================="
    
    if ($allPassed) {
        Write-Success "所有服务的依赖和导入路径验证通过！"
        if ($script:Warnings.Count -gt 0) {
            Write-Output ""
            Write-Output "警告:"
            foreach ($warning in $script:Warnings) {
                Write-Warning $warning
            }
        }
        exit 0
    } else {
        Write-Error "验证失败，发现依赖和导入不匹配问题"
        Write-Output ""
        Write-Output "问题详情:"
        foreach ($issue in $script:ImportIssues) {
            Write-Output "  服务: $($issue.Service)"
            Write-Output "  缺少模块: $($issue.MissingModule)"
            Write-Output "  使用的导入:"
            foreach ($import in $issue.Imports) {
                Write-Output "    - $import"
            }
            Write-Output ""
        }
        if ($FailOnError) {
            exit 1
        } else {
            exit 0
        }
    }
}

# 执行主函数
Main

