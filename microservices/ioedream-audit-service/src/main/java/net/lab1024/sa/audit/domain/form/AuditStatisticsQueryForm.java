package net.lab1024.sa.audit.domain.form;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 审计统计查询表单
 * <p>
 * 用于审计统计分析的查询条件
 * 严格遵循repowiki规范:
 * - 使用@Data注解自动生成getter/setter
 * - 使用@Accessors启用链式调用
 * - 包含完整的统计查询条件
 * - 支持多种统计维度和过滤条件
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-11-29
 */
@Data
@Accessors(chain = true)
public class AuditStatisticsQueryForm {

    /**
     * 用户ID（统计特定用户的操作）
     */
    private Long userId;

    /**
     * 模块名称（统计特定模块的操作）
     */
    private String moduleName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 操作类型过滤
     */
    private Integer operationType;

    /**
     * 结果状态过滤
     */
    private Integer resultStatus;

    /**
     * 风险等级过滤
     */
    private Integer riskLevel;

    /**
     * 是否包含分组统计
     */
    private Boolean includeGroupBy;

    /**
     * 统计类型：daily-按天, hourly-按小时, user-按用户, module-按模块
     */
    private String statisticsType;

    /**
     * 是否包含趋势分析
     */
    private Boolean includeTrend;

    /**
     * 是否包含对比分析（与上一周期对比）
     */
    private Boolean includeComparison;

    /**
     * 对比周期类型：week-周对比, month-月对比, year-年对比
     */
    private String comparisonType;

    /**
     * Top数量限制（用于用户活跃度、模块使用量等Top统计）
     */
    private Integer topLimit = 10;

    /**
     * 设置默认统计时间范围（最近7天）
     */
    public void setDefaultTimeRange() {
        if (startTime == null && endTime == null) {
            endTime = LocalDateTime.now();
            startTime = endTime.minusDays(7);
        }
    }

    /**
     * 设置今日统计范围
     */
    public void setTodayRange() {
        LocalDateTime now = LocalDateTime.now();
        startTime = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endTime = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * 设置本周统计范围
     */
    public void setThisWeekRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monday = now.minusDays(now.getDayOfWeek().getValue() - 1);
        startTime = monday.withHour(0).withMinute(0).withSecond(0).withNano(0);
        endTime = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * 设置本月统计范围
     */
    public void setThisMonthRange() {
        LocalDateTime now = LocalDateTime.now();
        startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        endTime = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * 设置上个周期时间范围（用于对比分析）
     */
    public void setPreviousPeriodRange() {
        if (startTime == null || endTime == null) {
            setDefaultTimeRange();
        }

        long days = java.time.Duration.between(startTime, endTime).toDays();

        if ("week".equals(comparisonType)) {
            // 周对比：向前推7天
            endTime = startTime.minusDays(1);
            startTime = endTime.minusDays(days);
        } else if ("month".equals(comparisonType)) {
            // 月对比：向前推一个月
            endTime = startTime.minusDays(1);
            startTime = endTime.minusDays(days);
        } else if ("year".equals(comparisonType)) {
            // 年对比：向前推一年
            endTime = startTime.minusDays(1);
            startTime = endTime.minusDays(days);
        }
    }

    /**
     * 验证查询参数
     */
    public boolean isValid() {
        // 验证时间范围
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            return false;
        }

        // 验证操作类型
        if (operationType != null && (operationType < 1 || operationType > 10)) {
            return false;
        }

        // 验证结果状态
        if (resultStatus != null && (resultStatus < 1 || resultStatus > 3)) {
            return false;
        }

        // 验证风险等级
        if (riskLevel != null && (riskLevel < 1 || riskLevel > 3)) {
            return false;
        }

        // 验证Top数量
        if (topLimit != null && (topLimit < 1 || topLimit > 100)) {
            return false;
        }

        return true;
    }

    /**
     * 是否为用户级统计
     */
    public boolean isUserLevelStatistics() {
        return userId != null;
    }

    /**
     * 是否为模块级统计
     */
    public boolean isModuleLevelStatistics() {
        return moduleName != null && !moduleName.trim().isEmpty();
    }

    /**
     * 是否需要按天统计
     */
    public boolean needDailyStatistics() {
        return "daily".equals(statisticsType) ||
               (statisticsType == null && !isUserLevelStatistics() && !isModuleLevelStatistics());
    }

    /**
     * 是否需要按小时统计
     */
    public boolean needHourlyStatistics() {
        return "hourly".equals(statisticsType);
    }

    /**
     * 是否需要按用户统计
     */
    public boolean needUserStatistics() {
        return "user".equals(statisticsType);
    }

    /**
     * 是否需要按模块统计
     */
    public boolean needModuleStatistics() {
        return "module".equals(statisticsType);
    }

    /**
     * 获取统计周期描述
     */
    public String getStatisticsPeriodDescription() {
        if (startTime == null || endTime == null) {
            return "未指定时间范围";
        }

        long days = java.time.Duration.between(startTime, endTime).toDays();

        if (days == 0) {
            return "当日";
        } else if (days == 1) {
            return "近2天";
        } else if (days <= 7) {
            return "近" + (days + 1) + "天";
        } else if (days <= 30) {
            return "近" + ((days / 7) + 1) + "周";
        } else if (days <= 365) {
            return "近" + ((days / 30) + 1) + "个月";
        } else {
            return "近" + ((days / 365) + 1) + "年";
        }
    }

    /**
     * 获取查询条件摘要
     */
    public String getQuerySummary() {
        StringBuilder summary = new StringBuilder();

        if (userId != null) {
            summary.append("用户ID: ").append(userId).append(", ");
        }

        if (moduleName != null && !moduleName.trim().isEmpty()) {
            summary.append("模块: ").append(moduleName).append(", ");
        }

        if (operationType != null) {
            summary.append("操作类型: ").append(operationType).append(", ");
        }

        if (resultStatus != null) {
            summary.append("结果状态: ").append(resultStatus).append(", ");
        }

        if (startTime != null || endTime != null) {
            summary.append("时间范围: ").append(startTime).append(" ~ ").append(endTime).append(", ");
        }

        if (statisticsType != null) {
            summary.append("统计类型: ").append(statisticsType).append(", ");
        }

        if (topLimit != null && topLimit > 0) {
            summary.append("Top数量: ").append(topLimit).append(", ");
        }

        // 移除最后的逗号和空格
        if (summary.length() > 0) {
            summary.setLength(summary.length() - 2);
        }

        return summary.toString();
    }
}