# 手动修复 Consume Service Controller 文件

$controllerPath = "D:\IOE-DREAM\microservices\ioedream-consume-service\src\main\java\net\lab1024\sa\consume\controller"

$controllers = @(
    "AccountController.java",
    "ConsumeAccountController.java",
    "ConsumeController.java",
    "ConsumeMobileController.java",
    "ConsumeProductController.java",
    "ConsumeRefundController.java",
    "DeviceConsumeController.java",
    "MealOrderController.java",
    "MealOrderMobileController.java",
    "MobileConsumeController.java",
    "PaymentController.java",
    "ReconciliationController.java",
    "RefundApplicationController.java",
    "ReimbursementApplicationController.java",
    "ReportController.java"
)

foreach ($controllerFile in $controllers) {
    $filePath = Join-Path $controllerPath $controllerFile
    $className = $controllerFile.Replace(".java", "")

    Write-Host "修复 $controllerFile..." -ForegroundColor Cyan

    if (Test-Path $filePath) {
        # 读取文件内容
        $content = Get-Content -Path $filePath -Raw -Encoding UTF8

        # 去除BOM
        if ($content -match "^\uFEFF") {
            $content = $content -replace "^\uFEFF", ""
        }

        # 修复类声明
        $content = $content -replace '@PermissionCheck\([^)]+\)\s*\{\s*$', "@PermissionCheck(value = `"$className`", description = `"$className 管理权限`")`r`npublic class $className {"
        $content = $content -replace '@Tag\([^)]+\)\s*\{\s*$', "@Tag(name = `"$className`", description = `"$className 管理接口`")`r`npublic class $className {"
        $content = $content -replace '@RequestMapping\([^)]+\)\s*\{\s*$', "@RequestMapping(`/api/v1/consume/$($className.ToLower())`)" + "`r`npublic class $className {"

        # 修复 @Slf4j
        $content = $content -replace '@Slf4j', ""

        # 写回文件
        $content | Out-File -FilePath $filePath -Encoding UTF8 -NoNewline

        Write-Host "✓ $controllerFile 修复完成" -ForegroundColor Green
    } else {
        Write-Host "✗ 文件不存在: $filePath" -ForegroundColor Red
    }
}