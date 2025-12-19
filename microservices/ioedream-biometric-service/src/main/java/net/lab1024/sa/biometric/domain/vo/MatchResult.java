package net.lab1024.sa.biometric.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 匹配结果VO
 * <p>
 * 封装1:1验证的匹配结果
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
public class MatchResult {
    /**
     * 是否匹配
     */
    private Boolean matched;

    /**
     * 匹配分数（0.0-1.0）
     */
    private Double matchScore;

    /**
     * 匹配阈值
     */
    private Double threshold;

    /**
     * 用户ID
     */
    private Long userId;
}
