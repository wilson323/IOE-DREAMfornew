package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.engine.model.ScheduleRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 冲突解决结果
 * <p>
 * 封装冲突解决的完整结果信息
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
public class ConflictResolutionResult {

    /**
     * 解决ID
     */
    private String resolutionId;

    /**
     * 冲突ID
     */
    private String conflictId;

    /**
     * 冲突类型
     */
    private String conflictType;

    /**
     * 是否成功解决
     */
    private Boolean resolutionSuccessful;

    /**
     * 解决策略
     */
    private ResolutionStrategy resolutionStrategy;

    /**
     * 原始排班记录
     */
    private List<ScheduleRecord> originalRecords;

    /**
     * 解决后的排班记录
     */
    private List<ScheduleRecord> resolvedRecords;

    /**
     * 修改的记录列表
     */
    private List<ScheduleRecordModification> modifications;

    /**
     * 解决时间
     */
    private LocalDateTime resolutionTime;

    /**
     * 解决耗时（毫秒）
     */
    private Long resolutionDuration;

    /**
     * 解决算法版本
     */
    private String algorithmVersion;

    /**
     * 解决参数
     */
    private Map<String, Object> resolutionParameters;

    /**
     * 解决描述
     */
    private String resolutionDescription;

    /**
     * 影响的员工数量
     */
    private Integer affectedEmployees;

    /**
     * 影响的班次数量
     */
    private Integer affectedShifts;

    /**
     * 解决质量评分（0-100，分数越高质量越好）
     */
    private Double resolutionQualityScore;

    /**
     * 是否需要人工确认
     */
    private Boolean requiresManualConfirmation;

    /**
     * 潜在风险
     */
    private List<String> potentialRisks;

    /**
     * 替代解决方案
     */
    private List<AlternativeSolution> alternativeSolutions;

    /**
     * 排班记录修改
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleRecordModification {
        private Long recordId;
        private String modificationType; // CREATE, UPDATE, DELETE
        private ScheduleRecord originalRecord;
        private ScheduleRecord modifiedRecord;
        private String modificationReason;
        private LocalDateTime modificationTime;
    }

    /**
     * 替代解决方案
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeSolution {
        private String solutionId;
        private String solutionDescription;
        private List<ScheduleRecord> proposedRecords;
        private Double solutionQualityScore;
        private Map<String, Object> solutionParameters;
    }
}
