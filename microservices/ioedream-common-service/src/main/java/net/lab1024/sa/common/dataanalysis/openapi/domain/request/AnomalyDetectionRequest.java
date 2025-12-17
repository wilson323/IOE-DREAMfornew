package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionRequest {
    private String dataSource;
    private String analysisType;
    private String detectionType;
    private String sensitivity;
    private Double threshold;
}
