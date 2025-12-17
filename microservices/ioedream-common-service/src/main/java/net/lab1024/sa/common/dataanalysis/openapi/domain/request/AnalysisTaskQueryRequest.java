package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 分析任务查询请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTaskQueryRequest {
    /** 任务类型 */
    private String taskType;
    /** 状态 */
    private Integer status;
    /** 页码 */
    private Integer pageNum;
    /** 每页大小 */
    private Integer pageSize;
    /** 任务状态 */
    private String taskStatus;
    /** 创建人 */
    private String creator;
}
