# =====================================================
# 代码覆盖率分析脚本
# 版本: v1.0.0
# 描述: 运行所有测试并生成覆盖率报告，分析未覆盖的代码区域
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$SkipTests,
    
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport,
    
    [Parameter(Mandatory=$false)]
    [string]$Module = ""
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "代码覆盖率分析" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 切换到项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

# 切换到microservices目录
Set-Location "microservices"

# 构建命令
$mvnCommand = "mvn clean"

if (-not $SkipTests) {
    $mvnCommand += " test"
}

if ($GenerateReport) {
    $mvnCommand += " jacoco:report"
}

# 如果指定了模块，只分析该模块
if ($Module -ne "") {
    $mvnCommand += " -pl $Module"
}

Write-Host "[1/3] 运行测试..." -ForegroundColor Yellow
Write-Host "命令: $mvnCommand" -ForegroundColor Gray
Write-Host ""

# 运行Maven命令
Invoke-Expression $mvnCommand

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] 测试执行失败" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/3] 生成覆盖率报告..." -ForegroundColor Yellow

# 生成覆盖率报告
if (-not $GenerateReport) {
    $reportCommand = "mvn jacoco:report"
    if ($Module -ne "") {
        $reportCommand += " -pl $Module"
    }
    Invoke-Expression $reportCommand
}

Write-Host ""
Write-Host "[3/3] 分析覆盖率报告..." -ForegroundColor Yellow

# 查找所有覆盖率报告
$reports = Get-ChildItem -Path "." -Filter "index.html" -Recurse | 
    Where-Object { $_.FullName -like "*jacoco*" }

if ($reports.Count -eq 0) {
    Write-Host "[WARN] 未找到覆盖率报告" -ForegroundColor Yellow
    Write-Host "请先运行: mvn clean test jacoco:report" -ForegroundColor Yellow
    exit 0
}

Write-Host ""
Write-Host "找到 $($reports.Count) 个覆盖率报告:" -ForegroundColor Green
foreach ($report in $reports) {
    $moduleName = if ($report.FullName -match "ioedream-(\w+)-service|microservices-common") {
        $matches[1]
    } else {
        "unknown"
    }
    Write-Host "  - $moduleName : $($report.FullName)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "[OK] 覆盖率分析完成" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "查看报告:" -ForegroundColor White
Write-Host "  打开HTML报告: target/site/jacoco/index.html" -ForegroundColor Gray
Write-Host "  或使用以下命令打开所有报告:" -ForegroundColor Gray
Write-Host "  Get-ChildItem -Path '.' -Filter 'index.html' -Recurse | Where-Object { `$_.FullName -like '*jacoco*' } | ForEach-Object { Start-Process `$_.FullName }" -ForegroundColor Gray
Write-Host ""

