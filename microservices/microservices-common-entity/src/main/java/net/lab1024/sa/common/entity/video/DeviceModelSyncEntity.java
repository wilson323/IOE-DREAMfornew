package net.lab1024.sa.common.entity.video;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备模型同步实体
 *
 * @author IOE-DREAM AI Team
 * @since 2025-01-30
 */
@Data
@TableName("t_video_device_model_sync")
@Schema(description = "设备模型同步实体")
public class DeviceModelSyncEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "同步ID")
    private Long syncId;

    @Schema(description = "模型ID")
    private Long modelId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "同步状态（0-待同步 1-同步中 2-成功 3-失败）")
    private Integer syncStatus;

    @Schema(description = "同步进度（0-100）")
    private Integer syncProgress;

    @Schema(description = "同步开始时间")
    private LocalDateTime syncStartTime;

    @Schema(description = "同步结束时间")
    private LocalDateTime syncEndTime;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
