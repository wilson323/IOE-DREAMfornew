# 扫描Entity业务方法调用
# 用途：识别所有需要在Manager层实现的业务方法

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "扫描Entity业务方法调用" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$erroFile = "D:\IOE-DREAM\erro.txt"
$outputFile = "D:\IOE-DREAM\documentation\technical\entity-methods-to-implement.txt"

# 扫描方法未定义错误
Write-Host "扫描方法未定义错误..." -ForegroundColor Yellow

$methodErrors = Select-String -Path $erroFile -Pattern "The method (.*) is undefined for the type (.*)"

Write-Host "发现 $($methodErrors.Count) 个方法未定义错误" -ForegroundColor Cyan
Write-Host ""

# 统计按Entity分组的方法
$entityMethods = @{}

foreach ($match in $methodErrors) {
    if ($match -match "The method (.+) is undefined for the type (.+)") {
        $method = $matches[1]
        $entity = $matches[2]

        if (-not $entityMethods.ContainsKey($entity)) {
            $entityMethods[$entity] = @{}
        }

        if (-not $entityMethods[$entity].ContainsKey($method)) {
            $entityMethods[$entity][$method] = 0
        }

        $entityMethods[$entity][$method]++
    }
}

# 输出统计结果
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Entity业务方法统计" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

foreach ($entity in $entityMethods.Keys | Sort-Object) {
    $methods = $entityMethods[$entity]
    Write-Host "【$entity】" -ForegroundColor Yellow
    Write-Host "  方法数: $($methods.Count)" -ForegroundColor Cyan

    foreach ($method in $methods.Keys | Sort-Object) {
        $count = $methods[$method]
        Write-Host "    - $method ($count 次调用)" -ForegroundColor Gray
    }

    Write-Host ""
}

# 生成实现清单
$report = @()
$report += "# Entity业务方法实现清单"
$report += ""
$report += "生成时间: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
$report += ""
$report += "## 总览"
$report += ""
$report += "- 总Entity数: $($entityMethods.Count)"
$report += "- 总方法数: $($entityMethods.Values | ForEach-Object { $_.Count } | Measure-Object -Sum | Select-Object -ExpandProperty Sum)"
$report += ""

foreach ($entity in $entityMethods.Keys | Sort-Object) {
    $methods = $entityMethods[$entity]
    $entityName = $entity -replace "Entity", ""

    $report += "## $entity"
    $report += ""
    $report += "对应Manager类: `${entityName}Manager`"
    $report += ""
    $report += "### 需要实现的方法"
    $report += ""

    foreach ($method in $methods.Keys | Sort-Object) {
        $count = $methods[$method]
        $report += "- \`$method()\` - 调用次数: $count"
    }

    $report += ""
}

# 保存到文件
$report | Out-File -FilePath $outputFile -Encoding UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "扫描完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "📄 详细报告已保存到: $outputFile" -ForegroundColor Green
Write-Host ""
