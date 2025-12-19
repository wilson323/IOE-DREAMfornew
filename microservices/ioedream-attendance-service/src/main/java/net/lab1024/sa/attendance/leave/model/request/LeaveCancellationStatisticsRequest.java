package net.lab1024.sa.attendance.leave.model.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销假统计请求
 *
 * <p>用于请求销假统计数据。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationStatisticsRequest {

    private Long employeeId;
    private Long departmentId;

    private LocalDate startDate;
    private LocalDate endDate;
}
