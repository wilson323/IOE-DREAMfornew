package net.lab1024.sa.attendance.leave.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.leave.model.LeaveCancellationApplication;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 销假申请列表结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationListResult {
    private boolean success;
    private List<LeaveCancellationApplication> data;
    private int total;
    private int pageNum;
    private int pageSize;
    private LocalDateTime queryTime;
    private String errorMessage;
    private String errorCode;
}

