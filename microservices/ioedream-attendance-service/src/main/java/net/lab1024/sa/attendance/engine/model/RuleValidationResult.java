package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 规则验证结果
 * <p>
 * 封装规则表达式的验证结果：
 * - 是否有效
 * - 验证消息
 * - 错误详情
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class RuleValidationResult {

    /**
     * 规则表达式
     */
    private String expression;

    /**
     * 是否有效
     */
    private Boolean valid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 错误详情
     */
    private String errorDetails;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误位置
     */
    private String errorPosition;

    /**
     * 建议修正
     */
    private String suggestion;

    /**
     * 验证步骤列表
     */
    private List<ValidationStep> validationSteps = new ArrayList<>();

    /**
     * 创建成功结果
     */
    public static RuleValidationResult success(String expression) {
        RuleValidationResult result = new RuleValidationResult();
        result.setExpression(expression);
        result.setValid(true);
        result.setMessage("规则表达式有效");
        return result;
    }

    /**
     * 创建失败结果
     */
    public static RuleValidationResult failure(String expression, String errorMessage) {
        RuleValidationResult result = new RuleValidationResult();
        result.setExpression(expression);
        result.setValid(false);
        result.setMessage("规则表达式无效: " + errorMessage);
        result.setErrorDetails(errorMessage);
        return result;
    }

    /**
     * 创建失败结果（带详细信息）
     */
    public static RuleValidationResult failure(String expression, String errorMessage, String errorDetails) {
        RuleValidationResult result = new RuleValidationResult();
        result.setExpression(expression);
        result.setValid(false);
        result.setMessage(errorMessage);
        result.setErrorDetails(errorDetails);
        return result;
    }

    /**
     * 添加验证步骤
     */
    public void addValidationStep(ValidationStep step) {
        if (this.validationSteps == null) {
            this.validationSteps = new ArrayList<>();
        }
        this.validationSteps.add(step);
    }

    /**
     * 验证步骤内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationStep {
        /**
         * 步骤ID
         */
        private Long stepId;

        /**
         * 步骤名称
         */
        private String stepName;

        /**
         * 步骤描述
         */
        private String stepDescription;

        /**
         * 是否通过
         */
        private Boolean passed;

        /**
         * 错误消息
         */
        private String errorMessage;

        /**
         * 执行时间
         */
        private LocalDateTime executionTime;

        /**
         * 步骤类型
         */
        private String stepType;
    }
}
