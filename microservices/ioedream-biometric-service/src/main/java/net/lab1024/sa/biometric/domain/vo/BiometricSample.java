package net.lab1024.sa.biometric.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.biometric.domain.entity.BiometricType;

/**
 * 生物样本VO
 * <p>
 * 封装生物识别所需的原始数据（图像/音频）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiometricSample {
    /**
     * 生物识别类型
     */
    private BiometricType type;

    /**
     * 原始数据（Base64编码的图像/音频）
     */
    private String imageData;

    /**
     * 活体检测数据（可选）
     */
    private String livenessData;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 用户ID（1:1验证时使用）
     */
    private Long userId;
}
