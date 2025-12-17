package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityRiskQueryRequest {
    private Long areaId;
    private String riskType;
    private String riskLevel;
    private String startDate;
    private String endDate;
    private Integer days;
}
