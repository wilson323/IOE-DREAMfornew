package net.lab1024.sa.common.audit.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 配置变更统计视图对象
 * <p>
 * 用于展示配置变更的统计信息，包含：
 * - 配置类型统计
 * - 操作用户统计
 * - 影响范围统计
 * - 变更趋势统计
 * - 时间段统计
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@Builder
@Schema(description = "配置变更统计视图对象")
public class ConfigChangeStatisticsVO {

    @Schema(description = "配置类型统计列表")
    private List<Map<String, Object>> configTypeStatistics;

    @Schema(description = "操作用户统计列表")
    private List<Map<String, Object>> operatorStatistics;

    @Schema(description = "影响范围统计列表")
    private List<Map<String, Object>> impactStatistics;

    @Schema(description = "小时级别变更趋势")
    private List<Map<String, Object>> hourlyTrend;

    @Schema(description = "天级别变更趋势")
    private List<Map<String, Object>> dailyTrend;

    @Schema(description = "今日统计信息")
    private Map<String, Object> todayStatistics;

    @Schema(description = "本周统计信息")
    private Map<String, Object> weeklyStatistics;

    @Schema(description = "本月统计信息")
    private Map<String, Object> monthlyStatistics;

    @Schema(description = "总变更数")
    private Long totalChanges;

    @Schema(description = "成功变更数")
    private Long successChanges;

    @Schema(description = "失败变更数")
    private Long failedChanges;

    @Schema(description = "成功率")
    private Double successRate;

    @Schema(description = "高风险变更数")
    private Long highRiskChanges;

    @Schema(description = "敏感配置变更数")
    private Long sensitiveChanges;

    @Schema(description = "待审批变更数")
    private Long pendingApprovals;

    @Schema(description = "平均执行时间（毫秒）")
    private Double avgExecutionTime;

    @Schema(description = "总影响用户数")
    private Long totalAffectedUsers;

    @Schema(description = "总影响设备数")
    private Long totalAffectedDevices;

    @Schema(description = "通知发送成功率")
    private Double notificationSuccessRate;

    // ==================== 业务方法 ====================

    /**
     * 计算成功率
     */
    public Double calculateSuccessRate() {
        if (totalChanges == null || totalChanges == 0) {
            return 0.0;
        }
        long successCount = successChanges != null ? successChanges : 0;
        return (double) successCount / totalChanges * 100;
    }

    /**
     * 计算失败率
     */
    public Double calculateFailureRate() {
        if (totalChanges == null || totalChanges == 0) {
            return 0.0;
        }
        long failedCount = failedChanges != null ? failedChanges : 0;
        return (double) failedCount / totalChanges * 100;
    }

    /**
     * 计算高风险变更率
     */
    public Double calculateHighRiskRate() {
        if (totalChanges == null || totalChanges == 0) {
            return 0.0;
        }
        long highRiskCount = highRiskChanges != null ? highRiskChanges : 0;
        return (double) highRiskCount / totalChanges * 100;
    }

    /**
     * 计算敏感配置变更率
     */
    public Double calculateSensitiveRate() {
        if (totalChanges == null || totalChanges == 0) {
            return 0.0;
        }
        long sensitiveCount = sensitiveChanges != null ? sensitiveChanges : 0;
        return (double) sensitiveCount / totalChanges * 100;
    }

    /**
     * 获取格式化平均执行时间
     */
    public String getFormattedAvgExecutionTime() {
        if (avgExecutionTime == null) {
            return "未知";
        }
        if (avgExecutionTime < 1000) {
            return String.format("%.0fms", avgExecutionTime);
        } else if (avgExecutionTime < 60000) {
            return String.format("%.2fs", avgExecutionTime / 1000);
        } else {
            return String.format("%.2fm", avgExecutionTime / 60000);
        }
    }

    /**
     * 获取成功率显示
     */
    public String getSuccessRateDisplay() {
        double rate = calculateSuccessRate();
        return String.format("%.1f%%", rate);
    }

    /**
     * 获取失败率显示
     */
    public String getFailureRateDisplay() {
        double rate = calculateFailureRate();
        return String.format("%.1f%%", rate);
    }

    /**
     * 获取高风险变更率显示
     */
    public String getHighRiskRateDisplay() {
        double rate = calculateHighRiskRate();
        return String.format("%.1f%%", rate);
    }

    /**
     * 获取今日变更统计摘要
     */
    public Map<String, Object> getTodaySummary() {
        if (todayStatistics == null) {
            return Map.of();
        }

        return Map.of(
                "totalChanges", todayStatistics.get("total_changes"),
                "successChanges", todayStatistics.get("success_count"),
                "failedChanges", todayStatistics.get("failed_count"),
                "highRiskChanges", todayStatistics.get("high_risk_count"),
                "approvalChanges", todayStatistics.get("approval_count")
        );
    }

