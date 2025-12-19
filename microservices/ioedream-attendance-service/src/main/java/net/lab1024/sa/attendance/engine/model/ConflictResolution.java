package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 冲突解决结果模型
 * <p>
 * 排班冲突解决的结果数据结构
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
public class ConflictResolution {

    /**
     * 解决方案ID
     */
    private String resolutionId;

    /**
     * 解决状态
     */
    private String status;

    /**
     * 解决策略
     */
    private String resolutionStrategy;

    /**
     * 是否成功（兼容简化引擎实现）
     */
    private Boolean success;

    /**
     * 解决消息（兼容简化引擎实现）
     */
    private String resolutionMessage;

    /**
     * 已解决数量（兼容简化引擎实现）
     */
    private Integer resolvedCount;

    /**
     * 剩余冲突列表（兼容简化引擎实现）
     */
    private List<Object> remainingConflicts;

    /**
     * 原始冲突数
     */
    private Integer originalConflictCount;

    /**
     * 已解决冲突数
     */
    private Integer resolvedConflictCount;

    /**
     * 未解决冲突数
     */
    private Integer unresolvedConflictCount;

    /**
     * 解决成功率
     */
    private Double resolutionRate;

    /**
     * 解决时间
     */
    private LocalDateTime resolutionTime;

    /**
     * 解决耗时（毫秒）
     */
    private Long resolutionDuration;

    /**
     * 解决方案详情
     */
    private Map<String, Object> resolutionDetails;

    /**
     * 修改的排班记录
     */
    private List<ScheduleRecordModification> modifiedRecords;

    /**
     * 替代解决方案
     */
    private List<AlternativeSolution> alternativeSolutions;

    /**
     * 统计信息
     */
    private Map<String, Object> statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleRecordModification {
        private Long recordId;
        private ScheduleRecord originalRecord;
        private ScheduleRecord modifiedRecord;
        private String modificationType;
        private String modificationReason;
        private LocalDateTime modificationTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeSolution {
        private String solutionId;
        private String description;
        private String solutionType;
        private Double effectiveness;
        private List<ScheduleRecord> suggestedRecords;
        private Map<String, Object> solutionParameters;
    }
}