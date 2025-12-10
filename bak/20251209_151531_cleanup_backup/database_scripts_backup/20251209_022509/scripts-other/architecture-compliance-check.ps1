# IOE-DREAM 架构合规性检查脚本
# 功能: 检查架构违规（@Autowired、@Repository、命名规范等）
# 作者: IOE-DREAM架构团队
# 日期: 2025-01-30

$ErrorActionPreference = "Stop"

# 颜色输出函数
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

# 检查结果统计
$script:Violations = @{
    Autowired = @()
    Repository = @()
    RepositoryNaming = @()
    CrossLayerAccess = @()
}

# 检查@Autowired违规
function Check-AutowiredViolations {
    param([string]$FilePath)

    $content = Get-Content $FilePath -Raw
    if ($content -match '@Autowired') {
        $script:Violations.Autowired += $FilePath
        return $true
    }
    return $false
}

# 检查@Repository违规
function Check-RepositoryViolations {
    param([string]$FilePath)

    $content = Get-Content $FilePath -Raw
    if ($content -match '@Repository') {
        $script:Violations.Repository += $FilePath
        return $true
    }
    return $false
}

# 检查Repository命名违规
function Check-RepositoryNamingViolations {
    param([string]$FilePath)

    $fileName = Split-Path $FilePath -Leaf
    if ($fileName -match 'Repository\.java$') {
        $script:Violations.RepositoryNaming += $FilePath
        return $true
    }
    return $false
}

# 检查跨层访问违规（简化版，检查Controller直接调用DAO）
function Check-CrossLayerAccess {
    param([string]$FilePath)

    # 只检查Controller文件
    if ($FilePath -notmatch 'Controller\.java$') {
        return $false
    }

    $content = Get-Content $FilePath -Raw

    # 检查Controller中是否直接注入DAO
    if ($content -match '@Resource\s+.*Dao\s+\w+Dao') {
        # 进一步检查是否直接调用DAO方法
        if ($content -match '\w+Dao\.(select|insert|update|delete)') {
            $script:Violations.CrossLayerAccess += $FilePath
            return $true
        }
    }

    return $false
}

# 生成检查报告
function Generate-Report {
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "架构合规性检查报告" "Cyan"
    Write-ColorOutput "========================================" "Cyan"
    Write-Host ""

    $totalViolations = 0

    # @Autowired违规
    if ($script:Violations.Autowired.Count -gt 0) {
        Write-ColorOutput "🔴 @Autowired违规: $($script:Violations.Autowired.Count) 个文件" "Red"
        foreach ($file in $script:Violations.Autowired) {
            Write-ColorOutput "   - $file" "Red"
        }
        $totalViolations += $script:Violations.Autowired.Count
        Write-Host ""
    }
    else {
        Write-ColorOutput "✅ @Autowired合规: 0个违规" "Green"
    }

    # @Repository违规
    if ($script:Violations.Repository.Count -gt 0) {
        Write-ColorOutput "🔴 @Repository违规: $($script:Violations.Repository.Count) 个文件" "Red"
        foreach ($file in $script:Violations.Repository) {
            Write-ColorOutput "   - $file" "Red"
        }
        $totalViolations += $script:Violations.Repository.Count
        Write-Host ""
    }
    else {
        Write-ColorOutput "✅ @Repository合规: 0个违规" "Green"
    }

    # Repository命名违规
    if ($script:Violations.RepositoryNaming.Count -gt 0) {
        Write-ColorOutput "🔴 Repository命名违规: $($script:Violations.RepositoryNaming.Count) 个文件" "Red"
        foreach ($file in $script:Violations.RepositoryNaming) {
            Write-ColorOutput "   - $file" "Red"
        }
        $totalViolations += $script:Violations.RepositoryNaming.Count
        Write-Host ""
    }
    else {
        Write-ColorOutput "✅ Repository命名合规: 0个违规" "Green"
    }

    # 跨层访问违规
    if ($script:Violations.CrossLayerAccess.Count -gt 0) {
        Write-ColorOutput "🔴 跨层访问违规: $($script:Violations.CrossLayerAccess.Count) 个文件" "Red"
        foreach ($file in $script:Violations.CrossLayerAccess) {
            Write-ColorOutput "   - $file" "Red"
        }
        $totalViolations += $script:Violations.CrossLayerAccess.Count
        Write-Host ""
    }
    else {
        Write-ColorOutput "✅ 跨层访问合规: 0个违规" "Green"
    }

    Write-Host ""
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "总计违规: $totalViolations 个文件" "Cyan"
    Write-ColorOutput "========================================" "Cyan"

    # 合规率计算
    $totalFiles = $script:TotalFiles
    if ($totalFiles -gt 0) {
        $complianceRate = [math]::Round((1 - $totalViolations / $totalFiles) * 100, 2)
        Write-ColorOutput "合规率: $complianceRate%" "Cyan"
    }

    # 返回合规状态
    return $totalViolations -eq 0
}

# 主函数
function Main {
    Write-ColorOutput "========================================" "Cyan"
    Write-ColorOutput "IOE-DREAM 架构合规性检查" "Cyan"
    Write-ColorOutput "========================================" "Cyan"
    Write-Host ""

    # 检查项目根目录
    $projectRoot = $PSScriptRoot
    if (-not (Test-Path "$projectRoot\..\microservices")) {
        Write-ColorOutput "❌ 错误: 未找到microservices目录" "Red"
        exit 1
    }

    $microservicesPath = "$projectRoot\..\microservices"

    Write-ColorOutput "📁 扫描目录: $microservicesPath" "Yellow"
    Write-Host ""

    # 扫描所有Java文件
    $javaFiles = Get-ChildItem -Path $microservicesPath -Filter "*.java" -Recurse | Where-Object {
        $_.FullName -notmatch '\\target\\' -and
        $_.FullName -notmatch '\\test\\' -and
        $_.FullName -notmatch '\\archive\\'
    }

    $script:TotalFiles = $javaFiles.Count
    Write-ColorOutput "📊 发现 $($script:TotalFiles) 个Java文件" "Yellow"
    Write-Host ""

    Write-ColorOutput "🔍 开始检查架构违规..." "Yellow"
    Write-Host ""

    # 执行各项检查
    foreach ($file in $javaFiles) {
        Check-AutowiredViolations $file.FullName | Out-Null
        Check-RepositoryViolations $file.FullName | Out-Null
        Check-RepositoryNamingViolations $file.FullName | Out-Null
        Check-CrossLayerAccess $file.FullName | Out-Null
    }

    Write-Host ""

    # 生成报告
    $isCompliant = Generate-Report

    # 返回退出码
    if ($isCompliant) {
        Write-Host ""
        Write-ColorOutput "✅ 架构合规性检查通过！" "Green"
        exit 0
    }
    else {
        Write-Host ""
        Write-ColorOutput "❌ 发现架构违规，请修复后重新检查！" "Red"
        Write-ColorOutput "💡 提示: 运行 .\scripts\fix-architecture-violations.ps1 自动修复" "Yellow"
        exit 1
    }
}

# 执行主函数
try {
    Main
}
catch {
    Write-ColorOutput "❌ 脚本执行失败: $_" "Red"
    Write-ColorOutput "   堆栈跟踪: $($_.ScriptStackTrace)" "Red"
    exit 1
}
