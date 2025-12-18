package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 设备控制请求表单
 * 用于移动端设备控制操作
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备控制请求表单")
public class DeviceControlRequest {

    @NotNull(message = "设备ID不能为空")
    @Schema(description = "设备ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long deviceId;

    @NotBlank(message = "控制命令不能为空")
    @Schema(description = "控制命令", requiredMode = Schema.RequiredMode.REQUIRED,
           example = "restart", allowableValues = {"restart", "shutdown", "maintenance", "calibrate", "unlock", "lock", "reset"})
    private String command;

    @Schema(description = "控制参数(JSON格式)",
           example = "{\"duration\":5,\"reason\":\"定期重启\"}")
    private String parameters;

    @Schema(description = "操作原因", example = "定期维护重启")
    private String reason;

    @Schema(description = "是否强制执行", example = "false")
    private Boolean forceExecute;

    @Schema(description = "执行超时时间(秒)", example = "30")
    @Min(value = 5, message = "超时时间不能少于5秒")
    @Max(value = 300, message = "超时时间不能超过300秒")
    private Integer timeoutSeconds;

    @Schema(description = "预期执行时间", example = "2025-12-16T15:00:00")
    private String scheduledTime;

    // 设备特定控制参数

    @Schema(description = "重启方式", example = "soft", allowableValues = {"soft", "hard"})
    private String restartType;

    @Schema(description = "维护模式持续时间(小时)", example = "24")
    @Min(value = 1, message = "维护时间不能少于1小时")
    @Max(value = 168, message = "维护时间不能超过168小时")
    private Integer maintenanceDuration;

    @Schema(description = "校准类型", example = "face", allowableValues = {"face", "fingerprint", "card", "all"})
    private String calibrationType;

    @Schema(description = "校准精度", example = "high", allowableValues = {"low", "medium", "high"})
    private String calibrationPrecision;

    // 门禁设备特有参数

    @Schema(description = "门锁保持时间(毫秒)", example = "3000")
    @Min(value = 1000, message = "门锁保持时间不能少于1秒")
    @Max(value = 30000, message = "门锁保持时间不能超过30秒")
    private Integer doorOpenDuration;

    @Schema(description = "是否反锁", example = "false")
    private Boolean antiLock;

    // 验证参数

    @Schema(description = "操作员用户名", example = "admin")
    private String operatorUsername;

    @Schema(description = "操作员密码（用于敏感操作验证）", example = "")
    private String operatorPassword;

    @Schema(description = "验证码", example = "123456")
    private String verificationCode;
}
