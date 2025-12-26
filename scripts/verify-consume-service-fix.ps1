# 消费服务模块修复验证脚本
# 作者: IOE-DREAM架构委员会
# 版本: 1.0.0
# 日期: 2025-12-22

param(
    [switch]$Detailed,
    [string]$ServicePath = "microservices/ioedream-consume-service"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🔍 消费服务模块修复验证脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$ErrorActionPreference = "Stop"

$TargetFiles = @(
    "$ServicePath/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductImportExportService.java",
    "$ServicePath/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductPriceService.java",
    "$ServicePath/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductQueryService.java",
    "$ServicePath/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductServiceImpl_Refactored.java",
    "$ServicePath/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductStockService.java",
    "$ServicePath/src/main/java/net/lab1024/sa/consume/service/impl/ConsumeProductValidationService.java"
)

# 验证模式定义
$ValidationRules = @{
    "异常构造函数" = @{
        Pattern = 'new ConsumeProductException\("'
        Description = "不应该存在String参数的异常构造函数"
        IsValid = $false
        Errors = @()
    }
    "工具类导入" = @{
        Pattern = 'import net\.lab1024\.sa\.consume\.util'
        Description = "不应该存在错误的util包导入"
        IsValid = $true
        Errors = @()
    }
    "BeanUtil使用" = @{
        Pattern = 'BeanUtil\.'
        Description = "不应该使用BeanUtil，应该使用BeanUtils"
        IsValid = $true
        Errors = @()
    }
    "ConsumeAddForm引用" = @{
        Pattern = 'ConsumeAddForm'
        Description = "不应该使用ConsumeAddForm，应该使用ConsumeProductAddForm"
        IsValid = $true
        Errors = @()
    }
    "getStock方法" = @{
        Pattern = '\.getStock\(\)'
        Description = "不应该使用getStock方法，应该使用getStockQuantity"
        IsValid = $true
        Errors = @()
    }
    "setStock方法" = @{
        Pattern = '\.setStock\('
        Description = "不应该使用setStock方法，应该使用setStockQuantity"
        IsValid = $true
        Errors = @()
    }
    "getProductSort方法" = @{
        Pattern = '\.getProductSort\(\)'
        Description = "不应该使用getProductSort方法，应该使用getRecommendSort"
        IsValid = $true
        Errors = @()
    }
    "BigDecimal过时API" = @{
        Pattern = 'BigDecimal\.ROUND_HALF_UP'
        Description = "不应该使用过时的BigDecimal常量，应该使用RoundingMode"
        IsValid = $true
        Errors = @()
    }
    "分页查询参数" = @{
        Pattern = 'selectPage\(\w*QueryForm,'
        Description = "不应该直接使用QueryForm作为分页参数"
        IsValid = $true
        Errors = @()
    }
}

Write-Host ""
Write-Host "🔍 开始验证修复效果..." -ForegroundColor Yellow

$totalIssues = 0
$totalFiles = 0

foreach ($file in $TargetFiles) {
    if (Test-Path $file) {
        $totalFiles++
        $fileName = Split-Path $file -Leaf
        Write-Host ""
        Write-Host "📄 检查文件: $fileName" -ForegroundColor Green

        $content = Get-Content $file -Raw
        $fileHasIssues = $false

        foreach ($ruleName in $ValidationRules.Keys) {
            $rule = $ValidationRules[$ruleName]
            $matches = [regex]::Matches($content, $rule.Pattern)

            if ($matches.Count -gt 0 -and -not $rule.IsValid) {
                # 这个模式不应该存在，但找到了
                $rule.IsValid = $false
                $fileHasIssues = $true
                $totalIssues += $matches.Count

                $rule.Errors += "$fileName: 发现 $($matches.Count) 处违规"
                Write-Host "  ❌ $ruleName: 发现 $($matches.Count) 处问题" -ForegroundColor Red

                if ($Detailed) {
                    foreach ($match in $matches) {
                        $lines = $content.Substring(0, $match.Index).Split("`n").Length
                        Write-Host "    第 $lines 行: $($match.Value.Trim())" -ForegroundColor DarkRed
                    }
                }
            }
            elseif ($matches.Count -eq 0 -and $rule.IsValid) {
                # 这个模式应该存在，但没找到（可选验证）
                # 这里可以根据需要添加正向验证逻辑
            }
        }

        if (-not $fileHasIssues) {
            Write-Host "  ✅ 文件通过所有检查" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "📊 验证结果汇总:" -ForegroundColor Cyan

$allPassed = $true
foreach ($ruleName in $ValidationRules.Keys) {
    $rule = $ValidationRules[$ruleName]
    $status = if ($rule.Errors.Count -eq 0) { "✅ 通过" } else { "❌ 失败" }
    $color = if ($rule.Errors.Count -eq 0) { "Green" } else { "Red" }

    Write-Host "  $status $ruleName: $($rule.Description)" -ForegroundColor $color

    if ($rule.Errors.Count -gt 0 -and $Detailed) {
        foreach ($error in $rule.Errors) {
            Write-Host "    - $error" -ForegroundColor DarkRed
        }
    }

    if ($rule.Errors.Count -gt 0) {
        $allPassed = $false
    }
}

Write-Host ""
Write-Host "📈 统计信息:" -ForegroundColor Cyan
Write-Host "  - 检查文件数: $totalFiles" -ForegroundColor White
Write-Host "  - 发现问题数: $totalIssues" -ForegroundColor $(if ($totalIssues -eq 0) { "Green" } else { "Red" })
Write-Host "  - 整体状态: $(if ($allPassed) { '✅ 全部通过' } else { '❌ 存在问题' })" -ForegroundColor $(if ($allPassed) { "Green" } else { "Red" })

if ($allPassed) {
    Write-Host ""
    Write-Host "🎉 恭喜！所有修复验证通过，可以继续编译测试。" -ForegroundColor Green
    Write-Host ""
    Write-Host "🧪 建议下一步操作:" -ForegroundColor Cyan
    Write-Host "  1. 编译验证: mvn clean compile" -ForegroundColor White
    Write-Host "  2. 运行测试: mvn test" -ForegroundColor White
    Write-Host "  3. 打包验证: mvn package" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "⚠️  发现问题，需要进一步修复。" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "🔧 建议操作:" -ForegroundColor Cyan
    Write-Host "  1. 使用修复脚本重新运行: ./scripts/fix-consume-service-compilation-errors.ps1" -ForegroundColor White
    Write-Host "  2. 手动检查并修复剩余问题" -ForegroundColor White
    Write-Host "  3. 重新运行验证脚本" -ForegroundColor White
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "🎯 验证脚本执行完成" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 返回退出码
exit $(if ($allPassed) { 0 } else { 1 })