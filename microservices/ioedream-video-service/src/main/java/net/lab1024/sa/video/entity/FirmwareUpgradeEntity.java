package net.lab1024.sa.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 固件升级实体
 * <p>
 * 记录视频设备固件升级信息，包括：
 * - 固件版本信息
 * - 升级任务状态
 * - 升级进度跟踪
 * - 升级结果记录
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_video_firmware_upgrade")
@Schema(description = "固件升级实体")
public class FirmwareUpgradeEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "升级任务ID", example = "1")
    private Long upgradeId;

    @Schema(description = "设备ID", example = "1001")
    private Long deviceId;

    @Schema(description = "设备编号", example = "CAM001")
    private String deviceCode;

    @Schema(description = "设备名称", example = "1楼大厅摄像头")
    private String deviceName;

    @Schema(description = "当前固件版本", example = "v1.0.0")
    private String currentVersion;

    @Schema(description = "目标固件版本", example = "v1.2.0")
    private String targetVersion;

    @Schema(description = "固件文件URL", example = "/firmware/v1.2.0/firmware.bin")
    private String firmwareUrl;

    @Schema(description = "固件文件大小（字节）", example = "10485760")
    private Long fileSize;

    @Schema(description = "固件文件MD5", example = "abc123def456")
    private String fileMd5;

    @Schema(description = "升级状态：1-待升级 2-升级中 3-升级成功 4-升级失败", example = "1")
    private Integer upgradeStatus;

    @Schema(description = "升级进度（0-100）", example = "0")
    private Integer progress;

    @Schema(description = "升级开始时间")
    private LocalDateTime startTime;

    @Schema(description = "升级完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "升级类型：1-手动升级 2-自动升级", example = "1")
    private Integer upgradeType;

    @Schema(description = "升级描述")
    private String description;

    @Schema(description = "创建人ID", example = "1")
    private Long createUserId;

    @Schema(description = "创建人姓名", example = "管理员")
    private String createUserName;

    @Schema(description = "创建时间", example = "2025-12-26T10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-26T10:05:00")
    private LocalDateTime updateTime;
}
