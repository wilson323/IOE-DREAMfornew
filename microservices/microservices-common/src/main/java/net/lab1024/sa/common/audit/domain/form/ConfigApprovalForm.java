package net.lab1024.sa.common.audit.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 配置变更审批表单
 * <p>
 * 用于配置变更的审批操作，包含：
 * - 审批决定（通过/拒绝）
 * - 审批意见
 * - 审批人信息
 * - 审批时间
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-09
 */
@Data
@Schema(description = "配置变更审批表单")
public class ConfigApprovalForm {

    @Schema(description = "审批人ID", example = "1")
    @NotNull(message = "审批人ID不能为空")
    private Long approverId;

    @Schema(description = "审批人姓名", example = "张三")
    @NotNull(message = "审批人姓名不能为空")
    @Size(max = 100, message = "审批人姓名长度不能超过100个字符")
    private String approverName;

    @Schema(description = "是否通过审批", example = "true")
    @NotNull(message = "审批决定不能为空")
    private Boolean approved;

    @Schema(description = "审批意见", example = "配置合理，同意变更")
    @Size(max = 500, message = "审批意见长度不能超过500个字符")
    private String approvalComment;

    @Schema(description = "审批条件（如果有）", example = "需要在非高峰时段执行")
    @Size(max = 200, message = "审批条件长度不能超过200个字符")
    private String approvalConditions;

    @Schema(description = "生效时间（延迟生效）")
    private String effectiveTime;

    @Schema(description = "是否需要监控", example = "true")
    private Boolean requireMonitoring = false;

    @Schema(description = "监控时间（小时）", example = "24")
    private Integer monitoringHours = 24;

    @Schema(description = "备注信息", example = "注意检查相关依赖")
    @Size(max = 300, message = "备注信息长度不能超过300个字符")
    private String remark;
}