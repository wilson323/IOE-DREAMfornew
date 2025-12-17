package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionAnalysisRequest {
    private String predictionType;
    private String targetField;
    private String predictionPeriod;
    private Long targetId;
    private Integer forecastDays;
}
