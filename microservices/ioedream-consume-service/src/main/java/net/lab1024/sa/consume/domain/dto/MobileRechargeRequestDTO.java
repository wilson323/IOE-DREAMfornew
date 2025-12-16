package net.lab1024.sa.consume.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 移动端充值请求DTO
 * 移动端充值接口请求数据结构
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端充值请求")
public class MobileRechargeRequestDTO {

    @Schema(description = "订单ID", example = "MOBILE_RECHARGE_20250130001")
    @NotBlank(message = "订单ID不能为空")
    private String orderId;

    @Schema(description = "账户ID", example = "1")
    @NotNull(message = "账户ID不能为空")
    private Long accountId;

    @Schema(description = "充值金额", example = "100.00")
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "充值类型", example = "ONLINE")
    @NotBlank(message = "充值类型不能为空")
    private String rechargeType;

    @Schema(description = "支付方式", example = "WECHAT")
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    @Schema(description = "支付渠道", example = "WECHAT_MINI")
    private String paymentChannel;

    @Schema(description = "第三方交易号", example = "wx123456789")
    private String thirdPartyTransactionId;

    @Schema(description = "充值描述", example = "微信充值")
    private String description;

    @Schema(description = "设备ID", example = "MOBILE_001")
    private String deviceId;

    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;

    @Schema(description = "是否使用优惠券", example = "false")
    private Boolean useCoupon;

    @Schema(description = "优惠券ID", example = "1001")
    private Long couponId;

    @Schema(description = "优惠金额", example = "0.00")
    private BigDecimal discountAmount;

    @Schema(description = "实际到账金额", example = "100.00")
    private BigDecimal actualAmount;

    @Schema(description = "是否自动充值", example = "false")
    private Boolean autoRecharge;

    @Schema(description = "自动充值阈值", example = "10.00")
    private BigDecimal autoRechargeThreshold;

    @Schema(description = "自动充值金额", example = "100.00")
    private BigDecimal autoRechargeAmount;

    // 充值类型枚举
    public static final String RECHARGE_TYPE_ONLINE = "ONLINE";        // 在线充值
    public static final String RECHARGE_TYPE_OFFLINE = "OFFLINE";      // 线下充值
    public static final String RECHARGE_TYPE_TRANSFER = "TRANSFER";    // 转账充值
    public static final String RECHARGE_TYPE_BONUS = "BONUS";          // 奖励充值

    // 支付方式枚举
    public static final String PAYMENT_WECHAT = "WECHAT";              // 微信支付
    public static final String PAYMENT_ALIPAY = "ALIPAY";              // 支付宝
    public static final String PAYMENT_BANK = "BANK";                  // 银行卡
    public static final String PAYMENT_BALANCE = "BALANCE";            // 余额转账
    public static final String PAYMENT_CASH = "CASH";                  // 现金
}


