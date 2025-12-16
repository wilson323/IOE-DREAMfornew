package net.lab1024.sa.attendance.domain.form;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 考勤请假表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AttendanceLeaveForm {

    /**
     * 员工ID
     */
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    /**
     * 请假类型
     */
    @NotBlank(message = "请假类型不能为空")
    private String leaveType;

    /**
     * 请假开始日期
     */
    @NotNull(message = "请假开始日期不能为空")
    private LocalDate startDate;

    /**
     * 请假结束日期
     */
    @NotNull(message = "请假结束日期不能为空")
    private LocalDate endDate;

    /**
     * 请假天数
     */
    @NotNull(message = "请假天数不能为空")
    private Double leaveDays;

    /**
     * 请假原因
     */
    @NotBlank(message = "请假原因不能为空")
    @Size(max = 500, message = "请假原因长度不能超过500字符")
    private String reason;

    /**
     * 备注
     */
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}



