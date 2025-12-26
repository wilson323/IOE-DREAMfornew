package net.lab1024.sa.attendance.mobile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 位置验证结果
 * <p>
 * 封装位置验证的结果信息
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
public class LocationVerificationResult {

    /**
     * 是否有效
     */
    private Boolean valid;

    /**
     * 位置信息
     */
    private LocationInfo location;

    /**
     * 验证方法
     */
    private String verificationMethod;

    /**
     * 验证时间
     */
    private LocalDateTime verificationTime;

    /**
     * 失败原因
     */
    private String failureReason;

    public boolean isValid() {
        return Boolean.TRUE.equals(valid);
    }
}
