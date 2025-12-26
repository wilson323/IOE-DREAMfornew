package net.lab1024.sa.oa.workflow.form.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 验证规则
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRule {

    /**
     * 规则ID
     */
    private String ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型
     * - required: 必填
     * - minLength: 最小长度
     * - maxLength: 最大长度
     * - min: 最小值
     * - max: 最大值
     * - pattern: 正则表达式
     * - email: 邮箱格式
     * - phone: 手机号格式
     * - url: URL格式
     * - date: 日期格式
     * - number: 数字格式
     * - integer: 整数格式
     * - custom: 自定义验证
     */
    private String ruleType;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 错误提示消息
     */
    private String errorMessage;

    /**
     * 规则参数
     */
    private Map<String, Object> parameters;

    /**
     * 规则表达式（自定义验证）
     */
    private String expression;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 规则优先级
     */
    private Integer priority;
}
