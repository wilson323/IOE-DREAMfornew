package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 数据洞察响应
 */
@Data
public class DataInsightResponse {
    private String insightType;
    private List<Map<String, Object>> insights;
    private List<String> recommendations;
}
