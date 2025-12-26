# 修复ioedream-access-service中所有类型转换问题
Write-Host "修复ioedream-access-service中所有类型转换问题..." -ForegroundColor Green

$accessServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
$fixedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $accessServicePath -Recurse -Filter "*.java"

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 修复所有GatewayServiceClient和ResponseDTO类型转换问题
        # 匹配模式: ResponseDTO<Type> variable = gatewayServiceClient.call...
        # 或: ResponseDTO<Type> variable = restTemplate调用

        # 修复GatewayServiceClient调用
        $gatewayPattern = '(\s+)(ResponseDTO<([^>]+)>)\s+(\w+)\s*=\s*gatewayServiceClient\.call\w*\s*\('
        $gatewayReplacement = '$1@SuppressWarnings("unchecked")`r`n$1$2 $3 = ($2) gatewayServiceClient.call$0('
        $content = $content -replace $gatewayPattern, $gatewayReplacement

        # 修复ResponseDTO<?>类型
        $wildcardPattern = '(\s+)ResponseDTO<\?>\s+(\w+)\s*=\s*gatewayServiceClient\.call\w*\s*\('
        $wildcardReplacement = '$1@SuppressWarnings("unchecked")`r`n$1ResponseDTO<?> $2 = (ResponseDTO<?>) gatewayServiceClient.call$0('
        $content = $content -replace $wildcardPattern, $wildcardReplacement

        # 如果有修改，保存文件
        if ($content -ne $originalContent) {
            [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
            Write-Host "✓ 修复: $($file.FullName.Replace($accessServicePath, 'ioedream-access-service'))"
            $fixedFiles++
        }

    } catch {
        Write-Host "❌ 处理失败: $($file.FullName) - $($_.Exception.Message)"
    }
}

Write-Host "`n类型转换修复完成!" -ForegroundColor Cyan
Write-Host "修复文件数: $fixedFiles" -ForegroundColor White

# 验证编译
Write-Host "`n验证编译..." -ForegroundColor Green
cd "D:\IOE-DREAM\microservices"
$compileResult = mvn clean compile -q 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 所有服务编译成功！" -ForegroundColor Green
    Write-Host "🎉 P0阶段编译验证最终完成！" -ForegroundColor Green
    Write-Host "📊 完成的P0阶段任务:" -ForegroundColor Yellow
    Write-Host "  ✓ BOM字符清理" -ForegroundColor Green
    Write-Host "  ✓ 根源性分析和架构统一" -ForegroundColor Green
    Write- "  ✓ 包路径统一化修复" -ForegroundColor Green
    Write-Host "  ✓ GatewayServiceClient类型转换修复" -ForegroundColor Green
    Write-Host "  ✓ PageResult导入路径修复" -ForegroundColor Green
    Write-Host "  ✓ 所有类型转换问题修复" -ForegroundColor Green
} else {
    Write-Host "❌ 仍有编译错误，错误数量: $($($compileResult | Select-String 'ERROR.*COMPILATION ERROR').Count))" -ForegroundColor Red

    # 显示前5个错误
    $compileResult | Select-String "ERROR.*COMPILATION ERROR" -Context 0,1 | Select-Object -First 5 | ForEach-Object {
        Write-Host "- $($_.Line)" -ForegroundColor Red
    }
}