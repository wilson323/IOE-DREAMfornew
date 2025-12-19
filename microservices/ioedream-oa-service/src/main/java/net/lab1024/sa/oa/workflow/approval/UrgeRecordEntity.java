package net.lab1024.sa.oa.workflow.approval;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 催办记录实体
 */
@TableName("t_workflow_urge_record")
public class UrgeRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("process_instance_id")
    private String processInstanceId;
    @TableField("task_id")
    private String taskId;
    @TableField("urger_id")
    private Long urgerId;
    @TableField("assignee_id")
    private Long assigneeId;
    @TableField("message")
    private String message;
    @TableField("urge_time")
    private LocalDateTime urgeTime;
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
    public Long getUrgerId() { return urgerId; }
    public void setUrgerId(Long urgerId) { this.urgerId = urgerId; }
    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getUrgeTime() { return urgeTime; }
    public void setUrgeTime(LocalDateTime urgeTime) { this.urgeTime = urgeTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
}
