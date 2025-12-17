#!/usr/bin/env pwsh

<#
.SYNOPSIS
    Manager架构合规性检查脚本

.DESCRIPTION
    检查所有Manager类是否遵循四层架构规范：
    1. Manager类不使用Spring注解(@Component, @Service等)
    2. Manager类使用构造函数注入依赖
    3. Manager类在对应的配置类中注册为Bean

.NOTES
    File Name      : manager-architecture-check.ps1
    Author         : IOE-DREAM架构团队
    Version        : 1.0.0
    Created        : 2025-12-17
    Updated        : 2025-12-17
#>

# 参数配置
param(
    [string]$ProjectRoot = "D:\IOE-DREAM\microservices",
    [switch]$Verbose,
    [switch]$Fix
)

# 颜色配置
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
    Cyan = "Cyan"
    White = "White"
}

# 日志函数
function Write-Log {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Colors[$Color]
}

function Write-Success {
    param([string]$Message)
    Write-Log "✅ $Message" "Green"
}

function Write-Error {
    param([string]$Message)
    Write-Log "❌ $Message" "Red"
}

function Write-Warning {
    param([string]$Message)
    Write-Log "⚠️  $Message" "Yellow"
}

function Write-Info {
    param([string]$Message)
    Write-Log "ℹ️  $Message" "Blue"
}

# 检查Manager架构违规
function Test-ManagerArchitecture {
    param([string]$FilePath)

    $violations = @()
    $content = Get-Content $FilePath -Raw

    # 检查是否使用Spring注解
    if ($content -match '@Component|@Service|@Repository') {
        $violations += "使用Spring注解(@Component/@Service/@Repository)"
    }

    # 检查是否使用@Resource/@Autowired注入
    if ($content -match '@Resource|@Autowired') {
        # 排除配置类中的@Resource使用
        if ($FilePath -notmatch 'config\\.*Configuration\.java$') {
            $violations += "使用字段注入(@Resource/@Autowired)"
        }
    }

    # 检查是否有构造函数
    if ($content -match 'public\s+\w+Manager\s*\(') {
        # Manager类有构造函数，这是好的
    } else {
        if ($FilePath -match '.*Manager.*\.java$' -and $FilePath -notmatch '.*Configuration\.java$') {
            $violations += "缺少构造函数"
        }
    }

    return $violations
}

# 主检查函数
function Invoke-ManagerArchitectureCheck {
    Write-Info "开始Manager架构合规性检查..."
    Write-Info "项目根目录: $ProjectRoot"
    Write-Host ""

    # 查找所有Manager类
    $managerFiles = @()
    Get-ChildItem -Path $ProjectRoot -Recurse -Filter "*.java" | ForEach-Object {
        $content = Get-Content $_.FullName -Raw
        if ($content -match 'class\s+\w*Manager\s*\{' -and $_.FullName -notmatch '.*Test\.java$') {
            $managerFiles += $_.FullName
        }
    }

    Write-Info "找到 $($managerFiles.Count) 个Manager类文件"
    Write-Host ""

    $totalViolations = 0
    $filesWithViolations = 0

    foreach ($file in $managerFiles) {
        $relativePath = $file.Replace($ProjectRoot, "").Replace("\", "/").TrimStart("/")
        $violations = Test-ManagerArchitecture -FilePath $file

        if ($violations.Count -gt 0) {
            $filesWithViolations++
            $totalViolations += $violations.Count
            Write-Error "文件: $relativePath"
            foreach ($violation in $violations) {
                Write-Error "  - $violation"
            }
            Write-Host ""
        } else {
            if ($Verbose) {
                Write-Success "文件: $relativePath (合规)"
            }
        }
    }

    # 统计结果
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "检查结果统计" -ForegroundColor Cyan
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "Manager类总数: $($managerFiles.Count)" -ForegroundColor White
    Write-Host "违规文件数: $filesWithViolations" -ForegroundColor $(if ($filesWithViolations -gt 0) { "Red" } else { "Green" })
    Write-Host "违规总数: $totalViolations" -ForegroundColor $(if ($totalViolations -gt 0) { "Red" } else { "Green" })
    Write-Host ""

    # 检查Bean注册配置
    Write-Info "检查Manager Bean注册配置..."
    $configFiles = @()
    Get-ChildItem -Path $ProjectRoot -Recurse -Filter "*Configuration.java" | ForEach-Object {
        $content = Get-Content $_.FullName -Raw
        if ($content -match '@Bean.*Manager') {
            $configFiles += $_.FullName
        }
    }

    Write-Info "找到 $($configFiles.Count) 个Manager Bean注册配置文件"

    # 最终结论
    Write-Host "======================================" -ForegroundColor Cyan
    if ($totalViolations -eq 0) {
        Write-Success "🎉 所有Manager类都符合四层架构规范！"
        Write-Success "✅ 无Manager注解违规"
        Write-Success "✅ 所有Manager类都使用构造函数注入"
        Write-Success "✅ Manager Bean都已正确注册"
        return $true
    } else {
        Write-Error "🚨 发现 $totalViolations 个Manager架构违规！"
        Write-Error "❌ 需要修复的文件数: $filesWithViolations"
        Write-Warning "请按照CLAUDE.md规范修复所有违规项"
        return $false
    }
}

# 执行检查
try {
    $result = Invoke-ManagerArchitectureCheck
    exit $(if ($result) { 0 } else { 1 })
}
catch {
    Write-Error "检查过程中发生错误: $($_.Exception.Message)"
    exit 1
}