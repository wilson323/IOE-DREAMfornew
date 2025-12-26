package net.lab1024.sa.oa.workflow.form.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 组件配置Schema
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentConfigSchema {

    /**
     * 组件类型
     */
    private String componentType;

    /**
     * 组件名称
     */
    private String componentName;

    /**
     * 组件描述
     */
    private String description;

    /**
     * 组件图标
     */
    private String icon;

    /**
     * 基础属性配置
     */
    private List<PropertyConfig> baseProperties;

    /**
     * 验证属性配置
     */
    private List<PropertyConfig> validationProperties;

    /**
     * 样式属性配置
     */
    private List<PropertyConfig> styleProperties;

    /**
     * 高级属性配置
     */
    private List<PropertyConfig> advancedProperties;

    /**
     * 事件配置
     */
    private List<EventConfig> events;

    /**
     * 插槽配置
     */
    private List<SlotConfig> slots;

    /**
     * 默认配置值
     */
    private Map<String, Object> defaultValues;

    /**
     * 配置示例
     */
    private Map<String, Object> example;

    /**
     * 属性配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyConfig {
        /**
         * 属性名称
         */
        private String name;

        /**
         * 属性标签
         */
        private String label;

        /**
         * 属性类型
         * - string: 字符串
         * - number: 数字
         * - boolean: 布尔
         * - array: 数组
         * - object: 对象
         * - enum: 枚举
         * - color: 颜色选择器
         * - slider: 滑块
         * - textarea: 多行文本
         * - code: 代码编辑器
         * - select: 下拉选择
         * - multiSelect: 多选下拉
         */
        private String type;

        /**
         * 默认值
         */
        private Object defaultValue;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 可选值（enum, select类型）
         */
        private List<PropertyValue> options;

        /**
         * 最小值（number, slider类型）
         */
        private Double min;

        /**
         * 最大值（number, slider类型）
         */
        private Double max;

        /**
         * 步长（number, slider类型）
         */
        private Double step;

        /**
         * 占位符
         */
        private String placeholder;

        /**
         * 帮助文本
         */
        private String helpText;

        /**
         * 属性分组
         */
        private String group;

        /**
         * 是否高级配置
         */
        private Boolean advanced;

        /**
         * 条件显示表达式
         */
        private String showIf;
    }

    /**
     * 属性值
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyValue {
        /**
         * 值
         */
        private Object value;

        /**
         * 标签
         */
        private String label;

        /**
         * 图标
         */
        private String icon;
    }

    /**
     * 事件配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventConfig {
        /**
         * 事件名称
         */
        private String eventName;

        /**
         * 事件标签
         */
        private String label;

        /**
         * 事件描述
         */
        private String description;

        /**
         * 参数列表
         */
        private List<EventParameter> parameters;

        /**
         * 返回值类型
         */
        private String returnType;
    }

    /**
     * 事件参数
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventParameter {
        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数类型
         */
        private String type;

        /**
         * 参数描述
         */
        private String description;

        /**
         * 是否必填
         */
        private Boolean required;
    }

    /**
     * 插槽配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlotConfig {
        /**
         * 插槽名称
         */
        private String name;

        /**
         * 插槽描述
         */
        private String description;

        /**
         * 作用域参数
         */
        private Map<String, String> scope;
    }
}
