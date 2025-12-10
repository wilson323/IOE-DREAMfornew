package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 移动端充值结果VO
 * 移动端充值接口响应数据结构
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端充值结果")
public class MobileRechargeResultVO {

    @Schema(description = "订单ID", example = "MOBILE_RECHARGE_20250130001")
    private String orderId;

    @Schema(description = "充值状态", example = "SUCCESS")
    private String status;

    @Schema(description = "状态描述", example = "充值成功")
    private String statusDescription;

    @Schema(description = "充值金额", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "实际到账金额", example = "100.00")
    private BigDecimal actualAmount;

    @Schema(description = "优惠金额", example = "0.00")
    private BigDecimal discountAmount;

    @Schema(description = "充值前余额", example = "850.30")
    private BigDecimal balanceBefore;

    @Schema(description = "充值后余额", example = "950.30")
    private BigDecimal balanceAfter;

    @Schema(description = "账户ID", example = "1")
    private Long accountId;

    @Schema(description = "账户编号", example = "ACC1001")
    private String accountNumber;

    @Schema(description = "充值时间", example = "2025-01-30 14:30:00")
    private LocalDateTime rechargeTime;

    @Schema(description = "充值类型", example = "ONLINE")
    private String rechargeType;

    @Schema(description = "充值类型描述", example = "在线充值")
    private String rechargeTypeDescription;

    @Schema(description = "支付方式", example = "WECHAT")
    private String paymentMethod;

    @Schema(description = "支付方式描述", example = "微信支付")
    private String paymentMethodDescription;

    @Schema(description = "第三方交易号", example = "wx123456789")
    private String thirdPartyTransactionId;

    @Schema(description = "支付完成时间", example = "2025-01-30 14:30:15")
    private LocalDateTime paymentTime;

    @Schema(description = "手续费", example = "0.00")
    private BigDecimal fee;

    @Schema(description = "积分奖励", example = "10")
    private Integer pointsRewarded;

    @Schema(description = "获得优惠券ID", example = "1001")
    private Long couponId;

    @Schema(description = "优惠券描述", example = "满200减20")
    private String couponDescription;

    @Schema(description = "是否首充", example = "false")
    private Boolean isFirstRecharge;

    @Schema(description = "首充奖励", example = "0.00")
    private BigDecimal firstRechargeBonus;

    @Schema(description = "充值来源", example = "移动端")
    private String source;

    @Schema(description = "设备信息", example = "iPhone 14 Pro")
    private String deviceInfo;

    @Schema(description = "交易流水号", example = "TXN202501301430001")
    private String transactionNumber;

    @Schema(description = "备注", example = "用户自主充值")
    private String remark;

    @Schema(description = "预计到账时间", example = "2025-01-30 14:30:00")
    private LocalDateTime expectedArrivalTime;

    @Schema(description = "是否需要人工审核", example = "false")
    private Boolean requiresManualReview;

    @Schema(description = "审核状态", example = "APPROVED")
    private String reviewStatus;

    @Schema(description = "审核时间", example = "2025-01-30 14:31:00")
    private LocalDateTime reviewTime;

    @Schema(description = "审核人", example = "系统自动")
    private String reviewer;
}