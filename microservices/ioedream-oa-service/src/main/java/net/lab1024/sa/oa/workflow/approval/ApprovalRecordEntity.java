package net.lab1024.sa.oa.workflow.approval;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 审批记录实体
 */
@TableName("t_workflow_approval_record")
public class ApprovalRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("process_instance_id")
    private String processInstanceId;
    @TableField("task_id")
    private String taskId;
    @TableField("approver_id")
    private Long approverId;
    @TableField("outcome")
    private Integer outcome;
    @TableField("comment")
    private String comment;
    @TableField("approval_time")
    private LocalDateTime approvalTime;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public Integer getOutcome() { return outcome; }
    public void setOutcome(Integer outcome) { this.outcome = outcome; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getApprovalTime() { return approvalTime; }
    public void setApprovalTime(LocalDateTime approvalTime) { this.approvalTime = approvalTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
}
