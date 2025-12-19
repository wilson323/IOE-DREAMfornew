# 批量创建缺失的Entity类脚本
# 目的: 快速创建consume-service缺失的所有Entity类

param(
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "批量创建缺失的Entity类" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan

$serviceRoot = "D:/IOE-DREAM/microservices/ioedream-consume-service"
$entityDir = "$serviceRoot/src/main/java/net/lab1024/sa/consume/entity"

$entities = @(
    @{
        Name = "ConsumeAreaEntity"
        TableName = "t_consume_area"
        Description = "消费区域实体"
        Fields = @(
            @{ Name = "areaId"; Type = "Long"; Column = "area_id"; Desc = "区域ID" }
            @{ Name = "areaName"; Type = "String"; Column = "area_name"; Desc = "区域名称" }
            @{ Name = "areaCode"; Type = "String"; Column = "area_code"; Desc = "区域编码" }
            @{ Name = "areaType"; Type = "Integer"; Column = "area_type"; Desc = "区域类型" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "状态" }
            @{ Name = "remark"; Type = "String"; Column = "remark"; Desc = "备注" }
        )
    },
    @{
        Name = "ConsumeProductEntity"
        TableName = "t_consume_product"
        Description = "消费产品实体"
        Fields = @(
            @{ Name = "productId"; Type = "Long"; Column = "product_id"; Desc = "产品ID" }
            @{ Name = "productCode"; Type = "String"; Column = "product_code"; Desc = "产品编码" }
            @{ Name = "productName"; Type = "String"; Column = "product_name"; Desc = "产品名称" }
            @{ Name = "category"; Type = "String"; Column = "category"; Desc = "产品分类" }
            @{ Name = "price"; Type = "java.math.BigDecimal"; Column = "price"; Desc = "价格" }
            @{ Name = "stock"; Type = "Integer"; Column = "stock"; Desc = "库存" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "状态" }
        )
    },
    @{
        Name = "ConsumeTransactionEntity"
        TableName = "t_consume_transaction"
        Description = "消费交易实体"
        Fields = @(
            @{ Name = "transactionId"; Type = "Long"; Column = "transaction_id"; Desc = "交易ID" }
            @{ Name = "transactionNo"; Type = "String"; Column = "transaction_no"; Desc = "交易流水号" }
            @{ Name = "accountNo"; Type = "String"; Column = "account_no"; Desc = "账户编号" }
            @{ Name = "amount"; Type = "java.math.BigDecimal"; Column = "amount"; Desc = "交易金额" }
            @{ Name = "transactionType"; Type = "String"; Column = "transaction_type"; Desc = "交易类型" }
            @{ Name = "status"; Type = "String"; Column = "status"; Desc = "交易状态" }
            @{ Name = "remark"; Type = "String"; Column = "remark"; Desc = "备注" }
        )
    },
    @{
        Name = "MealOrderEntity"
        TableName = "t_meal_order"
        Description = "用餐订单实体"
        Fields = @(
            @{ Name = "orderId"; Type = "Long"; Column = "order_id"; Desc = "订单ID" }
            @{ Name = "orderNo"; Type = "String"; Column = "order_no"; Desc = "订单编号" }
            @{ Name = "userId"; Type = "Long"; Column = "user_id"; Desc = "用户ID" }
            @{ Name = "userName"; Type = "String"; Column = "user_name"; Desc = "用户名" }
            @{ Name = "mealType"; Type = "String"; Column = "meal_type"; Desc = "餐次类型" }
            @{ Name = "totalAmount"; Type = "java.math.BigDecimal"; Column = "total_amount"; Desc = "总金额" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "订单状态" }
        )
    },
    @{
        Name = "MealOrderItemEntity"
        TableName = "t_meal_order_item"
        Description = "用餐订单项实体"
        Fields = @(
            @{ Name = "itemId"; Type = "Long"; Column = "item_id"; Desc = "项目ID" }
            @{ Name = "orderId"; Type = "Long"; Column = "order_id"; Desc = "订单ID" }
            @{ Name = "productName"; Type = "String"; Column = "product_name"; Desc = "产品名称" }
            @{ Name = "quantity"; Type = "Integer"; Column = "quantity"; Desc = "数量" }
            @{ Name = "unitPrice"; Type = "java.math.BigDecimal"; Column = "unit_price"; Desc = "单价" }
            @{ Name = "totalPrice"; Type = "java.math.BigDecimal"; Column = "total_price"; Desc = "小计" }
        )
    },
    @{
        Name = "RefundApplicationEntity"
        TableName = "t_refund_application"
        Description = "退款申请实体"
        Fields = @(
            @{ Name = "applicationId"; Type = "Long"; Column = "application_id"; Desc = "申请ID" }
            @{ Name = "applicationNo"; Type = "String"; Column = "application_no"; Desc = "申请编号" }
            @{ Name = "userId"; Type = "Long"; Column = "user_id"; Desc = "用户ID" }
            @{ Name = "consumeRecordId"; Type = "Long"; Column = "consume_record_id"; Desc = "消费记录ID" }
            @{ Name = "refundAmount"; Type = "java.math.BigDecimal"; Column = "refund_amount"; Desc = "退款金额" }
            @{ Name = "refundReason"; Type = "String"; Column = "refund_reason"; Desc = "退款原因" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "申请状态" }
        )
    },
    @{
        Name = "ReimbursementApplicationEntity"
        TableName = "t_reimbursement_application"
        Description = "报销申请实体"
        Fields = @(
            @{ Name = "applicationId"; Type = "Long"; Column = "application_id"; Desc = "申请ID" }
            @{ Name = "applicationNo"; Type = "String"; Column = "application_no"; Desc = "申请编号" }
            @{ Name = "userId"; Type = "Long"; Column = "user_id"; Desc = "用户ID" }
            @{ Name = "totalAmount"; Type = "java.math.BigDecimal"; Column = "total_amount"; Desc = "总金额" }
            @{ Name = "reimbursementType"; Type = "String"; Column = "reimbursement_type"; Desc = "报销类型" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "申请状态" }
            @{ Name = "remark"; Type = "String"; Column = "remark"; Desc = "备注" }
        )
    },
    @{
        Name = "PaymentRecordEntity"
        TableName = "t_payment_record"
        Description = "支付记录实体"
        Fields = @(
            @{ Name = "paymentId"; Type = "Long"; Column = "payment_id"; Desc = "支付ID" }
            @{ Name = "paymentNo"; Type = "String"; Column = "payment_no"; Desc = "支付编号" }
            @{ Name = "accountNo"; Type = "String"; Column = "account_no"; Desc = "账户编号" }
            @{ Name = "paymentMethod"; Type = "String"; Column = "payment_method"; Desc = "支付方式" }
            @{ Name = "amount"; Type = "java.math.BigDecimal"; Column = "amount"; Desc = "支付金额" }
            @{ Name = "paymentTime"; Type = "java.time.LocalDateTime"; Column = "payment_time"; Desc = "支付时间" }
            @{ Name = "status"; Type = "String"; Column = "status"; Desc = "支付状态" }
        )
    },
    @{
        Name = "PaymentRefundRecordEntity"
        TableName = "t_payment_refund_record"
        Description = "支付退款记录实体"
        Fields = @(
            @{ Name = "refundId"; Type = "Long"; Column = "refund_id"; Desc = "退款ID" }
            @{ Name = "refundNo"; Type = "String"; Column = "refund_no"; Desc = "退款编号" }
            @{ Name = "paymentId"; Type = "Long"; Column = "payment_id"; Desc = "原支付ID" }
            @{ Name = "refundAmount"; Type = "java.math.BigDecimal"; Column = "refund_amount"; Desc = "退款金额" }
            @{ Name = "refundTime"; Type = "java.time.LocalDateTime"; Column = "refund_time"; Desc = "退款时间" }
            @{ Name = "status"; Type = "String"; Column = "status"; Desc = "退款状态" }
            @{ Name = "remark"; Type = "String"; Column = "remark"; Desc = "备注" }
        )
    },
    @{
        Name = "ConsumeSubsidyIssueRecordEntity"
        TableName = "t_consume_subsidy_issue_record"
        Description = "消费补贴发放记录实体"
        Fields = @(
            @{ Name = "issueId"; Type = "Long"; Column = "issue_id"; Desc = "发放ID" }
            @{ Name = "userId"; Type = "Long"; Column = "user_id"; Desc = "用户ID" }
            @{ Name = "subsidyType"; Type = "String"; Column = "subsidy_type"; Desc = "补贴类型" }
            @{ Name = "subsidyAmount"; Type = "java.math.BigDecimal"; Column = "subsidy_amount"; Desc = "补贴金额" }
            @{ Name = "issueTime"; Type = "java.time.LocalDateTime"; Column = "issue_time"; Desc = "发放时间" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "发放状态" }
            @{ Name = "remark"; Type = "String"; Column = "remark"; Desc = "备注" }
        )
    },
    @{
        Name = "OfflineConsumeRecordEntity"
        TableName = "t_offline_consume_record"
        Description = "离线消费记录实体"
        Fields = @(
            @{ Name = "recordId"; Type = "Long"; Column = "record_id"; Desc = "记录ID" }
            @{ Name = "accountNo"; Type = "String"; Column = "account_no"; Desc = "账户编号" }
            @{ Name = "deviceCode"; Type = "String"; Column = "device_code"; Desc = "设备编码" }
            @{ Name = "amount"; Type = "java.math.BigDecimal"; Column = "amount"; Desc = "消费金额" }
            @{ Name = "consumeTime"; Type = "java.time.LocalDateTime"; Column = "consume_time"; Desc = "消费时间" }
            @{ Name = "syncTime"; Type = "java.time.LocalDateTime"; Column = "sync_time"; Desc = "同步时间" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "同步状态" }
        )
    },
    @{
        Name = "QrCodeEntity"
        TableName = "t_qr_code"
        Description = "二维码实体"
        Fields = @(
            @{ Name = "qrId"; Type = "Long"; Column = "qr_id"; Desc = "二维码ID" }
            @{ Name = "qrCode"; Type = "String"; Column = "qr_code"; Desc = "二维码内容" }
            @{ Name = "qrType"; Type = "String"; Column = "qr_type"; Desc = "二维码类型" }
            @{ Name = "accountNo"; Type = "String"; Column = "account_no"; Desc = "关联账户" }
            @{ Name = "validTime"; Type = "java.time.LocalDateTime"; Column = "valid_time"; Desc = "有效期" }
            @{ Name = "status"; Type = "Integer"; Column = "status"; Desc = "状态" }
            @{ Name = "remark"; Type = "String"; Column = "remark"; Desc = "备注" }
        )
    }
)

Write-Host "准备创建 $($entities.Count) 个Entity类..." -ForegroundColor White

$createdCount = 0

foreach ($entity in $entities) {
    $filePath = "$entityDir/$($entity.Name).java"

    if (Test-Path $filePath) {
        Write-Host "  ⚠️ 文件已存在: $($entity.Name)" -ForegroundColor Yellow
        continue
    }

    # 生成Entity类内容
    $content = @"
package net.lab1024.sa.consume.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * $($entity.Description)
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since $(Get-Date -Format "yyyy-MM-dd")
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("$($entity.TableName)")
@Schema(description = "$($entity.Description)")
public class $($entity.Name) extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;
"@

    # 添加字段
    foreach ($field in $entity.Fields) {
        if ($field.Type -eq "java.math.BigDecimal") {
            $content += @"

    /**
     * $($field.Desc)
     */
    @TableField("$($field.Column)")
    @Schema(description = "$($field.Desc)")
    private $($field.Type) $($field.Name);
"@
        } elseif ($field.Type -eq "java.time.LocalDateTime") {
            $content += @"

    /**
     * $($field.Desc)
     */
    @TableField("$($field.Column)")
    @Schema(description = "$($field.Desc)")
    private $($field.Type) $($field.Name);
"@
        } else {
            $content += @"

    /**
     * $($field.Desc)
     */
    @TableField("$($field.Column)")
    @Schema(description = "$($field.Desc)")
    private $($field.Type) $($field.Name);
"@
        }
    }

    $content += @"

    // 注意：createTime, updateTime, createUserId, updateUserId, deletedFlag, version
    // 已由BaseEntity提供，无需重复定义
}
"@

    if ($DryRun) {
        Write-Host "  [DRY RUN] 将创建: $($entity.Name)" -ForegroundColor Yellow
    } else {
        $content | Out-File -FilePath $filePath -Encoding UTF8
        Write-Host "  ✅ 创建: $($entity.Name)" -ForegroundColor Green
        $createdCount++
    }
}

Write-Host "`n创建完成！共创建 $createdCount 个Entity类" -ForegroundColor Green

# 验证创建结果
if (-not $DryRun) {
    Write-Host "`n验证创建结果..." -ForegroundColor Yellow
    $actualCount = (Get-ChildItem -Path $entityDir -Filter "*Entity.java" | Measure-Object).Count
    Write-Host "实际Entity类数量: $actualCount" -ForegroundColor White
}

exit 0