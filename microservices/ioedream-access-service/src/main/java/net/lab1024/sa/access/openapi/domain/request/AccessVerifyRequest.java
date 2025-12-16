package net.lab1024.sa.access.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 门禁通行验证请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Schema(description = "门禁通行验证请求")
public class AccessVerifyRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001", required = true)
    private Long userId;

    @NotBlank(message = "设备ID不能为空")
    @Schema(description = "门禁设备ID", example = "ACCESS_001", required = true)
    private String deviceId;

    @Schema(description = "验证类型", example = "card", allowableValues = {"card", "face", "fingerprint", "password", "qr_code"})
    private String verifyType = "card";

    @Schema(description = "验证数据", example = "1234567890")
    private String verifyData;

    @Schema(description = "卡号", example = "1234567890")
    private String cardNumber;

    @Schema(description = "人脸特征数据", example = "base64_encoded_face_data")
    private String faceData;

    @Schema(description = "指纹特征数据", example = "base64_encoded_fingerprint_data")
    private String fingerprintData;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "二维码数据", example = "qr_code_data_123")
    private String qrCodeData;

    @Schema(description = "温度数据", example = "36.5")
    private Double temperature;

    @Schema(description = "通行方向", example = "in", allowableValues = {"in", "out"})
    private String direction;

    @Schema(description = "是否需要活体检测", example = "true")
    private Boolean requireLivenessCheck = false;

    @Schema(description = "是否需要戴口罩检测", example = "false")
    private Boolean requireMaskCheck = false;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号码格式不正确")
    @Schema(description = "手机号码（用于短信验证）", example = "13812345678")
    private String phoneNumber;

    @Schema(description = "扩展参数（JSON格式）", example = "{\"key1\":\"value1\"}")
    private String extendedParams;
}