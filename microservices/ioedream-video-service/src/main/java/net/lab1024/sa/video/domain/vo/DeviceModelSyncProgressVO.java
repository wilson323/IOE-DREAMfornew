package net.lab1024.sa.video.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设备模型同步进度视图对象
 * <p>
 * 用于展示AI模型到设备的同步进度：
 * 1. 总体统计
 * 2. 各状态设备数量
 * 3. 同步进度百分比
 * </p>
 *
 * @author IOE-DREAM AI Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "设备模型同步进度视图对象")
public class DeviceModelSyncProgressVO {

    @Schema(description = "总设备数", example = "10")
    private Integer totalDevices;

    @Schema(description = "待同步设备数", example = "2")
    private Integer pendingDevices;

    @Schema(description = "同步中设备数", example = "3")
    private Integer syncingDevices;

    @Schema(description = "同步成功设备数", example = "4")
    private Integer successDevices;

    @Schema(description = "同步失败设备数", example = "1")
    private Integer failedDevices;

    @Schema(description = "同步进度百分比（0-100）", example = "50")
    private Integer progress;

    @Schema(description = "同步进度描述", example = "50%")
    private String progressDesc;
}
