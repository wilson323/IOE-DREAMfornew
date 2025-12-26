# 清理access-service的BOM字符
$accessServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"

Write-Host "清理access-service的BOM字符..." -ForegroundColor Green

if (Test-Path $accessServicePath) {
    $javaFiles = Get-ChildItem -Path $accessServicePath -Recurse -Filter "*.java"
    $bomCount = 0

    foreach ($file in $javaFiles) {
        try {
            $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
            if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
                $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
                [System.IO.File]::WriteAllBytes($file.FullName, $bytesWithoutBom)
                Write-Host "✓ BOM已移除: $($file.Name)"
                $bomCount++
            }
        } catch {
            Write-Host "❌ 处理失败: $($file.Name) - $($_.Exception.Message)"
        }
    }

    Write-Host "`nBOM清理完成! 移除BOM数: $bomCount" -ForegroundColor Cyan

    # 验证编译
    Write-Host "`n验证编译..." -ForegroundColor Green
    cd "D:\IOE-DREAM\microservices"
    $compileResult = mvn clean compile -q 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ 所有服务编译成功！" -ForegroundColor Green
        Write-Host "🎉 P0阶段编译验证完成！所有GatewayServiceClient类型转换和PageResult导入路径问题已解决！" -ForegroundColor Green
    } else {
        Write-Host "❌ 仍有编译错误" -ForegroundColor Red
        $compileResult | Select-String "ERROR.*COMPILATION ERROR" -Context 0,1 | Select-Object -First 5
    }
} else {
    Write-Host "❌ access-service目录不存在" -ForegroundColor Red
}