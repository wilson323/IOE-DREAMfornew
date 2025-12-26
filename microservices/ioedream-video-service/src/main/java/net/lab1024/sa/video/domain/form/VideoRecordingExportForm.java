package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "录像导出表单")
public class VideoRecordingExportForm {

    @Schema(description = "导出类型: 1-任务列表 2-统计报表 3-存储分析", required = true)
    @NotNull(message = "导出类型不能为空")
    private Integer exportType;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "报表格式: 1-Excel 2-CSV", required = false)
    private Integer format = 1;

    @Schema(description = "包含字段（JSON数组）")
    private String includeFields;
}
