package net.lab1024.sa.biometric.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 特征向量VO
 * <p>
 * 封装从生物样本中提取的特征向量
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
public class FeatureVector {
    /**
     * 特征向量数据（浮点数组）
     */
    private float[] features;

    /**
     * 特征向量维度
     */
    private Integer dimension;

    /**
     * 用户ID（关联用户）
     */
    private Long userId;

    /**
     * 模板ID（关联模板）
     */
    private Long templateId;
}
