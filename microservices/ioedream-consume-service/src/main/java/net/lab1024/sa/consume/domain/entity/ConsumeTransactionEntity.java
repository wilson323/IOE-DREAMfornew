package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费交易记录实体
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_transaction")
@Schema(description = "消费交易记录实体")
public class ConsumeTransactionEntity extends BaseEntity {

    /**
     * 获取交易ID（兼容性方法）
     */
    public Long getId() {
        return this.transactionId;
    }

    @TableId(type = IdType.AUTO)
    @Schema(description = "交易ID", example = "1")
    private Long transactionId;

    @TableField("transaction_no")
    @NotBlank(message = "交易编号不能为空")
    @Size(max = 50, message = "交易编号长度不能超过50个字符")
    @Schema(description = "交易编号", example = "TXN_20251222_001")
    private String transactionNo;

    @TableField("account_id")
    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "2001")
    private Long userId;

    @TableField("device_id")
    @Schema(description = "设备ID", example = "3001")
    private Long deviceId;

    @TableField("device_code")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    @Schema(description = "设备编码", example = "POS_001")
    private String deviceCode;

    @TableField("transaction_type")
    @NotNull(message = "交易类型不能为空")
    @Min(value = 1, message = "交易类型值无效")
    @Max(value = 4, message = "交易类型值无效")
    @Schema(description = "交易类型", example = "1")
    private Integer transactionType;

    @TableField(exist = false)
    @Schema(description = "交易类型名称", example = "消费")
    private String transactionTypeName;

    @TableField("transaction_amount")
    @NotNull(message = "交易金额不能为空")
    @DecimalMin(value = "0.01", message = "交易金额必须大于0")
    @Digits(integer = 12, fraction = 2, message = "交易金额格式不正确")
    @Schema(description = "交易金额", example = "15.00")
    private BigDecimal transactionAmount;

    @TableField("discount_amount")
    @DecimalMin(value = "0", message = "优惠金额不能为负数")
    @Digits(integer = 12, fraction = 2, message = "优惠金额格式不正确")
    @Schema(description = "优惠金额", example = "2.00")
    private BigDecimal discountAmount;

    @TableField("actual_amount")
    @NotNull(message = "实际金额不能为空")
    @DecimalMin(value = "0.01", message = "实际金额必须大于0")
    @Digits(integer = 12, fraction = 2, message = "实际金额格式不正确")
    @Schema(description = "实际金额", example = "13.00")
    private BigDecimal actualAmount;

    @TableField("balance_before")
    @NotNull(message = "交易前余额不能为空")
    @Digits(integer = 12, fraction = 2, message = "交易前余额格式不正确")
    @Schema(description = "交易前余额", example = "115.50")
    private BigDecimal balanceBefore;

    @TableField("balance_after")
    @NotNull(message = "交易后余额不能为空")
    @Digits(integer = 12, fraction = 2, message = "交易后余额格式不正确")
    @Schema(description = "交易后余额", example = "102.50")
    private BigDecimal balanceAfter;

    @TableField("payment_method")
    @NotNull(message = "支付方式不能为空")
    @Min(value = 1, message = "支付方式值无效")
    @Max(value = 5, message = "支付方式值无效")
    @Schema(description = "支付方式", example = "1")
    private Integer paymentMethod;

    @TableField(exist = false)
    @Schema(description = "支付方式名称", example = "账户余额")
    private String paymentMethodName;

    @TableField("transaction_status")
    @NotNull(message = "交易状态不能为空")
    @Min(value = 0, message = "交易状态值无效")
    @Max(value = 2, message = "交易状态值无效")
    @Schema(description = "交易状态", example = "1")
    private Integer transactionStatus;

    @TableField(exist = false)
    @Schema(description = "交易状态名称", example = "成功")
    private String transactionStatusName;

    @TableField("failure_reason")
    @Size(max = 255, message = "失败原因长度不能超过255个字符")
    @Schema(description = "失败原因", example = "")
    private String failureReason;

    @TableField("product_info")
    @Schema(description = "商品信息", example = "营养早餐套餐x1")
    private String productInfo;

    @TableField("merchant_id")
    @Schema(description = "商户ID", example = "4001")
    private Long merchantId;

    @TableField("merchant_name")
    @Size(max = 100, message = "商户名称长度不能超过100个字符")
    @Schema(description = "商户名称", example = "员工食堂")
    private String merchantName;

    @TableField("location_info")
    @Size(max = 200, message = "位置信息长度不能超过200个字符")
    @Schema(description = "位置信息", example = "1楼餐厅")
    private String locationInfo;

    @TableField("operator_id")
    @Schema(description = "操作员ID", example = "5001")
    private Long operatorId;

    @TableField("operator_name")
    @Size(max = 50, message = "操作员姓名长度不能超过50个字符")
    @Schema(description = "操作员姓名", example = "张三")
    private String operatorName;

    @TableField("verify_type")
    @Schema(description = "验证方式", example = "1")
    private Integer verifyType;

    @TableField("verify_info")
    @Size(max = 200, message = "验证信息长度不能超过200个字符")
    @Schema(description = "验证信息", example = "人脸识别")
    private String verifyInfo;

    @TableField("transaction_time")
    @NotNull(message = "交易时间不能为空")
    @Schema(description = "交易时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;

    @TableField("settlement_time")
    @Schema(description = "结算时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime settlementTime;

    @TableField("remark")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    @Schema(description = "备注", example = "")
    private String remark;

    @TableField("ext_data")
    @Schema(description = "扩展数据", example = "")
    private String extData;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    @Schema(description = "创建人ID")
    private Long createUserId;

    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新人ID")
    private Long updateUserId;

    @TableLogic
    @TableField("deleted_flag")
    @Schema(description = "删除标记", example = "0")
    private Integer deletedFlag;

    @Version
    @TableField("version")
    @Schema(description = "版本号", example = "1")
    private Integer version;

    // ==================== 业务状态方法 ====================

    /**
     * 检查交易是否成功
     */
    public boolean isSuccess() {
        return Integer.valueOf(1).equals(transactionStatus);
    }

    /**
     * 检查交易是否失败
     */
    public boolean isFailed() {
        return Integer.valueOf(0).equals(transactionStatus);
    }

    /**
     * 检查交易是否处理中
     */
    public boolean isProcessing() {
        return Integer.valueOf(2).equals(transactionStatus);
    }

    /**
     * 检查是否为消费交易
     */
    public boolean isConsumeTransaction() {
        return Integer.valueOf(1).equals(transactionType);
    }

    /**
     * 检查是否为充值交易
     */
    public boolean isRechargeTransaction() {
        return Integer.valueOf(2).equals(transactionType);
    }

    /**
     * 检查是否为退款交易
     */
    public boolean isRefundTransaction() {
        return Integer.valueOf(3).equals(transactionType);
    }

    /**
     * 检查是否为补贴交易
     */
    public boolean isSubsidyTransaction() {
        return Integer.valueOf(4).equals(transactionType);
    }

    /**
     * 获取交易类型描述
     */
    public String getTransactionTypeDescription() {
        if (isConsumeTransaction()) {
            return "消费";
        } else if (isRechargeTransaction()) {
            return "充值";
        } else if (isRefundTransaction()) {
            return "退款";
        } else if (isSubsidyTransaction()) {
            return "补贴";
        }
        return "未知";
    }

    /**
     * 获取交易状态描述
     */
    public String getTransactionStatusDescription() {
        if (isSuccess()) {
            return "成功";
        } else if (isFailed()) {
            return "失败";
        } else if (isProcessing()) {
            return "处理中";
        }
        return "未知";
    }

    /**
     * 检查是否有优惠
     */
    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 获取优惠率
     */
    public BigDecimal getDiscountRate() {
        if (transactionAmount == null || transactionAmount.compareTo(BigDecimal.ZERO) <= 0 || !hasDiscount()) {
            return BigDecimal.ZERO;
        }
        return discountAmount.divide(transactionAmount, 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 检查余额是否正确
     */
    public boolean isBalanceCorrect() {
        if (balanceBefore == null || balanceAfter == null || actualAmount == null) {
            return false;
        }

        if (isConsumeTransaction()) {
            // 消费交易：交易后余额 = 交易前余额 - 实际金额
            return balanceAfter.compareTo(balanceBefore.subtract(actualAmount)) == 0;
        } else if (isRechargeTransaction()) {
            // 充值交易：交易后余额 = 交易前余额 + 实际金额
            return balanceAfter.compareTo(balanceBefore.add(actualAmount)) == 0;
        }

        return true; // 其他类型暂不验证
    }

    /**
     * 检查交易时间是否合理
     */
    public boolean isTransactionTimeReasonable() {
        if (transactionTime == null) {
            return false;
        }

        // 检查交易时间不能超过当前时间
        return !transactionTime.isAfter(LocalDateTime.now());
    }

    /**
     * 获取支付方式描述
     */
    public String getPaymentMethodDescription() {
        if (paymentMethod == null) {
            return "未知";
        }

        switch (paymentMethod) {
            case 1:
                return "账户余额";
            case 2:
                return "信用额度";
            case 3:
                return "混合支付";
            case 4:
                return "现金";
            case 5:
                return "第三方支付";
            default:
                return "未知";
        }
    }

    /**
     * 检查是否为大额交易
     */
    public boolean isLargeAmountTransaction() {
        return actualAmount != null && actualAmount.compareTo(new BigDecimal("100")) >= 0;
    }

    /**
     * 检查是否为异常交易
     */
    public boolean isAbnormalTransaction() {
        // 检查交易金额异常
        if (actualAmount == null || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        // 检查余额异常
        if (!isBalanceCorrect()) {
            return true;
        }

        // 检查时间异常
        if (!isTransactionTimeReasonable()) {
            return true;
        }

        return false;
    }

    /**
     * 获取交易风险等级
     */
    public String getRiskLevel() {
        int riskScore = 0;

        // 金额风险
        if (isLargeAmountTransaction()) {
            riskScore += 2;
        }

        // 时间风险（深夜交易）
        if (transactionTime != null) {
            int hour = transactionTime.getHour();
            if (hour < 6 || hour > 22) {
                riskScore += 1;
            }
        }

        // 频率风险（简单判断，实际应该查询近期交易频率）

        if (riskScore >= 3) {
            return "高风险";
        } else if (riskScore >= 1) {
            return "中风险";
        } else {
            return "低风险";
        }
    }

    // ==================== 兼容性方法（为Manager层提供支持）====================

    /**
     * 设置交易ID（兼容性方法）
     */
    public void setId(String id) {
        this.transactionId = Long.valueOf(id);
    }

    /**
     * 设置人员ID（兼容性方法）
     */
    public void setPersonId(String personId) {
        this.userId = Long.valueOf(personId);
    }

    /**
     * 设置人员姓名（兼容性方法）
     */
    public void setPersonName(String personName) {
        // 这个字段在当前实体中不存在，可以考虑添加到extData中或者忽略
    }

    /**
     * 设置部门ID（兼容性方法）
     */
    public void setDeptId(String deptId) {
        // 这个字段在当前实体中不存在，可以考虑添加到extData中或者忽略
    }

    /**
     * 设置设备ID（兼容性方法，支持String类型）
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = Long.valueOf(deviceId);
    }

    /**
     * 设置餐次ID（兼容性方法）
     */
    public void setMealId(String mealId) {
        // 将餐次ID存储到扩展数据中
        if (this.extData == null) {
            this.extData = "";
        }
        this.extData = updateExtData(this.extData, "mealId", mealId);
    }

    /**
     * 设置消费金额（以分为单位，兼容性方法）
     */
    public void setConsumeMoney(int consumeMoney) {
        this.transactionAmount = new BigDecimal(consumeMoney).divide(new BigDecimal("100"));
    }

    /**
     * 设置最终金额（以分为单位，兼容性方法）
     */
    public void setFinalMoney(int finalMoney) {
        this.actualAmount = new BigDecimal(finalMoney).divide(new BigDecimal("100"));
    }

    /**
     * 设置消费模式（兼容性方法）
     */
    public void setConsumeMode(String consumeMode) {
        // 将消费模式存储到扩展数据中
        if (this.extData == null) {
            this.extData = "";
        }
        this.extData = updateExtData(this.extData, "consumeMode", consumeMode);
    }

    /**
     * 设置消费类型（兼容性方法）
     */
    public void setConsumeType(String consumeType) {
        // 根据消费类型设置对应的transactionType
        if ("CONSUME".equals(consumeType)) {
            this.transactionType = 1;
        } else if ("RECHARGE".equals(consumeType)) {
            this.transactionType = 2;
        } else if ("REFUND".equals(consumeType)) {
            this.transactionType = 3;
        } else if ("SUBSIDY".equals(consumeType)) {
            this.transactionType = 4;
        }
    }

    /**
     * 设置消费时间（兼容性方法）
     */
    public void setConsumeTime(LocalDateTime consumeTime) {
        this.transactionTime = consumeTime;
    }

    /**
     * 设置状态（兼容性方法）
     */
    public void setStatus(String status) {
        if ("SUCCESS".equals(status)) {
            this.transactionStatus = 1;
        } else if ("FAILED".equals(status)) {
            this.transactionStatus = 0;
        } else if ("PROCESSING".equals(status)) {
            this.transactionStatus = 2;
        }
    }

    /**
     * 更新扩展数据
     */
    private String updateExtData(String currentExtData, String key, String value) {
        // 简单实现：直接拼接JSON格式数据
        if (currentExtData == null || currentExtData.trim().isEmpty()) {
            return "{\"" + key + "\":\"" + value + "\"}";
        } else {
            // 这里应该解析JSON并更新，为了简化直接拼接
            return currentExtData + ",\"" + key + "\":\"" + value + "\"";
        }
    }

    // ==================== 兼容性Getter方法====================

    /**
     * 获取消费时间（兼容性方法）
     */
    public LocalDateTime getConsumeTime() {
        return this.transactionTime;
    }

    /**
     * 获取状态（兼容性方法）
     */
    public String getStatus() {
        if (this.transactionStatus == null) {
            return "UNKNOWN";
        }
        switch (this.transactionStatus) {
            case 1: return "SUCCESS";
            case 0: return "FAILED";
            case 2: return "PROCESSING";
            default: return "UNKNOWN";
        }
    }

    /**
     * 获取最终金额（以分为单位，兼容性方法）
     */
    public Integer getFinalMoney() {
        if (this.actualAmount == null) {
            return 0;
        }
        return this.actualAmount.multiply(new BigDecimal("100")).intValue();
    }
}