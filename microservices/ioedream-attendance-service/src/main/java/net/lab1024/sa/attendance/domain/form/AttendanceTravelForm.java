package net.lab1024.sa.attendance.domain.form;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttendanceTravelForm {
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;

    @NotBlank(message = "目的地不能为空")
    @Size(max = 200, message = "目的地长度不能超过200字符")
    private String destination;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @NotNull(message = "出差天数不能为空")
    private Integer travelDays;

    private BigDecimal estimatedCost;

    @NotBlank(message = "出差原因不能为空")
    @Size(max = 500, message = "出差原因长度不能超过500字符")
    private String reason;

    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}



