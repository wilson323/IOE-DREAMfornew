# 针对编译错误BOM文件的精确清理脚本
Write-Host "开始精确清理编译错误涉及的BOM文件..." -ForegroundColor Green

# 编译错误中列出的具体文件
$filesWithBom = @(
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\dataanalysis\openapi\controller\DataAnalysisOpenApiController.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\dataanalysis\openapi\service\DataAnalysisOpenApiService.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\monitor\service\AlertService.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\monitor\service\impl\AlertServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\notification\controller\NotificationConfigController.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\notification\service\NotificationConfigService.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\notification\service\impl\NotificationConfigServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\openapi\controller\UserOpenApiController.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\openapi\service\impl\UserOpenApiServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\system\area\controller\SystemAreaController.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\system\area\service\SystemAreaService.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\system\area\service\impl\SystemAreaServiceImpl.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\system\employee\controller\EmployeeController.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\system\employee\service\EmployeeService.java",
    "D:\IOE-DREAM\microservices\ioedream-common-service\src\main\java\net\lab1024\sa\common\system\employee\service\impl\EmployeeServiceImpl.java"
)

$processedCount = 0
$bomRemovedCount = 0

foreach ($filePath in $filesWithBom) {
    $processedCount++
    Write-Host "处理 [$processedCount/$($filesWithBom.Count)]: $filePath"

    if (Test-Path $filePath) {
        try {
            # 读取文件字节
            $bytes = [System.IO.File]::ReadAllBytes($filePath)

            if ($bytes.Length -ge 3 -and
                $bytes[0] -eq 0xEF -and
                $bytes[1] -eq 0xBB -and
                $bytes[2] -eq 0xBF) {

                # 移除BOM字符
                $bytesWithoutBom = $bytes[3..($bytes.Length-1)]
                [System.IO.File]::WriteAllBytes($filePath, $bytesWithoutBom)
                Write-Host "  ✓ BOM字符已移除" -ForegroundColor Green
                $bomRemovedCount++
            } else {
                Write-Host "  ⚠️ 未检测到BOM字符" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "  ❌ 处理失败: $($_.Exception.Message)" -ForegroundColor Red
        }
    } else {
        Write-Host "  ❌ 文件不存在" -ForegroundColor Red
    }
}

Write-Host "`nBOM清理完成!" -ForegroundColor Cyan
Write-Host "处理文件数: $processedCount" -ForegroundColor White
Write-Host "移除BOM数: $bomRemovedCount" -ForegroundColor White

# 验证
Write-Host "`n验证清理结果..." -ForegroundColor Green
$verificationPassed = $true

foreach ($filePath in $filesWithBom) {
    if (Test-Path $filePath) {
        $bytes = [System.IO.File]::ReadAllBytes($filePath)
        if ($bytes.Length -ge 3 -and
            $bytes[0] -eq 0xEF -and
            $bytes[1] -eq 0xBB -and
            $bytes[2] -eq 0xBF) {
            Write-Host "  ❌ 仍有BOM: $filePath" -ForegroundColor Red
            $verificationPassed = $false
        }
    }
}

if ($verificationPassed) {
    Write-Host "✅ 所有文件BOM清理验证通过！" -ForegroundColor Green
} else {
    Write-Host "❌ 部分文件仍有BOM字符" -ForegroundColor Red
}