package net.lab1024.sa.oa.workflow.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批配置表单
 * <p>
 * 用于创建和更新审批配置
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
@Schema(description = "审批配置表单")
public class ApprovalConfigForm {

    @Schema(description = "业务类型（唯一标识）", example = "CUSTOM_APPROVAL_001")
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    @Schema(description = "业务类型名称", example = "自定义审批")
    @NotBlank(message = "业务类型名称不能为空")
    private String businessTypeName;

    @Schema(description = "所属模块", example = "自定义模块")
    @NotBlank(message = "所属模块不能为空")
    private String module;

    @Schema(description = "流程定义ID", example = "100")
    private Long definitionId;

    @Schema(description = "流程定义Key（备用）", example = "custom_approval_process")
    private String processKey;

    @Schema(description = "审批规则配置（JSON格式）", example = "{\"amount_threshold\": 1000}")
    private String approvalRules;

    @Schema(description = "审批后处理配置（JSON格式）")
    private String postApprovalHandler;

    @Schema(description = "超时配置（JSON格式）")
    private String timeoutConfig;

    @Schema(description = "通知配置（JSON格式）")
    private String notificationConfig;

    @Schema(description = "适用范围配置（JSON格式）")
    private String applicableScope;

    @Schema(description = "状态（ENABLED-启用 DISABLED-禁用）", example = "ENABLED")
    private String status;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "生效时间")
    private LocalDateTime effectiveTime;

    @Schema(description = "失效时间（null表示永久有效）")
    private LocalDateTime expireTime;
}






