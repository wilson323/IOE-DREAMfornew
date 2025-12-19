package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分析任务响应
 */
@Data
public class AnalysisTaskResponse {
    private Long taskId;
    private String taskType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
}
