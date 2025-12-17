package net.lab1024.sa.oa.workflow.approval;

import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;

/**
 * 抄送记录实体
 */
@TableName("t_workflow_carbon_copy")
public class CarbonCopyEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("process_instance_id")
    private String processInstanceId;
    @TableField("task_id")
    private String taskId;
    @TableField("cc_user_id")
    private Long ccUserId;
    @TableField("sender_id")
    private Long senderId;
    @TableField("read_status")
    private Integer readStatus;
    @TableField("cc_time")
    private LocalDateTime ccTime;
    @TableField("read_time")
    private LocalDateTime readTime;
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
    public Long getCcUserId() { return ccUserId; }
    public void setCcUserId(Long ccUserId) { this.ccUserId = ccUserId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Integer getReadStatus() { return readStatus; }
    public void setReadStatus(Integer readStatus) { this.readStatus = readStatus; }
    public LocalDateTime getCcTime() { return ccTime; }
    public void setCcTime(LocalDateTime ccTime) { this.ccTime = ccTime; }
    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
}
