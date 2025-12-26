package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 固件上传表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "固件上传表单")
public class FirmwareUploadForm {

    @NotBlank(message = "固件名称不能为空")
    @Size(max = 200, message = "固件名称长度不能超过200个字符")
    @Schema(description = "固件名称", example = "AC-2000固件v1.0.0")
    private String firmwareName;

    @NotBlank(message = "固件版本号不能为空")
    @Size(max = 50, message = "固件版本号长度不能超过50个字符")
    @Schema(description = "固件版本号（如：v1.0.0）", example = "v1.0.0")
    private String firmwareVersion;

    @NotNull(message = "设备类型不能为空")
    @Schema(description = "设备类型：1-门禁 2-考勤 3-消费 4-视频 5-访客", example = "1")
    private Integer deviceType;

    @Size(max = 100, message = "设备型号长度不能超过100个字符")
    @Schema(description = "适用设备型号（如：AC-2000）", example = "AC-2000")
    private String deviceModel;

    @Size(max = 100, message = "设备品牌长度不能超过100个字符")
    @Schema(description = "设备品牌（如：Hikvision）", example = "Hikvision")
    private String brand;

    @Schema(description = "版本更新说明")
    private String releaseNotes;

    @Schema(description = "最低可升级版本（空表示无限制）", example = "v0.9.0")
    private String minVersion;

    @Schema(description = "最高可升级版本（空表示无限制）", example = "v1.2.0")
    private String maxVersion;

    @Schema(description = "是否强制升级：0-否 1-是", example = "0")
    private Integer isForce;

    @Schema(description = "备注", example = "首次发布版本")
    private String remark;
}
