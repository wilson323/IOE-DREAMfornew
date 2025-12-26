package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费统计视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@Schema(description = "消费统计视图对象")
public class ConsumeStatisticsVO {

    @Schema(description = "总消费次数", example = "156")
    private Integer totalCount;

    @Schema(description = "总消费金额", example = "5680.50")
    private BigDecimal totalAmount;

    @Schema(description = "平均消费金额", example = "36.41")
    private BigDecimal avgAmount;

    @Schema(description = "最大消费金额", example = "200.00")
    private BigDecimal maxAmount;

    @Schema(description = "最小消费金额", example = "1.50")
    private BigDecimal minAmount;

    @Schema(description = "今日消费次数", example = "3")
    private Integer todayCount;

    @Schema(description = "今日消费金额", example = "85.50")
    private BigDecimal todayAmount;

    @Schema(description = "本周消费次数", example = "15")
    private Integer weekCount;

    @Schema(description = "本周消费金额", example = "568.00")
    private BigDecimal weekAmount;

    @Schema(description = "本月消费次数", example = "62")
    private Integer monthCount;

    @Schema(description = "本月消费金额", example = "2340.50")
    private BigDecimal monthAmount;

    @Schema(description = "餐饮消费次数", example = "98")
    private Integer mealCount;

    @Schema(description = "餐饮消费金额", example = "3520.00")
    private BigDecimal mealAmount;

    @Schema(description = "购物消费次数", example = "35")
    private Integer shoppingCount;

    @Schema(description = "购物消费金额", example = "1250.50")
    private BigDecimal shoppingAmount;

    @Schema(description = "其他消费次数", example = "23")
    private Integer otherCount;

    @Schema(description = "其他消费金额", example = "910.00")
    private BigDecimal otherAmount;

    @Schema(description = "余额支付次数", example = "120")
    private Integer balancePaymentCount;

    @Schema(description = "余额支付金额", example = "4320.00")
    private BigDecimal balancePaymentAmount;

    @Schema(description = "卡片支付次数", example = "25")
    private Integer cardPaymentCount;

    @Schema(description = "卡片支付金额", example = "890.50")
    private BigDecimal cardPaymentAmount;

    @Schema(description = "其他支付次数", example = "11")
    private Integer otherPaymentCount;

    @Schema(description = "其他支付金额", example = "470.00")
    private BigDecimal otherPaymentAmount;
}