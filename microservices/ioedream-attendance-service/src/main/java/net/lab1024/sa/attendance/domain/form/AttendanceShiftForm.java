package net.lab1024.sa.attendance.domain.form;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttendanceShiftForm {
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotNull(message = "调班日期不能为空")
    private LocalDate shiftDate;

    @NotNull(message = "原班次ID不能为空")
    private Long originalShiftId;

    @NotNull(message = "目标班次ID不能为空")
    private Long targetShiftId;

    @NotBlank(message = "调班原因不能为空")
    @Size(max = 500, message = "调班原因长度不能超过500字符")
    private String reason;

    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}

