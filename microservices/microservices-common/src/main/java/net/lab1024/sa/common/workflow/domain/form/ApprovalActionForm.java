package net.lab1024.sa.common.workflow.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批操作表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - Form类用于接收前端请求参数
 * - 包含完整的参数验证注解
 * - 支持多种审批操作（同意、驳回、转办、委派）
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Data
public class ApprovalActionForm {

    /**
     * 任务ID（必填）
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    /**
     * 用户ID（操作人ID，必填）
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 操作类型（APPROVE-同意, REJECT-驳回, TRANSFER-转办, DELEGATE-委派）
     */
    @NotNull(message = "操作类型不能为空")
    private String actionType;

    /**
     * 处理意见
     */
    private String comment;

    /**
     * 目标用户ID（转办/委派时必填）
     */
    private Long targetUserId;

    /**
     * 目标用户姓名（转办/委派时使用）
     */
    private String targetUserName;
}

