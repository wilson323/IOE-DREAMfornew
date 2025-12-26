package net.lab1024.sa.oa.workflow.form.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 表单预览数据
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormPreviewData {

    /**
     * 表单ID
     */
    private Long formId;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单渲染配置
     */
    private FormRenderConfig renderConfig;

    /**
     * 表单组件列表（已排序）
     */
    private List<FormComponent> components;

    /**
     * 表单数据模型（默认值）
     */
    private Map<String, Object> formData;

    /**
     * 表单验证规则
     */
    private Map<String, List<ValidationRuleConfig>> validationRules;

    /**
     * 表单逻辑配置
     */
    private List<FormLogicConfig> logicConfigs;

    /**
     * 表单UI配置
     */
    private FormUIConfig uiConfig;

    /**
     * 表单渲染配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormRenderConfig {
        /**
         * 渲染模式（edit, view, preview）
         */
        private String renderMode;

        /**
         * 是否显示标签
         */
        private Boolean showLabel;

        /**
         * 是否显示错误提示
         */
        private Boolean showError;

        /**
         * 是否显示必填标记
         */
        private Boolean showRequired;

        /**
         * 标签宽度
         */
        private Integer labelWidth;

        /**
         * 标签对齐方式
         */
        private String labelAlign;

        /**
         * 表单尺寸
         */
        private String size;

        /**
         * 是否禁用表单
         */
        private Boolean disabled;

        /**
         * 是否只读表单
         */
        private Boolean readonly;
    }

    /**
     * 表单UI配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormUIConfig {
        /**
         * 表单宽度
         */
        private String width;

        /**
         * 表单高度
         */
        private String height;

        /**
         * 背景色
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

        /**
         * 阴影样式
         */
        private String boxShadow;

        /**
         * 主题色
         */
        private String themeColor;

        /**
         * 自定义CSS
         */
        private String customCss;

        /**
         * 自定义类名
         */
        private String customClass;
    }
}
