package net.lab1024.sa.oa.workflow.domain.form;

import jakarta.validation.constraints.NotNull;

/**
 * 审批操作表单
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
public class ApprovalActionForm {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "操作类型不能为空")
    private String actionType;

    private String comment;
    private Long targetUserId;
    private String targetUserName;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }

    public String getTargetUserName() { return targetUserName; }
    public void setTargetUserName(String targetUserName) { this.targetUserName = targetUserName; }
}
