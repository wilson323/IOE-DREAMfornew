package net.lab1024.sa.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消费补贴统计视图对象
 * <p>
 * 完整的企业级实现，包含：
 * - 综合统计信息
 * - 使用情况分析
 * - 趋势分析数据
 * - 分布统计信息
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Accessors(chain = true)
@Schema(description = "消费补贴统计信息")
public class ConsumeSubsidyStatisticsVO {

    @Schema(description = "统计时间", example = "2025-12-21T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statisticsTime;

    // ==================== 总体统计 ====================

    @Schema(description = "总补贴数量", example = "1250")
    private Long totalSubsidyCount;

    @Schema(description = "总补贴金额", example = "1250000.00")
    private BigDecimal totalSubsidyAmount;

    @Schema(description = "已发放补贴数量", example = "1180")
    private Long issuedSubsidyCount;

    @Schema(description = "已发放补贴金额", example = "1180000.00")
    private BigDecimal issuedSubsidyAmount;

    @Schema(description = "已使用补贴数量", example = "950")
    private Long usedSubsidyCount;

    @Schema(description = "已使用补贴金额", example = "850000.00")
    private BigDecimal usedSubsidyAmount;

    @Schema(description = "剩余补贴金额", example = "330000.00")
    private BigDecimal remainingSubsidyAmount;

    @Schema(description = "已过期补贴数量", example = "35")
    private Long expiredSubsidyCount;

    @Schema(description = "已过期补贴金额", example = "35000.00")
    private BigDecimal expiredSubsidyAmount;

    @Schema(description = "已作废补贴数量", example = "15")
    private Long cancelledSubsidyCount;

    @Schema(description = "已作废补贴金额", example = "15000.00")
    private BigDecimal cancelledSubsidyAmount;

    // ==================== 使用率统计 ====================

    @Schema(description = "整体使用率", example = "68.0")
    private BigDecimal overallUsageRate;

    @Schema(description = "发放率", example = "94.4")
    private BigDecimal issuanceRate;

    @Schema(description = "过期率", example = "2.8")
    private BigDecimal expirationRate;

    @Schema(description = "作废率", example = "1.2")
    private BigDecimal cancellationRate;

    @Schema(description = "剩余率", example = "28.0")
    private BigDecimal remainingRate;

    // ==================== 按类型统计 ====================

    @Schema(description = "按类型统计", example = "[]")
    private List<TypeStatistics> typeStatistics;

    @Schema(description = "按周期统计", example = "[]")
    private List<PeriodStatistics> periodStatistics;

    @Schema(description = "按状态统计", example = "[]")
    private List<StatusStatistics> statusStatistics;

    // ==================== 趋势分析 ====================

    @Schema(description = "本月新增补贴", example = "85")
    private Long monthlyNewSubsidies;

    @Schema(description = "本月发放补贴", example = "92")
    private Long monthlyIssuedSubsidies;

    @Schema(description = "本月使用补贴", example = "118")
    private Long monthlyUsedSubsidies;

    @Schema(description = "本月补贴金额", example = "125000.00")
    private BigDecimal monthlySubsidyAmount;

    @Schema(description = "本月使用金额", example = "95000.00")
    private BigDecimal monthlyUsedAmount;

    @Schema(description = "同比增长率", example = "15.5")
    private BigDecimal yearOverYearGrowth;

    @Schema(description = "环比增长率", example = "8.2")
    private BigDecimal monthOverMonthGrowth;

    // ==================== 用户统计 ====================

    @Schema(description = "覆盖用户数", example = "850")
    private Long coveredUserCount;

    @Schema(description = "活跃用户数", example = "720")
    private Long activeUserCount;

    @Schema(description = "人均补贴金额", example = "1470.59")
    private BigDecimal averageSubsidyPerUser;

    @Schema(description = "人均使用金额", example = "1180.56")
    private BigDecimal averageUsagePerUser;

    @Schema(description = "用户使用率", example = "84.7")
    private BigDecimal userUsageRate;

    // ==================== 预警统计 ====================

    @Schema(description = "即将过期补贴（7天内）", example = "28")
    private Long expiringSoonCount;

    @Schema(description = "即将过期补贴金额", example = "28000.00")
    private BigDecimal expiringSoonAmount;

    @Schema(description = "即将用完补贴（使用率>80%）", example = "45")
    private Long nearlyDepletedCount;

    @Schema(description = "即将用完补贴金额", example = "12500.00")
    private BigDecimal nearlyDepletedAmount;

    @Schema(description = "待审核补贴", example = "12")
    private Long pendingApprovalCount;

    @Schema(description = "待审核补贴金额", example = "18000.00")
    private BigDecimal pendingApprovalAmount;

    // ==================== 效率统计 ====================

    @Schema(description = "平均发放时长（小时）", example = "2.5")
    private BigDecimal averageIssuanceTime;

    @Schema(description = "平均使用时长（天）", example = "15.8")
    private BigDecimal averageUsageTime;

    @Schema(description = "补贴周转率", example = "3.2")
    private BigDecimal subsidyTurnoverRate;

    @Schema(description = "资金使用效率", example = "78.5")
    private BigDecimal fundUtilizationEfficiency;

    // ==================== 内部类定义 ====================

    /**
     * 按类型统计
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "补贴类型统计")
    public static class TypeStatistics {
        @Schema(description = "补贴类型", example = "1")
        private Integer subsidyType;

        @Schema(description = "补贴类型名称", example = "餐饮补贴")
        private String subsidyTypeName;

        @Schema(description = "补贴数量", example = "425")
        private Long subsidyCount;

        @Schema(description = "补贴金额", example = "425000.00")
        private BigDecimal subsidyAmount;

        @Schema(description = "使用数量", example = "380")
        private Long usedCount;

        @Schema(description = "使用金额", example = "342000.00")
        private BigDecimal usedAmount;

        @Schema(description = "使用率", example = "76.8")
        private BigDecimal usageRate;

        @Schema(description = "占比", example = "34.0")
        private BigDecimal percentage;
    }

    /**
     * 按周期统计
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "补贴周期统计")
    public static class PeriodStatistics {
        @Schema(description = "补贴周期", example = "4")
        private Integer subsidyPeriod;

        @Schema(description = "补贴周期名称", example = "月度")
        private String subsidyPeriodName;

        @Schema(description = "补贴数量", example = "680")
        private Long subsidyCount;

        @Schema(description = "补贴金额", example = "680000.00")
        private BigDecimal subsidyAmount;

        @Schema(description = "使用数量", example = "520")
        private Long usedCount;

        @Schema(description = "使用金额", example = "468000.00")
        private BigDecimal usedAmount;

        @Schema(description = "使用率", example = "68.8")
        private BigDecimal usageRate;

        @Schema(description = "占比", example = "54.4")
        private BigDecimal percentage;
    }

    /**
     * 按状态统计
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "补贴状态统计")
    public static class StatusStatistics {
        @Schema(description = "补贴状态", example = "2")
        private Integer subsidyStatus;

        @Schema(description = "补贴状态名称", example = "已发放")
        private String subsidyStatusName;

        @Schema(description = "补贴数量", example = "1180")
        private Long subsidyCount;

        @Schema(description = "补贴金额", example = "1180000.00")
        private BigDecimal subsidyAmount;

        @Schema(description = "占比", example = "94.4")
        private BigDecimal percentage;

        @Schema(description = "状态描述", example = "正常使用中")
        private String statusDescription;
    }

    // ==================== 业务计算方法 ====================

    /**
     * 计算整体使用率
     */
    public BigDecimal calculateOverallUsageRate() {
        if (issuedSubsidyAmount == null || issuedSubsidyAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal used = usedSubsidyAmount != null ? usedSubsidyAmount : BigDecimal.ZERO;
        return used.divide(issuedSubsidyAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 计算发放率
     */
    public BigDecimal calculateIssuanceRate() {
        if (totalSubsidyCount == null || totalSubsidyCount == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal issued = new BigDecimal(issuedSubsidyCount != null ? issuedSubsidyCount : 0);
        return issued.divide(new BigDecimal(totalSubsidyCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 计算剩余率
     */
    public BigDecimal calculateRemainingRate() {
        if (issuedSubsidyAmount == null || issuedSubsidyAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal remaining = remainingSubsidyAmount != null ? remainingSubsidyAmount : BigDecimal.ZERO;
        return remaining.divide(issuedSubsidyAmount, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 计算用户使用率
     */
    public BigDecimal calculateUserUsageRate() {
        if (coveredUserCount == null || coveredUserCount == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal active = new BigDecimal(activeUserCount != null ? activeUserCount : 0);
        return active.divide(new BigDecimal(coveredUserCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 获取统计摘要
     */
    public String getStatisticsSummary() {
        return String.format("总补贴：%d份，%.2f元；已发放：%d份，%.2f元；已使用：%d份，%.2f元；使用率：%.2f%%",
                totalSubsidyCount != null ? totalSubsidyCount : 0,
                totalSubsidyAmount != null ? totalSubsidyAmount : BigDecimal.ZERO,
                issuedSubsidyCount != null ? issuedSubsidyCount : 0,
                issuedSubsidyAmount != null ? issuedSubsidyAmount : BigDecimal.ZERO,
                usedSubsidyCount != null ? usedSubsidyCount : 0,
                usedSubsidyAmount != null ? usedSubsidyAmount : BigDecimal.ZERO,
                calculateOverallUsageRate());
    }

    /**
     * 检查统计数据是否完整
     */
    public boolean isComplete() {
        return totalSubsidyCount != null &&
               totalSubsidyAmount != null &&
               issuedSubsidyCount != null &&
               usedSubsidyCount != null &&
               coveredUserCount != null;
    }

    /**
     * 获取健康状态
     */
    public String getHealthStatus() {
        BigDecimal usageRate = calculateOverallUsageRate();
        BigDecimal expirationRate = calculateExpirationRate();

        if (usageRate.compareTo(new BigDecimal("70")) >= 0 && expirationRate.compareTo(new BigDecimal("5")) <= 0) {
            return "优秀";
        } else if (usageRate.compareTo(new BigDecimal("50")) >= 0 && expirationRate.compareTo(new BigDecimal("10")) <= 0) {
            return "良好";
        } else if (usageRate.compareTo(new BigDecimal("30")) >= 0 && expirationRate.compareTo(new BigDecimal("20")) <= 0) {
            return "一般";
        } else {
            return "需改进";
        }
    }

    /**
     * 计算过期率
     */
    private BigDecimal calculateExpirationRate() {
        if (issuedSubsidyCount == null || issuedSubsidyCount == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal expired = new BigDecimal(expiredSubsidyCount != null ? expiredSubsidyCount : 0);
        return expired.divide(new BigDecimal(issuedSubsidyCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}