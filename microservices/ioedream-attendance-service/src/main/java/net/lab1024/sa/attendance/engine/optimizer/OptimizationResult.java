package net.lab1024.sa.attendance.engine.optimizer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.lab1024.sa.attendance.engine.model.Chromosome;

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

    // ==================== 遗传算法特有字段 ====================

    /**
     * 最佳染色体（最优解）
     */
    private Chromosome bestChromosome;

    /**
     * 最佳适应度值
     */
    private Double bestFitness;

    /**
     * 迭代次数
     */
    private Integer iterations;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionDurationMs;

    /**
     * 是否收敛
     */
    private Boolean converged;

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
        private LocalDateTime workStartTime;
        private LocalDateTime workEndTime;
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
    // ==================== 遗传算法优化结果方法 ====================

    /**
     * 创建成功的优化结果（静态工厂方法）
     */
    public static OptimizationResult success(Chromosome bestChromosome, Double bestFitness, int iterations, long executionDurationMs) {
        return OptimizationResult.builder()
                .optimizationSuccessful(true)
                .bestChromosome(bestChromosome)
                .bestFitness(bestFitness)
                .iterations(iterations)
                .executionDurationMs(executionDurationMs)
                .converged(true)
                .status(OptimizationStatus.COMPLETED)
                .optimizationEndTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建失败的优化结果（静态工厂方法）
     */
    public static OptimizationResult failed(String errorMessage) {
        return OptimizationResult.builder()
                .optimizationSuccessful(false)
                .status(OptimizationStatus.FAILED)
                .optimizationEndTime(LocalDateTime.now())
                .optimizationRisks(Arrays.asList(errorMessage))
                .build();
    }

    /**
     * 获取最佳适应度
     */
    public Double getBestFitness() {
        // 优先返回直接设置的最佳适应度
        if (bestFitness != null) {
            return bestFitness;
        }
        // 如果没有直接设置，尝试从指标中获取
        if (afterMetrics == null || !afterMetrics.containsKey("fitness")) {
            return 0.0;
        }
        return afterMetrics.get("fitness");
    }

    /**
     * 获取最佳染色体
     */
    public Chromosome getBestChromosome() {
        // 优先返回直接设置的最佳染色体
        if (bestChromosome != null) {
            return bestChromosome;
        }
        // 如果没有直接设置，尝试从优化记录中提取
        if (optimizedRecords == null || optimizedRecords.isEmpty()) {
            return null;
        }
        // 简化实现：返回第一个优化记录
        Chromosome chromosome = new Chromosome();
        // TODO: 从optimizedRecords构建染色体
        return chromosome;
    }

    /**
     * 获取公平性得分
     */
    public Double getFairnessScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("fairness")) {
            return 0.0;
        }
        return afterMetrics.get("fairness");
    }

    /**
     * 获取成本得分
     */
    public Double getCostScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("cost")) {
            return 0.0;
        }
        return afterMetrics.get("cost");
    }

    /**
     * 获取效率得分
     */
    public Double getEfficiencyScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("efficiency")) {
            return 0.0;
        }
        return afterMetrics.get("efficiency");
    }

    /**
     * 获取满意度得分
     */
    public Double getSatisfactionScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("satisfaction")) {
            return 0.0;
        }
        return afterMetrics.get("satisfaction");
    }

    /**
     * 获取连续工作违规数
     */
    public Integer getConsecutiveWorkViolations() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("consecutiveWorkViolations")) {
            return 0;
        }
        return beforeMetrics.get("consecutiveWorkViolations").intValue();
    }

    /**
     * 获取休息天数违规数
     */
    public Integer getRestDaysViolations() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("restDaysViolations")) {
            return 0;
        }
        return beforeMetrics.get("restDaysViolations").intValue();
    }

    /**
     * 获取每日人员违规数
     */
    public Integer getDailyStaffViolations() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("dailyStaffViolations")) {
            return 0;
        }
        return beforeMetrics.get("dailyStaffViolations").intValue();
    }

    /**
     * 获取总违规数
     */
    public Integer getTotalViolations() {
        return getConsecutiveWorkViolations() +
               getRestDaysViolations() +
               getDailyStaffViolations();
    }

    /**
     * 获取成本效益得分
     */
    public Double getCostEffectivenessScore() {
        if (afterMetrics == null || !afterMetrics.containsKey("costEffectiveness")) {
            return 0.0;
        }
        return afterMetrics.get("costEffectiveness");
    }

    /**
     * 获取结果摘要
     */
    public String getSummary() {
        if (optimizationSuccessful == null || !optimizationSuccessful) {
            return "优化失败";
        }
        return String.format("优化成功: 得分=%.2f, 改进=%.2f%%",
                getBestFitness(),
                overallImprovementScore != null ? overallImprovementScore : 0.0);
    }

    /**
     * 获取加班班次总数
     */
    public Integer getTotalOvertimeShifts() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("overtimeShifts")) {
            return 0;
        }
        return beforeMetrics.get("overtimeShifts").intValue();
    }

    /**
     * 获取加班成本
     */
    public Double getTotalOvertimeCost() {
        if (beforeMetrics == null || !beforeMetrics.containsKey("overtimeCost")) {
            return 0.0;
        }
        return beforeMetrics.get("overtimeCost");
    }

    /**
     * 获取平均工作天数（每位员工）
     */
    public Double getAvgWorkDaysPerEmployee() {
        if (afterMetrics == null || !afterMetrics.containsKey("avgWorkDays")) {
            return 0.0;
        }
        return afterMetrics.get("avgWorkDays");
    }

    /**
     * 获取工作天数标准差
     */
    public Double getWorkDaysStandardDeviation() {
        if (afterMetrics == null || !afterMetrics.containsKey("workDaysStdDev")) {
            return 0.0;
        }
        return afterMetrics.get("workDaysStdDev");
    }

    /**
     * 获取执行耗时（毫秒）
     */
    public Long getExecutionDurationMs() {
        return optimizationDuration != null ? optimizationDuration : 0L;
    }

    /**
     * 获取迭代次数
     */
    public Integer getIterations() {
        return iterations != null ? iterations : 0;
    }

    /**
     * 是否收敛
     */
    public Boolean getConverged() {
        return converged != null ? converged : false;
    }

    // ==================== 质量评估方法 ====================

    /**
     * 获取质量等级（1-5，5为最高质量）
     */
    public Integer getQualityLevel() {
        if (optimizationQualityScore == null) {
            return 0;
        }
        // 将0-100的评分转换为1-5的等级
        if (optimizationQualityScore >= 90) return 5;
        if (optimizationQualityScore >= 70) return 4;
        if (optimizationQualityScore >= 50) return 3;
        if (optimizationQualityScore >= 30) return 2;
        return 1;
    }

    /**
     * 获取质量等级描述
     */
    public String getQualityLevelDescription() {
        Integer level = getQualityLevel();
        switch (level) {
            case 5: return "优秀";
            case 4: return "良好";
            case 3: return "中等";
            case 2: return "较差";
            case 1: return "很差";
            default: return "未知";
        }
    }

    /**
     * 判断是否高质量解（质量等级≥4）
     */
    public Boolean isHighQualitySolution() {
        return getQualityLevel() >= 4;
    }

    /**
     * 判断是否可接受解（质量等级≥3）
     */
    public Boolean isAcceptableSolution() {
        return getQualityLevel() >= 3;
    }

    /**
     * 获取执行耗时（秒）
     */
    public Double getExecutionDurationSeconds() {
        Long durationMs = getExecutionDurationMs();
        return durationMs != null ? durationMs / 1000.0 : 0.0;
    }

    /**
     * 获取执行速度（迭代次数/秒）
     */
    public Double getExecutionSpeed() {
        Double durationSeconds = getExecutionDurationSeconds();
        if (durationSeconds == null || durationSeconds == 0.0) {
            return 0.0;
        }
        return getIterations() / durationSeconds;
    }
}
