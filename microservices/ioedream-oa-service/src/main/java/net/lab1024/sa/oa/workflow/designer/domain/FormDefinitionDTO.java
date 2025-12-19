package net.lab1024.sa.oa.workflow.designer.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 表单定义DTO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-17
 */
/**
 * 表单定义DTO - 手动实现getter/setter以解决Lombok编译问题
 */
public class FormDefinitionDTO {

    private Long id;
    @NotBlank(message = "表单标识不能为空")
    @Size(max = 100, message = "表单标识最大100字符")
    private String formKey;
    @NotBlank(message = "表单名称不能为空")
    @Size(max = 200, message = "表单名称最大200字符")
    private String formName;
    @NotBlank(message = "表单Schema不能为空")
    private String formSchema;
    private Integer version;
    private Integer status;
    @Size(max = 50, message = "分类最大50字符")
    private String category;
    @Size(max = 500, message = "描述最大500字符")
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

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
}
