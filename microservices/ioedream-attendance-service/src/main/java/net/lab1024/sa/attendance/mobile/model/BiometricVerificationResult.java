package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 生物识别验证结果
 * <p>
 * 封装生物识别验证的结果信息
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
public class BiometricVerificationResult {

    /**
     * 是否验证通过
     */
    private Boolean verified;

    /**
     * 置信度
     */
    private Double confidence;

    /**
     * 生物识别类型
     */
    private String biometricType;

    /**
     * 验证时间
     */
    private LocalDateTime verificationTime;

    /**
     * 失败原因
     */
    private String failureReason;

    public boolean isVerified() {
        return Boolean.TRUE.equals(verified);
    }
}
