package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 编译后的规则
 * <p>
 * Aviator表达式编译后的可执行规则对象
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
public class CompiledRule {

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
     * 编译后的表达式对象（Aviator）
     */
    private Object compiledExpression;

    /**
     * 规则类型：VALIDATION-验证 CALCULATION-计算 TRANSFORMATION-转换
     */
    private String ruleType;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 编译时间
     */
    private LocalDateTime compileTime;

    /**
     * 编译是否成功
     */
    private Boolean compileSuccess;

    /**
     * 编译错误消息
     */
    private String compileError;

    /**
     * 规则参数
     */
    private Map<String, Object> parameters;

    /**
     * 创建编译成功的规则
     */
    public static CompiledRule success(Long ruleId, String ruleName, String expression,
                                       Object compiledExpr, String ruleType) {
        return CompiledRule.builder()
            .ruleId(ruleId)
            .ruleName(ruleName)
            .expression(expression)
            .compiledExpression(compiledExpr)
            .ruleType(ruleType)
            .enabled(true)
            .compileSuccess(true)
            .compileTime(LocalDateTime.now())
            .build();
    }

    /**
     * 创建编译失败的规则
     */
    public static CompiledRule failure(Long ruleId, String ruleName, String expression,
                                      String error) {
        return CompiledRule.builder()
            .ruleId(ruleId)
            .ruleName(ruleName)
            .expression(expression)
            .compileSuccess(false)
            .compileError(error)
            .compileTime(LocalDateTime.now())
            .build();
    }

    /**
     * 是否可执行
     */
    public boolean isExecutable() {
        return compileSuccess != null && compileSuccess &&
               compiledExpression != null &&
               enabled != null && enabled;
    }
}
