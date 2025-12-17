package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据洞察请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataInsightRequest {
    /** 洞察类型 */
    private String insightType;
    /** 数据源 */
    private String dataSource;
    /** 分析深度 */
    private String analysisDepth;
    /** 目标ID */
    private Long targetId;
    /** 天数 */
    private Integer days;
}
