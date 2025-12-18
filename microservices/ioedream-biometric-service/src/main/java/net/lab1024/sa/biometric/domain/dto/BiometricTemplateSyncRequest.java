package net.lab1024.sa.biometric.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 生物模板同步请求DTO
 * <p>
 * 用于调用device-comm-service同步模板到设备
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
@Schema(description = "生物模板同步请求")
public class BiometricTemplateSyncRequest {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String userName;

    @Schema(description = "生物识别类型", example = "1")
    private Integer biometricType;

    @Schema(description = "特征数据", example = "base64_encoded_feature_data")
    private String featureData;

    @Schema(description = "质量分数", example = "0.95")
    private Double qualityScore;

    @Schema(description = "模板版本", example = "1.0")
    private String templateVersion;
}
