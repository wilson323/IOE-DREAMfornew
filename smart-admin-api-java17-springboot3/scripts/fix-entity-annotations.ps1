# 批量修复Entity文件缺失的注解
# 修复模式：将 (callSuper = true) 替换为 @EqualsAndHashCode(callSuper = true)
# 并添加 @Data 注解和相应的 import

$files = @(
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\system\device\domain\entity\UnifiedDeviceEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\smart\device\domain\entity\SmartDeviceEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\smart\biometric\domain\entity\BiometricTemplateEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\smart\biometric\domain\entity\BiometricRecordEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\smart\biometric\domain\entity\AuthenticationStrategyEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\smart\access\domain\entity\SmartAccessRecordEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\smart\access\domain\entity\AccessRecordEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\smart\access\domain\entity\AccessDeviceEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\oa\workflow\domain\entity\WorkflowTaskEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\entity\UserBehaviorBaselineEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\entity\RechargeRecordEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\entity\OrderingEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\entity\DeviceConfigEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\entity\ConsumeLimitConfigEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\consume\domain\entity\BalanceChangeEntity.java",
    "sa-admin\src\main\java\net\lab1024\sa\admin\module\attendance\domain\entity\AttendanceExceptionEntity.java"
)

$basePath = "D:\IOE-DREAM\smart-admin-api-java17-springboot3"

foreach ($file in $files) {
    $fullPath = Join-Path $basePath $file
    if (Test-Path $fullPath) {
        Write-Host "Processing: $file"

        $content = Get-Content $fullPath -Raw -Encoding UTF8

        # 检查是否已经有lombok导入
        if ($content -notmatch "import lombok\.Data") {
            # 在BaseEntity import后添加lombok导入
            $content = $content -replace "(import net\.lab1024\.sa\.base\.common\.entity\.BaseEntity;)", "`$1`nimport lombok.Data;`nimport lombok.EqualsAndHashCode;"
        }

        # 修复注解：将 (callSuper = true) 替换为 @EqualsAndHashCode(callSuper = true)
        # 并在前面添加 @Data
        if ($content -match "^\s*\(callSuper = true\)") {
            $content = $content -replace "^\s*\(callSuper = true\)", "@Data`n@EqualsAndHashCode(callSuper = true)"
        }

        # 移除 @Accessors(chain = true) 如果存在（因为@Data已经包含）
        $content = $content -replace "@Accessors\(chain = true\)\s*\n", ""

        Set-Content -Path $fullPath -Value $content -Encoding UTF8 -NoNewline
        Write-Host "  Fixed: $file"
    } else {
        Write-Host "  Not found: $file"
    }
}

Write-Host "`nBatch fix completed!"

