package net.lab1024.sa.attendance.report.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 异常分析报表请求（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyAnalysisReportRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long departmentId;
}

