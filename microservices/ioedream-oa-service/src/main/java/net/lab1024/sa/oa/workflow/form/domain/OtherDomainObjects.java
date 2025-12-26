package net.lab1024.sa.oa.workflow.form.domain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 表单设计器其他领域对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
public class OtherDomainObjects {

    /**
     * 验证错误
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        /**
         * 字段ID
         */
        private String fieldId;

        /**
         * 错误消息
         */
        @NotBlank
        private String message;

        /**
         * 错误代码
         */
        private String errorCode;

        /**
         * 错误级别（error, warning, info）
         */
        private String level;
    }

    /**
     * 验证结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationResult {
        /**
         * 是否验证通过
         */
        private Boolean valid;

        /**
         * 错误列表
         */
        private List<ValidationError> errors;

        /**
         * 警告列表
         */
        private List<ValidationError> warnings;
    }

    /**
     * 表单测试数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormTestData {
        /**
         * 表单数据
         */
        private Map<String, Object> formData;

        /**
         * 测试场景
         */
        private String testScenario;

        /**
         * 期望结果
         */
        private Map<String, Object> expectedResults;
    }

    /**
     * 表单逻辑执行结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormLogicExecutionResult {
        /**
         * 是否执行成功
         */
        private Boolean success;

        /**
         * 执行的动作列表
         */
        private List<ExecutedAction> executedActions;

        /**
         * 错误信息
         */
        private String errorMessage;

        /**
         * 执行时间（毫秒）
         */
        private Long executionTime;

        /**
         * 已执行的动作
         */
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ExecutedAction {
            /**
             * 动作类型
             */
            private String actionType;

            /**
             * 目标字段ID
             */
            private String targetFieldId;

            /**
             * 执行前值
             */
            private Object beforeValue;

            /**
             * 执行后值
             */
            private Object afterValue;

            /**
             * 执行状态（success, failed, skipped）
             */
            private String status;

            /**
             * 错误消息
             */
            private String errorMessage;
        }
    }

    /**
     * 自定义组件表单
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomComponentForm {
        /**
         * 组件名称
         */
        @NotBlank
        private String componentName;

        /**
         * 组件描述
         */
        private String description;

        /**
         * 组件分类
         */
        private String category;

        /**
         * 组件图标
         */
        private String icon;

        /**
         * 组件配置Schema
         */
        private ComponentConfigSchema configSchema;

        /**
         * 组件渲染模板
         */
        private String renderTemplate;

        /**
         * 组件样式
         */
        private String componentStyle;

        /**
         * 扩展属性
         */
        private Map<String, Object> extendedAttributes;
    }

    /**
     * 应用模板表单
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplyTemplateForm {
        /**
         * 模板ID
         */
        @NotBlank
        private String templateId;

        /**
         * 新表单名称
         */
        @NotBlank
        private String newFormName;

        /**
         * 新表单描述
         */
        private String newFormDescription;

        /**
         * 自定义配置
         */
        private Map<String, Object> customizations;
    }
}
