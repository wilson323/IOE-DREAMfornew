package net.lab1024.sa.oa.workflow.form.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 表单模板
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplate {

    /**
     * 模板ID
     */
    @NotBlank
    private String templateId;

    /**
     * 模板名称
     */
    @NotBlank
    private String templateName;

    /**
     * 模板描述
     */
    private String templateDescription;

    /**
     * 模板分类（leave, reimbursement, procurement, etc.）
     */
    @NotBlank
    private String category;

    /**
     * 模板标签
     */
    private List<String> tags;

    /**
     * 缩略图URL
     */
    private String thumbnailUrl;

    /**
     * 表单组件列表
     */
    @NotEmpty
    private List<FormComponent> components;

    /**
     * 表单布局配置
     */
    private FormDesignForm.FormLayoutConfig layoutConfig;

    /**
     * 表单样式配置
     */
    private FormDesignForm.FormStyleConfig styleConfig;

    /**
     * 表单逻辑列表
     */
    private List<FormLogicConfig> logicConfigs;

    /**
     * 默认数据示例
     */
    private Map<String, Object> defaultData;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 评分（1-5）
     */
    private Double rating;

    /**
     * 是否为系统模板
     */
    private Boolean systemTemplate;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建时间
     */
    private Long createTime;
}
