package net.lab1024.sa.attendance.leave.model.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 销假导出请求
 *
 * <p>用于导出销假申请数据。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationExportRequest {

    private Long employeeId;
    private Long departmentId;

    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * 导出格式：xlsx/csv 等
     */
    private String exportFormat;
}