    /**
     * 获取本周变更统计摘要
     */
    public Map<String, Object> getWeeklySummary() {
        if (weeklyStatistics == null) {
            return Map.of();
        }

        return Map.of(
                "totalChanges", weeklyStatistics.get("total_changes"),
                "successChanges", weeklyStatistics.get("success_count"),
                "failedChanges", weeklyStatistics.get("failed_count"),
                "avgExecutionTime", weeklyStatistics.get("avg_execution_time"),
                "totalAffectedUsers", weeklyStatistics.get("total_affected_users"),
                "totalAffectedDevices", weeklyStatistics.get("total_affected_devices")
        );
    }

    /**
     * 获取本月变更统计摘要
     */
    public Map<String, Object> getMonthlySummary() {
        if (monthlyStatistics == null) {
            return Map.of();
        }

        return Map.of(
                "totalChanges", monthlyStatistics.get("total_changes"),
                "successChanges", monthlyStatistics.get("success_count"),
                "failedChanges", monthlyStatistics.get("failed_count"),
                "highRiskChanges", monthlyStatistics.get("high_risk_count"),
                "sensitiveChanges", monthlyStatistics.get("sensitive_count"),
                "totalAffectedUsers", monthlyStatistics.get("total_affected_users"),
                "totalAffectedDevices", monthlyStatistics.get("total_affected_devices")
        );
    }

    /**
     * 获取配置类型分布数据（用于图表展示）
     */
    public List<Map<String, Object>> getConfigTypeDistribution() {
        if (configTypeStatistics == null) {
            return List.of();
        }

        return configTypeStatistics.stream()
                .map(stat -> Map.of(
                        "name", stat.get("config_type"),
                        "value", stat.get("change_count"),
                        "displayName", getConfigTypeDisplayName((String) stat.get("config_type"))
                ))
                .toList();
    }

    /**
     * 获取操作用户排行数据（用于图表展示）
     */
    public List<Map<String, Object>> getOperatorRanking() {
        if (operatorStatistics == null) {
            return List.of();
        }

        return operatorStatistics.stream()
                .map(stat -> Map.of(
                        "name", stat.get("operator_name"),
                        "value", stat.get("change_count"),
                        "successRate", calculateOperatorSuccessRate(stat),
                        "highRiskCount", stat.get("high_risk_count")
                ))
                .toList();
    }

    /**
     * 获取变更趋势数据（用于图表展示）
     */
    public List<Map<String, Object>> getChangeTrendData() {
        if (dailyTrend == null) {
            return List.of();
        }

        return dailyTrend.stream()
                .map(stat -> Map.of(
                        "date", stat.get("day_bucket"),
                        "total", stat.get("change_count"),
                        "success", stat.get("success_count"),
                        "failed", stat.get("failed_count"),
                        "highRisk", stat.get("high_risk_count")
                ))
                .toList();
    }

    /**
     * 获取影响范围分布数据
     */
    public List<Map<String, Object>> getImpactDistribution() {
        if (impactStatistics == null) {
            return List.of();
        }

        return impactStatistics.stream()
                .map(stat -> Map.of(
                        "name", stat.get("impact_scope"),
                        "value", stat.get("change_count"),
                        "displayName", getImpactScopeDisplayName((String) stat.get("impact_scope")),
                        "totalAffectedUsers", stat.get("total_affected_users"),
                        "totalAffectedDevices", stat.get("total_affected_devices")
                ))
                .toList();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取配置类型显示名称
     */
    private String getConfigTypeDisplayName(String configType) {
        return switch (configType) {
            case "SYSTEM_CONFIG" -> "系统配置";
            case "USER_THEME" -> "用户主题";
            case "USER_PREFERENCE" -> "用户偏好";
            case "I18N_RESOURCE" -> "国际化资源";
            case "THEME_TEMPLATE" -> "主题模板";
            case "WORKFLOW_CONFIG" -> "工作流配置";
            default -> configType;
        };
    }

    /**
     * 获取影响范围显示名称
     */
    private String getImpactScopeDisplayName(String impactScope) {
        return switch (impactScope) {
            case "SINGLE" -> "单个配置";
            case "MODULE" -> "模块级别";
            case "SYSTEM" -> "系统级别";
            case "GLOBAL" -> "全局级别";
            default -> impactScope;
        };
    }

    /**
     * 计算操作用户成功率
     */
    private Double calculateOperatorSuccessRate(Map<String, Object> stat) {
        Object successCountObj = stat.get("success_count");
        Object totalCountObj = stat.get("change_count");

        if (successCountObj == null || totalCountObj == null) {
            return 0.0;
        }

        long successCount = ((Number) successCountObj).longValue();
        long totalCount = ((Number) totalCountObj).longValue();

        if (totalCount == 0) {
            return 0.0;
        }

        return (double) successCount / totalCount * 100;
    }
}