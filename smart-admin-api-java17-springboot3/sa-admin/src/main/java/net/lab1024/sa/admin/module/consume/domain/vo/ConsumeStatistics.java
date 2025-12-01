/*
 * 消费统计VO
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-17
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 消费统计VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/01/17
 */
@Schema(description = "消费统计")
public class ConsumeStatistics {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "人员ID")
    private Long personId;

    @Schema(description = "统计日期")
    private LocalDate statisticsDate;

    @Schema(description = "当日消费次数")
    private Integer dailyCount;

    @Schema(description = "当日消费金额")
    private BigDecimal dailyAmount;

    @Schema(description = "当月消费次数")
    private Integer monthlyCount;

    @Schema(description = "当月消费金额")
    private BigDecimal monthlyAmount;

    @Schema(description = "当周消费次数")
    private Integer weeklyCount;

    @Schema(description = "当周消费金额")
    private BigDecimal weeklyAmount;

    @Schema(description = "当年消费次数")
    private Integer yearlyCount;

    @Schema(description = "当年消费金额")
    private BigDecimal yearlyAmount;

    @Schema(description = "今日消费次数")
    private Integer todayCount;

    @Schema(description = "今日消费金额")
    private BigDecimal todayAmount;

    @Schema(description = "平均消费金额")
    private BigDecimal averageAmount;

    @Schema(description = "统计时间")
    private LocalDateTime statisticsTime;

    // 构造函数
    public ConsumeStatistics() {
        this.statisticsDate = LocalDate.now();
        this.statisticsTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }

    public LocalDate getStatisticsDate() { return statisticsDate; }
    public void setStatisticsDate(LocalDate statisticsDate) { this.statisticsDate = statisticsDate; }

    public Integer getDailyCount() { return dailyCount; }
    public void setDailyCount(Integer dailyCount) { this.dailyCount = dailyCount; }

    public BigDecimal getDailyAmount() { return dailyAmount; }
    public void setDailyAmount(BigDecimal dailyAmount) { this.dailyAmount = dailyAmount; }

    public Integer getMonthlyCount() { return monthlyCount; }
    public void setMonthlyCount(Integer monthlyCount) { this.monthlyCount = monthlyCount; }

    public BigDecimal getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(BigDecimal monthlyAmount) { this.monthlyAmount = monthlyAmount; }

    public Integer getWeeklyCount() { return weeklyCount; }
    public void setWeeklyCount(Integer weeklyCount) { this.weeklyCount = weeklyCount; }

    public BigDecimal getWeeklyAmount() { return weeklyAmount; }
    public void setWeeklyAmount(BigDecimal weeklyAmount) { this.weeklyAmount = weeklyAmount; }

    public Integer getYearlyCount() { return yearlyCount; }
    public void setYearlyCount(Integer yearlyCount) { this.yearlyCount = yearlyCount; }

    public BigDecimal getYearlyAmount() { return yearlyAmount; }
    public void setYearlyAmount(BigDecimal yearlyAmount) { this.yearlyAmount = yearlyAmount; }

    public Integer getTodayCount() { return todayCount; }
    public void setTodayCount(Integer todayCount) { this.todayCount = todayCount; }

    public BigDecimal getTodayAmount() { return todayAmount; }
    public void setTodayAmount(BigDecimal todayAmount) { this.todayAmount = todayAmount; }

    public BigDecimal getAverageAmount() { return averageAmount; }
    public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }

    public LocalDateTime getStatisticsTime() { return statisticsTime; }
    public void setStatisticsTime(LocalDateTime statisticsTime) { this.statisticsTime = statisticsTime; }

    // 业务方法
    /**
     * 获取当日总金额（优先使用todayAmount，其次使用dailyAmount）
     */
    public BigDecimal getTodayTotalAmount() {
        return todayAmount != null ? todayAmount : (dailyAmount != null ? dailyAmount : BigDecimal.ZERO);
    }

    /**
     * 获取当日总次数（优先使用todayCount，其次使用dailyCount）
     */
    public Integer getTodayTotalCount() {
        return todayCount != null ? todayCount : (dailyCount != null ? dailyCount : 0);
    }

    /**
     * 计算平均消费金额
     */
    public BigDecimal calculateAverageAmount() {
        Integer totalCount = getTodayTotalCount();
        BigDecimal totalAmount = getTodayTotalAmount();

        if (totalCount != null && totalAmount != null && totalCount > 0) {
            return totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 是否有消费记录
     */
    public boolean hasConsumption() {
        Integer totalCount = getTodayTotalCount();
        return totalCount != null && totalCount > 0;
    }

    /**
     * 是否超过日限额
     */
    public boolean isOverDailyLimit(BigDecimal dailyLimit) {
        if (dailyLimit == null) {
            return false;
        }
        BigDecimal todayAmount = getTodayTotalAmount();
        return todayAmount != null && todayAmount.compareTo(dailyLimit) > 0;
    }

    /**
     * 创建默认统计对象
     */
    public static ConsumeStatistics createDefault(Long userId, Long personId) {
        ConsumeStatistics statistics = new ConsumeStatistics();
        statistics.setUserId(userId);
        statistics.setPersonId(personId);
        statistics.setDailyCount(0);
        statistics.setDailyAmount(BigDecimal.ZERO);
        statistics.setWeeklyCount(0);
        statistics.setWeeklyAmount(BigDecimal.ZERO);
        statistics.setMonthlyCount(0);
        statistics.setMonthlyAmount(BigDecimal.ZERO);
        statistics.setYearlyCount(0);
        statistics.setYearlyAmount(BigDecimal.ZERO);
        statistics.setTodayCount(0);
        statistics.setTodayAmount(BigDecimal.ZERO);
        statistics.setAverageAmount(BigDecimal.ZERO);
        return statistics;
    }

    @Override
    public String toString() {
        return "ConsumeStatistics{" +
                "userId" + userId +
                ", personId" + personId +
                ", statisticsDate" + statisticsDate +
                ", dailyCount" + dailyCount +
                ", dailyAmount" + dailyAmount +
                ", monthlyCount" + monthlyCount +
                ", monthlyAmount" + monthlyAmount +
                ", weeklyCount" + weeklyCount +
                ", weeklyAmount" + weeklyAmount +
                ", yearlyCount" + yearlyCount +
                ", yearlyAmount" + yearlyAmount +
                ", todayCount" + todayCount +
                ", todayAmount" + todayAmount +
                ", averageAmount" + averageAmount +
                ", statisticsTime" + statisticsTime +
                '}';
    }
}