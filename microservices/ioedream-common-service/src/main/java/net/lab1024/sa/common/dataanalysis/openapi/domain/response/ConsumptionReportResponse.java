package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 消费报表响应
 */
@Data
public class ConsumptionReportResponse {
    private BigDecimal totalAmount;
    private Long totalTransactions;
    private List<Map<String, Object>> details;
}
