package net.lab1024.sa.biometric.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 生物模板更新表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
@Data
@Schema(description = "生物模板更新表单")
public class BiometricTemplateUpdateForm {

    @NotNull(message = "模板ID不能为空")
    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    @Schema(description = "模板名称", example = "用户人脸特征模板")
    private String templateName;

    @Schema(description = "模板状态", example = "1", allowableValues = {"1", "2", "3", "4"})
    private Integer templateStatus;

    @Schema(description = "匹配阈值", example = "0.85")
    private Double matchThreshold;

    @Size(max = 200, message = "备注长度不能超过200个字符")
    @Schema(description = "备注", example = "更新备注")
    private String remarks;
}
