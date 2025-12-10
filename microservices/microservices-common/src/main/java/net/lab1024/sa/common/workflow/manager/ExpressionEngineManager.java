package net.lab1024.sa.common.workflow.manager;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import net.lab1024.sa.common.workflow.function.CheckAreaPermissionFunction;
import net.lab1024.sa.common.workflow.function.CalculateWorkingHoursFunction;
import net.lab1024.sa.common.workflow.function.FormatDateFunction;
import net.lab1024.sa.common.workflow.function.ContainsFunction;
import net.lab1024.sa.common.workflow.function.IsEmptyFunction;
import net.lab1024.sa.common.workflow.function.IsNotEmptyFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Aviator表达式引擎Manager
 * <p>
 * 企业级表达式计算引擎，支持复杂的业务规则和条件判断
 * 严格遵循CLAUDE.md全局架构规范：
 * - microservices-common中的Manager类为纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖，保持为纯Java类
 * - 支持自定义函数注册和安全控制
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Slf4j
public class ExpressionEngineManager {

    private final AviatorEvaluatorInstance aviatorEvaluator;
    private final Map<String, Object> globalVariables;
    private final Map<String, Expression> compiledExpressions;
    private final net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    // 构造函数注入依赖（无GatewayServiceClient，用于纯计算场景）
    public ExpressionEngineManager() {
        this(null);
    }

    // 构造函数注入依赖（带GatewayServiceClient，用于需要调用服务的场景）
    public ExpressionEngineManager(net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient) {
        this.aviatorEvaluator = AviatorEvaluator.newInstance();
        this.globalVariables = new ConcurrentHashMap<>();
        this.compiledExpressions = new ConcurrentHashMap<>();
        this.gatewayServiceClient = gatewayServiceClient;

        // 初始化表达式引擎
        initializeEngine();
    }

