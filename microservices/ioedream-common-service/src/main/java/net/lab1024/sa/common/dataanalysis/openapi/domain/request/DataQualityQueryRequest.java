package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 数据质量查询请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityQueryRequest {
    /** 数据源 */
    private String dataSource;
    /** 表名 */
    private String tableName;
    /** 评估范围 */
    private String assessmentScope;
    /** 评估日期 */
    private String assessmentDate;
    /** 是否包含详情 */
    private Boolean includeDetail;
}
