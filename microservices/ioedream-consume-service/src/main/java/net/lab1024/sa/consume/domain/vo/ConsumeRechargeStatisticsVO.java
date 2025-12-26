package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消费充值统计视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "消费充值统计视图对象")
public class ConsumeRechargeStatisticsVO {

    @Schema(description = "总充值次数", example = "45")
    private Integer totalCount;

    @Schema(description = "总充值金额", example = "12000.00")
    private BigDecimal totalAmount;

    @Schema(description = "平均充值金额", example = "266.67")
    private BigDecimal avgAmount;

    @Schema(description = "最大充值金额", example = "1000.00")
    private BigDecimal maxAmount;

    @Schema(description = "最小充值金额", example = "10.00")
    private BigDecimal minAmount;

    @Schema(description = "今日充值次数", example = "5")
    private Integer todayCount;

    @Schema(description = "今日充值金额", example = "850.00")
    private BigDecimal todayAmount;

    @Schema(description = "本周充值次数", example = "18")
    private Integer weekCount;

    @Schema(description = "本周充值金额", example = "4680.00")
    private BigDecimal weekAmount;

    @Schema(description = "本月充值次数", example = "35")
    private Integer monthCount;

    @Schema(description = "本月充值金额", example = "9850.00")
    private BigDecimal monthAmount;

    @Schema(description = "现金充值次数", example = "12")
    private Integer cashCount;

    @Schema(description = "现金充值金额", example = "3200.00")
    private BigDecimal cashAmount;

    @Schema(description = "银行卡充值次数", example = "8")
    private Integer bankCardCount;

    @Schema(description = "银行卡充值金额", example = "4500.00")
    private BigDecimal bankCardAmount;

    @Schema(description = "微信充值次数", example = "15")
    private Integer wechatCount;

    @Schema(description = "微信充值金额", example = "2800.00")
    private BigDecimal wechatAmount;

    @Schema(description = "支付宝充值次数", example = "10")
    private Integer alipayCount;

    @Schema(description = "支付宝充值金额", example = "1500.00")
    private BigDecimal alipayAmount;

    @Schema(description = "补贴充值次数", example = "25")
    private Integer subsidyCount;

    @Schema(description = "补贴充值金额", example = "8000.00")
    private BigDecimal subsidyAmount;

    @Schema(description = "个人充值次数", example = "20")
    private Integer personalCount;

    @Schema(description = "个人充值金额", example = "4000.00")
    private BigDecimal personalAmount;

    @Schema(description = "成功充值次数", example = "40")
    private Long successCount;

    @Schema(description = "成功充值金额", example = "11500.00")
    private BigDecimal successAmount;

    @Schema(description = "失败充值次数", example = "5")
    private Long failureCount;

    // 兼容性字段别名方法
    public BigDecimal getTotalRechargeAmount() {
        return this.totalAmount;
    }

    public void setTotalRechargeAmount(BigDecimal totalRechargeAmount) {
        this.totalAmount = totalRechargeAmount;
    }

    public Long getTotalRechargeCount() {
        return this.totalCount != null ? this.totalCount.longValue() : 0L;
    }

    public void setTotalRechargeCount(Long totalRechargeCount) {
        this.totalCount = totalRechargeCount != null ? totalRechargeCount.intValue() : 0;
    }

    // 为Builder添加兼容性方法
    public static class ConsumeRechargeStatisticsVOBuilder {
        public ConsumeRechargeStatisticsVOBuilder totalRechargeAmount(BigDecimal totalRechargeAmount) {
            return this.totalAmount(totalRechargeAmount);
        }

        public ConsumeRechargeStatisticsVOBuilder totalRechargeCount(Long totalRechargeCount) {
            return this.totalCount(totalRechargeCount != null ? totalRechargeCount.intValue() : 0);
        }
    }

    @Schema(description = "处理中充值次数", example = "2")
    private Long processingCount;

    @Schema(description = "成功率", example = "0.89")
    private BigDecimal successRate;

    @Schema(description = "平均充值金额", example = "287.50")
    private BigDecimal averageAmount;

    @Schema(description = "统计时间")
    private LocalDateTime statisticsTime;
}
