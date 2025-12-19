package net.lab1024.sa.attendance.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 排班模板表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "排班模板表单")
public class ScheduleTemplateForm {

    @Schema(description = "模板ID", example = "1001")
    private Long templateId;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100)
    @Schema(description = "模板名称", example = "技术部标准排班模板")
    private String templateName;

    @NotBlank(message = "模板类型不能为空")
    @Size(max = 50)
    @Schema(description = "模板类型", example = "部门模板")
    private String templateType;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "模板配置JSON", example = "{\"cycle_type\": \"weekly\"}")
    private String templateConfigJson;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建人ID", example = "2001")
    private Long createUserId;
}