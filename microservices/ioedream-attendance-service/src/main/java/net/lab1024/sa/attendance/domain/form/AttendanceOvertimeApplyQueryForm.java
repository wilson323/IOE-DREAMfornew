package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 加班申请查询表单
 * <p>
 * 用于查询加班申请列表
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "加班申请查询表单")
public class AttendanceOvertimeApplyQueryForm extends net.lab1024.sa.common.domain.PageParam {

    @Schema(description = "申请人ID")
    private Long applicantId;

    @Schema(description = "部门ID")
    private Long departmentId;

    @Schema(description = "加班类型", allowableValues = {"WORKDAY", "OVERTIME", "HOLIDAY"})
    private String overtimeType;

    @Schema(description = "申请状态", allowableValues = {"DRAFT", "PENDING", "APPROVED", "REJECTED", "CANCELLED"})
    private String applyStatus;

    @Schema(description = "开始日期（加班日期范围查询）")
    private LocalDate startDate;

    @Schema(description = "结束日期（加班日期范围查询）")
    private LocalDate endDate;

    @Schema(description = "关键词（申请人姓名、申请编号）")
    private String keyword;
}
