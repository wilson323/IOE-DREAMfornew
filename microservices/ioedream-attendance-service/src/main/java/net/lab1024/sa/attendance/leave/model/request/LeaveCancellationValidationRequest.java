package net.lab1024.sa.attendance.leave.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销假校验请求
 *
 * <p>用于在提交前校验销假申请合法性。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationValidationRequest {

    private Long employeeId;
    private String originalLeaveId;

    private String validationScene;
}
