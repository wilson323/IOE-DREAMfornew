package net.lab1024.sa.attendance.engine.conflict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量冲突解决结果
 * <p>
 * 封装批量冲突解决的结果信息
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
public class BatchResolutionResult {

    /**
     * 批量解决ID
     */
    private String batchResolutionId;

    /**
     * 是否全部解决成功
     */
    private Boolean allResolved;

    /**
     * 成功解决的数量
     */
    private Integer successCount;

    /**
     * 失败的数量
     */
    private Integer failureCount;

    /**
     * 总冲突数量
     */
    private Integer totalCount;

    /**
     * 检测次数（用于兼容实现层字段名）
     */
    private Integer totalDetections;

    /**
     * 成功解决数量（用于兼容实现层字段名）
     */
    private Integer successfulResolutions;

    /**
     * 总冲突数量（用于兼容实现层字段名）
     */
    private Integer totalConflicts;

    /**
     * 成功率（0-1）
     */
    private Double successRate;

    /**
     * 解决结果列表
     */
    private List<ConflictResolutionResult> resolutionResults;

    /**
     * 解决开始时间
     */
    private LocalDateTime resolutionStartTime;

    /**
     * 解决结束时间
     */
    private LocalDateTime resolutionEndTime;

    /**
     * 解决耗时（毫秒）
     */
    private Long resolutionDuration;

    /**
     * 解决统计信息
     */
    private ResolutionStatistics statistics;
}
