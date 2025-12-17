package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SecurityRiskReportResponse {
    private Integer riskLevel;
    private List<Map<String, Object>> risks;
    private List<String> recommendations;
}
