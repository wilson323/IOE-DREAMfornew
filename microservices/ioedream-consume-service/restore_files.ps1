# 文件恢复脚本
# 从 smart-admin-api-java17-springboot3 复制文件到微服务，并调整包名

$adminPath = "..\..\smart-admin-api-java17-springboot3\sa-admin\src\main\java\net\lab1024\sa\admin\module\consume"
$microservicePath = "src\main\java\net\lab1024\sa\consume"

# 创建目录
New-Item -ItemType Directory -Force -Path "$microservicePath\controller" | Out-Null
New-Item -ItemType Directory -Force -Path "$microservicePath\service\impl" | Out-Null
New-Item -ItemType Directory -Force -Path "$microservicePath\service\consistency" | Out-Null
New-Item -ItemType Directory -Force -Path "$microservicePath\service\payment" | Out-Null
New-Item -ItemType Directory -Force -Path "$microservicePath\test\integration" | Out-Null
New-Item -ItemType Directory -Force -Path "$microservicePath\test\performance" | Out-Null

Write-Host "开始恢复文件..." -ForegroundColor Green

# Controller文件列表
$controllers = @(
    "ConsumeController.java",
    "ConsumptionModeController.java",
    "RechargeController.java",
    "RefundController.java",
    "AccountController.java",
    "IndexOptimizationController.java",
    "ConsistencyValidationController.java",
    "SagaTransactionController.java"
)

# Service文件列表
$services = @(
    @{Source="service\impl\ConsumeServiceImpl.java"; Target="service\impl\ConsumeServiceImpl.java"},
    @{Source="service\RechargeService.java"; Target="service\RechargeService.java"},
    @{Source="service\IndexOptimizationService.java"; Target="service\IndexOptimizationService.java"},
    @{Source="service\impl\AbnormalDetectionServiceImpl.java"; Target="service\impl\AbnormalDetectionServiceImpl.java"},
    @{Source="service\consistency\ReconciliationService.java"; Target="service\consistency\ReconciliationService.java"},
    @{Source="service\payment\WechatPaymentService.java"; Target="service\payment\WechatPaymentService.java"},
    @{Source="service\impl\SecurityNotificationServiceImpl.java"; Target="service\impl\SecurityNotificationServiceImpl.java"},
    @{Source="service\ConsumePermissionService.java"; Target="service\ConsumePermissionService.java"}
)

# 恢复Controller文件
foreach ($controller in $controllers) {
    $sourceFile = "$adminPath\controller\$controller"
    $targetFile = "$microservicePath\controller\$controller"

    if (Test-Path $sourceFile) {
        Write-Host "恢复: $controller" -ForegroundColor Yellow
        $content = Get-Content $sourceFile -Raw -Encoding UTF8
        # 替换包名
        $content = $content -replace "package net\.lab1024\.sa\.admin\.module\.consume", "package net.lab1024.sa.consume"
        $content = $content -replace "import net\.lab1024\.sa\.admin\.module\.consume", "import net.lab1024.sa.consume"
        $content = $content -replace "import net\.lab1024\.sa\.base\.common", "import net.lab1024.sa.common"
        $content = $content -replace "import net\.lab1024\.sa\.base\.module", "import net.lab1024.sa.common"
        # 保存文件
        [System.IO.File]::WriteAllText((Resolve-Path .).Path + "\" + $targetFile, $content, [System.Text.Encoding]::UTF8)
        Write-Host "  ✓ $controller 已恢复" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $controller 源文件不存在" -ForegroundColor Red
    }
}

# 恢复Service文件
foreach ($service in $services) {
    $sourceFile = "$adminPath\$($service.Source)"
    $targetFile = "$microservicePath\$($service.Target)"

    # 确保目标目录存在
    $targetDir = Split-Path $targetFile -Parent
    New-Item -ItemType Directory -Force -Path $targetDir | Out-Null

    if (Test-Path $sourceFile) {
        $fileName = Split-Path $service.Source -Leaf
        Write-Host "恢复: $fileName" -ForegroundColor Yellow
        $content = Get-Content $sourceFile -Raw -Encoding UTF8
        # 替换包名
        $content = $content -replace "package net\.lab1024\.sa\.admin\.module\.consume", "package net.lab1024.sa.consume"
        $content = $content -replace "import net\.lab1024\.sa\.admin\.module\.consume", "import net.lab1024.sa.consume"
        $content = $content -replace "import net\.lab1024\.sa\.base\.common", "import net.lab1024.sa.common"
        $content = $content -replace "import net\.lab1024\.sa\.base\.module", "import net.lab1024.sa.common"
        # 保存文件
        [System.IO.File]::WriteAllText((Resolve-Path .).Path + "\" + $targetFile, $content, [System.Text.Encoding]::UTF8)
        Write-Host "  ✓ $fileName 已恢复" -ForegroundColor Green
    } else {
        Write-Host "  ✗ $fileName 源文件不存在" -ForegroundColor Red
    }
}

Write-Host "`n文件恢复完成！" -ForegroundColor Green
Write-Host "请检查恢复的文件，确保包名和导入路径正确。" -ForegroundColor Yellow

