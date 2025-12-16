package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 充值请求DTO
 *
 * @author IOE-DREAM
 * @since 2025-12-09
 */
@Data
@Schema(description = "充值请求DTO")
public class RechargeRequestDTO {

    /**
     * 账户ID
     */
    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    /**
     * 账户编号
     */
    @NotBlank(message = "账户编号不能为空")
    @Schema(description = "账户编号", example = "ACC001")
    private String accountNo;

    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal rechargeAmount;

    /**
     * 充值方式
     */
    @NotBlank(message = "充值方式不能为空")
    @Schema(description = "充值方式", allowableValues = {"CASH", "CARD", "TRANSFER", "WECHAT", "ALIPAY"}, example = "WECHAT")
    private String rechargeType;

    /**
     * 充值方式描述
     */
    @Schema(description = "充值方式描述", example = "微信支付")
    private String rechargeTypeDesc;

    /**
     * 支付流水号
     */
    @Schema(description = "支付流水号", example = "PAY20251209001")
    private String paymentNo;

    /**
     * 第三方交易号
     */
    @Schema(description = "第三方交易号", example = "WX20251209001")
    private String thirdPartyNo;

    /**
     * 操作员ID
     */
    @Schema(description = "操作员ID", example = "1001")
    private Long operatorId;

    /**
     * 操作员姓名
     */
    @Schema(description = "操作员姓名", example = "张三")
    private String operatorName;

    /**
     * 设备编号
     */
    @Schema(description = "设备编号", example = "DEV001")
    private String deviceNo;

    /**
     * 设备名称
     */
    @Schema(description = "设备名称", example = "充值终端01")
    private String deviceName;

    /**
     * 区域ID
     */
    @Schema(description = "区域ID", example = "1")
    private Long areaId;

    /**
     * 区域名称
     */
    @Schema(description = "区域名称", example = "食堂")
    private String areaName;

    /**
     * 充值渠道
     */
    @Schema(description = "充值渠道", allowableValues = {"ONLINE", "OFFLINE", "MOBILE"}, example = "ONLINE")
    private String channel;

    /**
     * 充值渠道描述
     */
    @Schema(description = "充值渠道描述", example = "线上充值")
    private String channelDesc;

    /**
     * 充值说明
     */
    @Schema(description = "充值说明", example = "月度充值")
    private String remark;

    /**
     * 币种
     */
    @Schema(description = "币种", example = "CNY")
    private String currency;

    /**
     * 优惠金额
     */
    @Schema(description = "优惠金额", example = "5.00")
    private BigDecimal discountAmount;

    /**
     * 实际到账金额
     */
    @Schema(description = "实际到账金额", example = "105.00")
    private BigDecimal actualAmount;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", example = "李四")
    private String userName;

    /**
     * 手机号
     */
    @Schema(description = "手机号", example = "13800138000")
    private String mobile;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱", example = "test@example.com")
    private String email;

    /**
     * 是否发送通知
     */
    @Schema(description = "是否发送通知", example = "true")
    private Boolean sendNotification;

    /**
     * 扩展属性
     */
    @Schema(description = "扩展属性")
    private String extendAttrs;

    /**
     * 订单ID（用于移动端充值）
     */
    @Schema(description = "订单ID", example = "ORDER20251209001")
    private String orderId;

    // ========== 便捷方法：支持不同的字段访问方式 ==========

    /**
     * 获取充值金额（通用amount访问方式）
     */
    public BigDecimal getAmount() {
        return this.rechargeAmount;
    }

    /**
     * 设置充值金额（通用amount访问方式）
     */
    public void setAmount(BigDecimal amount) {
        this.rechargeAmount = amount;
    }

    /**
     * 获取充值金额（以分为单位）
     */
    public Long getAmountInCents() {
        if (this.rechargeAmount == null) {
            return 0L;
        }
        return this.rechargeAmount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 设置充值金额（以分为单位）
     */
    public void setAmountInCents(Long amountInCents) {
        if (amountInCents == null) {
            this.rechargeAmount = BigDecimal.ZERO;
        } else {
            this.rechargeAmount = BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100));
        }
    }

    /**
     * 获取充值类型（通用type访问方式）
     */
    public String getType() {
        return this.rechargeType;
    }

    /**
     * 设置充值类型（通用type访问方式）
     */
    public void setType(String type) {
        this.rechargeType = type;
    }

    /**
     * 验证充值数据完整性
     */
    public boolean isValid() {
        return this.accountId != null
                && this.rechargeAmount != null
                && this.rechargeAmount.compareTo(BigDecimal.ZERO) > 0
                && this.rechargeType != null
                && !this.rechargeType.trim().isEmpty();
    }
}



