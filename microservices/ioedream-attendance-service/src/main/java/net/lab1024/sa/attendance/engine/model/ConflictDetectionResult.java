package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 冲突检测结果模型
 * <p>
 * 排班冲突检测的结果数据结构
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
public class ConflictDetectionResult {

    /**
     * 检测结果ID
     */
    private String detectionId;

    /**
     * 是否有冲突
     */
    private Boolean hasConflicts;

    /**
     * 总冲突数
     */
    private Integer totalConflicts;

    /**
     * 技能冲突数
     */
    private Integer skillConflicts;

    /**
     * 工时冲突数
     */
    private Integer workHourConflicts;

    /**
     * 容量冲突数
     */
    private Integer capacityConflicts;

    /**
     * 其他冲突数
     */
    private Integer otherConflicts;

    /**
     * 冲突严重程度分布
     */
    private Map<String, Integer> severityDistribution;

    /**
     * 检测时间
     */
    private LocalDateTime detectionTime;

    /**
     * 检测耗时（毫秒）
     */
    private Long detectionDuration;

    /**
     * 冲突列表
     */
    private List<ScheduleConflict> conflicts;

    /**
     * 建议解决方案
     */
    private List<String> suggestedSolutions;

    /**
     * 冲突统计信息
     */
    private Map<String, Object> statistics;

    // 内部冲突类型
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleConflict {
        private String conflictId;
        private String conflictType;
        private String description;
        private String severity;
        private List<Long> affectedEmployees;
        private String conflictDate;
        private List<String> timeSlots;
        private Map<String, Object> conflictDetails;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillConflict extends ScheduleConflict {
        private String requiredSkill;
        private List<Long> lackingSkillEmployees;
        private Integer skillShortageCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkHourConflict extends ScheduleConflict {
        private Integer exceededHours;
        private Integer maxAllowedHours;
        private String conflictType; // DAILY, WEEKLY, MONTHLY
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacityConflict extends ScheduleConflict {
        private Integer requiredCount;
        private Integer actualCount;
        private Integer shortageCount;
        private String shiftName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtherConflict extends ScheduleConflict {
        private String customConflictType;
        private Map<String, Object> customDetails;
    }
}