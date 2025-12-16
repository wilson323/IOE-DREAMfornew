package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录VO
 *
 * @author IOE-DREAM
 * @since 2025-12-09
 */
@Data
@Schema(description = "消费记录VO")
public class ConsumeRecordVO {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID")
    private Long id;

    /**
     * 订单ID
     */
    @Schema(description = "订单ID")
    private String orderId;

    /**
     * 交易流水号
     */
    @Schema(description = "交易流水号")
    private String transactionNo;

    /**
     * 账户ID
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户编号
     */
    @Schema(description = "账户编号")
    private String accountNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 消费金额
     */
    @Schema(description = "消费金额")
    private BigDecimal amount;

    /**
     * 原始金额
     */
    @Schema(description = "原始金额")
    private BigDecimal originalAmount;

    /**
     * 优惠金额
     */
    @Schema(description = "优惠金额")
    private BigDecimal discountAmount;

    /**
     * 补贴金额
     */
    @Schema(description = "补贴金额")
    private BigDecimal subsidyAmount;

    /**
     * 消费前余额
     */
    @Schema(description = "消费前余额")
    private BigDecimal balanceBefore;

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
     * 消费方式
     */
    @Schema(description = "消费方式")
    private String consumeMode;

    /**
     * 消费方式描述
     */
    @Schema(description = "消费方式描述")
    private String consumeModeDesc;

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
     * 设备编号
     */
    @Schema(description = "设备编号")
    private String deviceNo;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称")
    private String deviceName;

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
     * 商户ID
     */
    @Schema(description = "商户ID")
    private Long merchantId;

    /**
     * 商户名称
     */
    @Schema(description = "商户名称")
    private String merchantName;

    /**
     * 商品ID
     */
    @Schema(description = "商品ID")
    private Long productId;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称")
    private String productName;

    /**
     * 商品分类
     */
    @Schema(description = "商品分类")
    private String productCategory;

    /**
     * 消费时间
     */
    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 操作员ID
     */
    @Schema(description = "操作员ID")
    private Long operatorId;

    /**
     * 操作员姓名
     */
    @Schema(description = "操作员姓名")
    private String operatorName;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 币种
     */
    @Schema(description = "币种", example = "CNY")
    private String currency;

    /**
     * 是否离线消费
     */
    @Schema(description = "是否离线消费")
    private Boolean isOffline;

    /**
     * 同步状态
     */
    @Schema(description = "同步状态")
    private Integer syncStatus;

    /**
     * 同步时间
     */
    @Schema(description = "同步时间")
    private LocalDateTime syncTime;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性")
    private String extendAttrs;

    // ========== 便捷方法：为了兼容现有代码调用 ==========

    /**
     * 获取记录ID（别名方法）
     */
    public Long getRecordId() {
        return this.id;
    }

    /**
     * 设置记录ID（别名方法）
     */
    public void setRecordId(Long recordId) {
        this.id = recordId;
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
        return Integer.valueOf(1).equals(this.status);
    }

    /**
     * 判断是否失败
     */
    public boolean isFailed() {
        return !isSuccess();
    }

    /**
     * 判断是否离线消费
     */
    public boolean isOfflineConsume() {
        return Boolean.TRUE.equals(this.isOffline);
    }

    /**
     * 获取消费金额（字符串格式）
     */
    public String getAmountAsString() {
        return this.amount != null ? this.amount.toString() : "0.00";
    }

    /**
     * 获取消费前余额（字符串格式）
     */
    public String getBalanceBeforeAsString() {
        return this.balanceBefore != null ? this.balanceBefore.toString() : "0.00";
    }

    /**
     * 获取消费后余额（字符串格式）
     */
    public String getBalanceAfterAsString() {
        return this.balanceAfter != null ? this.balanceAfter.toString() : "0.00";
    }
}


