package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AnomalyDetectionResponse {
    private Integer anomalyCount;
    private List<Map<String, Object>> anomalies;
}
