package net.lab1024.sa.biometric.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 识别结果VO
 * <p>
 * 封装1:N识别的结果（可能返回多个候选结果）
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
public class IdentificationResult {
    /**
     * 是否识别成功
     */
    private Boolean identified;

    /**
     * 最匹配的用户ID
     */
    private Long userId;

    /**
     * 最匹配的分数
     */
    private Double matchScore;

    /**
     * 候选结果列表（Top N）
     */
    private List<CandidateResult> candidates;

    /**
     * 候选结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateResult {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 匹配分数
         */
        private Double matchScore;
    }
}
