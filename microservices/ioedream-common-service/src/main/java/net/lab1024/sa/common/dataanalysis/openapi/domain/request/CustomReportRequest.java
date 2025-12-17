package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 自定义报表请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomReportRequest {
    /** 报表名称 */
    private String reportName;
    /** 数据源 */
    private String dataSource;
    /** 维度列表 */
    private List<String> dimensions;
    /** 指标列表 */
    private List<String> metrics;
    /** 度量列表 */
    private List<String> measures;
}