    /**
     * 初始化表达式引擎
     */
    private void initializeEngine() {
        log.info("[表达式引擎] 初始化Aviator表达式引擎");

        try {
            // 设置表达式引擎选项
            // Aviator 5.3.3 中，OPTIMIZE_LEVEL 使用整数常量：0=不优化, 1=简单优化, 2=完全优化
            aviatorEvaluator.setOption(Options.OPTIMIZE_LEVEL, 1);

            // 注册自定义函数
            registerCustomFunctions();

            // 设置全局变量
            initializeGlobalVariables();

            log.info("[表达式引擎] 初始化完成，优化级别: EVAL");

        } catch (Exception e) {
            log.error("[表达式引擎] 初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("表达式引擎初始化失败", e);
        }
    }

    /**
     * 注册自定义函数
     */
    private void registerCustomFunctions() {
        try {
            // 注册业务函数
            aviatorEvaluator.addFunction(new CheckAreaPermissionFunction());
            aviatorEvaluator.addFunction(new CalculateWorkingHoursFunction());
            aviatorEvaluator.addFunction(new FormatDateFunction());
            aviatorEvaluator.addFunction(new ContainsFunction());
            aviatorEvaluator.addFunction(new IsEmptyFunction());
            aviatorEvaluator.addFunction(new IsNotEmptyFunction());

            log.info("[表达式引擎] 已注册 {} 个自定义函数", 6);

        } catch (Exception e) {
            log.error("[表达式引擎] 注册自定义函数失败: {}", e.getMessage(), e);
            throw new RuntimeException("注册自定义函数失败", e);
        }
    }

    /**
     * 初始化全局变量
     */
    private void initializeGlobalVariables() {
        // 添加常用的全局变量
        globalVariables.put("TRUE", Boolean.TRUE);
        globalVariables.put("FALSE", Boolean.FALSE);
        globalVariables.put("NULL", null);

        // 添加常用的全局常量
        globalVariables.put("SYSTEM_TIME", System.currentTimeMillis());

        log.debug("[表达式引擎] 已初始化全局变量: {}", globalVariables.keySet());
    }

    /**
     * 执行表达式
     *
     * @param expression 表达式字符串
     * @param context 执行上下文变量
     * @return 执行结果
     */
    public Object executeExpression(String expression, Map<String, Object> context) {
        log.debug("[表达式引擎] 执行表达式: {}, 上下文: {}", expression, context);

        try {
            // 合并全局变量和上下文变量
            Map<String, Object> executionContext = new HashMap<>(globalVariables);
            if (context != null) {
                executionContext.putAll(context);
            }

            // 如果提供了GatewayServiceClient，将其放入执行上下文，供Aviator函数使用
            if (gatewayServiceClient != null) {
                executionContext.put("gatewayServiceClient", gatewayServiceClient);
            }

            // 编译并执行表达式（带缓存）
            Expression compiledExpression = getCompiledExpression(expression);
            Object result = compiledExpression.execute(executionContext);

            log.debug("[表达式引擎] 执行结果: {}", result);
            return result;

        } catch (Exception e) {
            log.error("[表达式引擎] 执行表达式失败: {}, 上下文: {}, 错误: {}",
                    expression, context, e.getMessage(), e);
            throw new RuntimeException("表达式执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行布尔表达式
     *
     * @param condition 条件表达式
     * @param context 执行上下文
     * @return 布尔值结果
     */
    public boolean executeBooleanExpression(String condition, Map<String, Object> context) {
        Object result = executeExpression(condition, context);

        if (result instanceof Boolean) {
            return (Boolean) result;
        }

        // 其他类型转换为布尔值
        return result != null && Boolean.parseBoolean(result.toString());
    }

    /**
     * 执行数值表达式
     *
     * @param expression 数值表达式
     * @param context 执行上下文
     * @return 数值结果
     */
    public Number executeNumberExpression(String expression, Map<String, Object> context) {
        Object result = executeExpression(expression, context);

        if (result instanceof Number) {
            return (Number) result;
        }

        // 尝试转换为数值
        try {
            return Double.parseDouble(result.toString());
        } catch (NumberFormatException e) {
            throw new RuntimeException("表达式结果不是数值: " + result);
        }
    }

    /**
     * 获取编译后的表达式（带缓存）
     */
    private Expression getCompiledExpression(String expression) {
        return compiledExpressions.computeIfAbsent(expression, expr -> {
            try {
                return aviatorEvaluator.compile(expr, true);
            } catch (Exception e) {
                log.error("[表达式引擎] 编译表达式失败: {}, 错误: {}", expr, e.getMessage(), e);
                throw new RuntimeException("表达式编译失败: " + e.getMessage(), e);
            }
        });
    }

    /**
     * 设置全局变量
     *
     * @param key 变量名
     * @param value 变量值
     */
    public void setGlobalVariable(String key, Object value) {
        globalVariables.put(key, value);
        log.debug("[表达式引擎] 设置全局变量: {} = {}", key, value);
    }

    /**
     * 移除全局变量
     *
     * @param key 变量名
     */
    public void removeGlobalVariable(String key) {
        globalVariables.remove(key);
        log.debug("[表达式引擎] 移除全局变量: {}", key);
    }

    /**
     * 获取所有全局变量
     *
     * @return 全局变量映射
     */
    public Map<String, Object> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }

    /**
     * 清除表达式缓存
     */
    public void clearExpressionCache() {
        compiledExpressions.clear();
        log.info("[表达式引擎] 已清除表达式缓存");
    }

    /**
     * 获取表达式引擎统计信息
     *
     * @return 统计信息
     */
    public Map<String, Object> getEngineStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("compiledExpressionsCount", compiledExpressions.size());
        stats.put("globalVariablesCount", globalVariables.size());
        // 优化级别：在初始化时已设置为1（简单优化），这里直接返回固定值
        // 注意：getOption方法已废弃，使用初始化时的值
        stats.put("optimizeLevel", 1);
        return stats;
    }

    /**
     * 验证表达式语法
     *
     * @param expression 表达式字符串
     * @return 是否语法正确
     */
    public boolean validateExpression(String expression) {
        try {
            aviatorEvaluator.compile(expression, true);
            return true;
        } catch (Exception e) {
            log.warn("[表达式引擎] 表达式语法验证失败: {}, 错误: {}", expression, e.getMessage());
            return false;
        }
    }

    /**
     * 安全检查：检测表达式是否包含危险操作
     *
     * @param expression 表达式字符串
     * @return 是否安全
     */
    public boolean isExpressionSafe(String expression) {
        // 检查危险关键词
        String[] dangerousKeywords = {
                "System", "Runtime", "Process", "Thread", "Class", "ClassLoader",
                "java.lang.reflect", "java.io", "java.net", "java.sql"
        };

        String lowerExpression = expression.toLowerCase();
        for (String keyword : dangerousKeywords) {
            if (lowerExpression.contains(keyword.toLowerCase())) {
                log.warn("[表达式引擎] 检测到危险操作: {} 包含 {}", expression, keyword);
                return false;
            }
        }

        return true;
    }
}
