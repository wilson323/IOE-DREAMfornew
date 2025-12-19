package net.lab1024.sa.attendance.leave.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.leave.model.LeaveCancellationApplication;

import java.time.LocalDateTime;

/**
 * 销假申请详情
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationDetail {
    private String cancellationId;
    private boolean exists;
    private LeaveCancellationApplication application;
    private LocalDateTime queryTime;
    private String errorMessage;
}

