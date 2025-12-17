package net.lab1024.sa.common.dataanalysis.openapi.domain.response;

import lombok.Data;

/**
 * 导出任务响应
 */
@Data
public class ExportTaskResponse {
    private Long taskId;
    private Integer status;
    private String downloadUrl;
}
