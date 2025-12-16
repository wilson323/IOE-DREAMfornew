# =====================================================
# 分布式追踪覆盖率验证脚本
# 版本: v1.0.0
# 描述: 验证所有Controller和ServiceImpl是否已添加@Observed注解
# 创建时间: 2025-01-30
# =====================================================

param(
    [Parameter(Mandatory=$false)]
    [switch]$GenerateReport
)

$ErrorActionPreference = "Stop"

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "分布式追踪覆盖率验证" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# 切换到项目根目录
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

# 统计变量
$totalControllers = 0
$totalServiceImpls = 0
$controllersWithObserved = 0
$serviceImplsWithObserved = 0
$controllersWithoutObserved = @()
$serviceImplsWithoutObserved = @()

Write-Host "[1/3] 检查Controller文件..." -ForegroundColor Yellow

# 检查Controller文件
$controllerFiles = Get-ChildItem -Path "microservices" -Filter "*Controller.java" -Recurse | 
    Where-Object { $_.FullName -notlike "*test*" -and $_.FullName -notlike "*Test*" }

$totalControllers = $controllerFiles.Count

foreach ($file in $controllerFiles) {
    $content = Get-Content $file.FullName -Raw
    $hasImport = $content -match "import.*observation.*Observed"
    $hasObserved = $content -match "@Observed"
    
    if ($hasImport -and $hasObserved) {
        $controllersWithObserved++
    } else {
        $service = if ($file.FullName -match "ioedream-(\w+)-service") { $matches[1] } else { "common" }
        $controllersWithoutObserved += "$service | $($file.Name)"
    }
}

Write-Host "[2/3] 检查ServiceImpl文件..." -ForegroundColor Yellow

# 检查ServiceImpl文件
$serviceImplFiles = Get-ChildItem -Path "microservices" -Filter "*ServiceImpl.java" -Recurse | 
    Where-Object { $_.FullName -notlike "*test*" -and $_.FullName -notlike "*Test*" }

$totalServiceImpls = $serviceImplFiles.Count

foreach ($file in $serviceImplFiles) {
    $content = Get-Content $file.FullName -Raw
    $hasImport = $content -match "import.*observation.*Observed"
    $hasObserved = $content -match "@Observed"
    
    if ($hasImport -and $hasObserved) {
        $serviceImplsWithObserved++
    } else {
        $service = if ($file.FullName -match "ioedream-(\w+)-service") { $matches[1] } else { "common" }
        $serviceImplsWithoutObserved += "$service | $($file.Name)"
    }
}

Write-Host "[3/3] 生成验证报告..." -ForegroundColor Yellow
Write-Host ""

# 计算覆盖率
$controllerCoverage = if ($totalControllers -gt 0) { 
    [math]::Round(($controllersWithObserved / $totalControllers) * 100, 2) 
} else { 
    0 
}

$serviceImplCoverage = if ($totalServiceImpls -gt 0) { 
    [math]::Round(($serviceImplsWithObserved / $totalServiceImpls) * 100, 2) 
} else { 
    0 
}

$totalCoverage = if (($totalControllers + $totalServiceImpls) -gt 0) {
    [math]::Round((($controllersWithObserved + $serviceImplsWithObserved) / ($totalControllers + $totalServiceImpls)) * 100, 2)
} else {
    0
}

# 输出结果
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "验证结果" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Controller统计:" -ForegroundColor White
Write-Host "  总数: $totalControllers" -ForegroundColor Gray
Write-Host "  已添加@Observed: $controllersWithObserved" -ForegroundColor Green
Write-Host "  未添加@Observed: $($totalControllers - $controllersWithObserved)" -ForegroundColor $(if (($totalControllers - $controllersWithObserved) -eq 0) { "Green" } else { "Red" })
Write-Host "  覆盖率: $controllerCoverage%" -ForegroundColor $(if ($controllerCoverage -eq 100) { "Green" } else { "Yellow" })
Write-Host ""

Write-Host "ServiceImpl统计:" -ForegroundColor White
Write-Host "  总数: $totalServiceImpls" -ForegroundColor Gray
Write-Host "  已添加@Observed: $serviceImplsWithObserved" -ForegroundColor Green
Write-Host "  未添加@Observed: $($totalServiceImpls - $serviceImplsWithObserved)" -ForegroundColor $(if (($totalServiceImpls - $serviceImplsWithObserved) -eq 0) { "Green" } else { "Red" })
Write-Host "  覆盖率: $serviceImplCoverage%" -ForegroundColor $(if ($serviceImplCoverage -eq 100) { "Green" } else { "Yellow" })
Write-Host ""

Write-Host "总体统计:" -ForegroundColor White
Write-Host "  总文件数: $($totalControllers + $totalServiceImpls)" -ForegroundColor Gray
Write-Host "  已添加@Observed: $($controllersWithObserved + $serviceImplsWithObserved)" -ForegroundColor Green
Write-Host "  未添加@Observed: $(($totalControllers - $controllersWithObserved) + ($totalServiceImpls - $serviceImplsWithObserved))" -ForegroundColor $(if ((($totalControllers - $controllersWithObserved) + ($totalServiceImpls - $serviceImplsWithObserved)) -eq 0) { "Green" } else { "Red" })
Write-Host "  总覆盖率: $totalCoverage%" -ForegroundColor $(if ($totalCoverage -eq 100) { "Green" } else { "Yellow" })
Write-Host ""

# 显示未添加的文件
if ($controllersWithoutObserved.Count -gt 0) {
    Write-Host "未添加@Observed的Controller文件:" -ForegroundColor Red
    foreach ($item in $controllersWithoutObserved) {
        Write-Host "  - $item" -ForegroundColor Red
    }
    Write-Host ""
}

if ($serviceImplsWithoutObserved.Count -gt 0) {
    Write-Host "未添加@Observed的ServiceImpl文件:" -ForegroundColor Red
    foreach ($item in $serviceImplsWithoutObserved) {
        Write-Host "  - $item" -ForegroundColor Red
    }
    Write-Host ""
}

# 验证结果
$isPassed = ($controllerCoverage -eq 100) -and ($serviceImplCoverage -eq 100)

if ($isPassed) {
    Write-Host "================================================" -ForegroundColor Green
    Write-Host "[OK] 追踪覆盖率验证通过 (100%)" -ForegroundColor Green
    Write-Host "================================================" -ForegroundColor Green
    exit 0
} else {
    Write-Host "================================================" -ForegroundColor Red
    Write-Host "[ERROR] 追踪覆盖率未达到100%" -ForegroundColor Red
    Write-Host "================================================" -ForegroundColor Red
    exit 1
}

