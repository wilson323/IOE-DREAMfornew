package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class IntelligentRecommendationResponse {
    private List<Map<String, Object>> recommendations;
    private Double relevanceScore;
}
