package net.lab1024.sa.biometric.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 特征向量VO
 * <p>
 * 封装生物特征提取的结果
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
@Schema(description = "特征向量")
public class FeatureVector {

    @Schema(description = "生物识别类型", example = "1")
    private Integer biometricType;

    @Schema(description = "特征向量维度", example = "512")
    private Integer dimension;

    @Schema(description = "特征数据(Base64编码)", example = "base64_encoded_feature_data")
    private String data;

    @Schema(description = "质量分数", example = "0.95")
    private Double qualityScore;

    @Schema(description = "算法版本", example = "v2.1.0")
    private String algorithmVersion;
}
