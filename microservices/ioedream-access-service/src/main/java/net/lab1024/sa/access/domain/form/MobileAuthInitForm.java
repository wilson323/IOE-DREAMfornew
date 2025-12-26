package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端认证初始化表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端认证初始化表单")
public class MobileAuthInitForm {

    @Schema(description = "设备ID", required = true, example = "MOBILE_001")
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @Schema(description = "设备类型", required = true, example = "android")
    @NotBlank(message = "设备类型不能为空")
    private String deviceType;

    @Schema(description = "设备名称", example = "华为P40")
    private String deviceName;

    @Schema(description = "设备指纹", example = "abc123def456")
    private String deviceFingerprint;

    @Schema(description = "应用版本", example = "1.0.0")
    private String appVersion;

    @Schema(description = "操作系统版本", example = "Android 10")
    private String osVersion;

    @Schema(description = "用户ID（已登录用户）", example = "1001")
    private Long userId;
}
