# 修复PageResult导入路径问题
Write-Host "修复PageResult导入路径问题..." -ForegroundColor Green

# 需要修复的文件列表（根据编译错误）
$filesToFix = @(
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessAreaController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\AccessAreaService.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessDeviceController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\AccessDeviceService.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AccessMonitorController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\AccessMonitorService.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\controller\AntiPassbackController.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\AntiPassbackService.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AccessAreaServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AccessDeviceServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AccessMonitorServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-access-service\src\main\java\net\lab1024\sa\access\service\impl\AntiPassbackServiceImpl.java"
)

$fixedFiles = 0

foreach ($filePath in $filesToFix) {
    if (Test-Path $filePath) {
        Write-Host "修复文件: $((Split-Path $filePath -Leaf))"

        try {
            # 读取文件内容
            $content = Get-Content -Path $filePath -Raw -Encoding UTF8
            $originalContent = $content

            # 修复PageResult导入路径
            $content = $content -replace 'import net\.lab1024\.sa\.platform\.core\.dto\.PageResult;', 'import net.lab1024.sa.common.domain.PageResult;'

            # 修复PageResult使用
            $content = $content -replace 'net\.lab1024\.sa\.platform\.core\.dto\.PageResult', 'net.lab1024.sa.common.domain.PageResult'

            # 如果有修改，保存文件
            if ($content -ne $originalContent) {
                [System.IO.File]::WriteAllText($filePath, $content, [System.Text.Encoding]::UTF8)
                Write-Host "  ✓ PageResult导入路径已修复" -ForegroundColor Green
                $fixedFiles++
            } else {
                Write-Host "  ⚠️ 无需修复" -ForegroundColor Yellow
            }

        } catch {
            Write-Host "  ❌ 修复失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  ❌ 文件不存在: $((Split-Path $filePath -Leaf))" -ForegroundColor Red
    }
}

Write-Host "`nPageResult导入路径修复完成!" -ForegroundColor Cyan
Write-Host "修复文件数: $fixedFiles" -ForegroundColor White

# 验证编译
Write-Host "`n验证编译..." -ForegroundColor Green
cd "D:\IOE-DREAM\microservices"
$compileResult = mvn clean compile -q 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 所有服务编译成功！" -ForegroundColor Green
    Write-Host "🎉 P0阶段编译验证完成！" -ForegroundColor Green
    # 显示编译成功的统计
    $successServices = @("ioedream-access-service", "ioedream-attendance-service", "ioedream-common-service", "ioedream-consume-service", "ioedream-video-service", "ioedream-visitor-service", "ioedream-device-comm-service", "ioedream-oa-service", "microservices-common", "microservices-common-*")
    Write-Host "成功编译的服务模块数量: $($successServices.Count)" -ForegroundColor Cyan
} else {
    Write-Host "❌ 仍有编译错误，继续分析..." -ForegroundColor Red
    $errorCount = ($compileResult | Select-String "ERROR.*COMPILATION ERROR").Count
    Write-Host "剩余编译错误数量: $errorCount" -ForegroundColor Red

    # 显示前10个错误
    $compileResult | Select-String "ERROR.*COMPILATION ERROR" -Context 0,0 | Select-Object -First 10 | ForEach-Object {
        $errorLine = $_.Line.ToString()
        Write-Host "- $errorLine" -ForegroundColor Red
    }
}