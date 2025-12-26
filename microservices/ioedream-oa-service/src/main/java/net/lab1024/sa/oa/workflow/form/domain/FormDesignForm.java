package net.lab1024.sa.oa.workflow.form.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 表单设计表单
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDesignForm {

    /**
     * 表单ID（更新时提供）
     */
    private Long formId;

    /**
     * 表单名称
     */
    @NotBlank(message = "表单名称不能为空")
    @Size(max = 100, message = "表单名称长度不能超过100个字符")
    private String formName;

    /**
     * 表单描述
     */
    @Size(max = 500, message = "表单描述长度不能超过500个字符")
    private String formDescription;

    /**
     * 表单分类
     */
    @NotBlank(message = "表单分类不能为空")
    private String formCategory;

    /**
     * 表单组件列表
     */
    @NotEmpty(message = "表单组件不能为空")
    @Valid
    private List<FormComponent> components;

    /**
     * 表单布局配置
     */
    @Valid
    private FormLayoutConfig layoutConfig;

    /**
     * 表单样式配置
     */
    @Valid
    private FormStyleConfig styleConfig;

    /**
     * 表单提交配置
     */
    @Valid
    private FormSubmitConfig submitConfig;

    /**
     * 表单权限配置
     */
    @Valid
    private FormPermissionConfig permissionConfig;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 表单布局配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormLayoutConfig {
        /**
         * 布局类型（grid, flex, absolute）
         */
        private String layoutType;

        /**
         * 列数（grid布局）
         */
        private Integer columns;

        /**
         * 列间距
         */
        private Integer columnGap;

        /**
         * 行间距
         */
        private Integer rowGap;

        /**
         * 标签宽度
         */
        private Integer labelWidth;

        /**
         * 标签对齐方式（left, right, top）
         */
        private String labelAlign;
    }

    /**
     * 表单样式配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormStyleConfig {
        /**
         * 表单宽度
         */
        private String formWidth;

        /**
         * 表单背景色
         */
        private String backgroundColor;

        /**
         * 边框样式
         */
        private String borderStyle;

        /**
         * 圆角大小
         */
        private String borderRadius;

        /**
         * 内边距
         */
        private String padding;

        /**
         * 外边距
         */
        private String margin;
    }

    /**
     * 表单提交配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormSubmitConfig {
        /**
         * 提交按钮文本
         */
        private String submitButtonText;

        /**
         * 重置按钮文本
         */
        private String resetButtonText;

        /**
         * 提交前验证
         */
        private Boolean validateBeforeSubmit;

        /**
         * 提交成功提示
         */
        private String successMessage;

        /**
         * 提交后操作（redirect, close, stay）
         */
        private String afterSubmitAction;

        /**
         * 提交URL（自定义提交）
         */
        private String submitUrl;
    }

    /**
     * 表单权限配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormPermissionConfig {
        /**
         * 只读模式
         */
        private Boolean readOnly;

        /**
         * 可编辑字段列表
         */
        private List<String> editableFields;

        /**
         * 必填字段列表
         */
        private List<String> requiredFields;

        /**
         * 隐藏字段列表
         */
        private List<String> hiddenFields;

        /**
         * 字段权限映射
         */
        private Map<String, FieldPermission> fieldPermissions;
    }

    /**
     * 字段权限
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldPermission {
        /**
         * 是否可读
         */
        private Boolean readable;

        /**
         * 是否可编辑
         */
        private Boolean editable;

        /**
         * 是否必填
         */
        private Boolean required;
    }
}
