package net.lab1024.sa.biometric.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活体检测结果VO
 * <p>
 * 封装活体检测的结果
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
public class LivenessResult {
    /**
     * 是否为活体
     */
    private Boolean isAlive;

    /**
     * 活体检测分数（0.0-1.0）
     */
    private Double livenessScore;

    /**
     * 检测方法（如：眨眼、温度、压力等）
     */
    private String detectionMethod;

    /**
     * 检测详情
     */
    private String details;
}
