package net.lab1024.sa.attendance.engine.optimizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 优化结果
 * <p>
 * 封排班优化的完整结果信息
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
public class OptimizationResult {

    /**
     * 优化ID
     */
    private String optimizationId;

    /**
     * 是否成功优化
     */
    private Boolean optimizationSuccessful;

    /**
     * 优化前指标
     */
    private Map<String, Double> beforeMetrics;

    /**
     * 优化后指标
     */
    private Map<String, Double> afterMetrics;

    /**
     * 优化改进幅度
     */
    private Map<String, Double> improvementRates;

    /**
     * 优化目标达成情况
     */
    private Map<String, GoalAchievement> goalAchievements;

    /**
     * 原始排班记录
     */
    private List<ScheduleRecordSnapshot> originalRecords;

    /**
     * 优化后排班记录
     */
    private List<ScheduleRecordSnapshot> optimizedRecords;

    /**
     * 优化修改记录
     */
    private List<OptimizationModification> modifications;

    /**
     * 优化开始时间
     */
    private LocalDateTime optimizationStartTime;

    /**
     * 优化结束时间
     */
    private LocalDateTime optimizationEndTime;

    /**
     * 优化耗时（毫秒）
     */
    private Long optimizationDuration;

    /**
     * 优化算法版本
     */
    private String algorithmVersion;

    /**
     * 优化参数
     */
    private Map<String, Object> optimizationParameters;

    /**
     * 优化质量评分（0-100，分数越高质量越好）
     */
    private Double optimizationQualityScore;

    /**
     * 整体改进评分（0-100）
     */
    private Double overallImprovementScore;

    /**
     * 优化风险
     */
    private List<String> optimizationRisks;

    /**
     * 优化建议
     */
    private List<String> optimizationSuggestions;

    /**
     * 是否需要人工确认
     */
    private Boolean requiresManualConfirmation;

    /**
     * 优化状态
     */
    private OptimizationStatus status;

    /**
     * 排班记录快照
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleRecordSnapshot {
        private Long recordId;
        private Long employeeId;
        private Long shiftId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String workLocation;
        private String workType;
        private Map<String, Object> attributes;
    }

    /**
     * 优化修改记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimizationModification {
        private String modificationId;
        private String modificationType; // CREATE, UPDATE, DELETE
        private Long recordId;
        private ScheduleRecordSnapshot beforeState;
        private ScheduleRecordSnapshot afterState;
        private String modificationReason;
        private Double improvementContribution; // 该修改对整体改进的贡献度
        private LocalDateTime modificationTime;
    }

    /**
     * 目标达成情况
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalAchievement {
        private String goalName;
        private Double targetValue;
        private Double achievedValue;
        private Double achievementRate; // 达成率
        private Boolean isAchieved; // 是否达成目标
        private String achievementDescription;
    }

    /**
     * 优化状态枚举
     */
    public enum OptimizationStatus {
        PENDING("待优化"),
        IN_PROGRESS("优化中"),
        COMPLETED("已完成"),
        FAILED("优化失败"),
        CANCELLED("已取消"),
        REQUIRES_CONFIRMATION("需确认");

        private final String description;

        OptimizationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}