# 批量创建缺失的Service接口脚本
# 目的: 快速创建consume-service缺失的Service接口

param(
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "批量创建缺失的Service接口" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

$serviceRoot = "D:/IOE-DREAM/microservices/ioedream-consume-service"
$serviceDir = "$serviceRoot/src/main/java/net/lab1024/sa/consume/service"

$services = @(
    @{
        Name = "PaymentService"
        Description = "支付服务接口"
        Methods = @(
            @{
                Name = "processPayment"
                ReturnType = "Boolean"
                Params = "String accountNo, BigDecimal amount, String paymentMethod"
                Description = "处理支付"
            },
            @{
                Name = "refundPayment"
                ReturnType = "Boolean"
                Params = "Long paymentId, String reason"
                Description = "退款处理"
            }
        )
    }
)

Write-Host "准备创建 $($services.Count) 个Service接口..." -ForegroundColor White

$createdCount = 0

foreach ($service in $services) {
    $filePath = "$serviceDir/$($service.Name).java"

    if (Test-Path $filePath) {
        Write-Host "  ⚠️ 文件已存在: $($service.Name)" -ForegroundColor Yellow
        continue
    }

    # 生成Service接口内容
    $content = @"
package net.lab1024.sa.consume.service;

/**
 * $($service.Description)
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since $(Get-Date -Format "yyyy-MM-dd")
 */
public interface $($service.Name) {
"@

    # 添加方法
    foreach ($method in $service.Methods) {
        $content += @"

    /**
     * $($method.Description)
     * $($method.Params)
     * @return $($method.ReturnType)
     */
    $($method.ReturnType) $($method.Name)($($method.Params));
"@
    }

    $content += @"
}
"@

    if ($DryRun) {
        Write-Host "  [DRY RUN] 将创建: $($service.Name)" -ForegroundColor Yellow
    } else {
        $content | Out-File -FilePath $filePath -Encoding UTF8
        Write-Host "  ✅ 创建: $($service.Name)" -ForegroundColor Green
        $createdCount++
    }
}

Write-Host "`n创建完成！共创建 $createdCount 个Service接口" -ForegroundColor Green

exit 0