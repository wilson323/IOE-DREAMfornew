package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 排班预测模型
 * <p>
 * 排班预测的结果数据结构
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
public class SchedulePrediction {

    /**
     * 预测ID
     */
    private String predictionId;

    /**
     * 预测类型
     */
    private String predictionType;

    /**
     * 预测时间范围
     */
    private TimeRange timeRange;

    /**
     * 预测精度
     */
    private Double accuracy;

    /**
     * 预测置信度
     */
    private Double confidence;

    /**
     * 预测出勤率（0-1，兼容简化引擎实现字段名）
     */
    private Double predictedAttendanceRate;

    /**
     * 预测加班率（0-1，兼容简化引擎实现字段名）
     */
    private Double predictedOvertimeRate;

    /**
     * 预测成本（兼容简化引擎实现字段名）
     */
    private Double predictedCost;

    /**
     * 预测效率（0-1，兼容简化引擎实现字段名）
     */
    private Double predictedEfficiency;

    /**
     * 预测时间
     */
    private LocalDateTime predictionTime;

    /**
     * 预测结果
     */
    private List<ScheduleRecord> predictedRecords;

    /**
     * 技能需求
     */
    private List<SkillRequirement> skillRequirements;

    /**
     * 人员需求
     */
    private Map<String, Integer> personnelRequirements;

    /**
     * 预测指标
     */
    private Map<String, Object> predictionMetrics;

    /**
     * 是否成功（兼容简化引擎实现）
     */
    private Boolean success;

    /**
     * 预测消息（兼容简化引擎实现）
     */
    private String predictionMessage;

    /**
     * 建议
     */
    private List<String> recommendations;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeRange {
        private String startDate;
        private String endDate;
        private Integer duration;
        private String timeUnit; // DAYS, WEEKS, MONTHS
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillRequirement {
        private String skillCode;
        private String skillName;
        private Integer requiredLevel;
        private Integer requiredCount;
        private String priority;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PredictionScope {
        private String departmentId;
        private List<String> shiftTypes;
        private List<String> skillTypes;
        private Map<String, Object> scopeParameters;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SchedulePredictionResult {
        private String predictionId;
        private PredictionScope scope;
        private Double accuracy;
        private Double confidence;
        private LocalDateTime predictionTime;
        private Map<String, Object> resultData;
        private List<String> insights;
    }
}