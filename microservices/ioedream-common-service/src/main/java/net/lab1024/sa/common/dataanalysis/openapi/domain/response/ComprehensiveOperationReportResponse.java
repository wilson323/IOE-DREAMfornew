package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.Map;

/**
 * 综合运营报表响应
 */
@Data
public class ComprehensiveOperationReportResponse {
    private String reportType;
    private Map<String, Object> data;
    private Map<String, Object> comparison;
}
