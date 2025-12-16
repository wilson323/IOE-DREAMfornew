package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 生物识别数据表单
 * <p>
 * 用于生物识别安全验证的请求数据
 * 严格遵循CLAUDE.md规范：
 * - 使用@Data注解
 * - 完整的字段验证注解
 * - Swagger文档注解
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "生物识别数据")
public class BiometricDataForm {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 生物识别类型
     * FACE - 人脸识别
     * FINGERPRINT - 指纹识别
     * IRIS - 虹膜识别
     * PALM - 掌纹识别
     * VOICE - 声纹识别
     */
    @NotNull(message = "生物识别类型不能为空")
    @Pattern(regexp = "^(FACE|FINGERPRINT|IRIS|PALM|VOICE)$", message = "生物识别类型无效")
    @Schema(description = "生物识别类型", example = "FACE", allowableValues = {"FACE", "FINGERPRINT", "IRIS", "PALM", "VOICE"})
    private String biometricType;

    /**
     * 生物识别数据（Base64编码）
     */
    @NotNull(message = "生物识别数据不能为空")
    @Schema(description = "生物识别数据（Base64编码）", example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...")
    private String biometricData;

    /**
     * 数据质量评分（0-100）
     */
    @Schema(description = "数据质量评分（0-100）", example = "92.5")
    private Integer qualityScore;

    /**
     * 设备信息
     */
    @Schema(description = "采集设备信息", example = "iPhone 13 Pro")
    private String deviceInfo;

    /**
     * 环境信息
     */
    @Schema(description = "环境信息", example = "室内正常光照")
    private String environmentInfo;

    /**
     * 元数据
     */
    @Schema(description = "额外元数据")
    private String metadata;
}