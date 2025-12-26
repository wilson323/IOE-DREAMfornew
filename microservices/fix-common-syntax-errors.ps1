# MicroServices Common模块语法错误企业级修复脚本
# 系统性解决所有import语句语法错误

Write-Host "=== MicroServices Common模块语法错误企业级修复脚本 ===" -ForegroundColor Cyan

# 需要修复的文件列表
$filesToFix = @(
    "microservices-common\src\main\java\net\lab1024\sa\common\edge\form\EdgeDeviceRegisterForm.java",
    "microservices-common\src\main\java\net\lab1024\sa\common\edge\model\EdgeConfig.java",
    "microservices-common\src\main\java\net\lab1024\sa\common\edge\model\EdgeDevice.java",
    "microservices-common\src\main\java\net\lab1024\sa\common\edge\model\InferenceRequest.java",
    "microservices-common\src\main\java\net\lab1024\sa\common\edge\model\InferenceResult.java",
    "microservices-common\src\main\java\net\lab1024\sa\common\edge\model\ModelInfo.java"
)

$processedCount = 0
$errorCount = 0

foreach ($file in $filesToFix) {
    try {
        Write-Host "修复: $file" -ForegroundColor Green

        # 读取文件内容
        $content = Get-Content -Path $file -Raw -Encoding UTF8

        # 修复import语句前的多余字符
        $content = $content -replace 'iimport', 'import'

        # 写回文件
        $content | Out-File -FilePath $file -Encoding UTF8 -NoNewline
        $processedCount++

        Write-Host "✅ 修复完成: $file" -ForegroundColor Green

    } catch {
        Write-Host "❌ 修复失败: $file - $($_.Exception.Message)" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host "`n=== 语法错误修复统计 ===" -ForegroundColor Cyan
Write-Host "✅ 成功修复: $processedCount 个文件" -ForegroundColor Green
Write-Host "❌ 修复失败: $errorCount 个文件" -ForegroundColor Red

# 检查StrategyFactory.java是否需要特殊处理
$strategyFactoryFile = "microservices-common\src\main\java\net\lab1024\sa\common\factory\StrategyFactory.java"
if (Test-Path $strategyFactoryFile) {
    Write-Host "`n检查StrategyFactory.java..." -ForegroundColor Yellow
    $content = Get-Content -Path $strategyFactoryFile -Raw -Encoding UTF8
    if ($content -match 'iimport') {
        Write-Host "发现StrategyFactory.java也有import问题，开始修复..." -ForegroundColor Yellow
        $content = $content -replace 'iimport', 'import'
        $content | Out-File -FilePath $strategyFactoryFile -Encoding UTF8 -NoNewline
        Write-Host "✅ StrategyFactory.java修复完成" -ForegroundColor Green
        $processedCount++
    }
}

Write-Host "`n准备编译验证..." -ForegroundColor Cyan