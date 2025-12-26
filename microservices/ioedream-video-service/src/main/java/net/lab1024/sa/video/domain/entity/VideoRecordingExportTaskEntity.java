package net.lab1024.sa.video.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_video_recording_export_task")
@Schema(description = "录像导出任务")
public class VideoRecordingExportTaskEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "导出类型")
    private Integer exportType;

    @Schema(description = "导出格式")
    private Integer format;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "任务状态: 1-待执行 2-处理中 3-已完成 4-失败")
    private Integer status;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "进度百分比")
    private Integer progress;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "创建人ID")
    private Long userId;
}
