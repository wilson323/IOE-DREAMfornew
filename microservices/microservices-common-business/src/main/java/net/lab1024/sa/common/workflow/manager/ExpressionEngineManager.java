package net.lab1024.sa.common.workflow.manager;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aviator表达式引擎Manager
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class ExpressionEngineManager {

    private final AviatorEvaluatorInstance aviatorEvaluator;
    private final Map<String, Object> globalVariables;
    private final Map<String, Expression> compiledExpressions;
    private final net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    public ExpressionEngineManager() {
        this(null);
    }

    public ExpressionEngineManager(net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient) {
        this.aviatorEvaluator = AviatorEvaluator.newInstance();
        this.globalVariables = new ConcurrentHashMap<>();
        this.compiledExpressions = new ConcurrentHashMap<>();
        this.gatewayServiceClient = gatewayServiceClient;
        initializeEngine();
    }

    private void initializeEngine() {
        log.info("[表达式引擎] 初始化Aviator表达式引擎");
        try {
            aviatorEvaluator.setOption(Options.OPTIMIZE_LEVEL, 1);
            initializeGlobalVariables();
            log.info("[表达式引擎] 初始化完成");
        } catch (Exception e) {
            log.error("[表达式引擎] 初始化失败: {}", e.getMessage(), e);
            throw new SystemException("EXPRESSION_ENGINE_INIT_ERROR", "表达式引擎初始化失败", e);
        }
    }

    private void initializeGlobalVariables() {
        globalVariables.put("TRUE", Boolean.TRUE);
        globalVariables.put("FALSE", Boolean.FALSE);
        globalVariables.put("NULL", null);
        globalVariables.put("SYSTEM_TIME", System.currentTimeMillis());
        log.debug("[表达式引擎] 已初始化全局变量: {}", globalVariables.keySet());
    }

    public Object executeExpression(String expression, Map<String, Object> context) {
        log.debug("[表达式引擎] 执行表达式: {}", expression);
        try {
            Map<String, Object> executionContext = new HashMap<>(globalVariables);
            if (context != null) {
                executionContext.putAll(context);
            }
            if (gatewayServiceClient != null) {
                executionContext.put("gatewayServiceClient", gatewayServiceClient);
            }
            Expression compiledExpression = getCompiledExpression(expression);
            Object result = compiledExpression.execute(executionContext);
            log.debug("[表达式引擎] 执行结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("[表达式引擎] 执行表达式失败: {}", expression, e);
            throw new SystemException("EXPRESSION_EXECUTE_ERROR", "表达式执行失败: " + e.getMessage(), e);
        }
    }

    public boolean executeBooleanExpression(String condition, Map<String, Object> context) {
        Object result = executeExpression(condition, context);
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return result != null && Boolean.parseBoolean(result.toString());
    }

    public Number executeNumberExpression(String expression, Map<String, Object> context) {
        Object result = executeExpression(expression, context);
        if (result instanceof Number) {
            return (Number) result;
        }
        try {
            return Double.parseDouble(result.toString());
        } catch (NumberFormatException e) {
            log.warn("[表达式引擎] 表达式结果不是数值, result={}", result);
            throw new BusinessException("EXPRESSION_RESULT_NOT_NUMBER", "表达式结果不是数值: " + result, e);
        }
    }

    private Expression getCompiledExpression(String expression) {
        return compiledExpressions.computeIfAbsent(expression, expr -> {
            try {
                return aviatorEvaluator.compile(expr, true);
            } catch (Exception e) {
                log.error("[表达式引擎] 编译表达式失败: {}", expr, e);
                throw new SystemException("EXPRESSION_COMPILE_ERROR", "表达式编译失败: " + e.getMessage(), e);
            }
        });
    }

    public void setGlobalVariable(String key, Object value) {
        globalVariables.put(key, value);
        log.debug("[表达式引擎] 设置全局变量: {} = {}", key, value);
    }

    public void removeGlobalVariable(String key) {
        globalVariables.remove(key);
        log.debug("[表达式引擎] 移除全局变量: {}", key);
    }

    public Map<String, Object> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }

    public void clearExpressionCache() {
        compiledExpressions.clear();
        log.info("[表达式引擎] 已清除表达式缓存");
    }

    public Map<String, Object> getEngineStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("compiledExpressionsCount", compiledExpressions.size());
        stats.put("globalVariablesCount", globalVariables.size());
        stats.put("optimizeLevel", 1);
        return stats;
    }

    public boolean validateExpression(String expression) {
        try {
            aviatorEvaluator.compile(expression, true);
            return true;
        } catch (Exception e) {
            log.warn("[表达式引擎] 表达式语法验证失败: {}", expression);
            return false;
        }
    }

    public boolean isExpressionSafe(String expression) {
        String[] dangerousKeywords = {
                "System", "Runtime", "Process", "Thread", "Class", "ClassLoader",
                "java.lang.reflect", "java.io", "java.net", "java.sql"
        };
        String lowerExpression = expression.toLowerCase();
        for (String keyword : dangerousKeywords) {
            if (lowerExpression.contains(keyword.toLowerCase())) {
                log.warn("[表达式引擎] 检测到危险操作: {}", expression);
                return false;
            }
        }
        return true;
    }
}
