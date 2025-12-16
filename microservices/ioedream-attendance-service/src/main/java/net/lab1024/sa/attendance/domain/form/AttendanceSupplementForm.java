package net.lab1024.sa.attendance.domain.form;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttendanceSupplementForm {
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotNull(message = "补签日期不能为空")
    private LocalDate supplementDate;

    @NotNull(message = "打卡时间不能为空")
    private LocalTime punchTime;

    @NotBlank(message = "打卡类型不能为空")
    private String punchType;

    @NotBlank(message = "补签原因不能为空")
    @Size(max = 500, message = "补签原因长度不能超过500字符")
    private String reason;

    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}



