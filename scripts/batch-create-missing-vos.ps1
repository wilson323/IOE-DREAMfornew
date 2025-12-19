# 批量创建缺失的VO类脚本
# 目的: 快速创建consume-service缺失的所有VO类

param(
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "批量创建缺失的VO类" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

$serviceRoot = "D:/IOE-DREAM/microservices/ioedream-consume-service"
$voDir = "$serviceRoot/src/main/java/net/lab1024/sa/consume/domain/vo"

$vos = @(
    @{
        Name = "MobileConsumeStatisticsVO"
        Description = "移动端消费统计VO"
        Fields = @(
            @{ Name = "todayConsumeCount"; Type = "Integer"; Desc = "今日消费次数" }
            @{ Name = "todayConsumeAmount"; Type = "java.math.BigDecimal"; Desc = "今日消费金额" }
            @{ Name = "weekConsumeCount"; Type = "Integer"; Desc = "本周消费次数" }
            @{ Name = "weekConsumeAmount"; Type = "java.math.BigDecimal"; Desc = "本周消费金额" }
            @{ Name = "monthConsumeCount"; Type = "Integer"; Desc = "本月消费次数" }
            @{ Name = "monthConsumeAmount"; Type = "java.math.BigDecimal"; Desc = "本月消费金额" }
        )
    },
    @{
        Name = "MobileAccountInfoVO"
        Description = "移动端账户信息VO"
        Fields = @(
            @{ Name = "accountNo"; Type = "String"; Desc = "账户编号" }
            @{ Name = "accountName"; Type = "String"; Desc = "账户名称" }
            @{ Name = "balance"; Type = "java.math.BigDecimal"; Desc = "账户余额" }
            @{ Name = "availableBalance"; Type = "java.math.BigDecimal"; Desc = "可用余额" }
            @{ Name = "accountType"; Type = "Integer"; Desc = "账户类型" }
            @{ Name = "status"; Type = "Integer"; Desc = "账户状态" }
        )
    },
    @{
        Name = "PaymentContext"
        Description = "支付上下文"
        Fields = @(
            @{ Name = "paymentMethod"; Type = "String"; Desc = "支付方式" }
            @{ Name = "accountNo"; Type = "String"; Desc = "账户编号" }
            @{ Name = "amount"; Type = "java.math.BigDecimal"; Desc = "支付金额" }
            @{ Name = "orderNo"; Type = "String"; Desc = "订单号" }
            @{ Name = "remark"; Type = "String"; Desc = "备注" }
        )
    },
    @{
        Name = "RefundContext"
        Description = "退款上下文"
        Fields = @(
            @{ Name = "paymentId"; Type = "Long"; Desc = "原支付ID" }
            @{ Name = "refundAmount"; Type = "java.math.BigDecimal"; Desc = "退款金额" }
            @{ Name = "refundReason"; Type = "String"; Desc = "退款原因" }
            @{ Name = "remark"; Type = "String"; Desc = "备注" }
        )
    },
    @{
        Name = "RefundResult"
        Description = "退款结果"
        Fields = @(
            @{ Name = "refundId"; Type = "Long"; Desc = "退款ID" }
            @{ Name = "refundNo"; Type = "String"; Desc = "退款编号" }
            @{ Name = "success"; Type = "Boolean"; Desc = "是否成功" }
            @{ Name = "message"; Type = "String"; Desc = "返回消息" }
            @{ Name = "refundAmount"; Type = "java.math.BigDecimal"; Desc = "退款金额" }
        )
    }
)

Write-Host "准备创建 $($vos.Count) 个VO类..." -ForegroundColor White

$createdCount = 0

foreach ($vo in $vos) {
    $filePath = "$voDir/$($vo.Name).java"

    if (Test-Path $filePath) {
        Write-Host "  ⚠️ 文件已存在: $($vo.Name)" -ForegroundColor Yellow
        continue
    }

    # 生成VO类内容
    $content = @"
package net.lab1024.sa.consume.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.math.BigDecimal;

/**
 * $($vo.Description)
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since $(Get-Date -Format "yyyy-MM-dd")
 */
@Data
@Accessors(chain = true)
public class $($vo.Name) {
"@

    # 添加字段
    foreach ($field in $vo.Fields) {
        if ($field.Type -eq "java.math.BigDecimal") {
            $content += @"

    /**
     * $($field.Desc)
     */
    private $($field.Type) $($field.Name);
"@
        } else {
            $content += @"

    /**
     * $($field.Desc)
     */
    private $($field.Type) $($field.Name);
"@
        }
    }

    $content += @"
}
"@

    if ($DryRun) {
        Write-Host "  [DRY RUN] 将创建: $($vo.Name)" -ForegroundColor Yellow
    } else {
        $content | Out-File -FilePath $filePath -Encoding UTF8
        Write-Host "  ✅ 创建: $($vo.Name)" -ForegroundColor Green
        $createdCount++
    }
}

Write-Host "`n创建完成！共创建 $createdCount 个VO类" -ForegroundColor Green

exit 0