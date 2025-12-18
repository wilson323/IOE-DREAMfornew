package net.lab1024.sa.biometric.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import net.lab1024.sa.common.domain.form.BaseQueryForm;

/**
 * 生物模板查询表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@lombok.EqualsAndHashCode(callSuper = true)
@Schema(description = "生物模板查询表单")
public class BiometricTemplateQueryForm extends BaseQueryForm {

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "生物识别类型", example = "1")
    private Integer biometricType;

    @Schema(description = "模板状态", example = "1")
    private Integer templateStatus;

    @Schema(description = "设备ID", example = "DEVICE_001")
    private String deviceId;

    @Schema(description = "模板名称（模糊查询）", example = "人脸")
    private String templateName;
}
