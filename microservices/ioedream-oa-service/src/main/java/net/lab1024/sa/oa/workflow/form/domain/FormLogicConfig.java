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
 * 表单逻辑配置
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormLogicConfig {

    /**
     * 逻辑ID
     */
    @NotBlank
    private String logicId;

    /**
     * 逻辑名称
     */
    @NotBlank
    private String logicName;

    /**
     * 逻辑类型
     * - visibility: 显示/隐藏逻辑
     * - readonly: 只读/可编辑逻辑
     * - required: 必填/非必填逻辑
     * - value: 值设置逻辑
     * - cascade: 级联下拉逻辑
     * - calculation: 计算逻辑
     * - validation: 动态验证逻辑
     */
    @NotBlank
    private String logicType;

    /**
     * 逻辑描述
     */
    private String description;

    /**
     * 触发条件列表
     */
    @NotEmpty
    private List<TriggerCondition> triggerConditions;

    /**
     * 执行动作列表
     */
    @NotEmpty
    private List<LogicAction> actions;

    /**
     * 逻辑表达式（复杂条件）
     */
    private String expression;

    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 执行顺序
     */
    private Integer order;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 触发条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerCondition {
        /**
         * 字段ID
         */
        @NotBlank
        private String fieldId;

        /**
         * 操作符
         * - eq: 等于
         * - ne: 不等于
         * - gt: 大于
         * - gte: 大于等于
         * - lt: 小于
         * - lte: 小于等于
         * - contains: 包含
         * - notContains: 不包含
         * - startsWith: 以...开始
         * - endsWith: 以...结束
         * - isEmpty: 为空
         * - isNotEmpty: 不为空
         * - in: 在列表中
         * - notIn: 不在列表中
         * - between: 在范围内
         */
        @NotBlank
        private String operator;

        /**
         * 比较值
         */
        private Object value;

        /**
         * 值类型（string, number, boolean, date, array）
         */
        private String valueType;

        /**
         * 是否取反
         */
        @Builder.Default
        private Boolean not = false;
    }

    /**
     * 逻辑动作
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogicAction {
        /**
         * 动作类型
         * - show: 显示字段
         * - hide: 隐藏字段
         * - enable: 启用字段
         * - disable: 禁用字段
         * - setRequired: 设置必填
         * - setNotRequired: 设置非必填
         * - setValue: 设置值
         * - clearValue: 清空值
         * - setOptions: 设置选项
         * - addOption: 添加选项
         * - removeOption: 移除选项
         * - calculate: 计算值
         * - fetchOptions: 远程获取选项
         * - showMessage: 显示消息
         * - callApi: 调用API
         */
        @NotBlank
        private String actionType;

        /**
         * 目标字段ID（动作作用对象）
         */
        private String targetFieldId;

        /**
         * 动作值
         */
        private Object value;

        /**
         * 动作参数
         */
        private Map<String, Object> parameters;

        /**
         * 延迟执行（毫秒）
         */
        private Integer delay;

        /**
         * 是否异步执行
         */
        @Builder.Default
        private Boolean async = false;
    }

    /**
     * 级联配置（级联下拉专用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CascadeConfig {
        /**
         * 父字段ID
         */
        @NotBlank
        private String parentFieldId;

        /**
         * 子字段ID
         */
        @NotBlank
        private String childFieldId;

        /**
         * 数据源URL
         */
        @NotBlank
        private String dataSourceUrl;

        /**
         * 请求参数映射
         */
        private Map<String, String> parameterMapping;

        /**
         * 响应数据映射
         */
        private Map<String, String> dataMapping;

        /**
         * 是否懒加载
         */
        @Builder.Default
        private Boolean lazy = true;

        /**
         * 缓存数据
         */
        @Builder.Default
        private Boolean cache = true;
    }

    /**
     * 计算配置（计算逻辑专用）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculationConfig {
        /**
         * 目标字段ID
         */
        @NotBlank
        private String targetFieldId;

        /**
         * 计算公式
         * 示例：{field1} + {field2} * {field3}
         */
        @NotBlank
        private String formula;

        /**
         * 参与计算的字段列表
         */
        @NotEmpty
        private List<String> sourceFields;

        /**
         * 计算精度（小数位数）
         */
        private Integer precision;

        /**
         * 舍入模式
         */
        private String roundingMode;

        /**
         * 实时计算
         */
        @Builder.Default
        private Boolean realTime = true;
    }
}
