package net.lab1024.sa.access.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 固件基本信息VO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "固件基本信息VO")
public class FirmwareVO {

    @Schema(description = "固件ID", example = "1")
    private Long firmwareId;

    @Schema(description = "固件名称", example = "AC-2000固件v1.0.0")
    private String firmwareName;

    @Schema(description = "固件版本号", example = "v1.0.0")
    private String firmwareVersion;

    @Schema(description = "设备类型：1-门禁 2-考勤 3-消费 4-视频 5-访客", example = "1")
    private Integer deviceType;

    @Schema(description = "设备类型名称", example = "门禁设备")
    private String deviceTypeName;

    @Schema(description = "设备型号", example = "AC-2000")
    private String deviceModel;

    @Schema(description = "设备品牌", example = "Hikvision")
    private String brand;

    @Schema(description = "固件文件名称", example = "ac2000_v1.0.0.bin")
    private String firmwareFileName;

    @Schema(description = "固件文件大小（字节）", example = "1024000")
    private Long firmwareFileSize;

    @Schema(description = "固件文件大小（格式化）", example = "1.00 MB")
    private String firmwareFileSizeFormatted;

    @Schema(description = "固件文件MD5校验值", example = "5d41402abc4b2a76b9719d911017c592")
    private String firmwareFileMd5;

    @Schema(description = "版本更新说明")
    private String releaseNotes;

    @Schema(description = "最低可升级版本", example = "v0.9.0")
    private String minVersion;

    @Schema(description = "最高可升级版本", example = "v1.2.0")
    private String maxVersion;

    @Schema(description = "是否强制升级：0-否 1-是", example = "0")
    private Integer isForce;

    @Schema(description = "是否启用：0-禁用 1-启用", example = "1")
    private Integer isEnabled;

    @Schema(description = "上传时间")
    private LocalDateTime uploadTime;

    @Schema(description = "上传人ID", example = "1")
    private Long uploaderId;

    @Schema(description = "上传人姓名", example = "系统管理员")
    private String uploaderName;

    @Schema(description = "下载次数", example = "100")
    private Integer downloadCount;

    @Schema(description = "被升级次数", example = "50")
    private Integer upgradeCount;

    @Schema(description = "固件状态：1-测试中 2-正式发布 3-已废弃", example = "2")
    private Integer firmwareStatus;

    @Schema(description = "固件状态名称", example = "正式发布")
    private String firmwareStatusName;

    @Schema(description = "备注", example = "首次发布版本")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
