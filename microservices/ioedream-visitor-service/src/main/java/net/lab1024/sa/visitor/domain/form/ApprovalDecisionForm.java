package net.lab1024.sa.visitor.domain.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 审批决策表单
 * <p>
 * 内存优化设计：
 * - 精简字段数量，只包含必要信息
 * - 合理的注解使用，避免过度验证
 * - 使用String而非枚举，减少对象创建
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "审批决策表单")
public class ApprovalDecisionForm {

    /**
     * 审批结果
     * <p>
     * APPROVED-已通过
     * REJECTED-已拒绝
     * </p>
     */
    @NotBlank(message = "审批结果不能为空")
    @Size(max = 20, message = "审批结果长度不能超过20个字符")
    @Schema(description = "审批结果", example = "APPROVED", allowableValues = {"APPROVED", "REJECTED"})
    private String approvalResult;

    /**
     * 审批意见
     */
    @Size(max = 500, message = "审批意见长度不能超过500个字符")
    @Schema(description = "审批意见", example = "同意预约，请遵守园区管理规定")
    private String approvalComment;

    /**
     * 审批人备注（可选，用于特殊情况说明）
     */
    @Size(max = 200, message = "审批人备注长度不能超过200个字符")
    @Schema(description = "审批人备注", example = "需要额外安全检查")
    private String approverRemark;
}
