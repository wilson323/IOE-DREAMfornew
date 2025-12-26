package net.lab1024.sa.oa.workflow.form.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 表单设计详情
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDesignDetail {

    /**
     * 表单ID
     */
    private Long formId;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单描述
     */
    private String formDescription;

    /**
     * 表单分类
     */
    private String formCategory;

    /**
     * 表单版本
     */
    private String version;

    /**
     * 表单状态（draft, published, deprecated）
     */
    private String status;

    /**
     * 表单组件列表
     */
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
     * 表单提交配置
     */
    private FormDesignForm.FormSubmitConfig submitConfig;

    /**
     * 表单权限配置
     */
    private FormDesignForm.FormPermissionConfig permissionConfig;

    /**
     * 表单逻辑列表
     */
    private List<FormLogicConfig> logicConfigs;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 创建人ID
     */
    private Long createUserId;

    /**
     * 创建人姓名
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 使用次数
     */
    private Long usageCount;

    /**
     * 表单预览URL
     */
    private String previewUrl;

    /**
     * 表单JSON Schema
     */
    private String jsonSchema;
}
