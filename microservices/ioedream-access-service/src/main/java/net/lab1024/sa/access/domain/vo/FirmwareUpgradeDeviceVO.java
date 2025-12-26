package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 固件升级设备明细VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "固件升级设备明细VO")
public class FirmwareUpgradeDeviceVO {

    @Schema(description = "明细ID", example = "1")
    private Long detailId;

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "设备ID", example = "100")
    private Long deviceId;

    @Schema(description = "设备编码", example = "DEV-192-168-1-100")
    private String deviceCode;

    @Schema(description = "设备名称", example = "A栋1楼门禁")
    private String deviceName;

    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String deviceIp;

    @Schema(description = "当前固件版本", example = "v0.9.0")
    private String currentVersion;

    @Schema(description = "目标固件版本", example = "v1.0.0")
    private String targetVersion;

    @Schema(description = "升级状态：1-待升级 2-升级中 3-升级成功 4-升级失败 5-已回滚", example = "3")
    private Integer upgradeStatus;

    @Schema(description = "升级状态名称", example = "升级成功")
    private String upgradeStatusName;

    @Schema(description = "开始升级时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时（秒）", example = "300")
    private Integer durationSeconds;

    @Schema(description = "耗时（格式化）", example = "5分钟")
    private String durationFormatted;

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetry;

    @Schema(description = "错误代码", example = "DEVICE_OFFLINE")
    private String errorCode;

    @Schema(description = "错误信息", example = "设备离线，连接超时")
    private String errorMessage;

    @Schema(description = "升级日志（JSON格式）")
    private String upgradeLog;

    @Schema(description = "是否已回滚：0-否 1-是", example = "0")
    private Integer isRollback;

    @Schema(description = "回滚时间")
    private LocalDateTime rollbackTime;
}
