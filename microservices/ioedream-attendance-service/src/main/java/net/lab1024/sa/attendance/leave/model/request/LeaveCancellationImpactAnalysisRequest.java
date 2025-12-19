package net.lab1024.sa.attendance.leave.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销假影响分析请求
 *
 * <p>用于评估销假对考勤/排班/薪资等影响。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationImpactAnalysisRequest {

    private String cancellationId;
    private Long employeeId;
    private String originalLeaveId;
}
