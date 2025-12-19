package net.lab1024.sa.oa.workflow.form;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工作流表单实例实体
 * <p>
 * 存储表单实例数据，关联流程实例和任务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@TableName("t_workflow_form_instance")
public class FormInstanceEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("form_schema_id")
    private Long formSchemaId;
    @TableField("form_key")
    private String formKey;
    @TableField("process_instance_id")
    private String processInstanceId;
    @TableField("task_id")
    private String taskId;
    @TableField("form_data")
    private String formData;
    @TableField("submitter_id")
    private Long submitterId;
    @TableField("status")
    private Integer status;
    @TableField("submit_time")
    private LocalDateTime submitTime;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFormSchemaId() { return formSchemaId; }
    public void setFormSchemaId(Long formSchemaId) { this.formSchemaId = formSchemaId; }
    public String getFormKey() { return formKey; }
    public void setFormKey(String formKey) { this.formKey = formKey; }
    public String getProcessInstanceId() { return processInstanceId; }
    public void setProcessInstanceId(String processInstanceId) { this.processInstanceId = processInstanceId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getFormData() { return formData; }
    public void setFormData(String formData) { this.formData = formData; }
    public Long getSubmitterId() { return submitterId; }
    public void setSubmitterId(Long submitterId) { this.submitterId = submitterId; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
}
