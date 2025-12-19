package net.lab1024.sa.attendance.leave.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.leave.model.LeaveCancellationApplication;

/**
 * 销假申请请求
 *
 * <p>用于发起销假申请的请求参数。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationApplicationRequest {

    private String originalLeaveId;

    private Long employeeId;
    private String employeeName;
    private String employeeCode;

    private Long departmentId;
    private String departmentName;

    private LeaveCancellationApplication.LeaveType originalLeaveType;
    private LocalDate originalLeaveStartDate;
    private LocalDate originalLeaveEndDate;
    private BigDecimal originalLeaveDays;
    private String originalLeaveReason;

    private LeaveCancellationApplication.CancellationType cancellationType;
    private LocalDate cancellationStartDate;
    private LocalDate cancellationEndDate;
    private String cancellationReason;
    private String cancellationReasonDetail;

    private Long applicantId;
    private String applicantName;

    private List<Long> approverIds;
    private List<String> approverNames;

    private LeaveCancellationApplication.UrgencyLevel urgencyLevel;
}
