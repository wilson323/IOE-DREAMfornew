# IOE-DREAM单元测试执行脚本
# 严格遵循测试规范：单元测试覆盖率 >= 80%

param(
    [string]$Service = "",
    [switch]$Coverage = $false,
    [switch]$Parallel = $true
)

$ErrorActionPreference = "Stop"

Write-Host "===== IOE-DREAM单元测试执行 =====" -ForegroundColor Cyan

# 构建Maven命令
$mvnCmd = "mvn test"

if ($Coverage) {
    $mvnCmd += " jacoco:report"
    Write-Host "启用代码覆盖率报告" -ForegroundColor Yellow
}

if ($Parallel) {
    $mvnCmd += " -T 4"
    Write-Host "启用并行测试（4线程）" -ForegroundColor Yellow
}

if ($Service -ne "") {
    $mvnCmd += " -pl microservices/$Service -am"
    Write-Host "仅测试服务: $Service" -ForegroundColor Yellow
} else {
    Write-Host "执行全局单元测试" -ForegroundColor Yellow
}

Write-Host "`n执行命令: $mvnCmd`n" -ForegroundColor Gray
Write-Host "=" * 60 -ForegroundColor Gray

# 执行测试
$startTime = Get-Date
Invoke-Expression $mvnCmd

$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host "`n" + "=" * 60 -ForegroundColor Gray
Write-Host "测试执行完成" -ForegroundColor Green
Write-Host "耗时: $($duration.TotalSeconds) 秒" -ForegroundColor Cyan

# 如果启用覆盖率，显示报告位置
if ($Coverage) {
    Write-Host "`n覆盖率报告位置:" -ForegroundColor Yellow
    if ($Service -ne "") {
        Write-Host "  microservices/$Service/target/site/jacoco/index.html" -ForegroundColor Gray
    } else {
        Write-Host "  */target/site/jacoco/index.html" -ForegroundColor Gray
    }
}

