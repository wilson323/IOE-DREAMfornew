package net.lab1024.sa.attendance.realtime.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 实时计算结果
 * <p>
 * 封装实时计算的完整结果信息
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeCalculationResult {

    /**
     * 计算ID
     */
    private String calculationId;

    /**
     * 关联的事件ID
     */
    private String eventId;

    /**
     * 计算时间
     */
    private LocalDateTime calculationTime;

    /**
     * 计算类型
     */
    private CalculationType calculationType;

    /**
     * 计算是否成功
     */
    private Boolean calculationSuccessful;

    /**
     * 计算耗时（毫秒）
     */
    private Long calculationDuration;

    /**
     * 计算结果数据
     */
    private Map<String, Object> resultData;

    /**
     * 统计数据
     */
    private Map<String, Object> statisticsData;

    /**
     * 异常数据
     */
    private List<AnomalyData> anomalies;

    /**
     * 预警信息
     */
    private List<AlertData> alerts;

    /**
     * 计算参数
     */
    private Map<String, Object> calculationParameters;

    /**
     * 计算规则应用情况
     */
    private List<AppliedRule> appliedRules;

    /**
     * 性能指标
     */
    private PerformanceMetrics performanceMetrics;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 计算引擎版本
     */
    private String engineVersion;

    /**
     * 缓存命中情况
     */
    private CacheHitInfo cacheHitInfo;

    /**
     * 计算类型枚举
     */
    public enum CalculationType {
        EMPLOYEE_DAILY("员工日统计"),
        EMPLOYEE_MONTHLY("员工月统计"),
        DEPARTMENT_DAILY("部门日统计"),
        DEPARTMENT_MONTHLY("部门月统计"),
        COMPANY_DAILY("公司日统计"),
        COMPANY_MONTHLY("公司月统计"),
        ANOMALY_DETECTION("异常检测"),
        ALERT_CHECKING("预警检查"),
        SCHEDULE_INTEGRATION("排班集成"),
        PERFORMANCE_ANALYSIS("性能分析"),
        REAL_TIME_MONITORING("实时监控");

        private final String description;

        CalculationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 异常数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyData {
        private String anomalyId;
        private String anomalyType;
        private String anomalyDescription;
        private String employeeId;
        private String departmentId;
        private LocalDateTime detectionTime;
        private Double anomalyScore;
        private String severity;
        private Map<String, Object> anomalyDetails;
    }

    /**
     * 预警数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertData {
        private String alertId;
        private String alertType;
        private String alertTitle;
        private String alertDescription;
        private String employeeId;
        private String departmentId;
        private LocalDateTime alertTime;
        private String alertLevel;
        private Boolean requiresAction;
        private String recommendedAction;
        private Map<String, Object> alertDetails;
    }

    /**
     * 应用的规则
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedRule {
        private String ruleId;
        private String ruleName;
        private String ruleType;
        private Boolean ruleResult;
        private String ruleMessage;
        private LocalDateTime applicationTime;
        private Map<String, Object> ruleContext;
    }

    /**
     * 性能指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private Long eventCount;
        private Double averageProcessingTime;
        private Double throughputPerSecond;
        private Long memoryUsage;
        private Double cpuUsage;
        private Integer activeThreads;
        private Integer queueSize;
        private Double cacheHitRate;
    }

    /**
     * 缓存命中信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheHitInfo {
        private Integer totalRequests;
        private Integer cacheHits;
        private Integer cacheMisses;
        private Double hitRate;
        private Double missRate;
        private Long averageCacheAccessTime;
    }
}