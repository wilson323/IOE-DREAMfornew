package net.lab1024.sa.attendance.engine.model;

import lombok.Data;

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
}
