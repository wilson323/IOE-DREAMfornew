package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PredictionAnalysisResponse {
    private String predictionType;
    private List<Map<String, Object>> predictions;
    private Double confidence;
}
