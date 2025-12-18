package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 生物识别注册表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "生物识别注册表单")
public class BiometricRegisterForm {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
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

    @Size(max = 200, message = "备注长度不能超过200个字符")
    @Schema(description = "备注", example = "人脸识别模板")
    private String remarks;
}
