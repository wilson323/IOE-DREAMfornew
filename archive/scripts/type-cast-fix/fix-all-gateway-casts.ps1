# 修复ioedream-oa-service中所有GatewayServiceClient类型转换问题
Write-Host "修复ioedream-oa-service中所有GatewayServiceClient类型转换问题..." -ForegroundColor Green

$oaServicePath = "D:\IOE-DREAM\microservices\ioedream-oa-service"
$fixedFiles = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $oaServicePath -Recurse -Filter "*.java"

foreach ($file in $javaFiles) {
    try {
        $content = Get-Content -Path $file.FullName -Raw -Encoding UTF8
        $originalContent = $content

        # 修复所有GatewayServiceClient类型转换问题
        # 匹配模式: ResponseDTO<Type> variable = gatewayServiceClient.callCommonService(...)
        $pattern = '(\s+)(ResponseDTO<([^>]+)>)\s+(\w+)\s*=\s*gatewayServiceClient\.callCommonService\('
        $replacement = '$1@SuppressWarnings("unchecked")`r`n$1$2 $4 = ($2) gatewayServiceClient.callCommonService('
        $content = $content -replace $pattern, $replacement

        # 如果有修改，保存文件
        if ($content -ne $originalContent) {
            [System.IO.File]::WriteAllText($file.FullName, $content, [System.Text.Encoding]::UTF8)
            Write-Host "✓ 修复: $($file.FullName.Replace($oaServicePath, 'ioedream-oa-service'))"
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
    # 更新TodoWrite状态
    Write-Host "P0阶段编译验证完成！" -ForegroundColor Green
} else {
    Write-Host "❌ 仍有编译错误，显示前10个:" -ForegroundColor Red
    $compileResult | Select-String "ERROR.*COMPILATION ERROR" -Context 0,1 | Select-Object -First 10
}