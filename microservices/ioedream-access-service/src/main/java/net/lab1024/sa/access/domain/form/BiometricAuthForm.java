package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 生物识别认证表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "生物识别认证表单")
public class BiometricAuthForm {

    @Schema(description = "用户ID（1:1验证时必填）", example = "1001")
    private Long userId;

    @NotNull(message = "生物识别类型不能为空")
    @Schema(description = "生物识别类型", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer biometricType;

    @NotBlank(message = "特征数据不能为空")
    @Size(max = 5000000, message = "特征数据不能超过5MB")
    @Schema(description = "特征数据(Base64编码)", example = "base64_encoded_feature_data")
    private String featureData;

    @NotBlank(message = "设备ID不能为空")
    @Size(max = 100, message = "设备ID长度不能超过100个字符")
    @Schema(description = "设备ID", example = "DEVICE_001")
    private String deviceId;

    @Min(value = 1, message = "返回结果数量不能小于1")
    @Max(value = 100, message = "返回结果数量不能超过100")
    @Schema(description = "返回结果数量限制（1:N识别时使用）", example = "10")
    private Integer limit;

    @Schema(description = "客户端IP地址", example = "192.168.1.100")
    private String clientIp;

    @Schema(description = "客户端信息", example = "iOS App v1.2.0")
    private String clientInfo;

    @Schema(description = "验证类型", example = "1", allowableValues = {"1", "2", "3", "4", "5"})
    private Integer authType;

    @Schema(description = "是否强制活体检测", example = "true")
    private Boolean forceLiveness;
}