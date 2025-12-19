package net.lab1024.sa.attendance.report.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工考勤明细
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAttendanceDetail {

    private LocalDate workDate;

    private String status;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private Double workHours;

    private String anomalyType;
}

