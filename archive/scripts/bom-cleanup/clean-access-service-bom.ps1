# 清理access-service的BOM字符
Write-Host "🔧 清理access-service的BOM字符..." -ForegroundColor Green

$accessServicePath = "D:\IOE-DREAM\microservices\ioedream-access-service"
$bomCount = 0

# 获取所有Java文件
$javaFiles = Get-ChildItem -Path $accessServicePath -Recurse -Filter "*.java" -File

Write-Host "找到 $($javaFiles.Count) 个Java文件，检查BOM字符..." -ForegroundColor Cyan

foreach ($file in $javaFiles) {
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)

        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            # 移除BOM字符
            $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
            [System.IO.File]::WriteAllBytes($file.FullName, $bytesWithoutBom)

            $relativePath = $file.FullName.Replace("$accessServicePath\", "")
            Write-Host "✓ 移除BOM: $relativePath" -ForegroundColor Green
            $bomCount++
        }
    } catch {
        $relativePath = $file.FullName.Replace("$accessServicePath\", "")
        Write-Host "❌ 处理失败: $relativePath - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`nBOM清理完成! 移除BOM数: $bomCount" -ForegroundColor Cyan

# 验证编译
Write-Host "`n🔍 验证编译..." -ForegroundColor Green
cd "D:\IOE-DREAM\microservices"

$compileResult = mvn clean compile -pl ioedream-access-service -q -DskipTests 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ access-service编译成功！" -ForegroundColor Green

    # 测试其他服务
    Write-Host "`n🔍 测试其他服务编译..." -ForegroundColor Green

    $services = @("ioedream-attendance-service", "ioedream-consume-service", "ioedream-video-service")
    $successCount = 0

    foreach ($service in $services) {
        Write-Host "  测试 $service..." -ForegroundColor Cyan
        $result = mvn clean compile -pl $service -q -DskipTests 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "    ✅ $service 编译成功" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "    ❌ $service 编译失败" -ForegroundColor Red
        }
    }

    Write-Host "`n📊 编译测试结果:" -ForegroundColor Magenta
    Write-Host "  成功服务: $successCount/$($services.Count + 1)" -ForegroundColor $(if ($successCount -eq ($services.Count + 1)) { "Green" } else { "Yellow" })

    if ($successCount -eq ($services.Count + 1)) {
        Write-Host "🎉 所有服务编译成功！" -ForegroundColor Green
        Write-Host "✅ P1阶段编译验证完成" -ForegroundColor Green
        Write-Host "✅ Platform.Core包路径修复 + BOM清理完成" -ForegroundColor Green
    }
} else {
    Write-Host "❌ access-service编译失败" -ForegroundColor Red
    Write-Host "错误信息:" -ForegroundColor Yellow

    # 显示前10个错误
    $compileErrorLines = $compileResult -split "`n" | Select-String "ERROR" | Select-Object -First 10
    foreach ($errorMsg in $compileErrorLines) {
        if ($errorMsg -match "非法字符.*\ufeff") {
            Write-Host "    ⚠️ BOM字符问题: $errorMsg" -ForegroundColor Yellow
        } else {
            Write-Host "    $errorMsg" -ForegroundColor Red
        }
    }
}