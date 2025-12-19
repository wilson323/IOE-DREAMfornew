package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ConsumptionAnalysisReportResponse {
    private BigDecimal totalAmount;
    private Long transactionCount;
    private List<Map<String, Object>> trends;
}
