package net.lab1024.sa.attendance.rule.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则计算请求
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCalculationRequest {
    private Long employeeId;
    private Long departmentId;
    private String shiftId;
    private LocalDateTime attendanceTime;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
}

