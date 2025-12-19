package net.lab1024.sa.attendance.report.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.ReportType;

import java.time.LocalDate;
import java.util.List;

/**
 * 批量报表生成请求（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchReportGenerationRequest {
    private List<ReportType> reportTypes;
    private LocalDate startDate;
    private LocalDate endDate;
}

