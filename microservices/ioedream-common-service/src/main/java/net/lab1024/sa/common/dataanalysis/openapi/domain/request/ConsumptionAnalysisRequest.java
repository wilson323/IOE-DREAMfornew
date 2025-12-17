package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 消费分析查询请求
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionAnalysisRequest {
    private String startDate;
    private String endDate;
    private Long areaId;
    private String consumeType;
}
