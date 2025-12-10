package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费结果DTO
 *
 * @author IOE-DREAM
 * @since 2025-12-09
 */
@Data
@Schema(description = "消费结果DTO")
public class ConsumeResultDTO {

    /**
     * 消费记录ID
     */
    @Schema(description = "消费记录ID")
    private Long consumeId;

    /**
     * 交易流水号
     */
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 消费金额
     */
    @Schema(description = "消费金额")
    private BigDecimal amount;

    /**
     * 消费后余额
     */
    @Schema(description = "消费后余额")
    private BigDecimal balanceAfter;

    /**
     * 消费状态
     */
    @Schema(description = "消费状态", allowableValues = {"0", "1", "2"}, example = "1")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;

    /**
     * 消费时间
     */
    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * 消费方式
     */
    @Schema(description = "消费方式")
    private String consumeMode;

    /**
     * 账户ID
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 设备编号
     */
    @Schema(description = "设备编号")
    private String deviceNo;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称")
    private String areaName;

    /**
     * 商品信息
     */
    @Schema(description = "商品信息")
    private String productInfo;

    /**
     * 币种
     */
    @Schema(description = "币种", example = "CNY")
    private String currency;

    /**
     * 优惠金额
     */
    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    /**
     * 原始金额
     */
    @Schema(description = "原始金额")
    private BigDecimal originalAmount;

    /**
     * 实际扣费金额
     */
    @Schema(description = "实际扣费金额")
    private BigDecimal actualAmount;

    /**
     * 补贴金额
     */
    @Schema(description = "补贴金额")
    private BigDecimal subsidyAmount;

    /**
     * 消费类型
     */
    @Schema(description = "消费类型")
    private String consumeType;

    /**
     * 消费类型描述
     */
    @Schema(description = "消费类型描述")
    private String consumeTypeDesc;

    /**
     * 是否成功
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * 结果码
     */
    @Schema(description = "结果码")
    private String resultCode;

    /**
     * 结果消息
     */
    @Schema(description = "结果消息")
    private String resultMessage;

    /**
     * 扩展信息
     */
    @Schema(description = "扩展信息")
    private String extendInfo;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    private String orderId;

    /**
     * 消费前余额
     */
    @Schema(description = "消费前余额")
    private BigDecimal balanceBefore;

    // ========== 便捷方法：为了兼容现有代码调用 ==========

    /**
     * 获取消息（统一消息获取方法）
     * 优先返回错误信息，否则返回结果消息
     */
    public String getMessage() {
        return this.errorMessage != null ? this.errorMessage : this.resultMessage;
    }

    /**
     * 获取消息（向后兼容的getMsg方法）
     */
    public String getMsg() {
        return this.getMessage();
    }

    /**
     * 获取状态（字符串格式）
     */
    public String getStatusAsString() {
        if (this.status == null) return "UNKNOWN";
        switch (this.status) {
            case 0: return "PENDING";
            case 1: return "SUCCESS";
            case 2: return "FAILED";
            default: return "UNKNOWN";
        }
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        // 优先使用success字段，否则基于status判断
        if (this.success != null) {
            return this.success;
        }
        return Integer.valueOf(1).equals(this.status);
    }

    /**
     * 判断是否失败
     */
    public boolean isFailed() {
        return !isSuccess();
    }
}