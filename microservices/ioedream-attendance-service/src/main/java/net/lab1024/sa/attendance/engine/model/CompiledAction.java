package net.lab1024.sa.attendance.engine.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 编译后的动作
 * <p>
 * 规则满足后执行的动作对象
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
public class CompiledAction {

    /**
     * 动作ID
     */
    private Long actionId;

    /**
     * 动作名称
     */
    private String actionName;

    /**
     * 动作类型：ALERT-告警 NOTIFICATION-通知 LOG-日志 UPDATE-更新 TRANSFORM-转换
     */
    private String actionType;

    /**
     * 动作表达式
     */
    private String expression;

    /**
     * 编译后的表达式对象
     */
    private Object compiledExpression;

    /**
     * 动作参数
     */
    private Map<String, Object> parameters;

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
     * 执行次数限制
     */
    private Integer executionLimit;

    /**
     * 已执行次数
     */
    private Integer executionCount;

    /**
     * 创建编译成功的动作
     */
    public static CompiledAction success(Long actionId, String actionName, String actionType,
                                       String expression, Object compiledExpr) {
        return CompiledAction.builder()
            .actionId(actionId)
            .actionName(actionName)
            .actionType(actionType)
            .expression(expression)
            .compiledExpression(compiledExpr)
            .enabled(true)
            .compileSuccess(true)
            .executionCount(0)
            .compileTime(LocalDateTime.now())
            .build();
    }

    /**
     * 创建编译失败的动作
     */
    public static CompiledAction failure(Long actionId, String actionName, String expression,
                                      String error) {
        return CompiledAction.builder()
            .actionId(actionId)
            .actionName(actionName)
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

    /**
     * 是否达到执行限制
     */
    public boolean isExecutionLimitReached() {
        if (executionLimit == null) {
            return false;
        }
        return executionCount != null && executionCount >= executionLimit;
    }

    /**
     * 增加执行计数
     */
    public void incrementExecutionCount() {
        if (this.executionCount == null) {
            this.executionCount = 0;
        }
        this.executionCount++;
    }
}
