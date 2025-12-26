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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备固件信息实体
 * <p>
 * 核心职责：
 * - 固件版本管理
 * - 固件文件存储（上传、下载、MD5校验）
 * - 固件状态管理（测试中、正式发布、已废弃）
 * - 升级范围控制（最低/最高版本、强制升级）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_firmware")
@Schema(description = "设备固件信息实体")
public class DeviceFirmwareEntity extends BaseEntity {

    @TableId(value = "firmware_id", type = IdType.AUTO)
    @Schema(description = "固件ID（主键）", example = "1")
    private Long firmwareId;

    // ==================== 基本信息 ====================

    @Schema(description = "固件名称", example = "AC-2000固件v1.0.0")
    private String firmwareName;

    @Schema(description = "固件版本号（如：v1.0.0）", example = "v1.0.0")
    private String firmwareVersion;

    @Schema(description = "设备类型：1-门禁 2-考勤 3-消费 4-视频 5-访客", example = "1")
    private Integer deviceType;

    @Schema(description = "适用设备型号（如：AC-2000）", example = "AC-2000")
    private String deviceModel;

    @Schema(description = "设备品牌（如：Hikvision）", example = "Hikvision")
    private String brand;

    // ==================== 文件信息 ====================

    @Schema(description = "固件文件存储路径", example = "/firmware/ac2000_v1.0.0.bin")
    private String firmwareFilePath;

    @Schema(description = "固件文件名称", example = "ac2000_v1.0.0.bin")
    private String firmwareFileName;

    @Schema(description = "固件文件大小（字节）", example = "1024000")
    private Long firmwareFileSize;

    @Schema(description = "固件文件MD5校验值", example = "5d41402abc4b2a76b9719d911017c592")
    private String firmwareFileMd5;

    @Schema(description = "版本更新说明")
    private String releaseNotes;

    // ==================== 升级控制 ====================

    @Schema(description = "最低可升级版本（空表示无限制）", example = "v0.9.0")
    private String minVersion;

    @Schema(description = "最高可升级版本（空表示无限制）", example = "v1.2.0")
    private String maxVersion;

    @Schema(description = "是否强制升级：0-否 1-是", example = "0")
    private Integer isForce;

    @Schema(description = "是否启用：0-禁用 1-启用", example = "1")
    private Integer isEnabled;

    // ==================== 上传信息 ====================

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "上传人ID", example = "1")
    private Long uploaderId;

    @Schema(description = "上传人姓名", example = "系统管理员")
    private String uploaderName;

    // ==================== 统计信息 ====================

    @Schema(description = "下载次数", example = "100")
    private Integer downloadCount;

    @Schema(description = "被升级次数", example = "50")
    private Integer upgradeCount;

    // ==================== 状态信息 ====================

    @Schema(description = "固件状态：1-测试中 2-正式发布 3-已废弃", example = "2")
    private Integer firmwareStatus;

    @Schema(description = "备注", example = "首次发布版本")
    private String remark;

    // ==================== 逻辑删除（覆盖BaseEntity） ====================
    // 注意：SQL表使用TINYINT类型，需要Integer而非Boolean
    @Schema(description = "逻辑删除：0-未删除 1-已删除", example = "0")
    @TableField("deleted")
    private Integer deletedFlag;
}
