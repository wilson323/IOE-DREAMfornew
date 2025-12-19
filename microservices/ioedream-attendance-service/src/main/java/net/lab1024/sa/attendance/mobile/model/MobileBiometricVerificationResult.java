package net.lab1024.sa.attendance.mobile.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 移动端生物识别验证结果
 * <p>
 * 封装移动端生物识别验证响应结果
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "移动端生物识别验证结果")
public class MobileBiometricVerificationResult {

    /**
     * 是否验证通过
     */
    @Schema(description = "是否验证通过", example = "true")
    private Boolean verified;

    /**
     * 匹配度
     */
    @Schema(description = "匹配度", example = "95.5")
    private Double confidence;

    /**
     * 生物识别类型
     */
    @Schema(description = "生物识别类型", example = "FACE")
    private String biometricType;
}
