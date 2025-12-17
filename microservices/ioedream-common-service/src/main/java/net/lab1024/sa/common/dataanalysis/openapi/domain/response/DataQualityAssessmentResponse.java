package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 数据质量评估响应
 */
@Data
public class DataQualityAssessmentResponse {
    private Double qualityScore;
    private List<Map<String, Object>> issues;
    private List<String> suggestions;
}
