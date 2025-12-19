package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 数据导出请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequest {
    /** 导出类型 */
    private String exportType;
    /** 导出格式 */
    private String format;
    /** 任务ID */
    private Long taskId;
    /** 报表类型 */
    private String reportType;
    /** 导出格式（详细） */
    private String exportFormat;
    /** 导出范围 */
    private String exportRange;
}
