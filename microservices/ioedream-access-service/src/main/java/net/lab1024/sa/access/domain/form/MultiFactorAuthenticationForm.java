package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 多因子认证请求表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用OpenAPI 3.0注解（@Schema）
 * - 使用Jakarta EE验证注解（@NotNull）
 * - 提供清晰的字段说明
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Schema(description = "多因子认证请求表单")
public class MultiFactorAuthenticationForm {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", required = true, example = "1")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 设备ID
     */
    @Schema(description = "设备ID", required = true, example = "DEV001")
    @NotNull(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 认证模式
     * STRICT - 严格模式（所有因子必须通过）
     * RELAXED - 宽松模式（至少一个因子通过）
     * PRIORITY - 优先模式（按优先级依次验证）
     */
    @Schema(description = "认证模式", required = true, example = "STRICT")
    @NotNull(message = "认证模式不能为空")
    private String authenticationMode;

    /**
     * 认证因子列表
     */
    @Schema(description = "认证因子列表", required = true)
    @NotNull(message = "认证因子列表不能为空")
    private List<AuthenticationFactor> factors;

    /**
     * 认证因子
     */
    @Data
    @Schema(description = "认证因子")
    public static class AuthenticationFactor {

        /**
         * 认证类型
         * FACE - 人脸
         * FINGERPRINT - 指纹
         * CARD - IC卡
         */
        @Schema(description = "认证类型", required = true, example = "FACE")
        @NotNull(message = "认证类型不能为空")
        private String type;

        /**
         * 认证数据
         * 人脸：Base64编码的图像数据
         * 指纹：特征字节数组
         * IC卡：卡号
         */
        @Schema(description = "认证数据", required = true)
        @NotNull(message = "认证数据不能为空")
        private String data;

        /**
         * 优先级（数字越小优先级越高）
         */
        @Schema(description = "优先级", example = "1")
        private Integer priority;

        /**
         * 是否必需
         */
        @Schema(description = "是否必需", example = "true")
        private Boolean required;
    }
}
