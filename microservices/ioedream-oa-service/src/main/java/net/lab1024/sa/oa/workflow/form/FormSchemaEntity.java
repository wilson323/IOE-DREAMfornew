package net.lab1024.sa.oa.workflow.form;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 工作流表单Schema定义实体
 * <p>
 * 存储JSON Schema格式的表单定义，替代Flowable 7.2已移除的Form引擎
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@TableName("t_workflow_form_definition")
public class FormSchemaEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("form_key")
    private String formKey;
    @TableField("form_name")
    private String formName;
    @TableField("form_schema")
    private String formSchema;
    @TableField("version")
    private Integer version;
    @TableField("status")
    private Integer status;
    @TableField("category")
    private String category;
    @TableField("description")
    private String description;
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableField("create_user_id")
    private Long createUserId;
    @TableField("update_user_id")
    private Long updateUserId;
    @TableLogic
    @TableField("deleted_flag")
    private Integer deletedFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFormKey() { return formKey; }
    public void setFormKey(String formKey) { this.formKey = formKey; }
    public String getFormName() { return formName; }
    public void setFormName(String formName) { this.formName = formName; }
    public String getFormSchema() { return formSchema; }
    public void setFormSchema(String formSchema) { this.formSchema = formSchema; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public Long getCreateUserId() { return createUserId; }
    public void setCreateUserId(Long createUserId) { this.createUserId = createUserId; }
    public Long getUpdateUserId() { return updateUserId; }
    public void setUpdateUserId(Long updateUserId) { this.updateUserId = updateUserId; }
    public Integer getDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(Integer deletedFlag) { this.deletedFlag = deletedFlag; }
}
