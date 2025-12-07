package net.lab1024.sa.common.workflow.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 审批模板表单
 * <p>
 * 用于创建和更新审批模板
 * 严格遵循CLAUDE.md规范：
 * - 使用@Valid进行参数校验
 * - 使用Swagger注解进行API文档
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "审批模板表单")
public class ApprovalTemplateForm {

    @Schema(description = "模板名称", example = "请假审批模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    @Schema(description = "模板编码（唯一标识）", example = "TEMPLATE_LEAVE_001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    @Schema(description = "模板分类", example = "考勤类")
    private String category;

    @Schema(description = "模板描述")
    private String description;

    @Schema(description = "模板图标", example = "icon-leave")
    private String icon;

    @Schema(description = "模板配置（JSON格式）")
    private String templateConfig;

    @Schema(description = "表单设计（JSON格式）")
    private String formDesign;

    @Schema(description = "是否公开（0-私有 1-公开）", example = "1")
    private Integer isPublic;

    @Schema(description = "状态（ENABLED-启用 DISABLED-禁用）", example = "ENABLED")
    private String status;

    @Schema(description = "排序号", example = "0")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}

