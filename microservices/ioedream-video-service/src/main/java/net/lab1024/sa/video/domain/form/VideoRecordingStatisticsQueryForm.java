package net.lab1024.sa.video.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "录像统计查询表单")
public class VideoRecordingStatisticsQueryForm {

    @Schema(description = "统计类型: 1-按设备 2-按时间 3-按质量 4-按状态 5-存储统计", required = true)
    @NotNull(message = "统计类型不能为空")
    private Integer statisticsType;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "通道ID")
    private Integer channelId;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "时间粒度: 1-小时 2-天 3-周 4-月")
    private Integer timeGranularity;

    @Schema(description = "录像质量")
    private Integer quality;

    @Schema(description = "任务状态")
    private Integer status;
}
