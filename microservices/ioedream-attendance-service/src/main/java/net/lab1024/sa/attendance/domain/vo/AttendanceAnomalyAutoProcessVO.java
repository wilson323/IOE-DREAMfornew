package net.lab1024.sa.attendance.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤异常自动处理视图对象
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "考勤异常自动处理视图对象")
public class AttendanceAnomalyAutoProcessVO {

    @Schema(description = "异常ID", example = "1")
    private Long anomalyId;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "部门名称", example = "研发部")
    private String departmentName;

    @Schema(description = "异常类型", example = "LATE")
    private String anomalyType;

    @Schema(description = "异常类型描述", example = "迟到")
    private String anomalyTypeDesc;

    @Schema(description = "考勤日期", example = "2025-01-15")
    private LocalDate attendanceDate;

    @Schema(description = "预期打卡时间", example = "2025-01-15T09:00:00")
    private LocalDateTime expectedPunchTime;

    @Schema(description = "实际打卡时间", example = "2025-01-15T09:15:30")
    private LocalDateTime actualPunchTime;

    @Schema(description = "异常时长（分钟）", example = "15")
    private Integer anomalyDuration;

    @Schema(description = "严重程度", example = "NORMAL")
    private String severityLevel;

    @Schema(description = "严重程度描述", example = "普通")
    private String severityLevelDesc;

    @Schema(description = "分类结果", example = "AUTO_APPROVE")
    private String categoryResult;

    @Schema(description = "分类结果描述", example = "自动批准")
    private String categoryResultDesc;

    @Schema(description = "处理状态", example = "AUTO_PROCESSED")
    private String processStatus;

    @Schema(description = "处理状态描述", example = "已自动处理")
    private String processStatusDesc;

    @Schema(description = "置信度（0-100）", example = "95")
    private Integer confidence;

    @Schema(description = "处理理由", example = "迟到时间少于3分钟，且本月无违规记录")
    private String processReason;

    @Schema(description = "自动处理时间", example = "2025-01-15T10:00:00")
    private LocalDateTime autoProcessTime;

    @Schema(description = "是否被人工覆盖", example = "false")
    private Boolean isOverridden;

    @Schema(description = "覆盖人ID", example = "10")
    private Long overriddenBy;

    @Schema(description = "覆盖人姓名", example = "管理员")
    private String overriddenByName;

    @Schema(description = "覆盖时间", example = "2025-01-15T11:00:00")
    private LocalDateTime overriddenTime;

    @Schema(description = "覆盖原因", example = "特殊情况需要人工审核")
    private String overriddenReason;

    @Schema(description = "创建时间", example = "2025-01-15T09:30:00")
    private LocalDateTime createTime;
}
