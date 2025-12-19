package net.lab1024.sa.attendance.leave.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 销假历史记录（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationHistoryRecord {
    private String cancellationId;
    private String status;
    private LocalDateTime time;
    private String remark;
}

