package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 智能排班结果视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "智能排班结果视图对象")
public class SchedulingResultVO {

    @Schema(description = "排班请求ID", example = "REQ_20250130_001")
    private String requestId;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "部门名称", example = "技术部")
    private String departmentName;

    @Schema(description = "排班期间", example = "2025-01-01~2025-01-31")
    private String period;

    @Schema(description = "算法类型", example = "GENETIC")
    private String algorithmType;

    @Schema(description = "优化评分", example = "92.5")
    private Double optimizationScore;

    @Schema(description = "冲突数量", example = "2")
    private Integer conflictCount;

    @Schema(description = "改进率", example = "15.8")
    private Double improvementRate;

    @Schema(description = "排班状态", example = "COMPLETED")
    private String status;

    @Schema(description = "状态描述", example = "排班完成")
    private String statusDesc;

    @Schema(description = "创建时间", example = "2025-01-01 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "完成时间", example = "2025-01-01 10:05:00")
    private LocalDateTime completedAt;

    @Schema(description = "执行耗时(毫秒)", example = "5000")
    private Long executionTime;

    @Schema(description = "排班记录列表")
    private List<ScheduleRecordVO> scheduleRecords;

    @Schema(description = "统计信息")
    private SchedulingStatisticsVO statistics;

    @Schema(description = "冲突列表")
    private List<String> conflicts;

    @Schema(description = "建议信息")
    private List<String> suggestions;

    @Schema(description = "是否自动应用", example = "false")
    private Boolean autoApplied;

    @Schema(description = "备注", example = "排班方案已优化")
    private String remark;
}
