package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

/**
 * 考勤异常自动处理查询表单
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "考勤异常自动处理查询表单")
public class AttendanceAnomalyAutoProcessQueryForm {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Schema(description = "每页大小", example = "20")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名（模糊查询）", example = "张三")
    private String username;

    @Schema(description = "异常类型", example = "LATE")
    private String anomalyType;

    @Schema(description = "分类结果", example = "AUTO_APPROVE")
    private String categoryResult;

    @Schema(description = "处理状态", example = "AUTO_PROCESSED")
    private String processStatus;

    @Schema(description = "开始日期", example = "2025-01-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2025-01-31")
    private LocalDate endDate;

    @Schema(description = "严重程度", example = "NORMAL")
    private String severityLevel;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;
}
