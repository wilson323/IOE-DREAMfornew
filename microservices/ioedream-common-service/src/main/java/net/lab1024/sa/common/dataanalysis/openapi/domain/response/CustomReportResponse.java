package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 自定义报表响应
 */
@Data
public class CustomReportResponse {
    private String reportName;
    private List<Map<String, Object>> data;
}
