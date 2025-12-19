package net.lab1024.sa.attendance.report.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.ReportType;

/**
 * 报表配置推荐请求（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportConfigurationRecommendationRequest {
    private ReportType reportType;
    private Object context;
}

