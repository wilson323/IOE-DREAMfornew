package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * 规则评估结果
 * <p>
 * 用于存储规则执行后的评估结果
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationResult {

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则表达式
     */
    private String expression;

    /**
     * 评估结果：true-通过 false-不通过
     */
    private Boolean passed;

    /**
     * 评估值
     */
    private Object evaluationValue;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * 评估时间戳
     */
    private LocalDateTime evaluationTime;

    /**
     * 扩展属性
     */
    private Map<String, Object> extendedAttributes;

    /**
     * 创建成功结果
     */
    public static RuleEvaluationResult success(Long ruleId, String ruleName, Object value) {
        return RuleEvaluationResult.builder()
            .ruleId(ruleId)
            .ruleName(ruleName)
            .passed(true)
            .evaluationValue(value)
            .evaluationTime(LocalDateTime.now())
            .extendedAttributes(new HashMap<>())
            .build();
    }

    /**
     * 创建失败结果
     */
    public static RuleEvaluationResult failure(Long ruleId, String ruleName, String errorMessage) {
        return RuleEvaluationResult.builder()
            .ruleId(ruleId)
            .ruleName(ruleName)
            .passed(false)
            .errorMessage(errorMessage)
            .evaluationTime(LocalDateTime.now())
            .extendedAttributes(new HashMap<>())
            .build();
    }

    /**
     * 添加扩展属性
     */
    public void addExtendedAttribute(String key, Object value) {
        if (this.extendedAttributes == null) {
            this.extendedAttributes = new HashMap<>();
        }
        this.extendedAttributes.put(key, value);
    }
}
