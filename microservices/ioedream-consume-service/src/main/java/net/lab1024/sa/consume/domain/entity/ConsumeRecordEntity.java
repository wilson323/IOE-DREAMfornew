package net.lab1024.sa.consume.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

/**
 * 消费记录实体
 * <p>
 * 用于记录用户的所有消费交易信息
 * 支持在线/离线消费、退款、撤销等操作
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_consume_record")
@Schema(description = "消费记录实体")
public class ConsumeRecordEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID", example = "10001")
    private Long recordId;

    @TableField("account_id")
    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "1001")
    private Long accountId;

    @TableField("user_id")
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    @TableField("user_name")
    @NotBlank(message = "用户姓名不能为空")
    @Size(max = 50, message = "用户姓名长度不能超过50个字符")
    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @TableField("device_id")
    @NotBlank(message = "设备ID不能为空")
    @Size(max = 32, message = "设备ID长度不能超过32个字符")
    @Schema(description = "设备ID", example = "POS001")
    private String deviceId;

    @TableField("device_name")
    @Size(max = 50, message = "设备名称长度不能超过50个字符")
    @Schema(description = "设备名称", example = "一楼餐厅POS机")
    private String deviceName;

    @TableField("merchant_id")
    @NotNull(message = "商户ID不能为空")
    @Schema(description = "商户ID", example = "1")
    private Long merchantId;

    @TableField("merchant_name")
    @Size(max = 100, message = "商户名称长度不能超过100个字符")
    @Schema(description = "商户名称", example = "员工餐厅")
    private String merchantName;

    @TableField("amount")
    @NotNull(message = "消费金额不能为空")
    @DecimalMin(value = "0.01", message = "消费金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "消费金额格式不正确")
    @Schema(description = "消费金额", example = "25.50")
    private BigDecimal amount;

    @TableField("original_amount")
    @Digits(integer = 10, fraction = 2, message = "原始金额格式不正确")
    @Schema(description = "原始金额（优惠前）", example = "30.00")
    private BigDecimal originalAmount;

    @TableField("discount_amount")
    @NotNull(message = "优惠金额不能为空")
    @DecimalMin(value = "0", message = "优惠金额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "优惠金额格式不正确")
    @Schema(description = "优惠金额", example = "4.50")
    private BigDecimal discountAmount;

    @TableField("consume_type")
    @NotBlank(message = "消费类型不能为空")
    @Size(max = 20, message = "消费类型长度不能超过20个字符")
    @Schema(description = "消费类型", example = "MEAL",
            allowableValues = {"MEAL", "SNACK", "DRINK"})
    private String consumeType;

    @TableField("consume_type_name")
    @Size(max = 50, message = "消费类型名称长度不能超过50个字符")
    @Schema(description = "消费类型名称", example = "餐饮")
    private String consumeTypeName;

    @TableField(exist = false)
    @Schema(description = "商品明细（JSON格式）", example = "[{\"name\":\"红烧肉\",\"price\":\"15.00\",\"quantity\":1}]")
    private String productDetail;

    @TableField("payment_method")
    @NotBlank(message = "支付方式不能为空")
    @Size(max = 20, message = "支付方式长度不能超过20个字符")
    @Schema(description = "支付方式", example = "BALANCE",
            allowableValues = {"BALANCE", "CARD", "CASH", "WECHAT", "ALIPAY"})
    private String paymentMethod;

    @TableField("order_no")
    @NotBlank(message = "订单号不能为空")
    @Size(max = 64, message = "订单号长度不能超过64个字符")
    @Schema(description = "订单号", example = "ORDER20251223001")
    private String orderNo;

    @TableField("transaction_no")
    @Size(max = 64, message = "交易流水号长度不能超过64个字符")
    @Schema(description = "交易流水号", example = "TXN20251223001")
    private String transactionNo;

    @TableField("transaction_status")
    @NotNull(message = "交易状态不能为空")
    @Min(value = 1, message = "交易状态值无效")
    @Max(value = 3, message = "交易状态值无效")
    @Schema(description = "交易状态", example = "1",
            allowableValues = {"1", "2", "3"})
    private Integer transactionStatus;

    @TableField("consume_status")
    @NotNull(message = "消费状态不能为空")
    @Min(value = 1, message = "消费状态值无效")
    @Max(value = 3, message = "消费状态值无效")
    @Schema(description = "消费状态", example = "1",
            allowableValues = {"1", "2", "3"})
    private Integer consumeStatus;

    @TableField("consume_time")
    @NotNull(message = "消费时间不能为空")
    @Schema(description = "消费时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime consumeTime;

    @TableField("consume_location")
    @Size(max = 100, message = "消费地点长度不能超过100个字符")
    @Schema(description = "消费地点", example = "一楼餐厅")
    private String consumeLocation;

    @TableField("refund_status")
    @NotNull(message = "退款状态不能为空")
    @Min(value = 0, message = "退款状态值无效")
    @Max(value = 2, message = "退款状态值无效")
    @Schema(description = "退款状态", example = "0",
            allowableValues = {"0", "1", "2"})
    private Integer refundStatus;

    @TableField("refund_amount")
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0", message = "退款金额不能为负数")
    @Digits(integer = 10, fraction = 2, message = "退款金额格式不正确")
    @Schema(description = "退款金额", example = "0.00")
    private BigDecimal refundAmount;

    @TableField("refund_time")
    @Schema(description = "退款时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime refundTime;

    @TableField("refund_reason")
    @Size(max = 200, message = "退款原因长度不能超过200个字符")
    @Schema(description = "退款原因", example = "菜品质量问题")
    private String refundReason;

    @TableField("offline_flag")
    @NotNull(message = "离线标记不能为空")
    @Schema(description = "离线标记（0-在线，1-离线）", example = "0")
    private Integer offlineFlag;

    @TableField("sync_status")
    @NotNull(message = "同步状态不能为空")
    @Schema(description = "同步状态（0-未同步，1-已同步）", example = "1")
    private Integer syncStatus;

    @TableField("sync_time")
    @Schema(description = "同步时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime syncTime;

    // ==================== 业务方法 ====================

    /**
     * 判断是否为在线消费
     */
    public boolean isOnline() {
        return Integer.valueOf(0).equals(this.offlineFlag);
    }

    /**
     * 判断是否为离线消费
     */
    public boolean isOffline() {
        return Integer.valueOf(1).equals(this.offlineFlag);
    }

    /**
     * 判断交易是否成功
     */
    public boolean isTransactionSuccess() {
        return Integer.valueOf(1).equals(this.transactionStatus);
    }

    /**
     * 判断是否已退款
     */
    public boolean isRefunded() {
        return Integer.valueOf(1).equals(this.refundStatus)
                || Integer.valueOf(2).equals(this.refundStatus);
    }

    /**
     * 判断是否已全额退款
     */
    public boolean isFullRefund() {
        return Integer.valueOf(2).equals(this.refundStatus);
    }

    /**
     * 判断是否需要同步
     */
    public boolean needSync() {
        return isOffline() && Integer.valueOf(0).equals(this.syncStatus);
    }

    /**
     * 获取实际消费金额（扣除优惠后）
     */
    public BigDecimal getActualAmount() {
        return this.amount;
    }
}
