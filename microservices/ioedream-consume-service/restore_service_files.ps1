# Service层文件恢复脚本
# 从 smart-admin-api-java17-springboot3 复制Service文件到微服务，并调整包名

$adminPath = "..\..\smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\consume"
$microservicePath = "src\main\java\net\lab1024\sa\consume"

# 创建目录
New-Item -ItemType Directory -Force -Path "$microservicePath\service\impl" | Out-Null
New-Item -ItemType Directory -Force -Path "$microservicePath\service\consistency" | Out-Null
New-Item -ItemType Directory -Force -Path "$microservicePath\service\payment" | Out-Null

Write-Host "开始恢复Service层文件..." -ForegroundColor Green

# Service文件映射（源路径 -> 目标路径）
$serviceFiles = @(
    @{Source="service\impl\ConsumeServiceImpl.java"; Target="service\impl\ConsumeServiceImpl.java"},
    @{Source="service\RechargeService.java"; Target="service\RechargeService.java"},
    @{Source="service\IndexOptimizationService.java"; Target="service\IndexOptimizationService.java"},
    @{Source="service\impl\AbnormalDetectionServiceImpl.java"; Target="service\impl\AbnormalDetectionServiceImpl.java"},
    @{Source="service\consistency\ReconciliationService.java"; Target="service\consistency\ReconciliationService.java"},
    @{Source="service\payment\WechatPaymentService.java"; Target="service\payment\WechatPaymentService.java"},
    @{Source="service\impl\SecurityNotificationServiceImpl.java"; Target="service\impl\SecurityNotificationServiceImpl.java"},
    @{Source="service\ConsumePermissionService.java"; Target="service\ConsumePermissionService.java"}
)

$successCount = 0
$failCount = 0

foreach ($file in $serviceFiles) {
    $sourceFile = "$adminPath\$($file.Source)"
    $targetFile = "$microservicePath\$($file.Target)"
    $fileName = Split-Path $file.Source -Leaf

    # 确保目标目录存在
    $targetDir = Split-Path $targetFile -Parent
    if (-not (Test-Path $targetDir)) {
        New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
    }

    if (Test-Path $sourceFile) {
        try {
            Write-Host "恢复: $fileName" -ForegroundColor Yellow

            # 读取源文件内容
            $content = Get-Content $sourceFile -Raw -Encoding UTF8

            # 替换包名
            $content = $content -replace "package net\.lab1024\.sa\.admin\.module\.consume", "package net.lab1024.sa.consume"
            $content = $content -replace "import net\.lab1024\.sa\.admin\.module\.consume", "import net.lab1024.sa.consume"
            $content = $content -replace "import net\.lab1024\.sa\.base\.common", "import net.lab1024.sa.common"
            $content = $content -replace "import net\.lab1024\.sa\.base\.module", "import net.lab1024.sa.common"
            $content = $content -replace "net\.lab1024\.sa\.admin\.module\.consume\.domain\.enums", "net.lab1024.sa.consume.domain.enums"

            # 保存文件
            $fullPath = Join-Path (Get-Location) $targetFile
            [System.IO.File]::WriteAllText($fullPath, $content, [System.Text.Encoding]::UTF8)

            Write-Host "  ✓ $fileName 已恢复" -ForegroundColor Green
            $successCount++
        } catch {
            Write-Host "  ✗ $fileName 恢复失败: $_" -ForegroundColor Red
            $failCount++
        }
    } else {
        Write-Host "  ✗ $fileName 源文件不存在: $sourceFile" -ForegroundColor Red
        $failCount++
    }
}

Write-Host "`nService层文件恢复完成！" -ForegroundColor Green
Write-Host "成功: $successCount 个文件" -ForegroundColor Green
Write-Host "失败: $failCount 个文件" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })

