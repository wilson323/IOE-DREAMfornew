package net.lab1024.sa.access.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 固件升级设备明细实体
 * <p>
 * 核心职责：
 * - 记录每台设备的升级详情
 * - 升级状态跟踪（待升级、升级中、成功、失败、已回滚）
 * - 重试机制支持
 * - 错误信息记录
 * - 回滚支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_firmware_upgrade_device")
@Schema(description = "固件升级设备明细实体")
public class FirmwareUpgradeDeviceEntity extends BaseEntity {

    @TableId(value = "detail_id", type = IdType.AUTO)
    @Schema(description = "明细ID（主键）", example = "1")
    private Long detailId;

    // ==================== 关联信息 ====================

    @Schema(description = "任务ID", example = "1")
    private Long taskId;

    @Schema(description = "设备ID", example = "100")
    private Long deviceId;

    // ==================== 设备信息 ====================

    @Schema(description = "设备编码", example = "DEV-192-168-1-100")
    private String deviceCode;

    @Schema(description = "设备名称", example = "A栋1楼门禁")
    private String deviceName;

    @Schema(description = "设备IP地址", example = "192.168.1.100")
    private String deviceIp;

    // ==================== 版本信息 ====================

    @Schema(description = "当前固件版本", example = "v0.9.0")
    private String currentVersion;

    @Schema(description = "目标固件版本", example = "v1.0.0")
    private String targetVersion;

    // ==================== 升级状态 ====================

    @Schema(description = "升级状态：1-待升级 2-升级中 3-升级成功 4-升级失败 5-已回滚", example = "3")
    private Integer upgradeStatus;

    @Schema(description = "开始升级时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时（秒）", example = "300")
    private Integer durationSeconds;

    // ==================== 重试机制 ====================

    @Schema(description = "重试次数", example = "0")
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetry;

    // ==================== 错误处理 ====================

    @Schema(description = "错误代码", example = "DEVICE_OFFLINE")
    private String errorCode;

    @Schema(description = "错误信息", example = "设备离线，连接超时")
    private String errorMessage;

    @Schema(description = "升级日志（JSON格式）")
    private String upgradeLog;

    // ==================== 回滚支持 ====================

    @Schema(description = "是否已回滚：0-否 1-是", example = "0")
    private Integer isRollback;

    @Schema(description = "回滚时间")
    private LocalDateTime rollbackTime;

    // ==================== 注意事项 ====================
    // 该表没有deleted字段，不继承BaseEntity的deleted字段映射
}
