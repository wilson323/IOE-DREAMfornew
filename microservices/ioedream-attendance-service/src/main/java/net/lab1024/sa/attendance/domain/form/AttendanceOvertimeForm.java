package net.lab1024.sa.attendance.domain.form;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttendanceOvertimeForm {
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotNull(message = "加班日期不能为空")
    private LocalDate overtimeDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @NotNull(message = "加班时长不能为空")
    private Double overtimeHours;

    @NotBlank(message = "加班原因不能为空")
    @Size(max = 500, message = "加班原因长度不能超过500字符")
    private String reason;

    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}

