package net.lab1024.sa.oa.workflow.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批节点配置表单
 * <p>
 * 用于创建和更新审批节点配置
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
@Schema(description = "审批节点配置表单")
public class ApprovalNodeConfigForm {

    @Schema(description = "审批配置ID", example = "1")
    @NotNull(message = "审批配置ID不能为空")
    private Long approvalConfigId;

    @Schema(description = "节点名称", example = "部门经理审批")
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    @Schema(description = "节点顺序（从1开始）", example = "1")
    @NotNull(message = "节点顺序不能为空")
    private Integer nodeOrder;

    @Schema(description = "节点类型（SERIAL-串行 PARALLEL-并行 COUNTERSIGN-会签 OR_SIGN-或签 CONDITION-条件分支 AUTO-自动）", example = "SERIAL")
    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    @Schema(description = "审批人设置方式", example = "DIRECT_MANAGER")
    @NotBlank(message = "审批人设置方式不能为空")
    private String approverType;

    @Schema(description = "审批人配置（JSON格式）")
    private String approverConfig;

    @Schema(description = "审批人数量限制", example = "3")
    private Integer approverCountLimit;

    @Schema(description = "审批通过条件（ALL-全部通过 ANY-任意一人通过 MAJORITY-多数通过 PERCENTAGE-按比例通过）", example = "ALL")
    private String passCondition;

    @Schema(description = "通过比例（0-100）", example = "50")
    private Integer passPercentage;

    @Schema(description = "条件分支配置（JSON格式）")
    private String conditionConfig;

    @Schema(description = "审批代理配置（JSON格式）")
    private String proxyConfig;

    @Schema(description = "审批抄送配置（JSON格式）")
    private String ccConfig;

    @Schema(description = "超时配置（JSON格式）")
    private String timeoutConfig;

    @Schema(description = "审批意见要求（NONE-不要求 OPTIONAL-可选 REQUIRED-必填 REQUIRED_ON_REJECT-驳回时必填）", example = "OPTIONAL")
    private String commentRequired;

    @Schema(description = "附件要求（NONE-不要求 OPTIONAL-可选 REQUIRED-必填）", example = "OPTIONAL")
    private String attachmentRequired;

    @Schema(description = "附件配置（JSON格式）")
    private String attachmentConfig;

    @Schema(description = "数据权限配置（JSON格式）")
    private String dataPermissionConfig;

    @Schema(description = "是否允许撤回（0-不允许 1-允许）", example = "1")
    private Integer allowWithdraw;

    @Schema(description = "撤回条件配置（JSON格式）")
    private String withdrawConfig;

    @Schema(description = "是否允许催办（0-不允许 1-允许）", example = "1")
    private Integer allowUrge;

    @Schema(description = "催办配置（JSON格式）")
    private String urgeConfig;

    @Schema(description = "是否允许评论（0-不允许 1-允许）", example = "1")
    private Integer allowComment;

    @Schema(description = "评论配置（JSON格式）")
    private String commentConfig;

    @Schema(description = "状态（ENABLED-启用 DISABLED-禁用）", example = "ENABLED")
    private String status;

    @Schema(description = "排序号", example = "0")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}






