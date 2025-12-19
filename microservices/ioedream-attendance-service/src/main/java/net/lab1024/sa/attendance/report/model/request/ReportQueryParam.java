package net.lab1024.sa.attendance.report.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.report.model.ReportType;

/**
 * 报表列表查询参数（简化）
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportQueryParam {
    private Integer pageNo;
    private Integer pageSize;
    private ReportType reportType;
    private String keyword;
}

