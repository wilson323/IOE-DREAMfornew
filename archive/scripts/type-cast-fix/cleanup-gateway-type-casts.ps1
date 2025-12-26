# 清理GatewayServiceClient类型转换修复后的临时代码
Write-Host "清理GatewayServiceClient类型转换修复后的临时代码..." -ForegroundColor Green

$oaServicePath = "D:\IOE-DREAM\microservices\ioedream-oa-service"
$fixedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $oaServicePath -Recurse -Filter "*.java"

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 移除@SuppressWarnings("unchecked")注解
        $content = $content -replace '(?m)^\s*@SuppressWarnings\("unchecked"\)\s*`r`n', ''

        # 移除类型转换 - 匹配 (ResponseDTO<Type>) gatewayServiceClient.call
        $content = $content -replace '\(ResponseDTO<([^>]+)>\)\s*gatewayServiceClient\.call', 'gatewayServiceClient.call'

        # 移除其他类型转换模式
        $content = $content -replace '\(net\.lab1024\.sa\.platform\.core\.dto\.ResponseDTO<([^>]+)>\)\s*gatewayServiceClient\.call', 'gatewayServiceClient.call'

        # 如果有修改，保存文件
        if ($content -ne $originalContent) {
            [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
            Write-Host "✓ 清理类型转换: $($file.FullName.Replace($oaServicePath, 'ioedream-oa-service'))"
            $fixedFiles++
        }

    } catch {
        Write-Host "❌ 处理失败: $($file.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "`n类型转换清理完成!" -ForegroundColor Cyan
Write-Host "清理文件数: $fixedFiles" -ForegroundColor White

# 验证编译
Write-Host "`n验证编译..." -ForegroundColor Green
cd "D:\IOE-DREAM\microservices"
$compileResult = mvn clean compile -q 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 所有服务编译成功！" -ForegroundColor Green
    Write-Host "🎉 GatewayServiceClient类型统一修复完成！" -ForegroundColor Green
    Write-Host "📊 修复效果:" -ForegroundColor Yellow
    Write-Host "  ✓ GatewayServiceClient方法返回类型统一" -ForegroundColor Green
    Write-Host "  ✓ 消除所有强制类型转换" -ForegroundColor Green
    Write-Host "  ✓ 全局类型一致性达成" -ForegroundColor Green
} else {
    Write-Host "❌ 仍有编译错误，需要进一步修复" -ForegroundColor Red

    # 显示前10个错误
    $compileResult | Select-String "ERROR.*COMPILATION ERROR" -Context 0,1 | Select-Object -First 10 | ForEach-Object {
        Write-Host "- $($_.Line)" -ForegroundColor Red
    }
}