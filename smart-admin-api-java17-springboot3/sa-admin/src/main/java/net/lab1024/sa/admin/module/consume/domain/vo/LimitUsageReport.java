package net.lab1024.sa.admin.module.consume.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 限制使用报告VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "限制使用报告")
public class LimitUsageReport {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "人员ID")
    private Long personId;

    @Schema(description = "报告日期")
    private LocalDate reportDate;

    @Schema(description = "单次限额")
    private BigDecimal singleLimit;

    @Schema(description = "日限额")
    private BigDecimal dailyLimit;

    @Schema(description = "周限额")
    private BigDecimal weeklyLimit;

    @Schema(description = "月限额")
    private BigDecimal monthlyLimit;

    @Schema(description = "年限额")
    private BigDecimal yearlyLimit;

    @Schema(description = "已用日限额")
    private BigDecimal dailyUsed;

    @Schema(description = "已用周限额")
    private BigDecimal weeklyUsed;

    @Schema(description = "已用月限额")
    private BigDecimal monthlyUsed;

    @Schema(description = "已用年限额")
    private BigDecimal yearlyUsed;

    @Schema(description = "日限额使用率")
    private BigDecimal dailyUsageRate;

    @Schema(description = "周限额使用率")
    private BigDecimal weeklyUsageRate;

    @Schema(description = "月限额使用率")
    private BigDecimal monthlyUsageRate;

    @Schema(description = "年限额使用率")
    private BigDecimal yearlyUsageRate;

    @Schema(description = "每日消费次数")
    private Integer dailyCount;

    @Schema(description = "每周消费次数")
    private Integer weeklyCount;

    @Schema(description = "每月消费次数")
    private Integer monthlyCount;

    @Schema(description = "每年消费次数")
    private Integer yearlyCount;

    @Schema(description = "消费次数")
    private Integer consumeCount;

    @Schema(description = "报告生成时间")
    private LocalDateTime reportTime;

    @Schema(description = "消费趋势")
    private List<ConsumptionTrend> consumptionTrend;

    @Schema(description = "预警信息")
    private List<String> warnings;

    /**
     * 消费趋势
     */
    
    
    
    
    @Schema(description = "消费趋势")
    public static class ConsumptionTrend {

        @Schema(description = "日期")
        private LocalDate date;

        @Schema(description = "消费金额")
        private BigDecimal amount;

        @Schema(description = "消费次数")
        private Integer count;

        @Schema(description = "平均消费金额")
        private BigDecimal averageAmount;

        // 手动添加的getter/setter方法 (Lombok失效备用)







    }

    /**
     * 计算使用率
     */
    public BigDecimal calculateUsageRate(String period) {
        BigDecimal used = null;
        BigDecimal limit = null;

        switch (period.toLowerCase()) {
            case "daily":
                used = dailyUsed;
                limit = dailyLimit;
                break;
            case "weekly":
                used = weeklyUsed;
                limit = weeklyLimit;
                break;
            case "monthly":
                used = monthlyUsed;
                limit = monthlyLimit;
                break;
            case "yearly":
                used = yearlyUsed;
                limit = yearlyLimit;
                break;
        }

        if (used == null || limit == null || limit.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        return used.divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
    }

    /**
     * 检查是否有预警
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * 检查是否接近限额
     */
    public boolean isNearLimit(String period, BigDecimal threshold) {
        BigDecimal usageRate = calculateUsageRate(period);
        return usageRate.compareTo(threshold) >= 0;
    }

    /**
     * 检查是否超过限额
     */
    public boolean isOverLimit(String period) {
        return isNearLimit(period, new BigDecimal("100"));
    }

    /**
     * 获取风险等级
     */

    /**
     * 创建默认报告
     */
    public static LimitUsageReport createDefault(Long userId, Long personId) {
        return LimitUsageReport.builder()
                .userId(userId)
                .personId(personId)
                .reportDate(LocalDate.now())
                .dailyUsed(BigDecimal.ZERO)
                .weeklyUsed(BigDecimal.ZERO)
                .monthlyUsed(BigDecimal.ZERO)
                .yearlyUsed(BigDecimal.ZERO)
                .dailyCount(0)
                .weeklyCount(0)
                .monthlyCount(0)
                .yearlyCount(0)
                .consumeCount(0)
                .reportTime(LocalDateTime.now())
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)















































}
