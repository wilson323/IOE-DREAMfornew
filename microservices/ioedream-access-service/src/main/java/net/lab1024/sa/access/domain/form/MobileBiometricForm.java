package net.lab1024.sa.access.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 移动端生物识别表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "移动端生物识别表单")
public class MobileBiometricForm {

    @Schema(description = "用户ID", required = true, example = "1001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "设备ID", required = true, example = "MOBILE_001")
    @NotNull(message = "设备ID不能为空")
    private String deviceId;

    @Schema(description = "区域ID", required = true, example = "1")
    @NotNull(message = "区域ID不能为空")
    private Long areaId;

    @Schema(description = "生物特征类型", required = true, example = "face")
    @NotNull(message = "生物特征类型不能为空")
    private String biometricType;

    @Schema(description = "生物特征数据（Base64编码）")
    private String biometricData;

    @Schema(description = "特征向量（用于1:N比对）")
    private String featureVector;

    @Schema(description = "置信度阈值", example = "0.8")
    private Double confidenceThreshold;
}
